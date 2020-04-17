package ru.webdevels.shopscript.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sku {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("product_id")
    @Expose
    public String productId;
    @SerializedName("sku")
    @Expose
    public String sku;
    @SerializedName("sort")
    @Expose
    public String sort;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("image_id")
    @Expose
    public Object imageId;
    @SerializedName("price")
    @Expose
    public String price;
    @SerializedName("primary_price")
    @Expose
    public String primaryPrice;
    @SerializedName("purchase_price")
    @Expose
    public String purchasePrice;
    @SerializedName("compare_price")
    @Expose
    public String comparePrice;
    @SerializedName("count")
    @Expose
    public Object count;
    @SerializedName("available")
    @Expose
    public String available;
    @SerializedName("dimension_id")
    @Expose
    public Object dimensionId;
    @SerializedName("file_name")
    @Expose
    public String fileName;
    @SerializedName("file_size")
    @Expose
    public String fileSize;
    @SerializedName("file_description")
    @Expose
    public Object fileDescription;
    @SerializedName("virtual")
    @Expose
    public String virtual;
    @SerializedName("evotor_uuid")
    @Expose
    public String evotorUuid;

}