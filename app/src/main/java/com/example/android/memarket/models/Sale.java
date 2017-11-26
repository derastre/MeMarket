package com.example.android.memarket.models;

/**
 * Created by aederas on 25/11/2017.
 */

public class Sale {
    public Float onSalePrice;
    public String userId;
    public Store storeData;
    public Product productData;

    public Sale(){
        // Default constructor required for calls to DataSnapshot.getValue(Sale.class)
    }

    public Sale(Float onSalePrice, String userId, Store storeData, Product productData) {
        this.onSalePrice = onSalePrice;
        this.userId = userId;
        this.storeData = storeData;
        this.productData = productData;
    }
}

