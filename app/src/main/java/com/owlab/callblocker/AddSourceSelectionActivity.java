package com.owlab.callblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class AddSourceSelectionActivity extends AppCompatActivity {
    private static final String TAG = AddSourceSelectionActivity.class.getSimpleName();

    private String transitionSource;
    private String transitionTarget;

    FloatingActionButton addFromCallLogFab;
    TextView addFromCallLogLabel;
    FloatingActionButton addFromContactsFab;
    TextView addFromContactsLabel;
    FloatingActionButton addByManualFab;
    TextView addByManualLabel;
    Animation rotateForwardAppear;
    Animation rotateBackwardAppear;
    Animation miniFabOpen;
    Animation rotateForwardDisappear;
    Animation rotateBackwardDisappear;
    Animation miniFabClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ">>>>> onCreate called");

        setContentView(R.layout.activity_add_source_selection);

        Intent createIntent = getIntent();
        transitionSource = createIntent.getStringExtra(CONS.INTENT_KEY_TRANSITION_SOURCE);

        //buttons
        addFromCallLogFab = (FloatingActionButton) findViewById(R.id.fab_add_from_call_log);
        addFromCallLogLabel = (TextView) findViewById(R.id.label_add_from_call_log);
        addFromContactsFab = (FloatingActionButton) findViewById(R.id.fab_add_from_contacts);
        addFromContactsLabel = (TextView) findViewById(R.id.label_add_from_contacts);
        addByManualFab = (FloatingActionButton) findViewById(R.id.fab_add_by_manual);
        addByManualLabel = (TextView) findViewById(R.id.label_add_by_manual);

        //animations
        rotateForwardAppear = AnimationUtils.loadAnimation(this, R.anim.rotate_forward_appear);
        rotateBackwardAppear = AnimationUtils.loadAnimation(this, R.anim.rotate_backward_appear);
        rotateForwardDisappear = AnimationUtils.loadAnimation(this, R.anim.rotate_forward_disappear);
        rotateBackwardDisappear = AnimationUtils.loadAnimation(this, R.anim.rotate_backward_disappear);
        miniFabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        miniFabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        addFromCallLogFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transitionTarget = CONS.FRAGMENT_CALL_LOG;
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra(CONS.INTENT_KEY_TARGET_FRAGMENT, transitionTarget);
                startActivity(intent);
                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(AddSourceSelectionActivity.this).toBundle());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, ">>>>> onResume called");
        Log.d(TAG, ">>>>> transitionSource ? " + transitionSource);

        //initialize for onPause
        transitionTarget = null;

        if (transitionSource != null && transitionSource.equals(CONS.FRAGMENT_PHONE_LIST)) {
            addFromCallLogFab.startAnimation(rotateForwardAppear);
            //addFromCallLogFab.startAnimation(rotateForwardDisappear);
        } else {
            //addFromCallLogFab.startAnimation(rotateBackwardAppear);
            addFromCallLogFab.startAnimation(rotateBackwardAppear);
        }

        addFromCallLogLabel.startAnimation(miniFabOpen);
        addFromContactsFab.startAnimation(miniFabOpen);
        addFromContactsLabel.startAnimation(miniFabOpen);
        addByManualFab.startAnimation(miniFabOpen);
        addByManualLabel.startAnimation(miniFabOpen);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, ">>>>> onPause called");
        Log.d(TAG, ">>>>> transitionTarget ? " + transitionTarget);

        //initialize for onResume
        transitionSource = null;

        if (transitionTarget != null && transitionTarget.equals(CONS.FRAGMENT_CALL_LOG)) {
            addFromCallLogFab.startAnimation(rotateForwardDisappear);
        } else {
            addFromCallLogFab.startAnimation(rotateBackwardDisappear);
        }

        addByManualLabel.startAnimation(miniFabClose);
        addByManualFab.startAnimation(miniFabClose);
        addFromContactsLabel.startAnimation(miniFabClose);
        addFromContactsFab.startAnimation(miniFabClose);
        addFromCallLogLabel.startAnimation(miniFabClose);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, ">>>>> back pressed");
        super.onBackPressed();

    }
}
