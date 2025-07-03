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

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder> {

    private List<Riwayat> riwayatList = new ArrayList<>();
    private Context context;

    public RiwayatAdapter(Context context) {
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
        Riwayat riwayat = riwayatList.get(position);

        holder.tvJenis.setText(riwayat.getJenis());
        holder.tvKeterangan.setText(riwayat.getKeterangan());

        // Format tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tvTanggal.setText(sdf.format(new Date(riwayat.getTimestamp())));

        // Format jumlah dan atur warna
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0); // Menghilangkan desimal
        String formattedJumlah = format.format(riwayat.getJumlah());

        if (riwayat.getJenis().equals("Top Up") || riwayat.getJenis().equals("Transfer Masuk")) {
            holder.tvJumlah.setText("+ " + formattedJumlah);
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.tvJumlah.setText("- " + formattedJumlah);
            holder.tvJumlah.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    class RiwayatViewHolder extends RecyclerView.ViewHolder {
        TextView tvJenis, tvKeterangan, tvJumlah, tvTanggal;

        public RiwayatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tvRiwayatJenis);
            tvKeterangan = itemView.findViewById(R.id.tvRiwayatKeterangan);
            tvJumlah = itemView.findViewById(R.id.tvRiwayatJumlah);
            tvTanggal = itemView.findViewById(R.id.tvRiwayatTanggal);
        }
    }
}