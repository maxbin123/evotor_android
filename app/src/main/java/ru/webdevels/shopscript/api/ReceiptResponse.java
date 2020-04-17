package ru.webdevels.shopscript.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReceiptResponse {

    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("data")
    @Expose
    public String message;

}