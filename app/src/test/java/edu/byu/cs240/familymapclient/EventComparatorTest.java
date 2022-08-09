package edu.byu.cs240.familymapclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.Event;

public class EventComparatorTest {

    // These events should occur in numerical order
    private Event e1 = new Event("1", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "birth", 1900);
    private Event e2 = new Event("2", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "birth", 1900);
    private Event e3 = new Event("3", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "birth", 1901);
    private Event e4 = new Event("4", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "Birth", 1902);
    private Event e5 = new Event("5", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "marriage", 1900);
    private Event e6 = new Event("6", "bkwebb23", "bkw", 23f,
            42f, "Gondor", "Minas Tirith", "death", 1900);
    private static final EventComparator comparator = new EventComparator();

    @Test
    public void compareTest() {
        System.out.println("compareTest() called");
        assertDoesNotThrow(() -> comparator.compare(e1, e2));
        assertEquals(0, comparator.compare(e1, e2),
                "Events with same eventTypes and years are considered unequal");
        assertDoesNotThrow(() -> comparator.compare(e2, e3));
        assertEquals(-1, comparator.compare(e2, e3),
                "Events with same eventTypes are not properly compared by year");
        assertEquals(-1, comparator.compare(e1, e3),
                "Events with same eventTypes are not properly compared by year");
        assertEquals(-1, comparator.compare(e3, e4),
                "Events with equivalent eventTypes are not properly compared by year");
        assertEquals(-1, comparator.compare(e4, e5),
                "birth events are not taking precedence over year");
        assertEquals(-1, comparator.compare(e5, e6),
                "non-death events are not taking precedence over eventType comparison");
        assertEquals(1, comparator.compare(e6, e5));
        assertEquals(1, comparator.compare(e5, e4));
        assertEquals(1, comparator.compare(e4, e3));
        assertEquals(1, comparator.compare(e3, e2));
        assertEquals(0, comparator.compare(e2, e1));
    }
}
