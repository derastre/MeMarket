package com.example.android.memarket.components;

import com.example.android.memarket.models.Company;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Arturo Deras on 12/11/2017.
 */

public class FirebaseMethods {

    public void writeNewCompanyOnFirebase(String name, String type){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Company company= new Company(name,type);
        myRef.child("companies").push().setValue(company);

    }
}
