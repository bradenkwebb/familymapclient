package edu.byu.cs240.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

public class EventActivity extends AppCompatActivity {

    public static final String EVENT_ID_KEY = "ReceivedEventIDKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.eventConstraintLayout);
        fragment = createMapFragment();

        fragmentManager.beginTransaction()
                .add(R.id.eventConstraintLayout, fragment)
                .commit();

        Intent intent = getIntent();
        String receivedID = intent.getStringExtra(EVENT_ID_KEY);
        Toast.makeText(this, receivedID, Toast.LENGTH_SHORT).show();
    }

    // this could be useful https://byu.app.box.com/s/5fvc6p5mbhefb7x1wt21euhs6no6qfll

    private MapFragment createMapFragment() {
        MapFragment fragment = new MapFragment();
//        fragment.registerListener(this);
        return fragment;
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
}