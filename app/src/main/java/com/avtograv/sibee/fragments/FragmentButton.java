package com.avtograv.sibee.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.avtograv.sibee.MessageFragmentListener;
import com.avtograv.sibee.R;

import org.jetbrains.annotations.NotNull;

public class FragmentButton extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_fragment_button,
                container,
                false);

        Button button = view.findViewById(R.id.fragment_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onNextMessageClicked();
            }
        });

        return view;
    }


    private MessageFragmentListener listener;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof MessageFragmentListener) {                       // if (getActivity() instanceof MessageFragmentListener) {
            listener = (MessageFragmentListener) context;                       // listener = (MessageFragmentListener) getActivity();
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }
}