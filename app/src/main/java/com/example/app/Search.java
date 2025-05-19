package com.example.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Search extends Fragment {

    private EditText searchBar;
    private GridView gridViewFoods;
    private ItemAdapter itemAdapter;
    private List<ItemActivity> foodList;
    private List<ItemActivity> filteredFoodList;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchBar = view.findViewById(R.id.searchBar);
        gridViewFoods = view.findViewById(R.id.gridViewFoods);

        foodList = new ArrayList<>();
        filteredFoodList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Foods");

        itemAdapter = new ItemAdapter(requireContext(), filteredFoodList, this::openDetailActivity);
        gridViewFoods.setAdapter(itemAdapter);

        loadFoodItems();
        setupSearch();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoodList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadFoodItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ItemActivity foodItem = dataSnapshot.getValue(ItemActivity.class);
                    if (foodItem != null) {
                        foodItem.setId(dataSnapshot.getKey());
                        foodList.add(foodItem);
                    }
                }
                filterFoodList(searchBar.getText().toString());
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterFoodList(String query) {
        filteredFoodList.clear();

        if (query.isEmpty()) {
            filteredFoodList.addAll(foodList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (ItemActivity item : foodList) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredFoodList.add(item);
                }
            }
        }

        itemAdapter.updateFoodList(filteredFoodList);

        if (filteredFoodList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(getContext(), "No items found for '" + query + "'", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDetailActivity(ItemActivity item) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("itemId", item.getId());
        intent.putExtra("itemName", item.getName());
        intent.putExtra("itemCategory", item.getCategory());
        intent.putExtra("itemImageUrl", item.getImageUrl());
        intent.putStringArrayListExtra("itemIngredients", new ArrayList<>(item.getIngredients()));
        intent.putExtra("itemRecipe", item.getRecipe());
        startActivity(intent);
    }
}
