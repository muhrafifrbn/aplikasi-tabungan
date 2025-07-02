package com.example.tabunganku;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tabunganku.DetailTabunganActivity;
import com.example.tabunganku.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TabunganAdapter extends RecyclerView.Adapter<TabunganAdapter.TabunganViewHolder> {

    private List<Tabungan> tabunganList = new ArrayList<>();

    public TabunganAdapter() {
    }

    @NonNull
    @Override
    public TabunganViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tabungan, parent, false);
        return new TabunganViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabunganViewHolder holder, int position) {
        // Ambil objek pada posisi saat ini
        Tabungan currentTabungan = tabunganList.get(position);

        // Set data ke view seperti biasa
        holder.tvNama.setText(currentTabungan.getNama());
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        holder.tvTarget.setText(format.format(currentTabungan.getTarget()));
        holder.tvTerkumpul.setText("Terkumpul: " + format.format(currentTabungan.getTerkumpul()));

        // SET ONCLICKLISTENER DI SINI
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ambil ID dari objek model, bukan dari posisi adapter
                String tabunganId = currentTabungan.getId();

                // Dapatkan context dari view yang diklik
                Context context = v.getContext();

                // Buat Intent
                Intent intent = new Intent(context, DetailTabunganActivity.class);
                intent.putExtra("TABUNGAN_ID", tabunganId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tabunganList.size();
    }

    public void setData(List<Tabungan> newList) {
        this.tabunganList.clear();
        this.tabunganList.addAll(newList);
        notifyDataSetChanged();
    }

    class TabunganViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTarget, tvTerkumpul;

        public TabunganViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvItemNama);
            tvTarget = itemView.findViewById(R.id.tvItemTarget);
            tvTerkumpul = itemView.findViewById(R.id.tvItemTerkumpul);
        }
    }
}