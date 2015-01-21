package com.johnsimon.payback.data;

import com.google.gson.Gson;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.preferences.Preferences;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.ColorPalette;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class AppData {
    public ArrayList<Person> people;
    public ArrayList<Debt> debts;

    public HashSet<UUID> deleted;

	public PeopleOrder peopleOrder;

	public Preferences preferences;

    public transient ArrayList<Contact> contacts;

    public AppData(ArrayList<Person> people, ArrayList<Debt> debts, HashSet<UUID> deleted, PeopleOrder peopleOrder, Preferences preferences) {
        this.people = people;
        this.debts = debts;
        this.deleted = deleted;
		this.peopleOrder = peopleOrder;
		this.preferences = preferences;
    }

    public static AppData defaultAppData() {
        return new AppData(new ArrayList<Person>(), new ArrayList<Debt>(), new HashSet<UUID>(), new PeopleOrder(), Preferences.defaultPreferences());
    }

    public String save() {
        return AppData.toJson(this);
    }

    public boolean isLinked() {
        return contacts != null;
    }

	public ArrayList<Person> peopleOrdered() {
		return peopleOrder.order(people);
	}

    public ArrayList<Debt> feed(Person person) {
        if(person == null) return debts;

        ArrayList<Debt> feed = new ArrayList<Debt>();
        for(Debt debt : debts) {
            if(debt.getOwner() == person) {
                feed.add(debt);
            }
        }
        return feed;
    }

    public static float total(ArrayList<Debt> debts) {
        float total = 0;
        for(Debt debt : debts) {
            if(!debt.isPaidBack()) {
                total += debt.getAmount();
            }
        }
        return total;
    }

    public float totalPlus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (debt.getAmount() > 0) {
                sum += debt.getAmount();
            }
        }
        return sum;
    }

    public float totalMinus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (debt.getAmount() < 0) {
                sum += debt.getAmount();
            }
        }
        return sum;
    }

    public Person findPerson(UUID id) {
        return findPerson(people, id);
    }
    public Person findPerson(String id) {
        return findPerson(people, UUID.fromString(id));
    }

    public Person findPerson(ArrayList<Person> people, UUID id) {
        for(Person p : people) {
            if(p.id.equals(id)) return p;
        }
        return null;
    }

    public Person findPersonByName(String name) {
        for(Person p : people) {
            if(p.getName().equals(name)) return p;
        }
        return null;
    }

    public Debt findDebt(UUID id) {
        for (Debt debt : debts) {
            if(debt.id.equals(id)) return debt;
        }

        return null;
    }

    public void merge(Person from, Person to) {
        for(Debt debt : debts) {
            if(debt.getOwner() == from) {
                debt.setOwner(to);
            }
        }
        delete(from);
    }

    public void unmerge(Person restore, ArrayList<Debt> debts, int index) {
        for(Debt debt : debts) {
            debt.setOwner(restore);
        }
        //people.add(index, restore);
    }
    public void delete(Person person) {
        deleteDebts(person);
        deleted.add(person.id);
		peopleOrder.remove(person.id);
        people.remove(person);
    }
    public void delete(Debt debt) {
        deleted.add(debt.id);
        debts.remove(debt);
    }

	public void add(Debt debt) {
		debts.add(debt);
	}

	public void add(Person person) {
		people.add(person);
		peopleOrder.add(person.id);
	}

    private void deleteDebts(Person person) {
        ArrayList<Debt> debts = feed(person);

        for(Debt debt : debts) {
            delete(debt);
        }
    }

    public void move(Debt debt, Person person) {
        debt.setOwner(person);
    }

    public void rename(Person person, String name) {
        person.setName(name);
    }

    public void sync(Person person, DebtSendable[] debts) {
        deleteDebts(person);
        for(DebtSendable debt : debts) {
            add(debt.extract(person));
        }
    }

    public Person getOrCreatePerson(String name, DataActivity context) {
        //Try to find existing person
        Person person = findPersonByName(name);
        if(person != null) {
            return person;
        }

        //Create new person
        //Attempt to find link
        Contact link = null;
        for (Contact contact : contacts) {
            if (contact.name.equals(name)) {
                link = contact;
                break;
            }
        }
        //Create person and add to people
        person = new Person(name, ColorPalette.getInstance(context));
        person.link = link;

        add(person);
        return person;
    }

    //Returns all unique people names
    public ArrayList<String> getPeopleNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Person person : people) {
            if(!names.contains(person.getName())) {
                names.add(person.getName());
            }
        }

        return names;
    }

    //Returns all unique names (from people and contacts)
    public ArrayList<String> getAllNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Person person : people) {
            if(!names.contains(person.getName())) {
                names.add(person.getName());
            }
        }
        for (Contact contact : contacts) {
            if(!names.contains(contact.name)) {
                names.add(contact.name);
            }
        }

        return names;
    }

    //Returns all unique contact names
    public ArrayList<String> getContactNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Contact contact : contacts) {
            if(!names.contains(contact.name)) {
                names.add(contact.name);
            }
        }

        return names;
    }

    public String guessName(User user, User sender) {
        //If user has no name, use currently viewed person
        if(user.name == null) {
            return FeedActivity.isAll() ? null : FeedActivity.person.getName();
        }

        //If user has a name:
        //First match name and number in people and their links
        for(Person person : people) {
            if(person.matchTo(sender) || (person.isLinked() && person.link.matchTo(sender))) {
                return person.getName();
            }
        }

        //Secondly, match name and number in contacts
        for(Contact contact : contacts) {
            if(contact.matchTo(sender)) {
                return contact.name;
            }
        }

        //Otherwise, use the senders name
        return sender.name;
    }

    public static AppData fromJson(String JSON) {
		if(JSON == null) {
			return AppData.defaultAppData();
		}

		AppData data = new Gson().fromJson(JSON, AppData.class);
		for(Debt debt : data.debts) {
			debt.linkOwner(data.people);
		}

		return data;
    }

    public static String toJson(AppData data) {
        return new Gson().toJson(data, AppData.class);
    }

	@Override
	public boolean equals(Object o){
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof AppData))return false;
		AppData other = (AppData) o;

		return people.equals(other.people)
			&& debts.equals(other.debts)
			&& deleted.equals(other.deleted)
			&& peopleOrder.equals(other.peopleOrder)
			&& preferences.equals(other.preferences);
	}
}
