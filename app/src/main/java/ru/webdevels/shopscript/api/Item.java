package ru.webdevels.shopscript.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.UUID;

import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd;
import ru.evotor.framework.receipt.Position;
import ru.evotor.framework.receipt.position.SettlementMethod;

class Item {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("product_id")
    @Expose
    public String productId;
    @SerializedName("sku_id")
    @Expose
    public String skuId;
    @SerializedName("sku_code")
    @Expose
    public String skuCode;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("service_id")
    @Expose
    private Object serviceId;
    @SerializedName("price")
    @Expose
    private BigDecimal price;
    @SerializedName("quantity")
    @Expose
    private BigDecimal quantity;
    @Expose
    public String purchasePrice;
    @SerializedName("total_discount")
    @Expose
    private BigDecimal totalDiscount;
    @SerializedName("tax_percent")
    @Expose
    public Object taxPercent;
    @SerializedName("tax_included")
    @Expose
    public String taxIncluded;
    @SerializedName("expected_product_code_blocks_count")
    @Expose
    public String expectedProductCodeBlocksCount;
    @SerializedName("evotor_uuid")
    @Expose
    public String evotorUuid;



    Position getPosition(SettlementMethod settlementMethod) {
        Position.Builder builder = Position.Builder.newInstance(
                UUID.randomUUID().toString(),
                evotorUuid,
                name,
                "шт",
                0,
                price,
                quantity
        );
//        builder.setMark(mark);
        builder.setPriceWithDiscountPosition(getPriceWithDiscountPosition());
        if (serviceId != null) {
            builder.toService();
        }
        builder.setSettlementMethod(settlementMethod);
        return builder.build();
    }

    PositionAdd getPositionAdd(SettlementMethod settlementMethod) {
        Position position = getPosition(settlementMethod);
        return new PositionAdd(position);
    }

    private BigDecimal getPriceWithDiscountPosition() {
        return price.subtract(totalDiscount.divide(quantity));
    }

}