package com.example.booma;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkerRegActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private EditText etLocation;
    private static final int REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CODE = 200;

    private CardView photoPreviewContainer;
    private ImageView imgPhotoPreview;
    private ImageButton btnRemovePhoto;
    private Uri selectedImageUri = null;

    // Track validation status
    private boolean isNameValid = false;
    private boolean isEmailValid = false;
    private boolean isPhoneValid = false;
    private boolean isDobValid = false;
    private boolean isLocationValid = false;
    private boolean isExpValid = false;
    private boolean isGenderValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workerreg);

        // Initialize views
        EditText name = findViewById(R.id.wname);
        EditText email = findViewById(R.id.wemail);
        EditText phone = findViewById(R.id.wphone);
        EditText dob = findViewById(R.id.wdob);
        EditText exp = findViewById(R.id.wexp);
        etLocation = findViewById(R.id.etLocation);
        MaterialButton btn = findViewById(R.id.btnContinue);
        Button uploadBtn = findViewById(R.id.btnUpload);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        RadioGroup genderGroup = findViewById(R.id.wgender);

        // Initialize photo preview elements
        photoPreviewContainer = findViewById(R.id.photoPreviewContainer);
        imgPhotoPreview = findViewById(R.id.imgPhotoPreview);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);

        // Initialize Fused Location Provider Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup spinners
        setupSpinners();

        // Setup real-time validation
        setupRealTimeValidation(name, email, phone, dob, exp, genderGroup);

        // Location button click listener
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        // Date of Birth picker
        dob.setOnClickListener(v -> showDatePicker(dob));

        // Image upload - SIMPLE & CLEAN like ClientRegActivity
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        // Remove photo button
        btnRemovePhoto.setOnClickListener(v -> {
            imgPhotoPreview.setImageDrawable(null);
            photoPreviewContainer.setVisibility(View.GONE);
            selectedImageUri = null;
            Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
        });

        // Continue button validation
        btn.setOnClickListener(v -> validateAndSubmit(name, email, phone, dob, exp, genderGroup));
    }

    private void setupRealTimeValidation(EditText name, EditText email, EditText phone,
                                         EditText dob, EditText exp, RadioGroup genderGroup) {

        // Name validation - Only letters and spaces
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nameStr = s.toString().trim();
                if (nameStr.isEmpty()) {
                    name.setError("Name required");
                    isNameValid = false;
                } else if (!nameStr.matches("[a-zA-Z ]+")) {
                    name.setError("Only letters and spaces allowed");
                    isNameValid = false;
                } else {
                    name.setError(null);
                    isNameValid = true;
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email validation
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailStr = s.toString().trim();
                if (emailStr.isEmpty()) {
                    email.setError("Email required");
                    isEmailValid = false;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                    email.setError("Invalid email format");
                    isEmailValid = false;
                } else {
                    email.setError(null);
                    isEmailValid = true;
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Phone validation
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phoneStr = s.toString().trim();
                if (phoneStr.isEmpty()) {
                    phone.setError("Phone required");
                    isPhoneValid = false;
                } else if (phoneStr.length() < 10) {
                    phone.setError("Minimum 10 digits required");
                    isPhoneValid = false;
                } else if (!phoneStr.matches("\\d+")) {
                    phone.setError("Only numbers allowed");
                    isPhoneValid = false;
                } else {
                    phone.setError(null);
                    isPhoneValid = true;
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // DOB validation
        dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String dobStr = s.toString().trim();
                if (dobStr.isEmpty()) {
                    dob.setError("DOB required");
                    isDobValid = false;
                } else {
                    // Check if age is above 18 (assuming format is dd/MM/yyyy)
                    try {
                        String[] parts = dobStr.split("/");
                        if (parts.length == 3) {
                            int day = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]) - 1;
                            int year = Integer.parseInt(parts[2]);

                            Calendar dobCal = Calendar.getInstance();
                            dobCal.set(year, month, day);
                            Calendar today = Calendar.getInstance();

                            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);

                            if (today.get(Calendar.MONTH) < dobCal.get(Calendar.MONTH) ||
                                    (today.get(Calendar.MONTH) == dobCal.get(Calendar.MONTH) &&
                                            today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH))) {
                                age--;
                            }

                            if (age < 18) {
                                Toast.makeText(WorkerRegActivity.this, "Must be 18+ years old", Toast.LENGTH_SHORT).show();
                                dob.setError("Must be 18+ years old");
                                isDobValid = false;
                            } else {
                                dob.setError(null);
                                isDobValid = true;
                            }

                        }
                    } catch (Exception e) {
                        dob.setError("Invalid date format");
                        isDobValid = false;
                    }
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Experience validation
        exp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String expStr = s.toString().trim();
                if (expStr.isEmpty()) {
                    exp.setError("Experience required");
                    isExpValid = false;
                } else if (!expStr.matches("\\d+")) {
                    exp.setError("Only numbers allowed");
                    isExpValid = false;
                } else {
                    exp.setError(null);
                    isExpValid = true;
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Gender validation
        genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isGenderValid = checkedId != -1;
            updateContinueButtonState();
        });

        // Location validation
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String locationStr = s.toString().trim();
                if (locationStr.isEmpty() || locationStr.equals("Fetching current location...")) {
                    isLocationValid = false;
                } else {
                    isLocationValid = true;
                }
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateContinueButtonState() {
        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        boolean allValid = isNameValid && isEmailValid && isPhoneValid &&
                isDobValid && isExpValid && isGenderValid && isLocationValid;

        btnContinue.setEnabled(allValid);
        btnContinue.setAlpha(allValid ? 1.0f : 0.5f);
    }

    private void setupSpinners() {
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        String[] categories = {
                "Electrician", "Plumber", "Carpenter", "Painter", "Mason",
                "Baby Sitter", "House Cleaner", "Gardener"
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(catAdapter);

        Spinner eduSpinner = findViewById(R.id.eduSpinner);
        String[] education = {
                "ITI", "Diploma", "SSLC", "HSC", "Vocational Training",
                "Apprenticeship", "Other"
        };
        ArrayAdapter<String> eduAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, education);
        eduSpinner.setAdapter(eduAdapter);
    }

    private void showDatePicker(EditText dob) {
        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, day) -> {
            int age = y - year;
            if (age < 18) {
                dob.setError("Age must be above 18");
                isDobValid = false;
            } else {
                dob.setText(day + "/" + (month + 1) + "/" + year);
                isDobValid = true;
            }
            updateContinueButtonState();
        }, y, m, d);
        dp.show();
    }

    private void getCurrentLocation() {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request permissions
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_CODE
            );
            return;
        }

        // Get location
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Get address from coordinates
                getExactAddress(location);
            } else {
                Toast.makeText(this, "Unable to get location. Please enable GPS.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getExactAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Set the first address line in the EditText
                String addressLine = address.getAddressLine(0);
                if (addressLine != null) {
                    etLocation.setText(addressLine);
                } else if (address.getLocality() != null) {
                    etLocation.setText(address.getLocality());
                }

                // Show full address in toast
                StringBuilder fullAddress = new StringBuilder();
                if (address.getAddressLine(0) != null) {
                    fullAddress.append(address.getAddressLine(0));
                }
                if (address.getLocality() != null) {
                    if (fullAddress.length() > 0) fullAddress.append(", ");
                    fullAddress.append(address.getLocality());
                }

                Toast.makeText(this, "Location set: " + fullAddress.toString(),
                        Toast.LENGTH_LONG).show();

                isLocationValid = true;
                updateContinueButtonState();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to coordinates if geocoding fails
            etLocation.setText("Lat: " + location.getLatitude() +
                    ", Long: " + location.getLongitude());
            Toast.makeText(this, "Using coordinates", Toast.LENGTH_SHORT).show();
            isLocationValid = true;
            updateContinueButtonState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndSubmit(EditText name, EditText email, EditText phone,
                                   EditText dob, EditText exp, RadioGroup genderGroup) {

        if (!isNameValid || !isEmailValid || !isPhoneValid ||
                !isDobValid || !isExpValid || !isGenderValid || !isLocationValid) {

            Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Worker image MUST be uploaded
        if (selectedImageUri == null) {
            Toast.makeText(this, "Profile photo is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String nameStr = name.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String phoneStr = phone.getText().toString().trim();
        String dobStr = dob.getText().toString().trim();
        String locationStr = etLocation.getText().toString().trim();
        String password = "1234"; // you can improve later

        // Save image to internal storage
        String imagePath = saveImageToInternalStorage(selectedImageUri);

        if (imagePath == null) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper dbHelper = new DBHelper(this);

        boolean inserted = dbHelper.insertUser(
                nameStr,
                emailStr,
                phoneStr,
                dobStr,
                locationStr,
                password,
                "WORKER",
                imagePath
        );

        if (!inserted) {
            Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save login session
        getSharedPreferences("booma_prefs", MODE_PRIVATE)
                .edit()
                .putString("loggedInUser", emailStr)
                .apply();

        Toast.makeText(this, "Worker registration successful!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, WorkerDashboardActivity.class));
        finish();
    }

    private String saveImageToInternalStorage(Uri imageUri) {

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {

            String fileName = "worker_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                return file.getAbsolutePath();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Check image size (200KB limit)
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                int size = is.available();
                if (size > 200 * 1024) {
                    Toast.makeText(this, "Image must be under 200KB", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null;
                    return;
                }
                is.close();

                // Show preview
                imgPhotoPreview.setImageURI(selectedImageUri);
                photoPreviewContainer.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                selectedImageUri = null;
            }
        }
    }
}