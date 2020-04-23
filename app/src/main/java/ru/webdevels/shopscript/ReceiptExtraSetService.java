package ru.webdevels.shopscript;

import android.app.Service;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sentry.core.Sentry;
import ru.evotor.framework.core.IntegrationService;
import ru.evotor.framework.core.action.event.receipt.changes.position.IPositionChange;
import ru.evotor.framework.core.action.event.receipt.changes.position.SetExtra;
import ru.evotor.framework.core.action.event.receipt.discount.ReceiptDiscountEvent;
import ru.evotor.framework.core.action.event.receipt.discount.ReceiptDiscountEventProcessor;
import ru.evotor.framework.core.action.event.receipt.discount.ReceiptDiscountEventResult;
import ru.evotor.framework.core.action.processor.ActionProcessor;
import ru.evotor.framework.receipt.Receipt;
import ru.evotor.framework.receipt.ReceiptApi;

public class ReceiptExtraSetService extends IntegrationService {

    @Nullable
    @Override
    protected Map<String, ActionProcessor> createProcessors() {
        Map<String, ActionProcessor> map = new HashMap<>();
        map.put(ReceiptDiscountEvent.NAME_SELL_RECEIPT, new ReceiptDiscountEventProcessor() {
            @Override
            public void call(@NonNull String action, @NonNull ReceiptDiscountEvent event, @NonNull Callback callback) {
                try {
                    Receipt receipt = ReceiptApi.getReceipt(getApplicationContext(), Receipt.Type.SELL);
                    String current_extra = receipt.getHeader().getExtra();
                    JSONObject object = new JSONObject();
                    if (current_extra != null) {
                        object = new JSONObject(current_extra);
                    }
                    if (object.has("new_order")) {
                        showToast("Заказ НЕ БУДЕТ выгружен в Shop-Script");
                        object.remove("new_order");
                    } else {
                        object.put("new_order", true);
                        showToast("Заказ БУДЕТ выгружен в Shop-Script");
                    }
                    SetExtra extra = new SetExtra(object);
                    List<IPositionChange> listOfChanges = new ArrayList<>();
                    callback.onResult(
                            new ReceiptDiscountEventResult(
                                    receipt.getDiscount(),
                                    extra,
                                    listOfChanges
                            ));
                } catch (JSONException | RemoteException e) {
                    e.printStackTrace();
                    Sentry.captureException(e);
                }
            }
        });
        return map;
    }

    void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}