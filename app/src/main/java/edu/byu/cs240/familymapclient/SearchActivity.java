package edu.byu.cs240.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.Event;
import model.Person;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private static final int EVENT_VIEW_TYPE = 0;
    private static final int PERSON_VIEW_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView search = findViewById(R.id.search_bar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Event> events = new ArrayList<>(DataCache.getInstance().getEvents().values());
        List<Person> people = new ArrayList<>(DataCache.getInstance().getPeople().values());

        SearchAdapter adapter = new SearchAdapter(events, people);
        recyclerView.setAdapter(adapter);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                List<Event> eventResults = new ArrayList<>();
                List<Person> personResults = new ArrayList<>();
                Pattern pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
                for (Person person : people) {
                    if (pattern.matcher(person.getFirstName() + " " + person.getLastName()).find()){
                        personResults.add(person);
                    }
                }
                for (Event event : filteredEvents()) {
                    if (pattern.matcher(event.getEventType()).find()) {
                        eventResults.add(event);
                    } else if (pattern.matcher(event.getCity()).find()) {
                        eventResults.add(event);
                    } else if (pattern.matcher(event.getCountry()).find()) {
                        eventResults.add(event);
                    } else if (pattern.matcher(event.getYear().toString()).find()) {
                        eventResults.add(event);
                    }
                }
                SearchAdapter adapter = new SearchAdapter(eventResults, personResults);
                recyclerView.setAdapter(adapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

        private final List<Event> events;
        private final List<Person> people;

        SearchAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == EVENT_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            }
            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if (position < events.size()) {
                holder.bind(events.get(position));
            } else {
                holder.bind(people.get(position - events.size()));
            }
        }

        @Override
        public int getItemCount() {
            return events.size() + people.size();
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView genderedIcon;
        private final TextView relationship;
        private final TextView personName;
        private final TextView descr;

        private final int viewType;
        private Event event;
        private Person person;

        SearchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            if (viewType == EVENT_VIEW_TYPE) {
                personName = itemView.findViewById(R.id.assocPersonName);
                descr = itemView.findViewById(R.id.eventDescr);
                genderedIcon = null;
                relationship = null;
            } else if (viewType == PERSON_VIEW_TYPE) {
                genderedIcon = itemView.findViewById(R.id.personIcon);
                relationship = itemView.findViewById(R.id.relationship);
                personName = itemView.findViewById(R.id.personName);
                descr = null;
            } else {
                throw new IllegalArgumentException();
            }
        }

        @SuppressLint("SetTextI18n")
        private void bind(Event event) {
            this.event = event;
            Person assocPerson = DataCache.getInstance().getPeople().get(event.getPersonID());
            personName.setText(assocPerson.getFirstName() + " " + assocPerson.getLastName());
            descr.setText(PersonActivity.eventDescr(event));
        }

        private void bind (Person person) {
            this.person = person;
            personName.setText(person.getFirstName() + " " + person.getLastName());
            relationship.setText(getRelationship(this.person, person));
            genderedIcon.setImageDrawable(getGenderedIcon(person));
        }

        @Override
        public void onClick(View view) {
            if (viewType == EVENT_VIEW_TYPE) {
                callEventActivity(event.getEventID());
            } else if (viewType == PERSON_VIEW_TYPE) {
                callPersonActivity(person.getPersonID());
            } else {
                throw new RuntimeException("Invalid class viewType data member");
            }
        }
    }

    // All of these help methods are COPIED FROM PersonActivity
    private String getRelationship(Person p1, Person p2) {
        if (p1.getMotherID() != null && p1.getMotherID().equals(p2.getPersonID())) {
            return getString(R.string.mother);
        } else if (p1.getFatherID() != null && p1.getFatherID().equals(p2.getPersonID())) {
            return getString(R.string.father);
        } else if (p1.getSpouseID() != null & p1.getSpouseID().equals(p2.getPersonID())) {
            return getString(R.string.spouse);
        } else if (p2.getFatherID() != null && p2.getFatherID().equals(p1.getPersonID()) ||
                p2.getMotherID() != null && p2.getMotherID().equals(p1.getPersonID())) {
            return getString(R.string.child);
        }
        return "Not immediately related";
    }

    private Drawable getGenderedIcon(Person p) {
        Drawable genderIcon;
        if (p.getGender().equalsIgnoreCase("f")) {
            genderIcon = new IconDrawable(this,
                    FontAwesomeIcons.fa_female).colorRes(R.color.female_color).sizeDp(40);
        } else {
            genderIcon = new IconDrawable(this,
                    FontAwesomeIcons.fa_male).colorRes(R.color.male_color).sizeDp(40);
        }
        return genderIcon;
    }

    private void callPersonActivity(String personID) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra(PersonActivity.PERSON_ID_KEY, personID);
        startActivity(intent);
    }

    private void callEventActivity(String eventID) {
        Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra(EventActivity.EVENT_ID_KEY, eventID);
        startActivity(intent);
    }

    private List<Event> filteredEvents() {
        List<Event> filtered = new ArrayList<>();
        Collection<Event> unfiltered = DataCache.getInstance().getEvents().values();
        for (Event event: unfiltered) {
            if (Settings.getInstance().filter(event)){
                filtered.add(event);
            }
        }
        return filtered;
    }
}