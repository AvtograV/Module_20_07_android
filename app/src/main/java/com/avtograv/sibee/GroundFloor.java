package com.avtograv.sibee;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.avtograv.sibee.fragments.FragmentButton;
import com.avtograv.sibee.fragments.FragmentGetTemp;

public class GroundFloor extends FragmentActivity
        implements MessageFragmentListener {

    public static String t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_floor);

        FragmentGetTemp fragmentGetTemp = FragmentGetTemp.newInstance(t);
        // подключаем FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.TODO, fragmentGetTemp)
                .commit();


        // does not apply to fragments
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            t = arguments.get("temp_go").toString();
        }
    }

    @Override
    public void onNextMessageClicked() {

        FragmentGetTemp fragmentGetTemp = FragmentGetTemp.newInstance("T");

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.TODO, fragmentGetTemp).
                commit();
    }
}