package edu.byu.cs240.familymapclient.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.byu.cs240.familymapclient.DataCache;
import edu.byu.cs240.familymapclient.EventComparator;
import edu.byu.cs240.familymapclient.R;
import edu.byu.cs240.familymapclient.Settings;
import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    public static final String PERSON_ID_KEY = "ReceivedPersonIDKey";
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Intent intent = getIntent();
        String personID = intent.getStringExtra(PERSON_ID_KEY);
        person = DataCache.getInstance().getPeople().get(personID);

        TextView textView = findViewById(R.id.personFirstName);
        textView.setText(person.getFirstName());
        textView = findViewById(R.id.personLastName);
        textView.setText(person.getLastName());
        textView = findViewById(R.id.personGender);

        if (person.getGender().equalsIgnoreCase("f")) {
            textView.setText(R.string.female);
        } else {
            textView.setText(R.string.male);
        }

        ExpandableListView expandableListView = findViewById(R.id.personExpandableListView);

        List<Event> personEvents = DataCache.getInstance().getPersonEvents().get(personID);
        if (personEvents != null) {
            for (Iterator<Event> iterator = personEvents.iterator(); iterator.hasNext();) {
                Event e = iterator.next();
                if (!Settings.getInstance().filter(e)) {
                    iterator.remove();
                }
            }

            // Sort personEvents according to project specs
            Collections.sort(personEvents, new EventComparator());
        }

        List<Person> immediateFamily = DataCache.getInstance().getImmediateFamily(personID);

        expandableListView.setAdapter(new ExpandableListAdapter(personEvents, immediateFamily));
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

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_GROUP_POSITION = 0;
        private static final int PEOPLE_GROUP_POSITION = 1;

        private final List<Event> events;
        private final List<Person> people;

        ExpandableListAdapter(List<Event> events, List<Person> people) {
            this.events = events;
            this.people = people;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.size();
                case PEOPLE_GROUP_POSITION:
                    return people.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            // Not used
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // Not used
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.eventsTitle);
                    break;
                case PEOPLE_GROUP_POSITION:
                    titleView.setText(R.string.peopleTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case PEOPLE_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        @SuppressLint("SetTextI18n")
        private void initializeEventView(View eventItemView, final int childPosition) {
            TextView eventDescrView = eventItemView.findViewById(R.id.eventDescr);
            Event event = events.get(childPosition);
            String descr = eventDescr(event);
            eventDescrView.setText(descr);

            TextView assocPersonName = eventItemView.findViewById(R.id.assocPersonName);
            assocPersonName.setText(person.getFirstName() + " " + person.getLastName());

            eventItemView.setOnClickListener(v -> callEventActivity(event.getEventID()));
        }

        @SuppressLint("SetTextI18n")
        private void initializePersonView(View personItemView, final int childPosition) {
            Person p = people.get(childPosition);
            ImageView icon = personItemView.findViewById(R.id.personIcon);
            Drawable genderIcon = getGenderedIcon(p);
            icon.setImageDrawable(genderIcon);

            TextView nameView = personItemView.findViewById(R.id.personName);
            nameView.setText(p.getFirstName() + " " + p.getLastName());

            TextView relToPerson = personItemView.findViewById(R.id.relationship);
            relToPerson.setText(getRelationship(person, p));

            personItemView.setOnClickListener(v -> callPersonActivity(p.getPersonID()));
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
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

    public static String eventDescr(Event event) {
        return event.getEventType().toUpperCase() + ": " + event.getCity() + ", " +
                event.getCountry() + " (" + event.getYear() + ")";
    }

    private String getRelationship(Person p1, Person p2) {
        if (p1.getMotherID() != null && p1.getMotherID().equals(p2.getPersonID())) {
            return getString(R.string.mother);
        } else if (p1.getFatherID() != null && p1.getFatherID().equals(p2.getPersonID())) {
            return getString(R.string.father);
        } else if (p1.getSpouseID() != null && p1.getSpouseID().equals(p2.getPersonID())) {
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
            genderIcon = new IconDrawable(PersonActivity.this,
                    FontAwesomeIcons.fa_female).colorRes(R.color.female_color).sizeDp(40);
        } else {
            genderIcon = new IconDrawable(PersonActivity.this,
                    FontAwesomeIcons.fa_male).colorRes(R.color.male_color).sizeDp(40);
        }
        return genderIcon;
    }
}