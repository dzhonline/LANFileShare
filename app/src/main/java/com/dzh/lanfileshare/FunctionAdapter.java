package com.dzh.lanfileshare;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.dzh.R;

import java.util.List;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionViewHolder> {

    private List<FunctionItem> items;
    private Context context;

    public FunctionAdapter(List<FunctionItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_function, parent, false);
        return new FunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionViewHolder holder, int position) {
        FunctionItem item = items.get(position);
        holder.txtTitle.setText(item.title);
        holder.imgIcon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> {
            context.startActivity(new Intent(context, item.targetActivity));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FunctionViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        ImageView imgIcon;

        public FunctionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}