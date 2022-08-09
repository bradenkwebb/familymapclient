package edu.byu.cs240.familymapclient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import requests.LoginRequest;
import results.LoginResult;

public class SettingsTest {

    private final Settings settings = Settings.getInstance();
    private final DataCache dataCache = DataCache.getInstance();

    @BeforeEach
    public void setUp() {
        System.out.println("setUp() called");
        LoginRequest request = new LoginRequest();
        request.setUsername("sheila");
        request.setPassword("parker");
        ServerProxy.setServerHost("127.0.0.1");
        ServerProxy.setServerPort("8080");
        LoginResult result = new ServerProxy().login(request);
        DataCache.getInstance().setUserPersonID(result.getPersonID());
    }

    @AfterEach
    public void tearDown() {
        System.out.println("tearDown() called");
        DataCache.getInstance().clear();
    }

    @Test
    public void filterValid() {
        System.out.println("filterValid() called");
        settings.setIncludeMaleEvents(true);
        settings.setIncludePaternalAncestors(true);
        assert(settings.filter(dataCache.getEvents().get("Blaine_Birth")));
        settings.setIncludeFemaleEvents(true);
        assert(settings.filter(dataCache.getEvents().get("Mrs_Rodham_Backflip")));
        settings.setIncludeMaternalAncestors(false);
        settings.setIncludePaternalAncestors(false);
        assert(settings.filter(dataCache.getEvents().get("Sheila_Birth")));
        assert(settings.filter(dataCache.getEvents().get("Other_Asteroids")));
        assert(settings.filter(dataCache.getEvents().get("Davis_Birth")));
        settings.setIncludeFemaleEvents(false);
        assert(settings.filter(dataCache.getEvents().get("Davis_Birth")));
    }

    @Test
    public void filterInvalid() {
        System.out.println("filterInvalid() called");
        settings.setIncludeFemaleEvents(false);
        settings.setIncludeMaleEvents(false);
        assertFalse(settings.filter(dataCache.getEvents().get("Sheila_Birth")));
        assertFalse(settings.filter(dataCache.getEvents().get("Betty_Death")));
        assertThrows(IndexOutOfBoundsException.class,
                () -> settings.filter(dataCache.filteredEvents().get(0)));
    }

}
