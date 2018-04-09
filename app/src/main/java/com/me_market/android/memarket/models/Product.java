package com.me_market.android.memarket.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Arturo Deras on 20/8/2017.
 */
@IgnoreExtraProperties
public class Product implements Serializable {

    private String Id;
    public String Barcode;
    public String Name;
    public String Type;
    public String Brand;
    public Float Quantity;
    public String Units;
    private Float currentPrice;
    private Float currentOffer;
    private Boolean isOffer;


    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String Barcode, String ProductName, String ProductType, String ProductBrand, Float ProductQuantity, String ProductUnits) {
        this.Barcode = Barcode;
        this.Name = ProductName;
        this.Type = ProductType;
        this.Brand = ProductBrand;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }

    public Product(String Barcode,String ProductName, String ProductType, Float ProductQuantity, String ProductUnits) {
        this.Barcode = Barcode;
        this.Name = ProductName;
        this.Type = ProductType;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }

    public Float getCurrentPrice() {
        return currentPrice;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setCurrentPrice(Float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Boolean getOffer() {
        return isOffer;
    }

    public void setOffer(Boolean offer) {
        isOffer = offer;
    }

    public Float getCurrentOffer() {
        return currentOffer;
    }

    public void setCurrentOffer(Float currentOffer) {
        this.currentOffer = currentOffer;
    }
}
