package ru.webdevels.shopscript;

import android.content.SharedPreferences;
import android.os.RemoteException;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sentry.core.Sentry;
import ru.evotor.devices.commons.DeviceServiceConnector;
import ru.evotor.devices.commons.exception.DeviceServiceException;
import ru.evotor.devices.commons.exception.ServiceNotConnectedException;
import ru.evotor.devices.commons.printer.printable.IPrintable;
import ru.evotor.devices.commons.printer.printable.PrintableText;
import ru.evotor.framework.core.IntegrationService;
import ru.evotor.framework.core.action.event.receipt.changes.receipt.print_extra.SetPrintExtra;
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEvent;
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEventProcessor;
import ru.evotor.framework.core.action.event.receipt.print_extra.PrintExtraRequiredEventResult;
import ru.evotor.framework.core.action.processor.ActionProcessor;
import ru.evotor.framework.receipt.Position;
import ru.evotor.framework.receipt.Receipt;
import ru.evotor.framework.receipt.ReceiptApi;
import ru.evotor.framework.receipt.position.SettlementMethod;
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePositionFooter;
import ru.evotor.framework.receipt.print_extras.PrintExtraPlacePrintGroupHeader;

public class ReceiptPrintService extends IntegrationService {
    @Nullable
    @Override
    protected Map<String, ActionProcessor> createProcessors() {
        Map<String, ActionProcessor> map = new HashMap<>();
        map.put(PrintExtraRequiredEvent.NAME_SELL_RECEIPT, new PrintExtraRequiredEventProcessor() {
            @Override
            public void call(@NotNull String s, @NotNull PrintExtraRequiredEvent printExtraRequiredEvent, @NotNull Callback callback) {
                List<SetPrintExtra> setPrintExtras = new ArrayList<>();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean printOrderNumber = sharedPreferences.getBoolean("print_order_number", false);
                boolean printMark = sharedPreferences.getBoolean("print_mark", false);


                Receipt receipt = ReceiptApi.getReceipt(ReceiptPrintService.this, Receipt.Type.SELL);
                List<Position> positionList = receipt.getPositions();
                String extra = receipt.getHeader().getExtra();


                try {
                    JSONObject order = new JSONObject(extra);
                    if (printOrderNumber && order.has("id_str")) {
                        setPrintExtras.add(new SetPrintExtra(
                                new PrintExtraPlacePrintGroupHeader(null),
                                new IPrintable[]{
                                        new PrintableText("        ЗАКАЗ " + order.getString("id_str")),
                                }
                        ));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Sentry.captureException(e);
                }

                if (printMark) {
                    for (Position position : positionList) {
                        if (position.getMark() != null) {
                            setPrintExtras.add(new SetPrintExtra(
                                    new PrintExtraPlacePositionFooter(position.getUuid()),
                                    new IPrintable[]{
                                            new PrintableText(position.getMark())
                                    }
                            ));
                        }
                    }
                }
                try {
                    callback.onResult(new PrintExtraRequiredEventResult(setPrintExtras).toBundle());
                } catch (RemoteException exc) {
                    exc.printStackTrace();
                    Sentry.captureException(exc);
                }
            }
        });
        return map;
    }
}
