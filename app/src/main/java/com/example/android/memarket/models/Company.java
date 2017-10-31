package com.example.android.memarket.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Arturo Deras on 7/9/2017.
 */
@IgnoreExtraProperties
public class Company {
    public  String Id;
    public String Name;
    public String Type;

    public Company() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Company(String companyName, String companyType) {

        this.Name = companyName;
        this.Type = companyType;

    }

    public Company(String companyId, String companyName, String companyType) {

        this.Id = companyId;
        this.Name = companyName;
        this.Type = companyType;

    }

}
