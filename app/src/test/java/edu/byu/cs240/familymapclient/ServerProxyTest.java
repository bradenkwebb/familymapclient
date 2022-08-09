package edu.byu.cs240.familymapclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import requests.LoginRequest;
import requests.RegisterRequest;
import results.LoginResult;

public class ServerProxyTest {

    private final Settings settings = Settings.getInstance();
    private final DataCache dataCache = DataCache.getInstance();
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    public void setUp() {
        System.out.println("setUp() called");
        ServerProxy.setServerHost("localhost");
        ServerProxy.setServerPort("8080");

        settings.setIncludeFemaleEvents(true);
        settings.setIncludeMaleEvents(true);
        settings.setIncludeMaternalAncestors(true);
        settings.setIncludePaternalAncestors(true);
    }

    @AfterEach
    public void tearDown() {
        dataCache.clear();
    }

    @Test
    public void loginPass() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("sheila");
        loginRequest.setPassword("parker");
        Assertions.assertDoesNotThrow(() -> new ServerProxy().login(loginRequest));
        dataCache.clear();

        LoginResult result = new ServerProxy().login(loginRequest);
        assertNotNull(result.getPersonID());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getUsername());
        assertNull(result.getMessage());
        assert(result.isSuccess());

        dataCache.clear();
        loginRequest.setUsername("patrick");
        loginRequest.setPassword("spencer");
        result = new ServerProxy().login(loginRequest);

        assertNotNull(result.getPersonID());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getUsername());
        assert(result.isSuccess());
    }

    @Test
    public void loginFail() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("bkwebb23");
        loginRequest.setPassword("password");
        LoginResult result = new ServerProxy().login(loginRequest);
        assertNotNull(result.getMessage());
        assertFalse(result.isSuccess());
        assertNull(result.getUsername());
        assertNull(result.getPersonID());
        assertNull(result.getAuthtoken());
    }

    @Test
    public void registerPass() {
        registerRequest = new RegisterRequest();
        String username = UUID.randomUUID().toString();
        registerRequest.setUsername(UUID.randomUUID().toString());
        registerRequest.setPassword("password");
        registerRequest.setEmail("email@email.com");
        registerRequest.setGender("m");
        registerRequest.setFirstName("braden");
        registerRequest.setLastName("webb");
        registerRequest.setGender("m");

        assertDoesNotThrow(() -> new ServerProxy().register(registerRequest));
        registerRequest.setUsername(username);

        LoginResult r = new ServerProxy().register(registerRequest);
        assert(r.isSuccess());
        assertNotNull(r.getPersonID());
        assertEquals(username, r.getUsername());
        assertNotNull(r.getAuthtoken());
        assertNull(r.getMessage());
    }

    @Test
    public void registerFail() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("sheila");
        registerRequest.setPassword("parker");
        registerRequest.setEmail("email@email.com");
        registerRequest.setFirstName("braden");
        registerRequest.setLastName("webb");
        registerRequest.setGender("m");

        assertDoesNotThrow(() -> new ServerProxy().register(registerRequest));
        LoginResult r = new ServerProxy().register(registerRequest);
        assertFalse(r.isSuccess());
        assertNotNull(r.getMessage());

        registerRequest.setPassword("password");
        assertDoesNotThrow(() -> new ServerProxy().register(registerRequest));
        r = new ServerProxy().register(registerRequest);
        assertFalse(r.isSuccess());
        assertNotNull(r.getMessage());
    }

    @Test
    public void getEventsPass() {
        registerRequest = new RegisterRequest();
        String username = UUID.randomUUID().toString();
        registerRequest.setUsername(username);
        registerRequest.setPassword("password");
        registerRequest.setEmail("email@email.com");
        registerRequest.setGender("m");
        registerRequest.setFirstName("braden");
        registerRequest.setLastName("webb");
        registerRequest.setGender("m");
        new ServerProxy().register(registerRequest);

        assertEquals(91, dataCache.getEvents().size());
    }

    @Test
    public void getEventsFail() {
        // Because this is a private method called only indirectly even by ServerProxy().login()
        // and ServerProxy().register(), I can't really test this from this side
    }

    @Test
    public void getPeoplePass() {
        registerRequest = new RegisterRequest();
        String username = UUID.randomUUID().toString();
        registerRequest.setUsername(username);
        registerRequest.setPassword("password");
        registerRequest.setEmail("email@email.com");
        registerRequest.setGender("m");
        registerRequest.setFirstName("braden");
        registerRequest.setLastName("webb");
        registerRequest.setGender("m");
        LoginResult r = new ServerProxy().register(registerRequest);
        dataCache.setUserPersonID(r.getPersonID());

        assertEquals(31, dataCache.getPeople().size());
        assertEquals(15, dataCache.getPatAncestorIDs().size());
        assertEquals(15, dataCache.getMatAncestorIDs().size());
    }
    @Test
    public void getPeopleFail() {
        // Because this is a private method called only indirectly even by ServerProxy().login()
        // and ServerProxy().register(), I can't really test this from this side
    }
}
