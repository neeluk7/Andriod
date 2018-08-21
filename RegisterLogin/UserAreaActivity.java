package com.example.admin.recipebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class UserAreaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        EditText etUsername = (EditText) findViewById(R.id.etUsername);

        // Display user details
        String message = username + " welcome to your user area";
        etUsername.setText(username);
    }
}

