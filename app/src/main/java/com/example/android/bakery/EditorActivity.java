package com.example.android.bakery;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.bakery.data.ProductContract.ProductEntry;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by orsi on 16/07/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the product
     * data loader
     */
    private static final int PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing image
     * (null if it's a new product
     * )
     */
    Uri imageUri;

    /**
     * EditText field to enter the product picture
     */
    private ImageView pictureImageView;
    /**
     * Content URI for the existing product
     * (null if it's a new product
     * )
     */
    Uri currentProductUri;

    /**
     * EditText field to enter the product's name
     */
    private EditText nameEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText priceEditText;
    /**
     * EditText field to enter the product's quantity avaliable
     */
    private EditText quantityEditText;


    /**
     * EditText field to enter the product's suplier name
     */
    private EditText supplierNameEditText;

    /**
     * EditText field to enter the product's suplyphone
     */
    private EditText resupplyPhoneEditText;

    /**
     * EditText field to enter the product's suply url
     */
    private EditText resupplyUrlEdittext;

    String quantityString;

    private boolean productHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    private static final int MAX_PRODUCT_QUANTITY = 999999;
    private static final int MIN_PRODUCT_QUANTITY = 0;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.edit_product_decrease_quantity:
                    decreaseProductQuantity();
                    break;
                case R.id.edit_product_increase_quantity:
                    increaseProductQuantity();
                    break;
                case R.id.edit_product_call_resupply_phone_button:
                    callResupplyPhone();
                    break;
                case R.id.edit_product_open_resupply_url_button:
                    openResupplyUrl();
                    break;
                default:
                    //  actually nothing to be done by default
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_editor);
        setSupportActionBar(toolbar);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        // If the intent DOES NOT contain a product
        // content URI, then we know that we are
        // creating a new product
        if (currentProductUri == null) {
            // This is a new product
            // , so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product
            // that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product
            // , so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product
            // data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }

        //  buttons to open URL, place a call, increase or decrease quantity avialable
        ImageButton increaseQuantityButton = (ImageButton) findViewById(R.id.edit_product_increase_quantity);
        ImageButton decreaseQuantityButton = (ImageButton) findViewById(R.id.edit_product_decrease_quantity);
        ImageButton callResupplyPhoneButton = (ImageButton) findViewById(R.id.edit_product_call_resupply_phone_button);
        ImageButton openResupplyUrlButton = (ImageButton) findViewById(R.id.edit_product_open_resupply_url_button);

        // Find all relevant views that we will need to read user input from
        pictureImageView = (ImageView) findViewById(R.id.edit_product_image);
        nameEditText = (EditText) findViewById(R.id.edit_product_name);
        priceEditText = (EditText) findViewById(R.id.edit_product_price);
        quantityEditText = (EditText) findViewById(R.id.edit_product_quantity_in_stock);
        supplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        resupplyPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        resupplyUrlEdittext = (EditText) findViewById(R.id.edit_supplier_url);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        pictureImageView.setOnTouchListener(mTouchListener);
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        resupplyPhoneEditText.setOnTouchListener(mTouchListener);
        resupplyUrlEdittext.setOnTouchListener(mTouchListener);

        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                productHasChanged = true;
            }
        });

        //  assign listener to take care of button actions
        increaseQuantityButton.setOnClickListener(onClickListener);
        decreaseQuantityButton.setOnClickListener(onClickListener);
        callResupplyPhoneButton.setOnClickListener(onClickListener);
        openResupplyUrlButton.setOnClickListener(onClickListener);
    }

    /**
     * Get user input from editor and save product into database.
     */
    private boolean saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String name = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        quantityString = quantityEditText.getText().toString().trim();
        String suplierString = supplierNameEditText.getText().toString().trim();
        String phoneString = resupplyPhoneEditText.getText().toString().trim();
        String urlString = resupplyUrlEdittext.getText().toString().trim();

        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.editor_required_name), Toast.LENGTH_SHORT).show();
            return false;
        }
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = !TextUtils.isEmpty(quantityString) ? Integer.parseInt(quantityString) : 0;
        values.put(ProductEntry.COLUMN_PRODUCT_IN_STOCK, quantity);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.editor_required_price), Toast.LENGTH_SHORT).show();
            return false;
        }

        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        if (imageUri == null) {
            Toast.makeText(this, getString(R.string.editor_required_photo), Toast.LENGTH_SHORT).show();
            return false;
        }

        String image = imageUri.toString();
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, image);

        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, suplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, phoneString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL, urlString);


        // Determine if this is a new or existing product by checking if currentPetUri is null or not
        if (currentProductUri == null) {
            setTitle("Add Product");
            supportInvalidateOptionsMenu();

            // This is a NEW product, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI_PRODUCTS, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_succeeded),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_succeeded),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        if (currentProductUri == null) {
            MenuItem orderItem = menu.findItem(R.id.action_send_order);
            orderItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order" menu option
            case R.id.action_send_order:
                // Pop up confirmation dialog for deletion
                sendOrderToSupplier(nameEditText.getText().toString().trim());
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };



    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI_PRODUCTS, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }

    /**
     *Show a dialog that ask the user about to delet this product
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
                deleteProduct();
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
    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void sendOrderToSupplier(String product) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String subject = getString(R.string.editor_ordering_product) + product;
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        String messageBody = getString(R.string.editor_message_body) + product + " ?";
        intent.putExtra(Intent.EXTRA_TEXT, messageBody);

        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // you will actually use after this query.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_IN_STOCK,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL,};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IN_STOCK);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int suplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            int urlColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_URL);

            // Extract out the value from the Cursor for the given column index
            String picture = cursor.getString(pictureColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String suplier = cursor.getString(suplierColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String url = cursor.getString(urlColumnIndex);

            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(Integer.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            pictureImageView.setImageURI(Uri.parse(picture));
            imageUri = Uri.parse(picture);
            supplierNameEditText.setText(suplier);
            resupplyPhoneEditText.setText(phone);
            resupplyUrlEdittext.setText(url);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        quantityEditText.setText("");
        priceEditText.setText("");
        supplierNameEditText.setText("");
        pictureImageView.setImageURI(Uri.parse(""));
        resupplyPhoneEditText.setText("");
        resupplyUrlEdittext.setText("");
    }

    /**
     * method to decrease product quantity by one.
     */
    private void decreaseProductQuantity() {
        int quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
        if (quantity > 0) {
            productHasChanged = true;
            quantityEditText.setText(String.valueOf(quantity - 1));
        }
    }

    /**
     * method to increase product quantity by one.
     */
    private void increaseProductQuantity() {
        productHasChanged = true;
        int quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
        quantityEditText.setText(String.valueOf(quantity + 1));
    }

    /**
     * method to call ACTION_DIAL intent if phone number is set. Does not require special permission
     * as the actual work will be done by the dialer application.
     */
    private void callResupplyPhone() {
        String phoneNumber = resupplyPhoneEditText.getText().toString();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.message_please_set_phonenumber_first), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);

        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_no_activity_to_place_call), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            //  theoretically we should not get here as we only send an implicit intent to call
            //  a number, but anyways, play nice and catch the exception.
            Toast.makeText(this, getResources().getString(R.string.error_message_no_permission_to_place_call), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * open resupply URL if it is set and valid
     */
    private void openResupplyUrl() {
        String urlString = resupplyUrlEdittext.getText().toString();
        if (urlString.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.message_please_set_url_first), Toast.LENGTH_SHORT).show();
            return;
        }

        URL url;
        //  make sure URL is valid
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_malformed_url), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url.toString()));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, getResources().getString(R.string.error_message_no_activity_to_open_url), Toast.LENGTH_SHORT).show();
        }
    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                pictureImageView.setImageURI(imageUri);
                pictureImageView.invalidate();
            }
        }
    }
}
