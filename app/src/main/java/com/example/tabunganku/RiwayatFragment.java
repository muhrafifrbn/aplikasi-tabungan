package com.example.tabunganku;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class RiwayatFragment extends Fragment {
    private RecyclerView rvRiwayat;
    private RiwayatAdapter riwayatAdapter;
    private DatabaseReference riwayatRef;
    private ValueEventListener riwayatListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riwayat, container, false);
        rvRiwayat = view.findViewById(R.id.rvRiwayat);
        setupRecyclerView();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query riwayatQuery = FirebaseDatabase.getInstance().getReference("users").child(uid).child("riwayat").orderByChild("timestamp").limitToLast(50);
        riwayatRef = riwayatQuery.getRef();
        return view;
    }

    private void setupRecyclerView() {
        rvRiwayat.setLayoutManager(new LinearLayoutManager(getContext()));
        riwayatAdapter = new RiwayatAdapter(getContext());
        rvRiwayat.setAdapter(riwayatAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (riwayatListener == null) {
            riwayatListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Riwayat> newList = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Riwayat riwayat = itemSnapshot.getValue(Riwayat.class);
                        if (riwayat != null) {
                            newList.add(0, riwayat);
                        }
                    }
                    riwayatAdapter.setData(newList);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            riwayatRef.addValueEventListener(riwayatListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (riwayatListener != null) {
            riwayatRef.removeEventListener(riwayatListener);
            riwayatListener = null;
        }
    }
}