package edu.byu.cs240.familymapclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import model.Person;
import results.PeopleResult;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DataCacheTest {

    // public Person(String personID, String associatedUsername, String firstName, String lastName,
    // String gender, String fatherID, String motherId, String spouseID)

    private Person braden;
    private Person anna;
    private Person jeremy;
    private Person stacy;
    private Person keith;
    private Person susan;

    private DataCache dataCache = DataCache.getInstance();

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @BeforeEach
    public void setUp() {
        braden = new Person("bkw", "bkwebb23", "Braden",
                "Webb", "m", "jdw", "slw", "ace");
        anna = new Person("ace", "bkwebb23", "Anna",
                "Everett", "f", "ce", "se", "bkw");
        jeremy = new Person("jdw", "bkwebb23", "Jeremy",
                "Webb", "m", "klw", "sw", "slw");
        stacy = new Person("slw", "bkwebb23", "Stacy",
                "Webb", "f", "ls", "sp", "jdw");
        keith = new Person("klw", "bkwebb23", "Keith",
                "Webb", "m", "gew", "gerrie", "sw");
        susan = new Person("sw", "bkwebb23", "Susan",
                "Webb", "f", "joyce", "walt?", "klw");

        System.out.println("setUp() called");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("tearDown() called");
    }

    @Test
    public void resultToPeoplePass() {
        List<Person> peopleList = Arrays.asList(braden, anna, jeremy, stacy, keith, susan);
        PeopleResult result = new PeopleResult(peopleList);

        assertDoesNotThrow(() -> dataCache.resultToPeople(result));
        Map<String, Person> people = dataCache.getPeople();
        assertNotNull(people);
        assertEquals(peopleList.size(), people.size());
        for (Map.Entry<String, Person> entry : dataCache.getPeople().entrySet()) {
//            assertEquals();
        }
    }


}