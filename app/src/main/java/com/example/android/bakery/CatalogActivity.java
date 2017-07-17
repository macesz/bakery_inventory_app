package com.example.android.bakery;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

import com.example.android.bakery.data.ProductContract;
import com.example.android.bakery.data.ProductContract.ProductEntry;

import static com.example.android.bakery.data.ProductProvider.LOG_TAG;


/**
 * Created by orsi on 16/07/2017.
 */

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ProductCursorAdapter adapter;
    private static final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView productListView = (ListView) findViewById(R.id.list_view_products);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Create empty adapter
        adapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(adapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI_PRODUCTS, id);

                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onListItemClick(long id) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI_PRODUCTS, id);
        intent.setData(currentProductUri);

        startActivity(intent);
    }

    public void onSaleBtnClick(long id, int quantity) {
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI_PRODUCTS, id);

        int newQuantity = quantity > 0 ? quantity - 1 : 0;

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_IN_STOCK, newQuantity);

        int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(this, getString(R.string.update_product_update_failed),
                    Toast.LENGTH_SHORT).show();
        } else if (quantity != newQuantity) {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.update_product_updated) + id,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Coffee cup");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, "22.99");
        values.put(ProductEntry.COLUMN_PRODUCT_IN_STOCK, 2);

        Uri imgUri = Uri.parse("android.resource://" + ProductContract.CONTENT_AUTHORITY + "/" + R.drawable.ic_bread_empty);
        Log.v(LOG_TAG, imgUri.toString());

        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imgUri.toString());

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI_PRODUCTS, values);
        Log.v(LOG_TAG, newUri.toString());
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI_PRODUCTS, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from products database");
    }

    /**
     * Show a dialog that ask the user about to delet this product
     *
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_IN_STOCK,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, ProductEntry.CONTENT_URI_PRODUCTS,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}

