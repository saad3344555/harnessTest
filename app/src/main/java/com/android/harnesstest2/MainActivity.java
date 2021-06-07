package com.android.harnesstest2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;

import io.harness.cfsdk.CfClient;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.EventsListener;

import static io.harness.cfsdk.cloud.oksse.model.StatusEvent.EVENT_TYPE.EVALUATION_CHANGE;


public class MainActivity extends AppCompatActivity {

    String CF_SERVER = "https://config.feature-flags.uat.harness.io";
    String BASE_URL = CF_SERVER + "/api/1.0";
    String STREAM_URL = BASE_URL + "/stream/environments/";
    ConstraintLayout root;
    TextView textView;


    private final EventsListener eventsListener = statusEvent -> {
        if (statusEvent.getEventType() == EVALUATION_CHANGE) {
            Evaluation evaluation = statusEvent.extractPayload();
            Log.d("ChangeListener", evaluation.getFlag());
            Log.d("ChangeListener", evaluation.value.toString());

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        root = findViewById(R.id.root);

        CfConfiguration sdkConfiguration = CfConfiguration.builder()
                .baseUrl(BASE_URL)
                .pollingInterval(30) //time in seconds
                .enableStream(true)
                .streamUrl(STREAM_URL)
                .build();

        Target target = new Target().identifier("test");

        CfClient.getInstance().initialize(this, "c0b208e8-0906-4342-90d1-eac99713cfe0", sdkConfiguration, target);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                textView.setText(CfClient.getInstance().boolVariation("pop", false) ? "ON" : "OFF");

                handler.postDelayed(this, 200);

            }
        }, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CfClient.getInstance().registerEventsListener(eventsListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        CfClient.getInstance().unregisterEventsListener(eventsListener);
    }
}