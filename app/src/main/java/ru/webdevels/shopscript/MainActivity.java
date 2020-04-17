package ru.webdevels.shopscript;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.webdevels.shopscript.api.ApiClient;
import ru.webdevels.shopscript.api.ApiEndpointInterface;
import ru.webdevels.shopscript.api.Order;
import ru.webdevels.shopscript.api.OrdersResponse;

public class MainActivity extends AppCompatActivity {

    List<Order> orderList;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String base_url = sharedPreferences.getString("frontend_url", "");
        if (base_url.isEmpty()) {
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        // loader
        progressBar = findViewById(R.id.progressBar);

        // recycler
        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(this, orderList);
        recyclerView.setAdapter(recyclerAdapter);

        // retrofit
        ApiEndpointInterface apiService = ApiClient.getClient(this).create(ApiEndpointInterface.class);
        Call<OrdersResponse> call = apiService.getOrders();

        call.enqueue(new Callback<OrdersResponse>() {
            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                OrdersResponse ordersResponse = response.body();
                orderList = ordersResponse.data;
                recyclerAdapter.setOrderList(orderList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                Log.v("TAG", "Response = " + t.toString());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
