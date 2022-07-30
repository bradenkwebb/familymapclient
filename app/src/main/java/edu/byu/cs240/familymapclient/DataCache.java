package edu.byu.cs240.familymapclient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;

public class DataCache {

    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {}

    Map<String, Person> people; // a map from personID to Person
    Map<String, Event> events; // a map from eventID to Event
    Map<String, List<Event>> personEvents; // a map from personID to that person's events
    Set<String> paternalAncestors; // a map from personID to their paternal ancestors
    Set<String> maternalAncestors; // a map from personID to their maternal ancestors

//    Settings settings;

//    Person getPersonByID(PersonID id) {}
//    Event getEventByID(EventID id) {}
//    List<Event> getPersonEvents(PersonID id) {}
}
