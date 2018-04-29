package com.me_market.android.memarket.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Arturo Deras on 20/8/2017.
 */
@IgnoreExtraProperties
public class Product implements Serializable, Parcelable {

    private String Id;
    public String Barcode;
    public String Name;
    public String Type;
    public String Brand;
    public Float Quantity;
    public String Units;
    public Boolean hasChild;

    private Product ProductChild;
    private Float currentPrice;
    private Float currentOffer;
    private Boolean isOffer;


    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    public Product(String Barcode, String ProductName, String ProductType, String ProductBrand, Float ProductQuantity, String ProductUnits, Boolean ProductChild) {
        this.Barcode = Barcode;
        this.Name = ProductName;
        this.Type = ProductType;
        this.Brand = ProductBrand;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
        this.hasChild = ProductChild;
    }

    public Product(String Barcode, String ProductName, String ProductType, Float ProductQuantity, String ProductUnits) {
        this.Barcode = Barcode;
        this.Name = ProductName;
        this.Type = ProductType;
        this.Quantity = ProductQuantity;
        this.Units = ProductUnits;
    }

    public Product getProductChild() {
        return ProductChild;
    }

    public void setProductChild(Product productChild) {
        ProductChild = productChild;
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

    public Product(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id);
        parcel.writeString(Barcode);
        parcel.writeString(Name);
        parcel.writeString(Type);
        parcel.writeString(Brand);
        parcel.writeFloat(Quantity);
        parcel.writeString(Units);
    }

    private void readFromParcel(Parcel in) {
        Id = in.readString();
        Barcode = in.readString();
        Name = in.readString();
        Type = in.readString();
        Brand = in.readString();
        Quantity = in.readFloat();
        Units = in.readString();

    }

}

