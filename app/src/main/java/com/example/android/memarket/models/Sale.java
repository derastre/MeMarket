package com.example.android.memarket.models;

import java.io.Serializable;

/**
 * Created by aederas on 25/11/2017.
 */

public class Sale implements Serializable{
    public Float onSalePrice;
    public String userId;
    public String storeId;
    public String productId;

    public Sale() {
        // Default constructor required for calls to DataSnapshot.getValue(Sale.class)
    }

    public Sale(Float onSalePrice, String userId, String storeId, String productId) {
        this.onSalePrice = onSalePrice;
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
    }
}

