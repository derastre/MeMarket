package com.example.android.memarket;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridLayout;

import com.example.android.memarket.components.BaseActivity;

public class MyCart extends BaseActivity implements View.OnClickListener {

    BottomSheetBehavior behavior;
    GridLayout gridLayout;

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
}
