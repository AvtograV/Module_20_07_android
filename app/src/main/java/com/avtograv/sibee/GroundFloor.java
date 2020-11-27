package com.avtograv.sibee;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.avtograv.sibee.fragments.FragmentGetTemp;

public class GroundFloor extends FragmentActivity {
    public static String t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_floor);

        FragmentGetTemp fragmentGetTemp = new FragmentGetTemp();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.TODO, fragmentGetTemp)
                .commit();

        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            t = arguments.get("temp_go").toString();
        }
    }
}