package ru.webdevels.shopscript.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiEndpointInterface {
    @GET("evotor/orders/{order_id}")
    Call<OrdersResponse> getOrder(@Path("order_id") String order_id);

    @GET("evotor/orders")
    Call<OrdersResponse> getOrders();

    @GET("evotor/receipts/{receipt_id}")
    Call<ReceiptResponse> getStatus(@Path("receipt_id") String receipt_id);
}
