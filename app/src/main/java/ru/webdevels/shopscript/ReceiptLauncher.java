package ru.webdevels.shopscript;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.webdevels.shopscript.api.ApiClient;
import ru.webdevels.shopscript.api.ApiEndpointInterface;
import ru.webdevels.shopscript.api.Order;
import ru.webdevels.shopscript.api.OrdersResponse;

public class ReceiptLauncher extends AppCompatActivity {

    private String settlement;
    private String action;
    private String payment;
    private boolean display;
    private ProgressBar progressBar;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_launcher);
        setTitle("Параметры чека");

        Bundle extras = getIntent().getExtras();

        String order_id = extras.getString("order_id");
        display = extras.getString("display", "true").equals("true"); // TODO
        settlement = extras.getString("settlement", "fullSettlement");
        payment = extras.getString("payment", "electron");
        action = extras.getString("action", "open");
        type = extras.getString("type", "sell");

        assert order_id != null;

        progressBar = findViewById(R.id.progressBarReceipt);
        progressBar.setVisibility(View.VISIBLE);

        ApiEndpointInterface apiService = ApiClient.getClient(ReceiptLauncher.this).create(ApiEndpointInterface.class);
        Call<OrdersResponse> call = apiService.getOrder(order_id);
        call.enqueue(new Callback<OrdersResponse>() {
            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                OrdersResponse ordersResponse = response.body();
                List<Order> orderList = ordersResponse.data;
                Order order = orderList.get(0);
                processOrder(order);
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                Log.v("TAG", "Response = " + t.toString());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void processOrder(Order order) {
        if (display) {
            LinearLayout form = findViewById(R.id.action_form);
            RadioGroup typeGroup = findViewById(R.id.type);
            RadioGroup settlementGroup = findViewById(R.id.settlement);
            RadioGroup paymentGroup = findViewById(R.id.payment);
            RadioGroup actionGroup = findViewById(R.id.action);

            progressBar.setVisibility(View.GONE);
            RadioButton email = findViewById(R.id.email);
            if (order.contact.email.isEmpty()) {
                email.setEnabled(false);
            } else {
                email.setText(String.format("Отправить на email (%s)", order.contact.email));
            }

            RadioButton phone = findViewById(R.id.phone);
            if (order.contact.phone.isEmpty()) {
                phone.setEnabled(false);
            } else {
                phone.setText(String.format("Отправить по СМС (%s)", order.contact.phone));
            }

            actionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.open) {
                        paymentGroup.setVisibility(View.INVISIBLE);
                        findViewById(R.id.paymentText).setVisibility(View.INVISIBLE);
                    } else {
                        paymentGroup.setVisibility(View.VISIBLE);
                        findViewById(R.id.paymentText).setVisibility(View.VISIBLE);
                    }
                }
            });

            if (!order.paymentId.equals("cash")) {
                RadioButton electron = findViewById(R.id.electron);
                electron.setChecked(true);
            }
            form.setVisibility(View.VISIBLE);
            Button run = findViewById(R.id.run);
            run.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (typeGroup.getCheckedRadioButtonId()) {
                        case R.id.sell:
                            type = "sell";
                            break;
                        case R.id.payback:
                            type = "payback";
                            break;
                    }
                    switch (settlementGroup.getCheckedRadioButtonId()) {
                        case R.id.fullSettlement:
                            settlement = "fullSettlement";
                            break;
                        case R.id.fullPrepayment:
                            settlement = "fullPrepayment";
                            break;
                        case R.id.advancePayment:
                            settlement = "advancePayment";
                            break;
                    }
                    switch (paymentGroup.getCheckedRadioButtonId()) {
                        case R.id.cash:
                            payment = "cash";
                            break;
                        case R.id.electron:
                            payment = "electron";
                            break;
                        case R.id.advance:
                            payment = "advance";
                            break;
                    }
                    switch (actionGroup.getCheckedRadioButtonId()) {
                        case R.id.open:
                            action = "open";
                            break;
                        case R.id.email:
                            action = "email";
                            break;
                        case R.id.phone:
                            action = "phone";
                            break;
                    }
                    form.setVisibility(View.GONE);
                    ReceiptLauncher.this.setTitle("Подготовка чека...");
                    MyReceipt receipt = new MyReceipt(ReceiptLauncher.this, order, type, action, settlement, payment);
                    receipt.process();
                }
            });
        } else {
            ReceiptLauncher.this.setTitle("Подготовка чека...");
            MyReceipt receipt = new MyReceipt(ReceiptLauncher.this, order, type, action, settlement, payment);
            receipt.process();
        }
    }
}
