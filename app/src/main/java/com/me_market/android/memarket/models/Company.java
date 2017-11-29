package com.me_market.android.memarket.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Arturo Deras on 7/9/2017.
 */
@IgnoreExtraProperties
public class Company implements Parcelable,Serializable {
    private String Id;
    public String Name;
    public String Type;

    public Company() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Company(String companyName, String companyType) {
        this.Name = companyName;
        this.Type = companyType;
    }

    public Company(Parcel in) {
        readFromParcel(in);
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public static final Parcelable.Creator<Company> CREATOR = new Parcelable.Creator<Company>(){
      public Company createFromParcel(Parcel in){
        return new Company(in);
      }

      public Company[] newArray(int size){
          return new Company[size];
      }

    };
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id);
        parcel.writeString(Name);
        parcel.writeString(Type);
    }

    private void readFromParcel(Parcel in) {
        Id = in.readString();
        Name = in.readString();
        Type = in.readString();
    }
}
