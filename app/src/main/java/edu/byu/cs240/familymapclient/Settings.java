package edu.byu.cs240.familymapclient;

import model.Event;
import model.Person;

public class Settings {

    private static final Settings instance = new Settings();
    public static Settings getInstance() { return instance; }

    private boolean settingsChanged;

    private boolean includePaternalAncestors;
    private boolean includeMaternalAncestors;
    private boolean includeMaleEvents;
    private boolean includeFemaleEvents;

    private boolean showLifeStoryLines;
    private boolean showFamTreeLines;
    private boolean showSpouseLines;

    public void setLogout(boolean logout) {
        this.logout = logout;
    }

    private boolean logout;

    public boolean filter(Event e) {
        Person p = DataCache.getInstance().getPeople().get(e.getPersonID());
        if (p.getPersonID() != DataCache.getInstance().getUserPersonID()) {
            if (!includePaternalAncestors &&
                    DataCache.getInstance().getPaternalAncestorIDs().contains(p.getPersonID())) {
                return  false;
            } else if (!includeMaternalAncestors &&
                    DataCache.getInstance().getMaternalAncestors().contains(p.getPersonID())) {
                return false;
            }
        }
        if (!includeMaleEvents && p.getGender().equalsIgnoreCase("m")) {
            return false;
        }
        if (!includeFemaleEvents && p.getGender().equalsIgnoreCase("f")) {
            return false;
        }
        return true;
    }

    private Settings() {
        settingsChanged = true;

        includePaternalAncestors = true;
        includeMaternalAncestors = true;
        includeMaleEvents = true;
        includeFemaleEvents = true;

        showLifeStoryLines = true;
        showFamTreeLines = true;
        showSpouseLines = true;

        logout = false;
    }

    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    public void setSettingsChanged(boolean settingsChanged) {
        this.settingsChanged = settingsChanged;
    }

    public boolean isIncludePaternalAncestors() {
        return includePaternalAncestors;
    }

    public void toggleIncludePaternalAncestors() {
        includePaternalAncestors = !includePaternalAncestors;
    }

    public void setIncludePaternalAncestors(boolean includePaternalAncestors) {
        this.includePaternalAncestors = includePaternalAncestors;
    }

    public boolean isIncludeMaternalAncestors() {
        return includeMaternalAncestors;
    }

    public void toggleIncludeMaternalAncestors() {
        includeMaternalAncestors = ! includeMaternalAncestors;
    }

    public void setIncludeMaternalAncestors(boolean includeMaternalAncestors) {
        this.includeMaternalAncestors = includeMaternalAncestors;
    }

    public boolean isIncludeMaleEvents() {
        return includeMaleEvents;
    }

    public void toggleIncludeMaleEvents() { includeMaleEvents = !includeMaleEvents; }

    public void setIncludeMaleEvents(boolean includeMaleEvents) {
        this.includeMaleEvents = includeMaleEvents;
    }

    public boolean isIncludeFemaleEvents() {
        return includeFemaleEvents;
    }

    public void toggleIncludeFemaleEvents() { includeFemaleEvents = !includeFemaleEvents; }

    public void setIncludeFemaleEvents(boolean includeFemaleEvents) {
        this.includeFemaleEvents = includeFemaleEvents;
    }

    public boolean isShowLifeStoryLines() {
        return showLifeStoryLines;
    }

    public void toggleShowLifeStoryLines() { showLifeStoryLines = !showLifeStoryLines; }

    public void setShowLifeStoryLines(boolean showLifeStoryLines) {
        this.showLifeStoryLines = showLifeStoryLines;
    }

    public boolean isShowFamTreeLines() {
        return showFamTreeLines;
    }

    public void toggleShowFamTreeLines() { showFamTreeLines = !showFamTreeLines; }

    public void setShowFamTreeLines(boolean showFamTreeLines) {
        this.showFamTreeLines = showFamTreeLines;
    }

    public boolean isShowSpouseLines() {
        return showSpouseLines;
    }

    public void toggleShowSpouseLines() { showSpouseLines = !showSpouseLines;}

    public void setShowSpouseLines(boolean showSpouseLines) {
        this.showSpouseLines = showSpouseLines;
    }

    public boolean isLogout() {
        return logout;
    }
}
