package com.example.booma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "booma.db";
    private static final int DATABASE_VERSION = 2; // ← IMPORTANT: increase version

    private static final String TABLE_USERS = "users";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "dob TEXT, " +
                "location TEXT, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "profile_image TEXT" +      // nullable (optional image)
                ")";


        db.execSQL(createTable);
        db.execSQL("CREATE TABLE worker_projects(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "worker_email TEXT," +
                "project_title TEXT," +
                "project_description TEXT," +
                "project_image TEXT)");
    }

        @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ===============================
    // INSERT USER
    // ===============================
    public boolean insertUser(String name, String email, String phone,
                              String dob, String location,
                              String password, String role,
                              String profileImage) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("dob", dob);
        values.put("location", location);
        values.put("password", password);
        values.put("role", role);
        values.put("profile_image", profileImage); // can be null

        long result = db.insert(TABLE_USERS, null, values);

        return result != -1;
    }
    public Cursor getProjectsByWorker(String workerEmail) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM worker_projects WHERE worker_email=?",
                new String[]{workerEmail}
        );
    }

    // ===============================
// GET USER ROLE BY EMAIL
// ===============================
    public String getUserRole(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT role FROM " + TABLE_USERS + " WHERE email=?",
                new String[]{email}
        );

        String role = null;

        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(
                    cursor.getColumnIndexOrThrow("role")
            );
            cursor.close();
        }

        return role;
    }


    // ===============================
    // CHECK LOGIN
    // ===============================
    public boolean checkUser(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE email=? AND password=?",
                new String[]{email, password}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    // ===============================
    // GET USER BY EMAIL
    // ===============================
    public Cursor getUserByEmail(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE email=?",
                new String[]{email}
        );
    }

    // ===============================
    // GET ALL USERS (Admin Page)
    // ===============================
    public Cursor getAllUsers() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS,
                null
        );
    }

    // ===============================
    // DELETE USER
    // ===============================
    public boolean deleteUser(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_USERS,
                "id=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }
    // ===============================
// UPDATE USER (Except Email)
// ===============================
    public boolean updateUser(String email,
                              String name,
                              String phone,
                              String dob,
                              String location,
                              String password) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("dob", dob);
        values.put("location", location);
        values.put("password", password);

        int result = db.update("users", values, "email=?", new String[]{email});
        return result > 0;
    }
    public boolean updateUserImage(String email, String imagePath) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_image", imagePath);

        int result = db.update("users", values, "email=?", new String[]{email});
        return result > 0;
    }




    // ===============================
// DELETE USER BY EMAIL
// ===============================
    public boolean deleteUserByEmail(String email) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_USERS,
                "email=?",
                new String[]{email}
        );

        return result > 0;
    }

}
