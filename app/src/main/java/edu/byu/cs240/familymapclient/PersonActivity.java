package edu.byu.cs240.familymapclient;

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
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        List<Person> immediateFamily = DataCache.getInstance().getImmediateFamily(personID);

        // Sort personEvents according to project specs
        Collections.sort(personEvents, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                if (e1.getEventType().equalsIgnoreCase("birth")) {
                    return -1;
                } else if (e2.getEventType().equalsIgnoreCase("birth")) {
                    return 1;
                } else if (e1.getEventType().equalsIgnoreCase("death")) {
                    return 1;
                } else if (e2.getEventType().equalsIgnoreCase("death")) {
                    return -1;
                } else if (e1.getYear() != e2.getYear()) {
                    return e1.getYear().compareTo(e2.getYear());
                }
                return e1.getEventType().toLowerCase().compareTo(e2.getEventType().toLowerCase());
            }
        });

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
                    intializeEventView(itemView, childPosition);
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
        private void intializeEventView(View eventItemView, final int childPosition) {
            TextView eventDescrView = eventItemView.findViewById(R.id.eventDescr);
            Event event = events.get(childPosition);
            String descr = event.getEventType().toUpperCase() + ": " + event.getCity() + ", " +
                            event.getCountry() + " (" + event.getYear() + ")";
            eventDescrView.setText(descr);

            TextView assocPersonName = eventItemView.findViewById(R.id.assocPersonName);
            assocPersonName.setText(person.getFirstName() + " " + person.getLastName());

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callEventActivity(event.getEventID());
                }
            });
        }

        @SuppressLint("SetTextI18n")
        private void initializePersonView(View personItemView, final int childPosition) {
            Person p = people.get(childPosition);
            ImageView icon = personItemView.findViewById(R.id.personIcon);

            if (p.getGender().equalsIgnoreCase("f")) {
                Drawable genderIcon = new IconDrawable(PersonActivity.this,
                        FontAwesomeIcons.fa_female).colorRes(R.color.female_icon_color).sizeDp(40);
                icon.setImageDrawable(genderIcon);
            } else {
                Drawable genderIcon = new IconDrawable(PersonActivity.this,
                        FontAwesomeIcons.fa_male).colorRes(R.color.male_icon_color).sizeDp(40);
                icon.setImageDrawable(genderIcon);
            }

            TextView nameView = personItemView.findViewById(R.id.personName);
            nameView.setText(p.getFirstName() + " " + p.getLastName());

            TextView relToPerson = personItemView.findViewById(R.id.relationship);
            String rel = getString(R.string.child);
            if (person.getMotherID() != null && person.getMotherID().equals(p.getPersonID())) {
                rel = getString(R.string.mother);
            } else if (person.getFatherID() != null && person.getFatherID().equals(p.getPersonID())) {
                rel = getString(R.string.father);
            } else if (person.getSpouseID() != null && person.getSpouseID().equals(p.getPersonID())) {
                rel = getString(R.string.spouse);
            }
            relToPerson.setText(rel);

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callPersonActivity(p.getPersonID());
            };});
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
}