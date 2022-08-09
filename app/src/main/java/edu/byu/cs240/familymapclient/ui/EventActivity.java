package edu.byu.cs240.familymapclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import edu.byu.cs240.familymapclient.R;

public class EventActivity extends AppCompatActivity {

    public static final String EVENT_ID_KEY = "ReceivedEventIDKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        String receivedID = getIntent().getStringExtra(EVENT_ID_KEY);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapFragment();

        Bundle args = new Bundle();
        args.putString(EVENT_ID_KEY, receivedID);
        fragment.setArguments(args);

        fragmentManager.beginTransaction()
                .replace(R.id.eventFragmentFrame, fragment)
                .commit();
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