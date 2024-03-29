package com.dreytech.clientdreymart.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.dreytech.clientdreymart.Database.ModelDB.Cart;
import com.dreytech.clientdreymart.R;
import com.dreytech.clientdreymart.Utils.Common;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>{

    Context context;
    List<Cart> cartList;

    public OrderDetailAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public OrderDetailAdapter.OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.order_detail_layout,parent,false);

        return new OrderDetailAdapter.OrderDetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderDetailAdapter.OrderDetailViewHolder holder, final int position) {
        Picasso.with(context)
                .load(cartList.get(position).link)
                .into(holder.img_product);

        holder.txt_price.setText(new StringBuilder("Rp. ").append(cartList.get(position).price));
        holder.txt_product_name.setText(new StringBuilder(cartList.get(position).name)
                .append(" x ")
                .append(cartList.get(position).amount)
                .append(" "));

        holder.txt_sugar_ice.setText(new StringBuilder("Sugar: ")
                .append("Ice: ").append(cartList.get(position).ice)
                .append("%").toString());

        //Mendapatkan harga dengan semua pilihan
        final double priceOneCup = cartList.get(position).price / cartList.get(position).amount;



    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder{

        ImageView img_product;
        TextView txt_product_name, txt_sugar_ice, txt_price;

        public RelativeLayout view_background;
        public LinearLayout view_foreground;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);

            img_product = (ImageView)itemView.findViewById(R.id.img_product);
            txt_product_name = (TextView) itemView.findViewById(R.id.txt_product_name);
            txt_sugar_ice = (TextView) itemView.findViewById(R.id.txt_sugar_ice);
            txt_price = (TextView) itemView.findViewById(R.id.txt_price);


            view_background = (RelativeLayout)itemView.findViewById(R.id.view_background);
            view_foreground = (LinearLayout)itemView.findViewById(R.id.view_foreground);

        }

    }

    public void removeItem(int position)
    {
        cartList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Cart item, int position)
    {
        cartList.add(position, item);
        notifyItemInserted(position);
    }
}
