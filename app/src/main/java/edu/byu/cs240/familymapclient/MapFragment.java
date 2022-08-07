package edu.byu.cs240.familymapclient;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import java.util.Objects;
import java.util.Set;

import model.Event;
import model.Person;


public class MapFragment extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private Map<String, Float> eventColors;
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

//        TextView eventDescription = getActivity().findViewById(R.id.mapTextView);
//        eventDescription.setText(R.string.defaultInfoPanelText);
//        ((ImageView) getActivity().findViewById(R.id.mapPersonIcon))
//                .setImageDrawable(getResources().getDrawable(R.drawable.marker_icon));

        lines = new HashSet<>();
        eventMarkers = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }



    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {

        selectedMarker = marker;

        clearLines();

        Event event = ((Event) marker.getTag());
        Person assocPerson = DataCache.getInstance().getPeople().get(event.getPersonID());
        TextView eventDescription = getActivity().findViewById(R.id.mapTextView);
        ImageView personImageView = getActivity().findViewById(R.id.mapPersonIcon);

        String description = assocPerson.getFirstName() + " " + assocPerson.getLastName() +
                "\n" + event.getEventType() + ": " + event.getCity() + ", " +
                event.getCountry() + " (" + event.getYear() + ")";
        eventDescription.setText(description);

        if (assocPerson.getGender().equalsIgnoreCase("f")) {
            Drawable genderIcon = new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_female).colorRes(R.color.female_color).sizeDp(40);
            personImageView.setImageDrawable(genderIcon);
        } else {
            Drawable genderIcon = new IconDrawable(getActivity(),
                    FontAwesomeIcons.fa_male).colorRes(R.color.male_color).sizeDp(40);
            personImageView.setImageDrawable(genderIcon);
        }

        if (Settings.getInstance().isShowSpouseLines()) {
            drawSpouseLine(event);
        }
        if (Settings.getInstance().isShowFamTreeLines()) {
            drawFamLines(event, 1);
        }
        if (Settings.getInstance().isShowLifeStoryLines()) {
            drawLifeLines(event);
        }

        eventDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPersonActivity(assocPerson.getPersonID());
            }
        });

        personImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPersonActivity(assocPerson.getPersonID());
            }
        });

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Settings.getInstance().isSettingsChanged()) {
            repopulate = true;
        } else if (getActivity() instanceof EventActivity) {
            repopulate = true;
        }
        else {
            repopulate = false;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        if (getArguments() != null) {
            String initEventID = getArguments().getString(EventActivity.EVENT_ID_KEY);
            if (initEventID != null) {
                selectedEvent = DataCache.getInstance().getEvents().get(initEventID);
                selectedMarker = eventMarkers.get(initEventID);
            }
        }

        if (selectedMarker != null && Settings.getInstance().filter((Event) selectedMarker.getTag())) {
                onMarkerClick(selectedMarker);
        } else {
            selectedMarker = null;
            TextView eventDescription = getActivity().findViewById(R.id.mapTextView);
            eventDescription.setText(R.string.defaultInfoPanelText);

            ImageView personImageView = getActivity().findViewById(R.id.mapPersonIcon);
            personImageView.setImageResource(R.drawable.marker_icon);
//            Drawable icon = new IconDrawable(getActivity(),
//                    FontAwesomeIcons.fa_female).colorRes(R.color.female_color).sizeDp(40);
//            personImageView.setImageDrawable(icon);
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

    public void centerCamera(LatLng loc) {
        map.animateCamera(CameraUpdateFactory.newLatLng(loc));
    }

    private void addEventMarkers() {
        eventColors = DataCache.getInstance().eventColors();
        Settings settings = Settings.getInstance();
        for (Event event : DataCache.getInstance().filteredEvents()) {
            Marker marker = map.addMarker(new MarkerOptions().
                    position(new LatLng(event.getLatitude(), event.getLongitude())).
                    icon(BitmapDescriptorFactory.defaultMarker(eventColors.get(event.getEventType()))));
            assert marker != null;
            marker.setTag(event);
            eventMarkers.put(event.getEventID(), marker);
        }
    }

    private void drawSpouseLine(Event event) {
        Person p = DataCache.getInstance().getPeople().get(event.getPersonID());
        if (p.getSpouseID() != null) {
            List<Event> spouseEvents = DataCache.getInstance().getPersonEvents().get(p.getSpouseID());
            for (Iterator<Event> iterator = spouseEvents.iterator(); iterator.hasNext();) {
                Event e = iterator.next();
                if (!Settings.getInstance().filter(e)) {
                    iterator.remove();
                }
            }
            if (spouseEvents != null && !spouseEvents.isEmpty()) {
                Event first = Collections.min(spouseEvents, new EventComparator());
                drawLine(event, first, R.color.purple, 10);
            }
        }
    }

    private void drawFamLines(Event event, int generation) {
        Person p = DataCache.getInstance().getPeople().get(event.getPersonID());
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
        List<Event> events = DataCache.getInstance().getPersonEvents().get(p.getPersonID());
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

    private void drawLine(Event e1, Event e2, int color, float width) {
        String hexColor = String.format("#%06X", (0xFFFFFF & color));
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
}
