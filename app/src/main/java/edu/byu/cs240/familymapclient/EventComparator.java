package edu.byu.cs240.familymapclient;

import java.util.Comparator;

import model.Event;

public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event e1, Event e2) {
        if (e1.getEventType().equalsIgnoreCase(e2.getEventType())) {
            if (e1.getYear().equals(e2.getYear())) {
                if (e1.getEventType().equals(e2.getEventType())) {
                    return 0;
                } else {
                    return e1.getEventType().toLowerCase().compareTo(e2.getEventType().toLowerCase());
                }
            } else {
                return e1.getYear().compareTo(e2.getYear());
            }
        } else if (e1.getEventType().equalsIgnoreCase("birth")) {
            return -1;
        } else if (e2.getEventType().equalsIgnoreCase("birth")) {
            return 1;
        } else if (e1.getEventType().equalsIgnoreCase("death")) {
            return 1;
        } else if (e2.getEventType().equalsIgnoreCase("death")) {
            return -1;
        } else if (e1.getYear() != e2.getYear()) {
            return e1.getYear().compareTo(e2.getYear());
        }
        return e1.getEventType().toLowerCase().compareTo(e2.getEventType().toLowerCase());
    }
}
