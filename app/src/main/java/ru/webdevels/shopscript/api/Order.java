package ru.webdevels.shopscript.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.sentry.core.Sentry;
import ru.evotor.framework.component.PaymentPerformer;
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd;
import ru.evotor.framework.core.action.event.receipt.changes.position.SetExtra;
import ru.evotor.framework.receipt.Payment;
import ru.evotor.framework.receipt.Position;
import ru.evotor.framework.receipt.position.SettlementMethod;

public class Order {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("state_id")
    @Expose
    public String stateId;
    @SerializedName("total")
    @Expose
    public BigDecimal total;
    @SerializedName("tax")
    @Expose
    public String tax;
    @SerializedName("shipping")
    @Expose
    public String shipping;
    @SerializedName("discount")
    @Expose
    public BigDecimal discount;
    @SerializedName("contact")
    @Expose
    public Contact contact;
    @SerializedName("id_encoded")
    @Expose
    public String idEncoded;
    @SerializedName("products")
    @Expose
    public Object products;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("total_str")
    @Expose
    private String totalStr;
    @SerializedName("create_datetime_str")
    @Expose
    public String createDatetimeStr;
    @SerializedName("style")
    @Expose
    private String style;
    @SerializedName("encoded_state_id")
    @Expose
    public String encodedStateId;
    @SerializedName("payment_id")
    @Expose
    public String paymentId = "";
    @SerializedName("items")
    @Expose
    private final List<Item> items = null;

    private String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static Order fromJson(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, Order.class);
    }

    public String getId() {
        return id;
    }

    public HashMap<String, String> getOrderStyles() {
        HashMap<String, String> styles = new HashMap<>();
        String[] parts = style.split(";");
        for (String part : parts) {
            String[] str = part.split(":");
            styles.put(str[0], str[1]);
        }
        return styles;
    }

    public String getOrderTotalString() {
        return totalStr.replaceAll("<.*?>", "");
    }

    public List<Position> getPositionList(SettlementMethod settlementMethod) {
        List<Position> positionList = new ArrayList<>();
        for (Item item : items) {
//            positionList.addAll(item.getPositions(settlementMethod));
            positionList.add(item.getPosition(settlementMethod));
        }
        return positionList;
    }

    public List<PositionAdd> getPositionAddList(SettlementMethod settlementMethod) {
        List<PositionAdd> positionAddList = new ArrayList<>();
        for (Item item : items) {
            PositionAdd positionAdd = item.getPositionAdd(settlementMethod);
            positionAddList.add(positionAdd);
        }
        return positionAddList;
    }

    public HashMap<Payment, BigDecimal> buildPayment(PaymentPerformer paymentPerformer) {
        HashMap<Payment, BigDecimal> payments = new HashMap<>();
        payments.put(new Payment(
                UUID.randomUUID().toString(),
                total,
                null,
                paymentPerformer,
                null,
                null,
                null
        ), total);
        return payments;
    }

    public SetExtra getExtra() {
        String json = toJson();
        try {
            JSONObject object = new JSONObject(json);
            return new SetExtra(object);
        } catch (JSONException e) {
            e.printStackTrace();
            Sentry.captureException(e);
        }
        return new SetExtra(new JSONObject());
    }
}