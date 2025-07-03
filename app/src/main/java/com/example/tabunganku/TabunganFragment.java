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

public class TabunganFragment extends Fragment {
    private RecyclerView recyclerViewTabungan;
    private TabunganAdapter adapter;
    private DatabaseReference tabunganRef;
    private ValueEventListener tabunganListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabungan, container, false);
        recyclerViewTabungan = view.findViewById(R.id.recyclerViewTabungan);
        setupRecyclerView();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tabunganRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("tabungan");
        return view;
    }

    private void setupRecyclerView() {
        recyclerViewTabungan.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TabunganAdapter();
        recyclerViewTabungan.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (tabunganListener == null) {
            tabunganListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Tabungan> newList = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Tabungan tabungan = itemSnapshot.getValue(Tabungan.class);
                        if (tabungan != null) {
                            tabungan.setId(itemSnapshot.getKey());
                            newList.add(tabungan);
                        }
                    }
                    adapter.setData(newList);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            tabunganRef.addValueEventListener(tabunganListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tabunganListener != null) {
            tabunganRef.removeEventListener(tabunganListener);
            tabunganListener = null;
        }
    }
}
