package com.owlab.callblocker;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class AddActivity extends AppCompatActivity {
    private boolean isAddFromContactsFabOpen = false;
    private boolean isAddByManualFabOpen = false;

    private boolean isButtonsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Animation miniFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        Animation rotateForwardOnSeen = AnimationUtils.loadAnimation(this, R.anim.rotate_forward_on_seen);
        if(!isButtonsOpen) {

            FloatingActionButton addFromCallHistoryFab = (FloatingActionButton)findViewById(R.id.fab_add_from_call_history);
            addFromCallHistoryFab.startAnimation(rotateForwardOnSeen);

            TextView addFromCallHistoryLabel = (TextView)findViewById(R.id.label_add_from_call_history);
            addFromCallHistoryLabel.startAnimation(miniFabOpen);

            FloatingActionButton addFromContactsFab = (FloatingActionButton) findViewById(R.id.fab_add_from_contacts);
            addFromContactsFab.startAnimation(miniFabOpen);

            TextView addFromContactsLabel = (TextView)findViewById(R.id.label_add_from_contacts);
            addFromContactsLabel.startAnimation(miniFabOpen);

            FloatingActionButton addByManualFab = (FloatingActionButton) findViewById(R.id.fab_add_by_manual);
            addByManualFab.startAnimation(miniFabOpen);

            TextView addByManualLabel = (TextView)findViewById(R.id.label_add_by_manual);
            addByManualLabel.startAnimation(miniFabOpen);

            isButtonsOpen = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Animation miniFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        Animation rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);
        if(isButtonsOpen) {
            FloatingActionButton addFromCallHistoryFab = (FloatingActionButton)findViewById(R.id.fab_add_from_call_history);
            addFromCallHistoryFab.startAnimation(rotateBackward);

            TextView addFromCallHistoryLabel = (TextView)findViewById(R.id.label_add_from_call_history);
            addFromCallHistoryLabel.startAnimation(miniFabClose);

            FloatingActionButton addFromContactsFab = (FloatingActionButton) findViewById(R.id.fab_add_from_contacts);
            addFromContactsFab.startAnimation(miniFabClose);

            TextView addFromContactsLabel = (TextView)findViewById(R.id.label_add_from_contacts);
            addFromContactsLabel.startAnimation(miniFabClose);

            FloatingActionButton addByManualFab = (FloatingActionButton) findViewById(R.id.fab_add_by_manual);
            addByManualFab.startAnimation(miniFabClose);

            TextView addByManualLabel = (TextView)findViewById(R.id.label_add_by_manual);
            addByManualLabel.startAnimation(miniFabClose);

            isButtonsOpen = false;
        }

    }

}
