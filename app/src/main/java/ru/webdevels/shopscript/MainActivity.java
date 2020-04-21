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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.sentry.core.Sentry;
import io.sentry.core.protocol.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.webdevels.shopscript.api.ApiClient;
import ru.webdevels.shopscript.api.ApiEndpointInterface;
import ru.webdevels.shopscript.api.Order;
import ru.webdevels.shopscript.api.OrdersResponse;

public class MainActivity extends AppCompatActivity {

    private List<Order> orderList;
    private RecyclerAdapter recyclerAdapter;
    private SwipeRefreshLayout swipeContainer;

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

        // sentry
        try {
            URL frontendUrl = new URL(base_url);
            String host = frontendUrl.getHost();
            User sentryUser = new User();
            sentryUser.setUsername(host);
            Sentry.setUser(sentryUser);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // recycler
        orderList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(this, orderList);
        recyclerView.setAdapter(recyclerAdapter);

        // Swipe to refresh
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setRefreshing(true);
        swipeContainer.setOnRefreshListener(this::updateList);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        updateList();
    }

    private void updateList() {
        // retrofit
        ApiEndpointInterface apiService = ApiClient.getClient(this).create(ApiEndpointInterface.class);
        Call<OrdersResponse> call = apiService.getOrders();

        call.enqueue(new Callback<OrdersResponse>() {
            @Override
            public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                OrdersResponse ordersResponse = response.body();
                orderList = Objects.requireNonNull(ordersResponse).data;
                recyclerAdapter.setOrderList(orderList);
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<OrdersResponse> call, Throwable t) {
                Log.v("TAG", "Response = " + t.toString());
                swipeContainer.setRefreshing(false);
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
