package com.example.tabunganku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailTabunganAdapter extends RecyclerView.Adapter<DetailTabunganAdapter.RiwayatViewHolder> {

    private List<Riwayat> riwayatList = new ArrayList<>();
    private Context context;

    public DetailTabunganAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Riwayat> newList) {
        this.riwayatList.clear();
        this.riwayatList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_riwayat, parent, false);
        return new RiwayatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        Riwayat currentRiwayat = riwayatList.get(position);

        holder.tvJenis.setText(currentRiwayat.getJenis());
        holder.tvTanggal.setText(formatTimestamp(currentRiwayat.getTimestamp()));
        holder.tvKeterangan.setText(currentRiwayat.getKeterangan());

        NumberFormat formatMataUang = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        formatMataUang.setMaximumFractionDigits(0);

        if (currentRiwayat.getJenis().equals("Menabung")) {
            holder.tvJumlah.setText("+ " + formatMataUang.format(currentRiwayat.getJumlah()));
            // Pastikan Anda punya color 'green' di res/values/colors.xml
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.tvJumlah.setText("- " + formatMataUang.format(currentRiwayat.getJumlah()));
            // Pastikan Anda punya color 'red' di res/values/colors.xml
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    // Fungsi helper untuk mengubah long menjadi String tanggal
    private String formatTimestamp(long timestamp) {
        try {
            // Format: 12 Jul 2025
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Tanggal tidak valid";
        }
    }

    static class RiwayatViewHolder extends RecyclerView.ViewHolder {
        TextView tvJenis, tvTanggal, tvKeterangan, tvJumlah;

        public RiwayatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tvRiwayatJenis);
            tvTanggal = itemView.findViewById(R.id.tvRiwayatTanggal);
            tvKeterangan = itemView.findViewById(R.id.tvRiwayatKeterangan);
            tvJumlah = itemView.findViewById(R.id.tvRiwayatJumlah);
        }
    }
}