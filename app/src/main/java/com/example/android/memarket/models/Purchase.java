package com.example.android.memarket.models;

/**
 * Created by Arturo Deras on 21/10/2017.
 */

public class Purchase {

    public Float price;
    public String storeId;
    public Integer quantity;
    public Long timeStamp;
    public Boolean isOffer;


    public Purchase() {

    }

    public Purchase(Float price, String storeid, Long timestamp, Boolean isoffer, Integer quantity) {
        this.price = price;
        this.storeId = storeid;
        this.isOffer = isoffer;
        this.timeStamp = timestamp;
        this.quantity = quantity;
    }
}


