package ru.webdevels.shopscript.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrdersResponse {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("data")
    @Expose
    public final List<Order> data = null;

}