package com.example.booma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etPhone, etDob, etLocation, etPassword;
    Button btnSave;

    DBHelper dbHelper;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DBHelper(this);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        SharedPreferences sp = getSharedPreferences("booma_prefs", MODE_PRIVATE);
        email = sp.getString("loggedInUser", null);

        loadData();

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void loadData() {

        if (email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor cursor = dbHelper.getUserByEmail(email);

        if (cursor != null && cursor.moveToFirst()) {

            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            etDob.setText(cursor.getString(cursor.getColumnIndexOrThrow("dob")));
            etLocation.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            etPassword.setText(cursor.getString(cursor.getColumnIndexOrThrow("password")));

            cursor.close();
        }
    }


    private void updateProfile() {

        boolean updated = dbHelper.updateUser(
                email,
                etName.getText().toString(),
                etPhone.getText().toString(),
                etDob.getText().toString(),
                etLocation.getText().toString(),
                etPassword.getText().toString()

        );

        if (updated) {
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            finish(); // return to ProfileActivity
        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }
}

