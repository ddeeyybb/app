package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class Setting extends Fragment {

    private FirebaseAuth mAuth;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mAuth = FirebaseAuth.getInstance();
        logoutButton = view.findViewById(R.id.logoutButton);

        // Set up Log Out button click listener
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Sign the user out
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to Login Activity after logout
            Intent intent = new Intent(getActivity(), LoginActivity.class); // Redirect to LoginActivity
            startActivity(intent);
            getActivity().finish(); // Close the current activity to prevent going back to it
        });

        return view;
    }
}
