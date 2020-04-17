package ru.webdevels.shopscript;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import ru.webdevels.shopscript.api.Order;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyviewHolder> {

    private final Activity activity;
    private List<Order> orderList;

    public RecyclerAdapter(Activity activity, List<Order> orderList) {
        this.activity = activity;
        this.orderList = orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.recyclerview_adapter,parent,false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {
        Order order = orderList.get(position);

        String title = String.format("%s [%s]", order.idEncoded, order.encodedStateId);
        holder.order_number.setText(title);
        holder.customer_name.setText(order.contact.name);
        holder.total_str.setText(order.getOrderTotalString());
        holder.create_datetime_str.setText(order.createDatetimeStr);

        HashMap<String, String> styles = order.getOrderStyles();
        if (styles.containsKey("color")) {
            holder.order_number.setTextColor(Color.parseColor(styles.get("color")));
        }
        if (styles.containsKey("font-weight")) {
            holder.order_number.setTypeface(null, Typeface.BOLD);
        }
        if (styles.containsKey("font-style")) {
            holder.order_number.setTypeface(null, Typeface.ITALIC);
        }

        if(position % 2 == 0)
        {
            //holder.rootView.setBackgroundColor(Color.BLACK);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        if(orderList != null){
            return orderList.size();
        }
        return 0;
    }

    public class MyviewHolder extends RecyclerView.ViewHolder {
        TextView order_number;
        TextView customer_name;
        TextView total_str;
        TextView create_datetime_str;
        CardView cardView;

        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            order_number = itemView.findViewById(R.id.title);
            customer_name = itemView.findViewById(R.id.customer_name);
            total_str = itemView.findViewById(R.id.total_str);
            create_datetime_str = itemView.findViewById(R.id.create_datetime_str);
            cardView = itemView.findViewById(R.id.card);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Order order = orderList.get(position);
                    String order_id = order.id;
                    Intent intent = new Intent(activity, ReceiptLauncher.class)
                            .putExtra("order_id", order_id)
                            .putExtra("display", true);
                    activity.startActivity(intent);
                }
            });
        }
    }
}
