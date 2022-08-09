package edu.byu.cs240.familymapclient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;
import results.AllEventsResult;
import results.PeopleResult;

public class DataCache {

    private static final DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private final Map<String, Person> people; // a map from personID to Person
    private final Map<String, Event> events; // a map from eventID to Event
    private final Map<String, List<Event>> personEvents; // a map from personID to that person's events
    private Set<String> paternalAncestors; // a map from personID to their paternal ancestors
    private Set<String> maternalAncestors; // a map from personID to their maternal ancestors
    private String userPersonID; // the personID of the person corresponding to the user

    /**
     * The private constructor, which should run only when getInstance() is called for the first time.
     */
    private DataCache() {
        people = new HashMap<>();
        events = new HashMap<>();
        personEvents = new HashMap<>();
        paternalAncestors = new HashSet<>();
        maternalAncestors = new HashSet<>();
    }

    /**
     * Stores the information contained in a PeopleResult object in the DataCache, as a map from
     * their personIDs to Person objects.
     *
     * @param result the PeopleResult object to parse
     */
    public void resultToPeople(PeopleResult result) {
        this.people.clear();

        List<Person> people = result.getData();
        for (Person person : people) {
            this.people.put(person.getPersonID(), person);
        }
    }

    /**
     * Stores the information contained in an AllEventsResult object in the DataCache, as a map from
     * their eventIDs to Events.
     *
     * @param result the AllEventsResult object to parse
     */
    public void resultToEvents(AllEventsResult result){
        this.events.clear();

        List<Event> events = result.getData();
        for (Event event : events) {
            this.events.put(event.getEventID(), event);
        }
    }

    /**
     * Returns a list of Person objects corresponding to the immediate family members of the
     * person with ID personID.
     * @param personID the person whose immediate family we're searching for
     * @return a list of Person objects
     */
    public List<Person> getImmediateFamily(String personID) {
        List<Person> immediateFamily = new ArrayList<>();
        Person person = people.get(personID);

        assert person != null;
        if (person.getMotherID() != null) {
            immediateFamily.add(people.get(person.getMotherID()));
        }
        if (person.getFatherID() != null) {
            immediateFamily.add(people.get(person.getFatherID()));
        }
        if (person.getSpouseID() != null) {
            immediateFamily.add(people.get(person.getSpouseID()));
        }
        for (Person p : people.values()) {
            if (p.getMotherID() != null && p.getMotherID().equals(personID)) {
                immediateFamily.add(p);
            }
            if (p.getFatherID() != null && p.getFatherID().equals(personID)) {
                immediateFamily.add(p);
            }
        }
        return immediateFamily;
    }

    /**
     * Returns a map from eventTypes to colors, where each color is maximally (and equally) distant
     * from the others.
     *
     * @return a map from eventType (Strings) to color (Floats)
     */
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

    /**
     * Wipes the data in the existing personEvents data member, and re-calculates the map from
     * the events dataset
     */
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

    /**
     * A getter, but re-generates the data member each time it's called for
     * @return the map from each personID to a list of their corresponding Event objects
     */
    public Map<String, List<Event>> getPersonEvents() {
        generatePersonEvents();
        return personEvents;
    }

    /**
     * Gets the set of personIDs corresponding to the userPerson's paternal ancestors.
     * @return a set of Strings
     */
    public Set<String> getPaternalAncestorIDs() {
        Person userPerson = people.get(userPersonID);
        if (userPerson != null && userPerson.getFatherID() != null) {
            paternalAncestors = getAncestors(people.get(people.get(userPersonID).getFatherID()));
        }
        return paternalAncestors;
    }

    /**
     * Gets the set of personIDs corresponding to the userPerson's maternal ancestors.
     * @return a set of Strings
     */
    public Set<String> getMaternalAncestors() {
        Person userPerson = people.get(userPersonID);
        if (userPerson.getMotherID() != null) {
            maternalAncestors = getAncestors(people.get(people.get(userPersonID).getMotherID()));
        }
        return maternalAncestors;
    }

    /**
     * Checks to see if each event in the data member "events" is valid under the current settings,
     * and if it is, returns that event as part of a list.
     *
     * @return a list of Event objects
     */
    public List<Event> filteredEvents() {
        List<Event> filtered = new ArrayList<>();
        Collection<Event> unfiltered = DataCache.getInstance().getEvents().values();
        for (Event event: unfiltered) {
            if (Settings.getInstance().filter(event)){
                filtered.add(event);
            }
        }
        return filtered;
    }

    // GETTERS, SETTERS, AND PRIVATE METHODS

    public void setUserPersonID(String userPersonID) {
        this.userPersonID = userPersonID;
    }

    public String getUserPersonID() {
        return userPersonID;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    private Set<String> getAncestors(Person p) {
        Set<String> ancestorIDs = new HashSet<>();
        if (p.getFatherID() != null) {
            ancestorIDs.addAll(getAncestors(people.get(p.getFatherID())));
        }
        if (p.getMotherID() != null) {
            ancestorIDs.addAll(getAncestors(people.get(p.getMotherID())));
        }
        ancestorIDs.add(p.getPersonID());
        return ancestorIDs;
    }

    private Set<String> eventTypes() {
        HashSet<String> types = new HashSet<>();
        for (Event event : events.values()) {
            types.add(event.getEventType());
        }
        return types;
    }

    public void clear() {
        people.clear();
        events.clear();
        personEvents.clear();
        paternalAncestors.clear();
        maternalAncestors.clear();
        userPersonID = null;
    }
}
