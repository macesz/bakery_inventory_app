package com.example.android.bakery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bakery.data.ProductContract.ProductEntry;


/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {


    //?? ut'na n;zni
    private CatalogActivity catalogActivity = new CatalogActivity();


    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0);
        this.catalogActivity = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.product_list_item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.product_list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.product_list_item_quantity);
        ImageView productImageView = (ImageView) view.findViewById(R.id.product_list_item_image);
        Button sellButton = (Button) view.findViewById(R.id.product_list_item_sell_button);


        final Long id = cursor.getLong(cursor.getColumnIndex(ProductEntry._ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IN_STOCK));
        String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE));

        nameTextView.setText(name + Long.toString(id));
        quantityTextView.setText(String.valueOf(quantity));
        if(image!=null) {
            productImageView.setImageURI(Uri.parse(image));
        }
        priceTextView.setText(price + " EUR");

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catalogActivity.onSaleBtnClick(id, quantity);
                notifyDataSetChanged();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catalogActivity.onListItemClick(id);
            }
        });
    }
}
