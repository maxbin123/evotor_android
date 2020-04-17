package ru.webdevels.shopscript.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
//    @SerializedName("summary")
//    @Expose
//    public String summary;
//    @SerializedName("type_id")
//    @Expose
//    public String typeId;
//    @SerializedName("image_id")
//    @Expose
//    public String imageId;
//    @SerializedName("image_filename")
//    @Expose
//    public String imageFilename;
    @SerializedName("sku_id")
    @Expose
    public String skuId;
//    @SerializedName("ext")
//    @Expose
//    public String ext;
//    @SerializedName("url")
//    @Expose
//    public String url;
//    @SerializedName("rating")
//    @Expose
//    public Integer rating;
//    @SerializedName("rating_count")
//    @Expose
//    public String ratingCount;
//    @SerializedName("currency")
//    @Expose
//    public String currency;
    @SerializedName("tax_id")
    @Expose
    public String taxId;
//    @SerializedName("cross_selling")
//    @Expose
//    public Object crossSelling;
//    @SerializedName("upselling")
//    @Expose
//    public Object upselling;
//    @SerializedName("category_id")
//    @Expose
//    public String categoryId;
//    @SerializedName("badge")
//    @Expose
//    public String badge;
//    @SerializedName("sku_type")
//    @Expose
//    public String skuType;
//    @SerializedName("unconverted_currency")
//    @Expose
//    public String unconvertedCurrency;
//    @SerializedName("rating_html")
//    @Expose
//    public String ratingHtml;
//    @SerializedName("images")
//    @Expose
//    public Object images;
//    @SerializedName("image")
//    @Expose
//    public Object image;
//    @SerializedName("image_crop_small")
//    @Expose
//    public Object imageCropSmall;
//    @SerializedName("frontend_url")
//    @Expose
//    public String frontendUrl;

}