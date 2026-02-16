package com.example.booma;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private boolean isAddAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Check if coming from "Add Account"
        String mode = getIntent().getStringExtra("MODE");
        isAddAccount = "ADD_ACCOUNT".equals(mode);

        // Background fade animation
        ConstraintLayout layout = findViewById(R.id.rootLayout);
        Drawable background = layout.getBackground();

        if (background != null) {
            ObjectAnimator fade = ObjectAnimator.ofInt(
                    background,
                    "alpha",
                    0,
                    255
            );
            fade.setDuration(5000);
            fade.setRepeatCount(ObjectAnimator.INFINITE);
            fade.setRepeatMode(ObjectAnimator.REVERSE);
            fade.start();
        }

        MaterialButton btnWorker = findViewById(R.id.btnWorker);
        MaterialButton btnClient = findViewById(R.id.btnClient);

        // Worker selection
        btnWorker.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkerRegActivity.class);
            intent.putExtra("ROLE", "WORKER");
            intent.putExtra("ADD_ACCOUNT", isAddAccount);
            startActivity(intent);
        });

        // Client selection
        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClientRegActivity.class);
            intent.putExtra("ROLE", "CLIENT");
            intent.putExtra("ADD_ACCOUNT", isAddAccount);
            startActivity(intent);
        });
    }
}
