package com.johnsimon.payback.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.johnsimon.payback.BuildConfig;
import com.johnsimon.payback.async.Background;
import com.johnsimon.payback.async.BackgroundBlock;
import com.johnsimon.payback.async.Promise;
import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.preferences.Preferences;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.ui.FeedActivity;
import com.johnsimon.payback.util.Alarm;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

public class AppData {
	@SerializedName("people")
	public ArrayList<Person> people;

	@SerializedName("debts")
    public ArrayList<Debt> debts;

	@SerializedName("deleted")
    public HashSet<UUID> deleted;


	@SerializedName("peopleOrder")
	public PeopleOrder peopleOrder;

	@SerializedName("peopleOrderTouched")
	public long peopleOrderTouched;


	@SerializedName("preferences")
	public Preferences preferences;

	@SerializedName("version")
	public int version;

    public transient ArrayList<Contact> contacts;

    public AppData() {
    }

    public AppData(ArrayList<Person> people, ArrayList<Debt> debts, HashSet<UUID> deleted, PeopleOrder peopleOrder, long peopleOrderTouched, Preferences preferences) {
        this.people = people;
        this.debts = debts;
        this.deleted = deleted;
		this.peopleOrder = peopleOrder;
		this.peopleOrderTouched = peopleOrderTouched;
		this.preferences = preferences;
    }

    public static AppData defaultAppData() {
        return new AppData(new ArrayList<Person>(),
						   new ArrayList<Debt>(),
				 		   new HashSet<UUID>(),
						   //set peopleOrderTouched to 0 in order to penalize during syncing
						   PeopleOrder.defaultPeopleOrder(), 0,
						   Preferences.defaultPreferences());
    }

    public String save() {
        return AppData.toJson(this);
    }

    public Promise<String> saveAsync(Context context) {
        return Background.run(context, new BackgroundBlock<String>() {
            @Override
            public String run() {
                return AppData.this.save();
            }
        });
    }


    public boolean isLinked() {
        return contacts != null;
    }

	public ArrayList<Person> peopleOrdered() {
		return peopleOrder.order(people);
	}

    public ArrayList<Debt> feed(Person person) {
        if(person == null) return new ArrayList<>(debts);

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
                total += debt.getRemainingDebt();
            }
        }
        return total;
    }

    public float totalPlus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (!debt.isPaidBack() && debt.getAmount() > 0) {
                sum += debt.getRemainingAbsoluteDebt();
            }
        }
        return sum;
    }

    public float totalMinus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (!debt.isPaidBack() && debt.getAmount() < 0) {
                sum += debt.getRemainingAbsoluteDebt();
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

    public void merge(Context context, Person from, Person to) {
        for(Debt debt : debts) {
            if(debt.getOwner() == from) {
                debt.setOwner(to);
            }
        }
        delete(context, from);
    }

    public void delete(Context context, Person person) {
        deleteDebts(context, person);
        deleted.add(person.id);
		peopleOrder.remove(person.id);
		touchPeopleOrder();
		people.remove(person);
    }
    public void delete(Context context, Debt debt) {
        deleted.add(debt.id);
        debts.remove(debt);

		if(debt.hasReminder()) {
			Alarm.cancelAlarm(context, debt);
		}
        Alarm.cancelNotification(context, debt);
	}

    public void add(Debt debt) {
        debts.add(debt);
    }
    public void addFirst(Debt debt) {
        debts.add(0, debt);
    }

	public void add(Person person) {
		peopleOrder.add(person.id);
		people.add(person);
		touchPeopleOrder();
	}

    private void deleteDebts(Context context, Person person) {
        ArrayList<Debt> debts = feed(person);

        for(Debt debt : debts) {
            delete(context, debt);
        }
    }

    public void move(Debt debt, Person person) {
        debt.setOwner(person);
    }

    public void sync(Context context, Person person, DebtSendable[] debts) {
        deleteDebts(context, person);
        for(DebtSendable debt : debts) {
            add(debt.extract(person));
        }
    }

    public Person getOrCreatePerson(String name, ColorPalette colorPalette) {
        //Try to find existing person
        Person person = findPersonByName(name);
        if(person != null) {
            return person;
        }

        //Create new person
        //Attempt to find link
        Contact link = null;
        if (contacts != null) {
            for (Contact contact : contacts) {
                if (contact.name.equals(name)) {
                    link = contact;
                    break;
                }
            }
        }

        //Create person and add to people
        person = new Person(name, colorPalette);
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

	public void touchPeopleOrder() {
		peopleOrderTouched = System.currentTimeMillis();
	}

	public static boolean isEven(ArrayList<Debt> debts) {
		return total(debts) == 0;
	}

    public static AppData fromJson(String JSON) {
		if(JSON == null) {
			return AppData.defaultAppData();
		}

		//TODO remove when shitty period ends

		//AppData data = Resource.gson().fromJson(JSON, AppData.class);

		AppData data = ShittyAppData.fromJson(JSON);

		for(Debt debt : data.debts) {
			debt.linkOwner(data.people);
		}

        ensureNewFeatures(data);

		if(BuildConfig.DEBUG) {
			analyzeData(data);
		}

		return data;

    }

    public static void ensureNewFeatures(AppData data) {
        if(data.preferences == null) {
            data.preferences = Preferences.defaultPreferences();
        }

        if(data.peopleOrder == null) {
            data.peopleOrder = new PeopleOrder(data.people);
        }
    }

	public static void analyzeData(AppData data) {
		for(Debt debt : data.debts) {
			if(debt == null) {
				throw new RuntimeException("Null debt in debts");
			}
			if(debt.getOwner() == null){
				throw new RuntimeException("Null owner in debts");
			}
		}

		for(Person person : data.people) {
			if(person == null) {
				throw new RuntimeException("Null person in people");
			}
		}

		if(data.peopleOrder.size() != data.people.size()) {
			throw new RuntimeException("peopleOrder size not equal to people size. peopleOrder.size = " + data.peopleOrder.size() + ", people.size = " + data.people.size());
		}
	}

    public static String toJson(AppData data) {
        return Resource.gson().toJson(data, AppData.class);
    }
}
