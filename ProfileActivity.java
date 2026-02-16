package com.example.booma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    Button btnAddProject;
    TextView tvName, tvPhone, tvDob, tvLocation, tvEmail;
    ImageView btnEdit;
    LinearLayout portfolioSection, projectContainer;
    String userRole;

    ImageView btnEditProfile,editImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    Button deleteAccount;

    DBHelper dbHelper;
    String email;
    String profileImagePath;
    ShapeableImageView profileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);
        deleteAccount = findViewById(R.id.btnDeleteAccount);

        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvDob = findViewById(R.id.tvDob);
        tvLocation = findViewById(R.id.tvLocation);
        btnAddProject = findViewById(R.id.btnAddProject);
        tvEmail = findViewById(R.id.tvEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        profileImage = findViewById(R.id.profileImage);
        editImage = findViewById(R.id.editImage);
        portfolioSection = findViewById(R.id.portfolioSection);
        projectContainer = findViewById(R.id.projectContainer);


        editImage.setOnClickListener(v -> openGallery());




        SharedPreferences sp = getSharedPreferences("booma_prefs", MODE_PRIVATE);
        email = sp.getString("loggedInUser", null);

        loadUserData();

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );


        deleteAccount.setOnClickListener(v -> confirmDelete());
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            android.net.Uri imageUri = data.getData();

            String savedPath = saveImageToInternalStorage(imageUri);

            if (savedPath != null) {

                // Save image path in database
                dbHelper.updateUserImage(email, savedPath);

                // Update UI
                profileImage.setImageURI(android.net.Uri.fromFile(new java.io.File(savedPath)));
            }
        }
    }
    private String saveImageToInternalStorage(android.net.Uri imageUri) {

        try {
            java.io.InputStream inputStream =
                    getContentResolver().openInputStream(imageUri);

            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(getFilesDir(), fileName);

            java.io.FileOutputStream outputStream =
                    new java.io.FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void loadUserData() {

        Cursor cursor = dbHelper.getUserByEmail(email);

        if (cursor != null && cursor.moveToFirst()) {

            tvName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            tvPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            tvDob.setText(cursor.getString(cursor.getColumnIndexOrThrow("dob")));
            tvLocation.setText(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            tvEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));

            String imagePath = cursor.getString(
                    cursor.getColumnIndexOrThrow("profile_image")
            );

            if (imagePath != null && !imagePath.isEmpty()) {

                java.io.File imgFile = new java.io.File(imagePath);

                if (imgFile.exists()) {
                    profileImage.setImageURI(android.net.Uri.fromFile(imgFile));
                } else {
                    profileImage.setImageResource(R.drawable.circle_bg);
                }

            } else {
                profileImage.setImageResource(R.drawable.circle_bg);
            }


            userRole = cursor.getString(
                    cursor.getColumnIndexOrThrow("role")
            );

            if ("WORKER".equals(userRole)) {
                portfolioSection.setVisibility(View.VISIBLE);
                btnAddProject.setVisibility(View.VISIBLE);
                loadWorkerProjects();   // load projects for worker
            } else {
                portfolioSection.setVisibility(View.GONE);
            }


            cursor.close();
        }
    }
    private void loadWorkerProjects() {

        projectContainer.removeAllViews();

        Cursor cursor = dbHelper.getProjectsByWorker(email);

        if (cursor != null && cursor.moveToFirst()) {

            do {

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow("project_title"));

                String description = cursor.getString(
                        cursor.getColumnIndexOrThrow("project_description"));

                TextView projectView = new TextView(this);
                projectView.setText(title + "\n" + description);
                projectView.setPadding(0, 10, 0, 10);

                projectContainer.addView(projectView);

            } while (cursor.moveToNext());

            cursor.close();
        }
    }




    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    boolean deleted = dbHelper.deleteUserByEmail(email);

                    if (deleted) {

                        SharedPreferences sp = getSharedPreferences("booma_prefs", MODE_PRIVATE);
                        sp.edit().clear().apply();

                        Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, RegisterActivity.class));
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
