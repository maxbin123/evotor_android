package ru.webdevels.shopscript;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ru.evotor.devices.commons.DeviceServiceConnector;
import ru.evotor.devices.commons.exception.DeviceServiceException;
import ru.evotor.devices.commons.printer.PrinterDocument;
import ru.evotor.devices.commons.printer.printable.IPrintable;
import ru.evotor.devices.commons.printer.printable.PrintableText;
import ru.evotor.framework.component.PaymentPerformer;
import ru.evotor.framework.core.Error;
import ru.evotor.framework.core.IntegrationException;
import ru.evotor.framework.core.IntegrationManagerCallback;
import ru.evotor.framework.core.IntegrationManagerFuture;
import ru.evotor.framework.core.action.command.open_receipt_command.OpenSellReceiptCommand;
import ru.evotor.framework.core.action.command.print_receipt_command.PrintReceiptCommandResult;
import ru.evotor.framework.core.action.command.print_receipt_command.PrintSellReceiptCommand;
import ru.evotor.framework.core.action.command.print_z_report_command.PrintZReportCommand;
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd;
import ru.evotor.framework.navigation.NavigationApi;
import ru.evotor.framework.payment.PaymentSystem;
import ru.evotor.framework.payment.PaymentType;
import ru.evotor.framework.receipt.Payment;
import ru.evotor.framework.receipt.Position;
import ru.evotor.framework.receipt.PrintGroup;
import ru.evotor.framework.receipt.Receipt;
import ru.evotor.framework.receipt.ReceiptApi;
import ru.evotor.framework.receipt.position.SettlementMethod;
import ru.webdevels.shopscript.api.Order;

class MyReceipt {

    private final Activity activity;
    private final Order order;
    private final String action;
    private final SettlementMethod settlementMethod;
    private final PaymentPerformer paymentPerformer;

    private String uuid;

    public MyReceipt(Activity activity, Order order, String action, String settlement, String payment) {
        this.activity = activity;
        this.order = order;
        this.action = action;
        this.settlementMethod = getSettlementMethod(settlement);
        this.paymentPerformer = getPaymentPerformer(payment);
    }

    public void process() {
        if (action.equals("open")) {
            openReceipt();
        } else {
            printReceipt();
        }
    }

    private void openReceipt() {
        List<PositionAdd> positionAddList = order.getPositionAddList(settlementMethod);

        new OpenSellReceiptCommand(positionAddList, order.getExtra()).process(
                activity,
                new IntegrationManagerCallback() {
                    @Override
                    public void run(IntegrationManagerFuture future) {
                        try {
                            IntegrationManagerFuture.Result result = future.getResult();
                            if (result.getType() == IntegrationManagerFuture.Result.Type.OK) {
                                activity.startActivity(NavigationApi.createIntentForSellReceiptPayment());
                            }
                        } catch (IntegrationException e) {
                            e.printStackTrace();
                        }
                        activity.finish();
                    }
                }
        );
    }

    private void printReceipt() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean print = sharedPreferences.getBoolean("print_internet", false);
        HashMap<Payment, BigDecimal> payment = order.buildPayment(paymentPerformer);
        PrintGroup printGroup = new PrintGroup(UUID.randomUUID().toString(),
                PrintGroup.Type.CASH_RECEIPT, null, null, null, null, print, null, null);
        Receipt.PrintReceipt printReceipt = new Receipt.PrintReceipt(
                printGroup,
                order.getPositionList(settlementMethod),
                payment,
                new HashMap<>(), new HashMap<>()
        );
        ArrayList<Receipt.PrintReceipt> listDocs = new ArrayList<>();
        listDocs.add(printReceipt);

        new PrintSellReceiptCommand(listDocs, order.getExtra(), order.contact.phone, order.contact.email, BigDecimal.ZERO, null, null).process(activity, new IntegrationManagerCallback() {
            @Override
            public void run(IntegrationManagerFuture integrationManagerFuture) {

                try {
                    IntegrationManagerFuture.Result result = integrationManagerFuture.getResult();
                    switch (result.getType()) {
                        case OK:
                            PrintReceiptCommandResult printSellReceiptResult = PrintReceiptCommandResult.create(result.getData());
                            uuid = printSellReceiptResult.getReceiptUuid();
                            Toast.makeText(activity, "Чек успешно отправлен покупателю", Toast.LENGTH_LONG).show();
                            printOrderInfo();
                            activity.finish();
                            break;
                        case ERROR:
                            if (result.getError().getCode() == PrintReceiptCommandResult.ERROR_CODE_SESSION_TIME_EXPIRED) {
                                new PrintZReportCommand().process(activity, new IntegrationManagerCallback() {
                                    @Override
                                    public void run(IntegrationManagerFuture future) {
                                        printReceipt();
                                    }
                                });
                            } else {
                                Toast.makeText(activity, result.getError().getMessage(), Toast.LENGTH_LONG).show();
                                activity.finish();
                            }
                            break;
                    }
                } catch (IntegrationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printReceiptNew() {
        List<Position> positionList = order.getPositionList(settlementMethod);
        List<Payment> payments = new ArrayList<>();
        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                order.total,
                null,
                paymentPerformer,
                null,
                null,
                null
        );
        payments.add(payment);
        PrintSellReceiptCommand command = new PrintSellReceiptCommand(positionList, payments, order.contact.phone, order.contact.email, null, null);
        command.process(activity, new IntegrationManagerCallback() {
            @Override
            public void run(IntegrationManagerFuture future) {
                IntegrationManagerFuture.Result result = null;
                try {
                    result = future.getResult();
                    switch (result.getType()) {
                        case OK:
                            PrintReceiptCommandResult printSellReceiptResult = PrintReceiptCommandResult.create(result.getData());
                            uuid = printSellReceiptResult.getReceiptUuid();
                            Toast.makeText(activity, "Чек успешно отправлен покупателю", Toast.LENGTH_LONG).show();
                            printReport();
                            break;
                        case ERROR:
                            Error error = result.getError();
                            Toast.makeText(activity, error.getMessage(), Toast.LENGTH_LONG).show();
                            break;
                    }
                } catch (IntegrationException e) {
                    e.printStackTrace();
                }
                activity.finish();
            }
        });
    }

    private SettlementMethod getSettlementMethod(String settlement) {
        switch (settlement) {
            /*
              Предоплата 100% – полная предварительная оплата до момента передачи предмета расчёта.
             */
            case "fullPrepayment":
                return new SettlementMethod.FullPrepayment();
            /*
              Предоплата – частичная предварительная оплата до момента передачи предмета расчёта.
             */
            case "partialPrepayment":
                return new SettlementMethod.PartialPrepayment();
            /*
              Полный расчёт – полная оплата, в том числе с учётом аванса (предварительной оплаты) в момент передачи предмета расчёта.
             */
            case "fullSettlement":
                return new SettlementMethod.FullSettlement();
            /*
              Аванс.
             */
            case "advancePayment":
                return new SettlementMethod.AdvancePayment();
             /*
              Полный расчёт – полная оплата, в том числе с учётом аванса (предварительной оплаты) в момент передачи предмета расчёта.
             */
            default:
                return new SettlementMethod.FullSettlement();
        }
    }

    private PaymentPerformer getPaymentPerformer(String payment) {
        switch (payment) {
            /*
             * Наличными средствами
             */
            case "cash":
                return new PaymentPerformer(
                        new PaymentSystem(PaymentType.CASH, "Наличные", "ru.evotor.paymentSystem.cash.base"),
                        null,
                        null,
                        null,
                        "Наличные"
                );
            /*
             * Безналичными средствами
             */
            case "electron":
                return new PaymentPerformer(
                        new PaymentSystem(PaymentType.ELECTRON, "Банковская карта", "ru.evotor.paymentSystem.cashless.base"),
                        null,
                        null,
                        null,
                        "Банковская карта"
                );
            /*
              Предоплатой (зачетом аванса)
             */
            case "advance":
                return new PaymentPerformer(
                        new PaymentSystem(PaymentType.ADVANCE, "По предоплате", "ru.evotor.payment.prepayment"),
                        "ru.evotor.payment.prepayment",
                        "ru.evotor.payment.prepayment.service.PrepaymentPaymentType",
                        "9e1cf6f4-b676-4951-8dd5-8a7db1f686d6",
                        "По предоплате"
                );
            /*
              Постоплатой (в кредит)
             */
//            case "credit":
//                return PaymentType.CREDIT;
             /*
              Неизвестно. По-умолчанию
             */
            default:
                return new PaymentPerformer(
                        new PaymentSystem(PaymentType.ELECTRON, "Банковская карта", "ru.evotor.paymentSystem.cashless.base"),
                        null,
                        null,
                        null,
                        "Банковская карта"
                );
        }
    }

    private void printReport() {
        DeviceServiceConnector.startInitConnections(activity);
        new Thread() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                try {
                    Receipt receipt = ReceiptApi.getReceipt(activity, uuid);
                    List<Position> positionList = receipt.getPositions();
                    List<IPrintable> printList = new ArrayList<>();
                    int max_len = DeviceServiceConnector.getPrinterService().getAllowableSymbolsLineLength(ru.evotor.devices.commons.Constants.DEFAULT_DEVICE_INDEX);

                    printList.add(new PrintableText(center("КОПИЯ ЧЕКА - ИНТЕРНЕТ-МАГАЗИН", max_len)));
                    printList.add(new PrintableText(center("ПРИХОД", max_len)));
                    printList.add(new PrintableText(center(String.format("Продажа №%s", receipt.getHeader().getNumber()), max_len)));
                    printList.add(new PrintableText(center(String.format("ЗАКАЗ %s", order.idStr), max_len)));
                    for (Position position : positionList) {
                        printList.add(new PrintableText(position.getName()));
                        printList.add(new PrintableText(String.format("%.0f X %.2f = %.2f", position.getQuantity(), position.getPrice(), position.getTotalWithoutDiscounts())));
                        if (position.getDiscountPositionSum().compareTo(BigDecimal.ZERO) > 0) {
                            printList.add(new PrintableText(String.format("СКИДКА: %.2f", position.getDiscountPositionSum())));
                        }
                    }
                    printList.add(new PrintableText(String.format("ИТОГ: %.2f", order.total)));
                    printList.add(new PrintableText(getSettlementName()));
                    if (receipt.getHeader().getClientEmail() != null) {
                        printList.add(new PrintableText(String.format("Email: %s", receipt.getHeader().getClientEmail())));
                    }
                    if (receipt.getHeader().getClientPhone() != null) {
                        printList.add(new PrintableText(String.format("Телефон: %s", receipt.getHeader().getClientPhone())));
                    }

                    DeviceServiceConnector.getPrinterService().printDocument(
                            ru.evotor.devices.commons.Constants.DEFAULT_DEVICE_INDEX,
                            new PrinterDocument(printList.toArray(new IPrintable[printList.size()])));

                } catch (DeviceServiceException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private void printOrderInfo() {
        DeviceServiceConnector.startInitConnections(activity);
        new Thread() {
            @Override
            public void run() {
                try {
                    Receipt receipt = ReceiptApi.getReceipt(activity, uuid);
                    List<IPrintable> printList = new ArrayList<>();
                    int max_len = DeviceServiceConnector.getPrinterService().getAllowableSymbolsLineLength(ru.evotor.devices.commons.Constants.DEFAULT_DEVICE_INDEX);
                    printList.add(new PrintableText(center(String.format("ЗАКАЗ %s", order.idStr), max_len)));
                    printList.add(new PrintableText(center(String.format("Продажа №%s", receipt.getHeader().getNumber()), max_len)));
                    printList.add(new PrintableText(String.format("ИТОГ: %.2f", order.total)));
                    printList.add(new PrintableText(getSettlementName()));
                    if (receipt.getHeader().getClientEmail() != null) {
                        printList.add(new PrintableText(String.format("Email: %s", receipt.getHeader().getClientEmail())));
                    }
                    if (receipt.getHeader().getClientPhone() != null) {
                        printList.add(new PrintableText(String.format("Телефон: %s", receipt.getHeader().getClientPhone())));
                    }
                    DeviceServiceConnector.getPrinterService().printDocument(
                            ru.evotor.devices.commons.Constants.DEFAULT_DEVICE_INDEX,
                            new PrinterDocument(printList.toArray(new IPrintable[printList.size()])));
                } catch (DeviceServiceException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    private String getSettlementName() {
        if (settlementMethod instanceof SettlementMethod.FullSettlement) {
            return "ПОЛНЫЙ РАСЧЕТ";
        }
        if (settlementMethod instanceof SettlementMethod.FullPrepayment) {
            return "ПРЕДОПЛАТА 100%";
        }
        if (settlementMethod instanceof SettlementMethod.AdvancePayment) {
            return "АВАНС";
        }
        return "";
    }

    private static String center(String text, int len) {
        String out = String.format("%" + len + "s%s%" + len + "s", "", text, "");
        float mid = out.length() / 2;
        float start = mid - (len / 2);
        float end = start + len;
        return out.substring((int) start, (int) end);
    }
}
