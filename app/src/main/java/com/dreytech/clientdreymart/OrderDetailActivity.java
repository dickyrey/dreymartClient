package com.dreytech.clientdreymart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dreytech.clientdreymart.Adapter.OrderDetailAdapter;
import com.dreytech.clientdreymart.Database.ModelDB.Cart;
import com.dreytech.clientdreymart.Retrofit.IDreyMarketAPI;
import com.dreytech.clientdreymart.Utils.Common;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    TextView txt_order_id, txt_order_price, txt_order_address, txt_order_comment, txt_order_status;
    Button btn_cancel;
    RecyclerView recycler_order_detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);


        txt_order_id = (TextView)findViewById(R.id.txt_order_id);
        txt_order_price = (TextView)findViewById(R.id.txt_order_price);
        txt_order_address = (TextView)findViewById(R.id.txt_order_address);
        txt_order_comment = (TextView)findViewById(R.id.txt_order_comment);
        txt_order_status = (TextView)findViewById(R.id.txt_order_status);

        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrder();
            }
        });
        recycler_order_detail = (RecyclerView)findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setLayoutManager(new LinearLayoutManager(this));
        recycler_order_detail.setHasFixedSize(true);

        txt_order_id.setText(new StringBuilder("#").append(Common.currentOrder.getOrderId()));
        txt_order_price.setText(new StringBuilder("Rp.").append(Common.currentOrder.getOrderPrice()));
        txt_order_address.setText(Common.currentOrder.getOrderAddress());
        txt_order_comment.setText(Common.currentOrder.getOrderComment());
        txt_order_status.setText(new StringBuilder("Status Pemesanan: ").append(Common.convertCodeToStatus(Common.currentOrder.getOrderStatus())));

        displayOrderDetail();

    }

    private void cancelOrder() {
        IDreyMarketAPI dreyMarketAPI = Common.getAPI();
        dreyMarketAPI.cancelOrder(String.valueOf(Common.currentOrder.getOrderId()),
                Common.currentUser.getPhone())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Toast.makeText(OrderDetailActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                        if (response.body().contains("Pemesanan telah dibatalkan"))
                            finish();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("DEBUG", t.getMessage());
                    }
                });
    }

    private void displayOrderDetail() {
        List<Cart> orderDetail = new Gson().fromJson(Common.currentOrder.getOrderDetail(),
                new TypeToken<List<Cart>>(){}.getType());

        recycler_order_detail.setAdapter(new OrderDetailAdapter(this,orderDetail));
    }
}
