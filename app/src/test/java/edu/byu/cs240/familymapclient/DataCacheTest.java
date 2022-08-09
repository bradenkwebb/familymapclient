package edu.byu.cs240.familymapclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import model.Person;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.PeopleResult;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DataCacheTest {

    private Person braden;
    private Person anna;
    private Person jeremy;
    private Person stacy;
    private Person keith;
    private Person susan;

    private LoginRequest patrickLogin;
    private DataCache dataCache = DataCache.getInstance();
    private ServerProxy proxy;


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @BeforeEach
    public void setUp() {
        System.out.println("setUp() called");

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

        patrickLogin = new LoginRequest();
        patrickLogin.setUsername("patrick");
        patrickLogin.setPassword("spencer");

        proxy = new ServerProxy();
        ServerProxy.setServerHost("localhost");
        ServerProxy.setServerPort("8080");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("tearDown() called");
        dataCache.clear();
    }

    @Test
    public void resultToPeopleNormal() {
        System.out.println("resultToPeopleNormal() called");
        List<Person> peopleList = Arrays.asList(braden, anna, jeremy, stacy, keith, susan);
        PeopleResult result = new PeopleResult(peopleList);

        assertDoesNotThrow(() -> dataCache.resultToPeople(result));
        Map<String, Person> people = dataCache.getPeople();
        assertNotNull(people);
        assertEquals(peopleList.size(), people.size());
        for (Person p : peopleList) {
            assertEquals(p.getFirstName(), people.get(p.getPersonID()).getFirstName());
            assertEquals(p.getLastName(), people.get(p.getPersonID()).getLastName());
        }
    }

    @Test
    public void resultToPeopleAbnormal() {
        System.out.println("resultToPeopleAbnormal() called");
        PeopleResult result = new PeopleResult(new ArrayList());
        dataCache.resultToPeople(result);
        assertEquals(0, dataCache.getPeople().size());

        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);
        assertEquals(8, DataCache.getInstance().getPeople().size());

        dataCache.resultToPeople(result);
        assertEquals(0, dataCache.getPeople().size());
    }

    @Test
    public void resultToEventsPass() {
        System.out.println("resultToEventsPass() called");

        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);

        assertEquals(16, dataCache.getEvents().size());
    }

    @Test
    public void resultToEventsFail() {
        System.out.println("resultToEventsFail() called");

        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);

        dataCache.clear();
        assertEquals(0, dataCache.getEvents().size());
    }

    @Test
    public void getImmediateFamilyPass() {
        System.out.println("getImmediateFamilyPass() called");
        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);

        assertEquals(3, dataCache.getImmediateFamily("Sheila_Parker").size());
        assertEquals(4, dataCache.getImmediateFamily("Betty_White").size());
        assertEquals(2, dataCache.getImmediateFamily("Frank_Jones").size());
    }

    @Test
    public void getImmediateFamilyFail() {
        System.out.println("getImmediateFamilyFail() called ");
        assertThrows(AssertionError.class, () -> dataCache.getImmediateFamily("braden_webb"));
    }

    @Test
    public void eventColorsPass() {
        System.out.println("eventColorsPass() called");
        RegisterRequest r = new RegisterRequest();
        String username = UUID.randomUUID().toString();
        r.setUsername(username);
        r.setPassword("password");
        r.setEmail("email@email.com");
        r.setGender("m");
        r.setFirstName("braden");
        r.setLastName("webb");
        r.setGender("m");
        new ServerProxy().register(r);

        assertEquals(3, dataCache.eventColors().size());
        assert(dataCache.eventColors().containsKey("birth"));
        assertNotNull(dataCache.eventColors().get("birth"));
        assert(dataCache.eventColors().containsKey("marriage"));
        assertNotNull(dataCache.eventColors().get("marriage"));
        assert(dataCache.eventColors().containsKey("death"));
        assertNotNull(dataCache.eventColors().get("death"));
        Float diff1 = dataCache.eventColors().get("birth") - dataCache.eventColors().get("marriage");
        Float diff2 = dataCache.eventColors().get("marriage") - dataCache.eventColors().get("death");
        assertEquals(Math.abs(diff1), Math.abs(diff2));
        dataCache.clear();
        assertEquals(0, dataCache.eventColors().size());
    }

    @Test
    public void eventColorsFail() {
        System.out.println("eventColorsFail() called");
        assertNull(dataCache.eventColors().get("baptism"));
    }

    @Test
    public void getPersonEventsNormal() {
        System.out.println("getPersonEventsNormal() called");

        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);

        assertEquals(5, dataCache.getPersonEvents().get("Sheila_Parker").size());
        assertEquals(1, dataCache.getPersonEvents().get("Betty_White").size());
        assertEquals(2, dataCache.getPersonEvents().get("Mrs_Rodham").size());
    }

    @Test
    public void getPersonEventsAbnormal() {
        System.out.println("getPersonEventsAbnormal() called");

        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        new ServerProxy().login(request);

        // Filtering shouldn't matter within the datacache, only outside
        Settings.getInstance().setIncludeMaleEvents(false);
        assertEquals(1, dataCache.getPersonEvents().get("Blaine_McGary").size());
        Settings.getInstance().setIncludeFemaleEvents(false);
        assertEquals(5, dataCache.getPersonEvents().get("Sheila_Parker").size());
    }
}