package com.example.android.memarket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Product;
import com.example.android.memarket.models.Purchase;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MyCart extends BaseActivity implements View.OnClickListener {

    BottomSheetBehavior behavior;
    GridLayout gridLayout;
    ArrayList<Product> productsArray;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_cart_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.my_cart_label);
        ab.setDisplayHomeAsUpEnabled(true);

        //Buttons listeners
        findViewById(R.id.my_cart_gridlayout).setOnClickListener(this);

        //Setting bottom sheet
        gridLayout = (GridLayout) findViewById(R.id.my_cart_gridlayout);
        behavior = BottomSheetBehavior.from(gridLayout);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        hideBottomItems();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        showBottomItems();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        showBottomItems();
                        break;
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        //Getting the product list
        setListProducts();
    }

    private void setListProducts() {
        productsArray
        listView = (ListView) findViewById(R.id.my_cart_listview);
        productArrayAdapter adapter=new productArrayAdapter()

    }

    private void showBottomItems() {
        findViewById(R.id.subtotal_text).setVisibility(View.VISIBLE);
        findViewById(R.id.subtotal_number).setVisibility(View.VISIBLE);
        findViewById(R.id.discount_text).setVisibility(View.VISIBLE);
        findViewById(R.id.discount_number).setVisibility(View.VISIBLE);
    }

    private void hideBottomItems() {
        findViewById(R.id.subtotal_text).setVisibility(View.GONE);
        findViewById(R.id.subtotal_number).setVisibility(View.GONE);
        findViewById(R.id.discount_text).setVisibility(View.GONE);
        findViewById(R.id.discount_number).setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.my_cart_gridlayout:
                expandBottomSheet();
                break;
        }
    }

    private void expandBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private ArrayList<Purchase> readRegisterProductLocally() {
        try {
            FileInputStream fi = new FileInputStream(new File("myObjects.txt"));
            ObjectInputStream oi = new ObjectInputStream(fi);
            ArrayList<Purchase> purchases = new ArrayList<>();

            // Read objects
            while (true) {
                try {
                    Purchase purchase = (Purchase) oi.readObject();
                    purchases.add(purchase);
                } catch (EOFException e) {
                    // If there are no more objects to read, return what we have.
                    return purchases;
                } finally {
                    // Close the stream.
                    oi.close();
                    fi.close();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            return null;
        } catch (ClassNotFoundException e){
            System.out.println("Class not found");
            return null;
        }


    }

    class productArrayAdapter extends ArrayAdapter<Product>{
        private Context context;
        private ArrayList<Product> products;
        private ArrayList<Purchase> purchases;

        public productArrayAdapter(Context context,int resource, ArrayList<Product> objects){
            super(context,resource,objects);
            this.context=context;
            this.products=objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {



            //get the property we are displaying
            Product product = products.get(position);

            //get the inflater and inflate the XML layout for each item
            ViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.my_cart_listview_layout, null, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.getNameText().setText(product.Name);
            holder.getTypeText().setText(product.Type);
            holder.getPriceText().setText(NumberFormat.getCurrencyInstance().format(product.getCurrentPrice()));
           // holder.getButton().setOnClickListener(MyCart.this);


            return convertView;
        }

    }

    public class ViewHolder {
        private View row;
        private TextView name = null, type = null, price = null;
        private ImageView image;
        private  Button erase_button;

        ViewHolder(View row) {
            this.row = row;
        }

        public TextView getNameText() {
            if (this.name == null) {
                this.name = (TextView) row.findViewById(R.id.cart_product_name);
            }
            return this.name;
        }

        public TextView getTypeText() {
            if (this.type == null) {
                this.type = (TextView) row.findViewById(R.id.cart_product_type);
            }
            return this.type;
        }

        public TextView getPriceText() {
            if (this.price == null) {
                this.price = (TextView) row.findViewById(R.id.cart_product_price);
            }
            return this.price;
        }

        public ImageView getImage() {
            if (this.image == null) {
                this.image = (ImageView) row.findViewById(R.id.cart_product_image);
            }
            return this.image;
        }

        public Button getButton(){
            if (this.erase_button == null) {
                this.erase_button = (Button) row.findViewById(R.id.cart_product_delete_button);
            }
            return this.erase_button;
        }

    }

}
