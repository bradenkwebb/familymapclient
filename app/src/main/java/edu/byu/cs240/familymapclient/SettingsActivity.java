package edu.byu.cs240.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch lifeStoryLines = findViewById(R.id.lifeStoryLinesSetting);
        Switch famTreeLines = findViewById(R.id.familyTreeLinesSetting);
        Switch spouseLines = findViewById(R.id.spouseLinesSetting);
        Switch paternalAncestors = findViewById(R.id.fatherSideSetting);
        Switch maternalAncestors = findViewById(R.id.motherSideSetting);
        Switch maleEvents = findViewById(R.id.maleEventSetting);
        Switch femaleEvents = findViewById(R.id.femaleEventSetting);

        Settings currSettings = Settings.getInstance();

        lifeStoryLines.setChecked(currSettings.isShowLifeStoryLines());
        famTreeLines.setChecked(currSettings.isShowFamTreeLines());
        spouseLines.setChecked(currSettings.isShowSpouseLines());
        paternalAncestors.setChecked(currSettings.isIncludePaternalAncestors());
        maternalAncestors.setChecked(currSettings.isIncludeMaternalAncestors());
        maleEvents.setChecked(currSettings.isIncludeMaleEvents());
        femaleEvents.setChecked(currSettings.isIncludeFemaleEvents());

        // Right now, if a user toggles a setting and then toggles it back before leaving the
        // screen, the app will still consider the settings to have "changed"—which I think is fine
        lifeStoryLines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleShowLifeStoryLines();
                currSettings.setSettingsChanged(true);
            }
        });
        famTreeLines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleShowFamTreeLines();
                currSettings.setSettingsChanged(true);
            }
        });
        spouseLines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleShowSpouseLines();
                currSettings.setSettingsChanged(true);
            }
        });
        paternalAncestors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleIncludePaternalAncestors();
                currSettings.setSettingsChanged(true);
            }
        });
        maternalAncestors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleIncludeMaternalAncestors();
                currSettings.setSettingsChanged(true);
            }
        });
        maleEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleIncludeMaleEvents();
                currSettings.setSettingsChanged(true);
            }
        });
        femaleEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.toggleIncludeFemaleEvents();
                currSettings.setSettingsChanged(true);
            }
        });


        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSettings.setSettingsChanged(true);
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
}