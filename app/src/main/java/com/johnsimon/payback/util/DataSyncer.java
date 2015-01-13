package com.johnsimon.payback.util;

import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Identifiable;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.core.Syncable;
import com.johnsimon.payback.core.SyncedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Created by johnrs on 2015-01-04.
 */
public class DataSyncer {
    public static AppData sync(AppData a, AppData b) {
        HashSet<UUID> deleted = new HashSet<>();
        deleted.addAll(a.deleted);
        deleted.addAll(b.deleted);

        removeDeleted(a.people, deleted);
        removeDeleted(b.people, deleted);
        removeDeleted(a.debts, deleted);
        removeDeleted(b.debts, deleted);

        ArrayList<Person> people = sync(a.people, b.people);
        ArrayList<Debt> debts = sync(a.debts, b.debts);

        return new AppData(people, debts, deleted);
    }

    private static <T extends SyncedData> void removeDeleted(ArrayList<T> array, HashSet<UUID> deleted) {
        for(Iterator<T> iterator = array.iterator(); iterator.hasNext();) {
            if(deleted.contains(iterator.next().id)) {
                iterator.remove();
            }
        }
    }

    private static <T extends SyncedData> T find(ArrayList<T> array, UUID id) {
        for(T item : array) {
            if(item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    private static <T extends SyncedData<T>> ArrayList<T> sync(ArrayList<T> a, ArrayList<T> b) {
        ArrayList<T> array = new ArrayList<>();

        array.addAll(a);
        for(T item : b) {
            T other = find(array, item.id);
            if(other != null) {
                array.remove(other);
                array.add(item.syncWith(other));
            } else {
                array.add(item);
            }
        }

        return array;
    }
}
