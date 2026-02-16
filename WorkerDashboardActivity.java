package com.example.booma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class WorkerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workerboard);

        ImageView imgProfile = findViewById(R.id.imgProfile);
        ImageView imgSearch = findViewById(R.id.imgSearch);

// Open Profile when clicking profile image
        imgProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );


        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navBookings = findViewById(R.id.navBookings);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        // Load profile image from SharedPreferences
        // ===============================
// LOAD PROFILE IMAGE FROM DATABASE
// ===============================

        SharedPreferences sp = getSharedPreferences("booma_prefs", MODE_PRIVATE);
        String email = sp.getString("loggedInUser", null);

        if (email != null) {

            DBHelper dbHelper = new DBHelper(this);
            android.database.Cursor cursor = dbHelper.getUserByEmail(email);

            if (cursor != null && cursor.moveToFirst()) {

                String imagePath = cursor.getString(
                        cursor.getColumnIndexOrThrow("profile_image")
                );

                if (imagePath != null && !imagePath.isEmpty()) {

                    java.io.File imgFile = new java.io.File(imagePath);

                    if (imgFile.exists()) {
                        imgProfile.setImageURI(Uri.fromFile(imgFile));
                    } else {
                        imgProfile.setImageResource(R.drawable.circle_bg);
                    }

                } else {
                    imgProfile.setImageResource(R.drawable.circle_bg);
                }

                cursor.close();
            }
        }


        // Search click
        imgSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class))
        );

        // Bottom navigation
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, WorkerDashboardActivity.class))
        );

        navBookings.setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class))
        );

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, BookingHistoryActivity.class))
        );

        navHelp.setOnClickListener(v ->
                startActivity(new Intent(this, HelpSupportActivity.class))
        );
    }
}