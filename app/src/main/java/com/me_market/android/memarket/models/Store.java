package com.me_market.android.memarket.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Arturo Deras on 7/9/2017.
 */
@IgnoreExtraProperties
public class Store implements Parcelable,Serializable{

    private String Id;
    public String Name;
    public String Address;
    public String Phone;
    public Company CompanyData;




    public Store() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Store(String Name, String Address, String Phone, Company CompanyData) {

        this.Name = Name;
        this.Address = Address;
        this.Phone = Phone;
        this.CompanyData = CompanyData;
    }

    public Store(Parcel in) {
        readFromParcel(in);
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public static final Parcelable.Creator<Store> CREATOR = new Parcelable.Creator<Store>(){
        public Store createFromParcel(Parcel in){
            return new Store(in);
        }

        public Store[] newArray(int size){
            return new Store[size];
        }

    };
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(Id);
        parcel.writeString(Name);
        parcel.writeString(Address);
        parcel.writeString(Phone);
        parcel.writeParcelable(CompanyData,flags);
    }

    private void readFromParcel(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        Address = in.readString();
        Phone = in.readString();
        CompanyData = in.readParcelable(Company.class.getClassLoader());
    }
}
