package com.me_market.android.memarket.models;

/**
 * Created by aederas on 29/12/2017.
 */

public class ShoppingListItem {
    private Integer position;
    public String productId;
    public String productName;
    public String productType;
    public Float quantity;
    public Boolean checked;

    public ShoppingListItem(String productId, String productName, String productType, Float quantity, Boolean checked) {
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.checked = checked;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
