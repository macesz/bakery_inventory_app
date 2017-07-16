package com.example.android.bakery.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.bakery.data.ProductContract.ProductEntry;

import static com.example.android.bakery.data.ProductProvider.LOG_TAG;

/**
 * Database helper class
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    /** Name of the database file */
    private static final String DATABASE_NAME = "bakery.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Create a String that contains the SQL statement to create the product table
    public String SQL_CREATE_ENTRIES = "CREATE TABLE " + ProductEntry.TABLE_NAME +
            "("
            + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT, "
            + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
            + ProductEntry.COLUMN_PRODUCT_IN_STOCK + " INTEGER NOT NULL DEFAULT 0, "
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT, "
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + " TEXT, "
            + ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL + " TEXT );";

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
