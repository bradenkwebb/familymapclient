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

    private Map<String, Person> people; // a map from personID to Person
    private Map<String, Event> events; // a map from eventID to Event
    private Map<String, List<Event>> personEvents; // a map from personID to that person's events
    private Set<String> paternalAncestors; // a map from personID to their paternal ancestors
    private Set<String> maternalAncestors; // a map from personID to their maternal ancestors
    private String userPersonID;

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

    public List<Person> getImmediateFamily(String personID) {
        List<Person> immediateFamily = new ArrayList<>();
        Map<String, Person> allPeople = DataCache.getInstance().getPeople();
        Person person = allPeople.get(personID);

        assert person != null;
        if (person.getMotherID() != null) {
            immediateFamily.add(allPeople.get(person.getMotherID()));
        }
        if (person.getFatherID() != null) {
            immediateFamily.add(allPeople.get(person.getFatherID()));
        }
        if (person.getSpouseID() != null) {
            immediateFamily.add(allPeople.get(person.getSpouseID()));
        }
        if (person.getGender().equalsIgnoreCase("f")) {
            for (Person p : allPeople.values()) {
                if (p.getMotherID() != null && p.getMotherID().equals(personID)) {
                    immediateFamily.add(p);
                }
            }
        } else {
            for (Person p : allPeople.values()) {
                if (p.getFatherID() != null && p.getFatherID().equals(personID)) {
                    immediateFamily.add(p);
                }
            }
        }
        return immediateFamily;
    }

    public Map<String, Float> eventColors() {
        Map<String, Float> eventColors = new HashMap<>();
        Set<String> eventTypes = eventTypes();

        float skip = 360f / eventTypes.size();
        int counter = 0;
        for (String eventType : eventTypes) {
            eventColors.put(eventType, counter * skip);
            counter++;
        }
        return eventColors;
    }

    // THERE COULD DEFINITELY BE BUGS HERE
    public void generatePersonEvents() {
        personEvents.clear();
        for (Map.Entry<String, Event> entry : events.entrySet()) {
            String personID = entry.getValue().getPersonID();
            if (personEvents.containsKey(personID) && personEvents.get(personID) != null) {
                personEvents.get(personID).add(entry.getValue());
            } else {
                personEvents.put(personID,
                        new ArrayList<>(Collections.singletonList(entry.getValue())));
            }
        }
    }

    public String getUserPersonID() {
        return userPersonID;
    }

    public Set<String> eventTypes() {
        HashSet<String> types = new HashSet<>();
        for (Event event : events.values()) {
            types.add(event.getEventType());
        }
        return types;
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
