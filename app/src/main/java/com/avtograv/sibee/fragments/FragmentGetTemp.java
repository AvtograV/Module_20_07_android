package com.avtograv.sibee.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.avtograv.sibee.R;

public class FragmentGetTemp extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_fragment_temp,
                container,
                false);

        TextView textView = view.findViewById(R.id.textTemp);

        if (getArguments() != null) {
            String message = getArguments().getString(ARGS_MESSAGE);
            textView.setText(message);
        }

        return view;
    }


    private static final String ARGS_MESSAGE = "args:message";

    public static FragmentGetTemp newInstance(String message) {
        FragmentGetTemp fragmentGetTemp = new FragmentGetTemp();
        Bundle bundle = new Bundle();

        bundle.putString(ARGS_MESSAGE, message);
        fragmentGetTemp.setArguments(bundle);

        return fragmentGetTemp;
    }
}