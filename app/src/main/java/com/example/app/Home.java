package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private static final int ADD_RECIPE_REQUEST = 1001;

    private TabLayout categoryTabs;
    private GridView gridViewFoods;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private ItemAdapter itemAdapter;
    private final List<ItemActivity> foodList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ValueEventListener foodListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        categoryTabs = view.findViewById(R.id.categoryTabs);
        gridViewFoods = view.findViewById(R.id.gridViewFoods);
        progressBar = view.findViewById(R.id.progressBar);
        fabAdd = view.findViewById(R.id.fab_add);

        // Add category tabs
        categoryTabs.addTab(categoryTabs.newTab().setText("All"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Main Dish"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Dessert"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Drinks"));

        itemAdapter = new ItemAdapter(requireContext(), foodList, this::openDetailActivity);
        gridViewFoods.setAdapter(itemAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Foods");

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Add.class);
                startActivityForResult(intent, ADD_RECIPE_REQUEST);
            }
        });

        // Load all foods initially
        loadAllFoods();

        // Listen for tab selection to filter data
        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                if (selectedCategory.equals("All")) {
                    loadAllFoods();
                } else {
                    loadFoodsByCategory(selectedCategory);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (databaseReference != null && foodListener != null) {
            databaseReference.removeEventListener(foodListener);
        }
    }

    private void loadAllFoods() {
        progressBar.setVisibility(View.VISIBLE);

        if (databaseReference != null && foodListener != null) {
            databaseReference.removeEventListener(foodListener);
        }

        foodListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                foodList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ItemActivity foodItem = itemSnapshot.getValue(ItemActivity.class);
                    if (foodItem != null) {
                        foodItem.setId(itemSnapshot.getKey());
                        foodList.add(foodItem);
                    }
                }
                itemAdapter.updateFoodList(foodList);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load recipes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        databaseReference.addValueEventListener(foodListener);
    }

    private void loadFoodsByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);

        if (databaseReference != null && foodListener != null) {
            databaseReference.removeEventListener(foodListener);
        }

        foodListener = databaseReference.orderByChild("category").equalTo(category)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded()) return;

                        foodList.clear();
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            ItemActivity foodItem = itemSnapshot.getValue(ItemActivity.class);
                            if (foodItem != null) {
                                foodItem.setId(itemSnapshot.getKey());
                                foodList.add(foodItem);
                            }
                        }
                        itemAdapter.updateFoodList(foodList);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to load recipes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDetailActivity(ItemActivity clickedItem) {
        if (clickedItem == null) return;

        Intent intent = new Intent(requireContext(), DetailActivity.class);
        intent.putExtra("itemId", clickedItem.getId());
        intent.putExtra("itemName", clickedItem.getName());
        intent.putExtra("itemImageUrl", clickedItem.getImageUrl());
        intent.putExtra("itemCategory", clickedItem.getCategory());
        intent.putStringArrayListExtra("itemIngredients", new ArrayList<>(clickedItem.getIngredients()));
        intent.putExtra("itemRecipe", clickedItem.getRecipe());
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_RECIPE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (categoryTabs.getSelectedTabPosition() == 0) {
                loadAllFoods();
            } else {
                String selectedCategory = categoryTabs.getTabAt(categoryTabs.getSelectedTabPosition()).getText().toString();
                loadFoodsByCategory(selectedCategory);
            }
        }
    }
}
