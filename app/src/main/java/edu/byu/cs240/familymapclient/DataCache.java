package edu.byu.cs240.familymapclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;
import model.User;
import results.AllEventsResult;
import results.PeopleResult;

public class DataCache {

    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {
        people = new HashMap<>();
        events = new HashMap<>();
        personEvents = new HashMap<>();
        paternalAncestors = new HashSet<>();
        maternalAncestors = new HashSet<>();
    }

    public void resultToPeople(PeopleResult result) {
        this.people.clear();

        List<Person> people = result.getData();
        for (Person person : people) {
            this.people.put(person.getPersonID(), person);
        }
    }

    public void resultToEvents(AllEventsResult result){
        this.events.clear();

        List<Event> events = result.getData();
        for (Event event : events) {
            this.events.put(event.getEventID(), event);
        }
    }

    // THERE COULD DEFINITELY BE BUGS HERE
    private void generatePersonEvents() {
        personEvents.clear();
        for (Map.Entry<String, Event> entry : events.entrySet()) {
            String personID = entry.getValue().getPersonID();
            if (personEvents.containsKey(personID) && personEvents.get(personID) != null) {
                personEvents.get(personID).add((Event) entry.getValue());
            } else {
                personEvents.put(personID,
                        new ArrayList<Event>(Collections.singletonList(entry.getValue())));
            }
        }
    }

    private Map<String, Person> people; // a map from personID to Person
    private Map<String, Event> events; // a map from eventID to Event
    private Map<String, List<Event>> personEvents; // a map from personID to that person's events
    private Set<String> paternalAncestors; // a map from personID to their paternal ancestors
    private Set<String> maternalAncestors; // a map from personID to their maternal ancestors

    private String userPersonID;

    public String getUserPersonID() {
        return userPersonID;
    }

    public void setUserPersonID(String userPersonID) {
        this.userPersonID = userPersonID;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    public Map<String, List<Event>> getPersonEvents() {
        return personEvents;
    }

    public void setPersonEvents(Map<String, List<Event>> personEvents) {
        this.personEvents = personEvents;
    }

    public Set<String> getPaternalAncestors() {
        return paternalAncestors;
    }

    public void setPaternalAncestors(Set<String> paternalAncestors) {
        this.paternalAncestors = paternalAncestors;
    }

    public Set<String> getMaternalAncestors() {
        return maternalAncestors;
    }

    public void setMaternalAncestors(Set<String> maternalAncestors) {
        this.maternalAncestors = maternalAncestors;
    }
//    Settings settings;

//    Person getPersonByID(PersonID id) {}
//    Event getEventByID(EventID id) {}
//    List<Event> getPersonEvents(PersonID id) {}
}
