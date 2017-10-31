package com.example.android.memarket.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Arturo Deras on 20/8/2017.
 */
@IgnoreExtraProperties
public class Product {

    public String Id;
    public String Name;
    public String Type;
    public String Brand;
    public String Quantity;
    public String Units;

    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Product(String ProductName, String ProductType, String ProductBrand, String ProductQuantity, String ProductUnits) {

        this.Name = ProductName;
        this.Type = ProductType;
        this.Brand = ProductBrand;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }

    public Product(String ProductId, String ProductName, String ProductType, String ProductBrand, String ProductQuantity, String ProductUnits) {

        this.Id = ProductId;
        this.Name = ProductName;
        this.Type = ProductType;
        this.Brand = ProductBrand;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }
}