package com.example.android.memarket.models;

/**
 * Created by Arturo Deras on 21/10/2017.
 */

public class Purchase {

    public String price;
    public String storeId;
    public Long timeStamp;
    public Boolean isOffer;


    public Purchase() {

    }

    public Purchase(String price, String storeid, Long timestamp, Boolean isoffer) {
        this.price = price;
        this.storeId = storeid;
        this.isOffer = isoffer;
        this.timeStamp = timestamp;
    }
}


