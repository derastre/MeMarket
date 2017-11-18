package com.example.android.memarket.models;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Arturo Deras on 20/8/2017.
 */
@IgnoreExtraProperties
public class Product {

    private String Id;
    public String Name;
    public String Type;
    public String Brand;
    public Float Quantity;
    public String Units;
    private Float currentPrice;
    private Bitmap Image;
    private Boolean isOffer;


    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Product(String ProductName, String ProductType, String ProductBrand, Float ProductQuantity, String ProductUnits) {

        this.Name = ProductName;
        this.Type = ProductType;
        this.Brand = ProductBrand;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }

//    public Product(String ProductId, String ProductName, String ProductType, String ProductBrand, String ProductQuantity, String ProductUnits) {
//
//        this.Id = ProductId;
//        this.Name = ProductName;
//        this.Type = ProductType;
//        this.Brand = ProductBrand;
//        this.Quantity = ProductQuantity;
//        this.Units = ProductUnits;
//    }

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

    public Bitmap getImage() {
        return Image;
    }

    public void setImage(Bitmap image) {
        Image = image;
    }

    public Boolean getOffer() {
        return isOffer;
    }

    public void setOffer(Boolean offer) {
        isOffer = offer;
    }

}
