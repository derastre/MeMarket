package com.example.android.memarket.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Arturo Deras on 7/9/2017.
 */
@IgnoreExtraProperties
public class Store{
    public String Id;
    public String Name;
    public String Address;
    public String Phone;
    public String CompanyId;




    public Store() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Store(String storeName, String storeAddress, String storePhone, String storeCompanyId) {

        this.Name = storeName;
        this.Address = storeAddress;
        this.Phone = storePhone;
        this.CompanyId = storeCompanyId;
    }

    public Store(String storeId, String storeName, String storeAddress, String storePhone, String storeCompanyId) {
        this.Id = storeId;
        this.Name = storeName;
        this.Address = storeAddress;
        this.Phone = storePhone;
        this.CompanyId = storeCompanyId;
    }

}
