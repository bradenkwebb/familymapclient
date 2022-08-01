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
import android.widget.Toast;

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
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;
    private Map<String, Float> eventColors;

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        setHasOptionsMenu(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);

        // Add a marker in Sydney and move the camera
        LatLng kosice = new LatLng(48.741937, 21.275188);
//        map.addMarker(new MarkerOptions().position(kosice).title("Marker in Košice"));
        map.animateCamera(CameraUpdateFactory.newLatLng(kosice));

        addEventMarkers();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                // Currently, this calls EventActivity, when I think it should instead call PersonActivity

                // Actually, I think clicking a marker should keep us WITHIN the MapFragment, but then
                // we should move to the PersonActivity if we then click on the person

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
                            FontAwesomeIcons.fa_female).colorRes(R.color.female_icon_color).sizeDp(40);
                    personImageView.setImageDrawable(genderIcon);
                } else {
                    Drawable genderIcon = new IconDrawable(getActivity(),
                            FontAwesomeIcons.fa_male).colorRes(R.color.male_icon_color).sizeDp(40);
                    personImageView.setImageDrawable(genderIcon);
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
        });
    }

    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
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

    private void addEventMarkers() {
        eventColors = new HashMap<>();
        Set<String> eventTypes = DataCache.getInstance().eventTypes();

        float skip = 360f / eventTypes.size();
        int counter = 0;
        for (String eventType : eventTypes) {
            eventColors.put(eventType, counter * skip);
            counter++;
        }

        for (Event event : DataCache.getInstance().getEvents().values()) {
            Marker marker = map.addMarker(new MarkerOptions().
                    position(new LatLng(event.getLatitude(), event.getLongitude())).
                    icon(BitmapDescriptorFactory.defaultMarker(eventColors.get(event.getEventType()))));
            assert marker != null;
            marker.setTag(event);
        }
    }

    private void callPersonActivity(String personID) {
        Intent intent = new Intent(getActivity(), PersonActivity.class);
        intent.putExtra(PersonActivity.PERSON_ID_KEY, personID);
        startActivity(intent);
    }
}
