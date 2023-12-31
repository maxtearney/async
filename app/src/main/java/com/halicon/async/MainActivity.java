package com.halicon.async;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
    String[] names;
    ImageView windowSheet;
    String mode, name, selected, previousItem;
    ImageView rain1, settings;
    TextView transitionView;
    boolean windowSelected, premium;
    int previousItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainVariables.servicesRunning == null){
            MainVariables.servicesRunning = false;
        }
        setContentView(R.layout.activity_main);
        settings = findViewById(R.id.settings);
        if(MainVariables.timer != 0){
            timer();
        }
        if(getIntent().getBooleanExtra("init", false)){
            settings.setAlpha(0.0f);
            settings.animate().alpha(1.0f).setDuration(500);
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
        SharedPreferences sp = getSharedPreferences("settings",0);
        premium = sp.getBoolean("premium", false);
        String[] array_spinner;
        if(premium){
            array_spinner = new String[3];
            array_spinner[2] = "Off";
        }else{
            array_spinner = new String[2];
        }
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
                MainVariables.path = getPath();
                if(!MainVariables.servicesRunning){
                    Intent sndIntent = new Intent(MainActivity.this, soundService.class);
                    Intent sfxIntent = new Intent(MainActivity.this, sfxService.class);
                    Intent thndIntent = new Intent(MainActivity.this, thunderService.class);
                    startService(sndIntent);
                    startService(sfxIntent);
                    startService(thndIntent);
                    MainVariables.servicesRunning = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        int selection = sp.getInt("spinnerSelection", 1);
        spinner.setSelection(selection);
        transitionView.animate().alpha(0.0f).setDuration(500);
    }

    void startSFX(Button button, String sound) {
        Drawable pressed = getResources().getDrawable(getResources().getIdentifier(sound + "_pressed", "drawable", getPackageName()));
        Drawable notPressed = getResources().getDrawable(getResources().getIdentifier(sound, "drawable", getPackageName()));
        if(!sound.contains("prem")){
            //relates to randomly played sounds
            for (String name : names) {
                //disables other randomly played sounds
                if (!name.contains("prem") && MainVariables.thundBooleans.get(name) && !name.equals(sound)) {
                    Log.d("yeah2", name);
                    int i = Arrays.asList(names).indexOf(name);
                    sounds[i].setForeground(getResources().getDrawable(getResources()
                            .getIdentifier(name, "drawable", getPackageName())));
                    MainVariables.thundBooleans.put(name, false);
                }
            }
            if (MainVariables.thundBooleans.get(sound)) {
                //if the sound is active, turn it off
                button.setForeground(notPressed);
                MainVariables.thundBooleans.put(sound, false);
            } else {
                //if the sound is inactive, turn it on
                button.setForeground(pressed);
                MainVariables.thundBooleans.put(sound, true);
            }
        }else{
            //relates to premium sounds
            for (String name : names) {
                //disables other premium sounds
                if (name.contains("prem") && MainVariables.sfxBooleans.get(name) && !name.equals(sound)) {
                    Log.d("yeah2", name);
                    int i = Arrays.asList(names).indexOf(name);
                    sounds[i].setForeground(getResources().getDrawable(getResources().getIdentifier(name, "drawable", getPackageName())));
                    MainVariables.sfxBooleans.put(name, false);
                }
            }
            if (MainVariables.sfxBooleans.get(sound)) {
                //if the sound is active, turn it off
                button.setForeground(notPressed);
                MainVariables.sfxBooleans.put(sound, false);
                Log.d("yeah", sound + " is false");
            } else {
                //if the sound is inactive, turn it on
                button.setForeground(pressed);
                MainVariables.sfxBooleans.put(sound, true);
                Log.d("yeah", sound + " is true");
            }
        }
    }

    String getPath() {
        name = selected.toLowerCase();
        if (windowSelected) {
            mode = "window";
            MainVariables.window = true;
        } else {
            mode = "base";
            MainVariables.window = false;
        }
        String path = "android.resource://com.halicon.async/raw/" + name + "_" + mode;
        if(Objects.equals(selected, "Off")){
            path = "android.resource://com.halicon.async/raw/silence";
            Glide.with(MainActivity.this)
                    .load(R.drawable.empty)
                    .transition(DrawableTransitionOptions.withCrossFade(4000))
                    .apply(new RequestOptions().override(1080, 1920)
                            .error(R.drawable.icon).centerCrop()
                    )
                    .into(rain1);
        }
        return path;
    }

    void animateRain() {
        if (selected.equalsIgnoreCase("heavy")) {
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
            MainVariables.path = getPath();
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
            MainVariables.path = getPath();
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
            names=MainVariables.enabled.split(" ");
            for(String name : names){
                int i = Arrays.asList(names).indexOf(name);
                sounds[i].setVisibility(View.VISIBLE);
                sounds[i].setForeground(getResources().getDrawable(getResources()
                        .getIdentifier(name, "drawable", getPackageName())));
                sounds[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startSFX((Button) view, name);
                    }
                });
                if(name.contains("prem")){
                    MainVariables.sfxBooleans.put(name, false);
                }else{
                    MainVariables.thundBooleans.put(name, false);
                }
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
                MainVariables.window = false;
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