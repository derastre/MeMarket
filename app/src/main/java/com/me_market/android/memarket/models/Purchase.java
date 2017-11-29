package com.me_market.android.memarket.models;

import java.io.Serializable;

/**
 * Created by Arturo Deras on 21/10/2017.
 */

public class Purchase implements Serializable {


    public String productId;
    public String productName;
    public String productType;
    public String storeId;
    public Float quantity;
    public Long timeStamp;
    public Boolean isOffer;
    public Float price;
    public Float offerPrice;


    public Purchase() {

    }


    public Purchase(String productId, String productName, String storeid, Long timestamp, Float quantity, Float price) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.offerPrice = null;
        this.storeId = storeid;
        this.isOffer = false;
        this.timeStamp = timestamp;
        this.quantity = quantity;
    }

    public Purchase(String productId, String productName, String productType, String storeid, Long timestamp, Float quantity, Float price, Float offerPrice, Boolean isOffer) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.offerPrice = offerPrice;
        this.storeId = storeid;
        this.isOffer = isOffer;
        this.timeStamp = timestamp;
        this.quantity = quantity;
    }
}


