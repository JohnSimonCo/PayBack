package com.johnsimon.payback.util;

import android.app.Activity;
import android.content.Context;

import com.johnsimon.payback.core.Contact;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.core.User;
import com.johnsimon.payback.send.DebtSendable;
import com.johnsimon.payback.ui.FeedActivity;

import java.util.ArrayList;
import java.util.UUID;

public class AppData {
    public ArrayList<Contact> contacts;
    public User user;

    private SaveData data;
    public ArrayList<Debt> debts;
    public ArrayList<Person> people;

    public AppData(Activity context, String JSON) {
        this.contacts = Contacts.getContacts(context);
        this.user = Contacts.getUser(context);

        this.data = SaveData.fromJson(JSON, contacts);
        this.debts = data.debts;
        this.people = data.people;
    }

    public String save() {
        return SaveData.toJson(data);
    }

    public ArrayList<Debt> feed(Person person) {
        if(person == null) return debts;

        ArrayList<Debt> feed = new ArrayList<Debt>();
        for(Debt debt : debts) {
            if(debt.owner == person) {
                feed.add(debt);
            }
        }
        return feed;
    }

    public static float total(ArrayList<Debt> debts) {
        float total = 0;
        for(Debt debt : debts) {
            if(!debt.isPaidBack) {
                total += debt.amount;
            }
        }
        return total;
    }

    public float totalPlus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (debt.amount > 0) {
                sum += debt.amount;
            }
        }
        return sum;
    }

    public float totalMinus() {
        float sum = 0;
        for (Debt debt : debts) {
            if (debt.amount < 0) {
                sum += debt.amount;
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

    public /*static*/ Person findPerson(ArrayList<Person> people, UUID id) {
        for(Person p : people) {
            if(p.id.equals(id)) return p;
        }
        return null;
    }

    public Person findPersonByName(String name) {
        for(Person p : people) {
            if(p.name.equals(name)) return p;
        }
        return null;
    }

    public Debt findDebt(long timestamp) {
        for (Debt debt : debts) {
            if(debt.timestamp == timestamp) return debt;
        }

        return null;
    }

    public void merge(Person from, Person to) {
        for(Debt debt : debts) {
            if(debt.owner == from) {
                debt.owner = to;
            }
        }
        people.remove(from);
    }

    public void unmerge(Person restore, ArrayList<Debt> debts, int index) {
        for(Debt debt : debts) {
            debt.owner = restore;
        }
        people.add(index, restore);
    }
    public void delete(Person person) {
        deleteDebts(person);
        people.remove(person);
    }

    private void deleteDebts(Person person) {
        ArrayList<Debt> remove = new ArrayList<Debt>();
        for(Debt debt : debts) {
            if(debt.owner == person) {
                remove.add(debt);
            }
        }

        for(Debt debt : remove) {
            debts.remove(debt);
        }
    }

    public void move(Debt debt, Person person) {
        debt.owner = person;
    }

    public void rename(Person person, String name) {
        person.name = name;
    }

    public void sync(Person person, DebtSendable[] debts) {
        deleteDebts(person);
        for(DebtSendable debt : debts) {
            this.debts.add(debt.extract(person));
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
        person = new Person(name, link, ColorPalette.getInstance(context));
        people.add(person);
        return person;
    }

    //Returns all unique people names
    public ArrayList<String> getPeopleNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Person person : people) {
            if(!names.contains(person.name)) {
                names.add(person.name);
            }
        }

        return names;
    }

    //Returns all unique names (from people and contacts)
    public ArrayList<String> getAllNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Person person : people) {
            if(!names.contains(person.name)) {
                names.add(person.name);
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

    public String guessName(User sender) {
        //If user has no name, use currently viewed person
        if(user.name == null) {
            return FeedActivity.isAll() ? null : FeedActivity.person.name;
        }

        //If user has a name:
        //First match name and number in people and their links
        for(Person person : people) {
            if(person.matchTo(sender) || (person.isLinked() && person.link.matchTo(sender))) {
                return person.name;
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
}
