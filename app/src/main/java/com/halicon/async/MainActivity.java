package com.halicon.async;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Spinner spinner;
    Button window;
    Button[] sounds = new Button[5];
    Boolean[] fxEnabled = new Boolean[5];
    ImageView windowSheet;
    String mode, name, selected, previousItem;
    ImageView rain1, settings;
    TextView transitionView;
    boolean windowSelected;
    int previousItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = findViewById(R.id.settings);
        if(MainVariables.timer != 0){
            timer();
        }
        if(getIntent().getBooleanExtra("init", false)){
            settings.setAlpha(0.0f);
            settings.animate().alpha(1.0f).setDuration(2000);
        }
        selected = " ";
        transitionView = findViewById(R.id.transition2);
        transitionView.setVisibility(View.VISIBLE);
        transitionView.setAlpha(1);
        rain1 = findViewById(R.id.rain1);
        spinner = findViewById(R.id.spinner);
        window = findViewById(R.id.window);
        windowSheet = findViewById(R.id.windowSheet);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.setClickable(false);
                transition(Settings.class);
            }
        });
        sounds[0] = findViewById(R.id.sound1);
        sounds[1] = findViewById(R.id.sound2);
        sounds[2] = findViewById(R.id.sound3);
        sounds[3] = findViewById(R.id.sound4);
        sounds[4] = findViewById(R.id.sound5);
        initMenu();
        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWindow();
            }
        });
        String[] array_spinner = new String[2];
        array_spinner[0] = "Heavy";
        array_spinner[1] = "Soft";
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.list_item, R.id.itemText, array_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                previousItemIndex = i;
                previousItem = selected;
                selected = spinner.getSelectedItem().toString();
                SharedPreferences.Editor editor = getSharedPreferences("settings",0).edit();
                editor.putInt("spinnerSelection", i);
                editor.apply();
                animateRain();
                startAudio();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        SharedPreferences sp = getSharedPreferences("settings",0);
        int selection = sp.getInt("spinnerSelection", 1);
        spinner.setSelection(selection);
        transitionView.animate().alpha(0.0f).setDuration(1500);
    }

    void startAudio() {
        Intent intent = new Intent(this, soundService.class);
        intent.putExtra("path", getPath());
        startService(intent);
    }

    void startSFX(Button button, String sound) {
        Intent fxIntent = new Intent(this, thunderService.class);
        if (fxEnabled[Arrays.asList(sounds).indexOf(button)]) {
            button.setForeground(getResources().getDrawable(getResources()
                    .getIdentifier(sound, "drawable", getPackageName())));
            fxIntent.putExtra("enabled", false);
            fxIntent.putExtra("sound", sound);
            fxEnabled[Arrays.asList(sounds).indexOf(button)] = false;
        } else {
            button.setForeground(getResources().getDrawable(getResources()
                    .getIdentifier(sound + "_pressed", "drawable", getPackageName())));
            fxIntent.putExtra("enabled", true);
            fxIntent.putExtra("sound", sound);
            fxEnabled[Arrays.asList(sounds).indexOf(button)] = true;
        }
        startService(fxIntent);
    }

    String getPath() {
        name = selected.toLowerCase();
        if (windowSelected) {
            mode = "window";
        } else {
            mode = "base";
        }
        String path;
        if (!Objects.equals(previousItem, name + mode)) {
            path = "android.resource://com.halicon.async/raw/" + name + "_" + mode;
        } else {
            path = "android.resource://com.halicon.async/raw/silence";
        }
        return path;
    }

    void animateRain() {
        if (selected.toLowerCase().equals("heavy")) {
            Glide.with(MainActivity.this)
                    .load(R.drawable.heavy_rain)
                    .transition(DrawableTransitionOptions.withCrossFade(4000))
                    .apply(new RequestOptions().override(1080, 1920)
                            .error(R.drawable.icon).centerCrop()
                    )
                    .into(rain1);
        } else {
            Glide.with(MainActivity.this)
                    .load(R.drawable.light_rain)
                    .transition(DrawableTransitionOptions.withCrossFade(4000))
                    .apply(new RequestOptions().override(1080, 1920)
                            .error(R.drawable.icon).centerCrop()
                    )
                    .into(rain1);
        }
    }

    void enableWindow() {
        if (!windowSelected) {
            window.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.window_pressed, null));
            windowSelected = true;
            startAudio();
            Glide.with(MainActivity.this)
                    .load(R.drawable.window)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .apply(new RequestOptions().override(1080, 1920)
                            .error(R.drawable.icon).centerCrop()
                    )
                    .into(windowSheet);
        } else {
            window.setForeground(ResourcesCompat.getDrawable(getResources(), R.drawable.windowbutton, null));
            windowSelected = false;
            startAudio();
            Glide.with(MainActivity.this)
                    .load(R.drawable.empty)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .apply(new RequestOptions().override(1080, 1920)
                            .error(R.drawable.icon).centerCrop()
                    )
                    .into(windowSheet);
        }
    }

    public void timer() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(MainVariables.timer * 60000L);
                    Intent intent = new Intent(MainActivity.this, soundService.class);
                    intent.putExtra("path", "android.resource://com.halicon.async/raw/silence");
                    startService(intent);
                    sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                finishAffinity();
                System.exit(0);
            }
        };
        thread.start();
    }
    void initMenu(){
        if(!Objects.equals(MainVariables.enabled, "")){
            String[] names;
            names=MainVariables.enabled.split(" ");
            for(String name : names){
                int i = Arrays.asList(names).indexOf(name);
                sounds[i].setVisibility(View.VISIBLE);
                sounds[i].setForeground(getResources().getDrawable(getResources()
                        .getIdentifier(name, "drawable", getPackageName())));
                fxEnabled[i] = true;
                sounds[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startSFX((Button) view, name);
                    }
                });
            }
        }
    }
    void transition(Class destination){
        transitionView.animate().alpha(1.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                settings.setClickable(true);
                Intent intent = new Intent(MainActivity.this, destination);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }
}