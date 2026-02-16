package com.example.booma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash delay (1 second)
        new Handler().postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("booma_prefs", MODE_PRIVATE);
            String email = sp.getString("loggedInUser", null);

            // If no user logged in → Register
            if (email == null) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();
                return;
            }

            // Check role from database
            DBHelper dbHelper = new DBHelper(MainActivity.this);
            String role = dbHelper.getUserRole(email);

            if (role == null) {

                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            } else if (role.equalsIgnoreCase("ADMIN")) {

                startActivity(new Intent(MainActivity.this, AdminActivity.class));

            } else if (role.equalsIgnoreCase("CLIENT")) {

                startActivity(new Intent(MainActivity.this, ClientDashboardActivity.class));

            } else if (role.equalsIgnoreCase("WORKER")) {

                startActivity(new Intent(MainActivity.this, WorkerDashboardActivity.class));

            } else {

                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }

            finish();

        }, 1000);
    }
}
