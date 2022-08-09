package edu.byu.cs240.familymapclient;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;


public class MapFragment extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private static final String LOG_TAG = "MapFragment";

    private GoogleMap map;
    private Map<String, Marker> eventMarkers;
    private Event selectedEvent;
    private Marker selectedMarker;
    private Set<Polyline> lines;
    private boolean repopulate;

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(true);

        // Initialize data members so we can add to them later
        lines = new HashSet<>();
        eventMarkers = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(LOG_TAG, "mapFragment was null");
        }
        return view;
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {

        // Specify that the map's attention is now on this marker and its corresponding event
        selectedMarker = marker;
        if (marker.getTag() instanceof Event) {
            selectedEvent = (Event) marker.getTag();
        }

        // Clear any lines that we previously had on the map
        clearLines();

        // Center the camera on the selected marker
        centerCamera(selectedMarker.getPosition());

        // Set the caption and gendered icon for the selected event, as well as listeners
        updateEventDescription();

        // Draw the appropriate lines on the map
        if (Settings.getInstance().isShowSpouseLines()) {
            drawSpouseLine(selectedEvent);
        }
        if (Settings.getInstance().isShowFamTreeLines()) {
            drawFamLines(selectedEvent, 1);
        }
        if (Settings.getInstance().isShowLifeStoryLines()) {
            drawLifeLines(selectedEvent);
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        // We decide to repopulate the map iff either the settings have changed, or we're in the
        // event activity
        if (Settings.getInstance().isSettingsChanged()) {
            repopulate = true;
        } else repopulate = getActivity() instanceof EventActivity;

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(LOG_TAG, "mapFragment was null");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);

        if (repopulate) {
            populateMap();
            repopulate = false;
        }

        map.setOnMarkerClickListener(this);
        Settings.getInstance().setSettingsChanged(false);
    }

    @Override
    public void onMapLoaded() {
        // If we're in the EventActivity, we should have been provided a specific event to focus on
        if (getActivity() instanceof EventActivity) {
            if (getArguments() != null) {
                String initEventID = getArguments().getString(EventActivity.EVENT_ID_KEY);
                if (initEventID != null) {
                    selectedEvent = DataCache.getInstance().getEvents().get(initEventID);
                    selectedMarker = eventMarkers.get(initEventID);
                } else {
                    Log.e(LOG_TAG, "The EVENT_ID_KEY is null");
                }
            } else {
                Log.e(LOG_TAG, "getArguments() is null even though we're in the eventActivity");
            }
        }

        // If we have a specific event to focus on, let's focus on it
        if (selectedMarker != null && selectedMarker.getTag() instanceof Event) {
            if (Settings.getInstance().filter((Event) selectedMarker.getTag())) {
                onMarkerClick(selectedMarker);
            } else {
                // Since the selectedEvent is now being filtered out, we set the marker to null
                selectedMarker = null;
                selectedEvent = null;
                makeDefaultEventDescription();
            }
        } else if (selectedMarker != null) {
            Log.e(LOG_TAG, "the selectedMarker isn't null, but it doesn't have an event tag");
        } else {
            selectedEvent = null;
            makeDefaultEventDescription();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() instanceof MainActivity){
            inflater.inflate(R.menu.main_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.settingsMenuItem) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.searchMenuItem) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void centerCamera(LatLng loc) {
        map.animateCamera(CameraUpdateFactory.newLatLng(loc));
    }

    private void addEventMarkers() {
        Map<String, Float> eventColors = DataCache.getInstance().eventColors();
        for (Event event : DataCache.getInstance().filteredEvents()) {
            Float color = eventColors.get(event.getEventType());
            assert color != null;
            Marker marker = map.addMarker(new MarkerOptions().
                    position(new LatLng(event.getLatitude(), event.getLongitude())).
                    icon(BitmapDescriptorFactory.defaultMarker(color)));
            assert marker != null;
            marker.setTag(event);
            eventMarkers.put(event.getEventID(), marker);
        }
    }

    private void drawSpouseLine(Event event) {
        Person p = DataCache.getInstance().getPeople().get(event.getPersonID());
        if (p != null && p.getSpouseID() != null) {
            List<Event> spouseEvents = DataCache.getInstance().getPersonEvents().get(p.getSpouseID());
            if (spouseEvents == null) {
                return;
            }
            for (Iterator<Event> iterator = spouseEvents.iterator(); iterator.hasNext();) {
                Event e = iterator.next();
                if (!Settings.getInstance().filter(e)) {
                    iterator.remove();
                }
            }
            if (!spouseEvents.isEmpty()) {
                Event first = Collections.min(spouseEvents, new EventComparator());
                drawLine(event, first, R.color.purple, 10);
            }
        }
    }

    private void drawFamLines(Event event, int generation) {
        Person p = DataCache.getInstance().getPeople().get(event.getPersonID());
        if (p == null) { return; }
        if (p.getFatherID() != null & Settings.getInstance().isIncludeMaleEvents()) {
            List<Event> candidateEvents = DataCache.getInstance().getPersonEvents().get(p.getFatherID());
            if (candidateEvents != null && !candidateEvents.isEmpty()) {
                Event fatherEvent = Collections.min(candidateEvents,
                        new EventComparator());
                if (fatherEvent != null && Settings.getInstance().filter(fatherEvent)) {
                    drawFamLines(fatherEvent, generation + 1);
                    drawLine(event, fatherEvent, R.color.purple_500,20f / generation);
                }
            }
        }
        if (p.getMotherID() != null & Settings.getInstance().isIncludeFemaleEvents()) {
            List<Event> candidateEvents = DataCache.getInstance().getPersonEvents().get(p.getMotherID());
            if (candidateEvents != null && !candidateEvents.isEmpty()) {
                Event motherEvent = Collections.min(candidateEvents,
                        new EventComparator());
                if (motherEvent != null && Settings.getInstance().filter(motherEvent)) {
                    drawFamLines(motherEvent, generation + 1);
                    drawLine(event, motherEvent, R.color.purple_500, 20f / generation);
                }
            }
        }
    }

    private void drawLifeLines(Event event) {
        Person p = DataCache.getInstance().getPeople().get(event.getPersonID());
        if (p == null) {
            return;
        }
        List<Event> events = DataCache.getInstance().getPersonEvents().get(p.getPersonID());
        if (events != null) {
            Collections.sort(events, new EventComparator());
            Iterator<Event> it = events.iterator();
            if (it.hasNext()){
                Event e1 = it.next();
                Event e2;
                while (it.hasNext()) {
                    e2 = it.next();
                    drawLine(e1, e2, R.color.red, 10);
                    e1 = e2;
                }
            }
        }
    }

    private void drawLine(Event e1, Event e2, int color, float width) {
        if (getContext() == null) {
            Log.e(LOG_TAG, "Since getContext() was null, we couldn't set the line color");
            return;
        }
        PolylineOptions options = new PolylineOptions()
                .add(eventLocation(e1))
                .add(eventLocation(e2))
                .color(getContext().getResources().getColor(color))
                .width(width);
        Polyline line = map.addPolyline(options);
        lines.add(line);
    }

    private void callPersonActivity(String personID) {
        Intent intent = new Intent(getActivity(), PersonActivity.class);
        intent.putExtra(PersonActivity.PERSON_ID_KEY, personID);
        startActivity(intent);
    }

    private LatLng eventLocation(Event e) {
        return new LatLng(e.getLatitude(), e.getLongitude());
    }

    private void clearLines() {
        if (!lines.isEmpty()) {
            for (Polyline line: lines) {
                line.remove();
            }
        }
    }

    private void populateMap() {
        String selectedEventID = (selectedMarker != null) ? ((Event) selectedMarker.getTag()).getEventID() : null;
        map.clear();
        eventMarkers.clear();
        addEventMarkers();
        if (selectedEventID != null && eventMarkers.containsKey(selectedEventID)) {
            selectedMarker = eventMarkers.get(selectedEventID);
        } else {
            selectedMarker = null;
        }
        Settings.getInstance().setSettingsChanged(false);
    }

    private void updateEventDescription() {
        Person assocPerson = DataCache.getInstance().getPeople().get(selectedEvent.getPersonID());
        if (assocPerson == null || getActivity() == null) {
            throw new RuntimeException(LOG_TAG + ": error occurred here");
        }
        TextView eventDescription = getActivity().findViewById(R.id.mapTextView);
        ImageView personImageView = getActivity().findViewById(R.id.mapPersonIcon);
        setEventCaption(assocPerson, eventDescription);
        setGenderedIcon(assocPerson, personImageView);

        // If the user clicks on either the caption or the caption icon, call the person activity
        eventDescription.setOnClickListener(view -> callPersonActivity(assocPerson.getPersonID()));
        personImageView.setOnClickListener(view -> callPersonActivity(assocPerson.getPersonID()));
    }

    private void setGenderedIcon(Person assocPerson, ImageView personImageView) {
        if (assocPerson.getGender().equalsIgnoreCase("f")) {
            Drawable genderIcon = new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_female).colorRes(R.color.female_color).sizeDp(40);
            personImageView.setImageDrawable(genderIcon);
        } else {
            Drawable genderIcon = new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_male).colorRes(R.color.male_color).sizeDp(40);
            personImageView.setImageDrawable(genderIcon);
        }
    }

    private void setEventCaption(Person assocPerson, TextView eventDescription) {
        String description = assocPerson.getFirstName() + " " + assocPerson.getLastName() +
                "\n" + selectedEvent.getEventType() + ": " + selectedEvent.getCity() + ", " +
                selectedEvent.getCountry() + " (" + selectedEvent.getYear() + ")";
        eventDescription.setText(description);
    }

    private void makeDefaultEventDescription() {
        if (getActivity() != null) {
            TextView eventDescription = getActivity().findViewById(R.id.mapTextView);
            eventDescription.setText(R.string.defaultInfoPanelText);
            ImageView personImageView = getActivity().findViewById(R.id.mapPersonIcon);
            personImageView.setImageResource(R.drawable.marker_icon);
        }
    }
}
