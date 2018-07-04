package com.flocktory.pushdemo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.flocktory.pushdemo.databinding.ActivityMainBinding;
import com.google.firebase.iid.FirebaseInstanceId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // todo uuid
    // todo dump flocktory web session button

    private FlocktoryApiClient flApi;

    private String vaultBoyImageUrl = "https://assets.flocktory.com/uploads/clients/1845/e2ed6a3e-b645-4f19-a5e8-a0c86672c8c0_valutboy.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // грязный хак для запросов из UI треда
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // привязать textview к состоянию логгера
        lg = new MyLogger();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setVariable(BR.mylogger, lg);


        // ищем нужные элементы
        subscribeButton = (Button) findViewById(R.id.subscribeButton);
        sendPushButton = (Button) findViewById(R.id.sendPushButton);
        dumpFirebaseTokenButton = (Button) findViewById(R.id.dumpToken);
        dumpFlocktoryProfileButton = (Button) findViewById(R.id.dumpFlProfile);
        dumpFlocktoryWebSessionButton = (Button) findViewById(R.id.dumpFlWebSession);
        dumpFlocktorySiteSessionButton = (Button) findViewById(R.id.dumpFlSiteSession);
        logTextView = (TextView) findViewById(R.id.log);
        logTextView.setMovementMethod(new ScrollingMovementMethod());

        flApi = new FlocktoryApiClient(getApplicationContext());

        // onclicklisteners
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String t = FirebaseInstanceId.getInstance().getToken();
                if (t != null ) {
                    flApi.regToken(t);
                } else {
                    log("TOKEN IS NOT CREATED YET");
                }
            }
        });
        sendPushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String profileString = flApi.getPushProfile();
                if (profileString != null) {
                    try {
                        flApi.sendTestPush(
                                profileString,
                                "http://ya.ru",
                                vaultBoyImageUrl,
                                "test subject",
                                "test body");
                        log("push sent!!!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("Exception while sending a test push: " + e.getMessage());
                    }
                } else {
                    log("EMPTY FL PROFILE STRING");
                }
            }
        });
        dumpFirebaseTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = FirebaseInstanceId.getInstance().getToken();
                log("current token: " + t);
            }
        });
        dumpFlocktoryProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String flProfile = flApi.getPushProfile();
                if (flProfile != null) {
                    log("FL PUSH PROFILE: " + flProfile);
                } else {
                    log("FL PUSH PROFILE NOT FOUND");
                }
            }
        });
        dumpFlocktoryWebSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = flApi.getWebSessionId();
                if (s != null) {
                    log("FL WEB SESSION: " + s);
                } else {
                    log("FL WEB SESSION NOT FOUND");
                }
            }
        });
        dumpFlocktorySiteSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = flApi.getSavedSiteSessionId();
                if (s != null) {
                    log("FL SITE SESSION: " + s);
                } else {
                    log("FL SITE SESSION NOT FOUND");
                }
            }
        });

    }

    // ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                clearLog();
                break;
            case R.id.copy_log:
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", lg.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), (CharSequence) "log copied to clipboard", Toast.LENGTH_SHORT).show();
                    break;
                } catch (Exception e) {
                    log("Exception while doing setPrimaryClip" + e.getMessage());
                }
        }
        return true;
    }

    ////////// definitions

    Button subscribeButton;
    Button sendPushButton;
    Button dumpFirebaseTokenButton;
    Button dumpFlocktoryProfileButton;
    Button dumpFlocktoryWebSessionButton;
    Button dumpFlocktorySiteSessionButton;
    TextView logTextView;

    private static MyLogger lg;
    ActivityMainBinding binding;

    private static String timeFormat = "HH:mm:ss";
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat, Locale.US);
    public static String LOGTAG = "flock";

    private static String timeTag () {
        return
                "["
                + timeFormatter.format(new Date())
                +"] ";
    }

    public static void log(String message) {
        Log.d(LOGTAG, message);
        String humanMessage = timeTag() + message + "\n";
        lg.log(humanMessage);
    }

    public void clearLog(){
        lg.reset();
    }



}