package ru.webdevels.shopscript;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.evotor.framework.receipt.event.ReceiptCompletedEvent;
import ru.evotor.framework.receipt.event.handler.receiver.SellReceiptBroadcastReceiver;
import ru.webdevels.shopscript.api.ApiClient;
import ru.webdevels.shopscript.api.ApiEndpointInterface;
import ru.webdevels.shopscript.api.ReceiptResponse;

public class ClosedReceiptsReceiver extends SellReceiptBroadcastReceiver {
    @Override
    protected void handleReceiptCompletedEvent(@NotNull Context context, @NotNull ReceiptCompletedEvent event) {
        String receipt_uuid = event.getReceiptUuid();
        ApiEndpointInterface apiService = ApiClient.getClient(context).create(ApiEndpointInterface.class);
        Call<ReceiptResponse> call = apiService.getStatus(receipt_uuid);
        Handler handler = new Handler();
        handler.postDelayed(() -> call.enqueue(new Callback<ReceiptResponse>() {
            @Override
            public void onResponse(Call<ReceiptResponse> call1, Response<ReceiptResponse> response) {
                Log.v("ru.webdevels.shopscript", response.toString());
                ReceiptResponse api = response.body();
                assert api != null;
                String result = api.message;
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ReceiptResponse> call1, Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }), 10000);   //15 seconds
    }
}
