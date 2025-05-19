package com.example.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.models.Review;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Add extends AppCompatActivity {

    private EditText foodNameEditText, ingredientsEditText, recipeEditText, imageUrlEditText;
    private Spinner categorySpinner;
    private Button submitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add); // Ensure you have add.xml with these views

        initViews();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        foodNameEditText = findViewById(R.id.editTextFoodName);
        ingredientsEditText = findViewById(R.id.editTextIngredients);
        recipeEditText = findViewById(R.id.editTextRecipe);
        imageUrlEditText = findViewById(R.id.editTextImageUrl);
        categorySpinner = findViewById(R.id.spinnerCategory);
        submitButton = findViewById(R.id.buttonSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Dessert");
        categories.add("Main dish");
        categories.add("Drinks");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                addItemToDatabase();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(foodNameEditText.getText())) {
            foodNameEditText.setError("Food name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(ingredientsEditText.getText())) {
            ingredientsEditText.setError("Ingredients are required");
            isValid = false;
        }

        if (TextUtils.isEmpty(recipeEditText.getText())) {
            recipeEditText.setError("Recipe is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(imageUrlEditText.getText())) {
            imageUrlEditText.setError("Image URL is required");
            isValid = false;
        }

        return isValid;
    }

    private void addItemToDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Foods");
        String itemId = UUID.randomUUID().toString();

        // Create empty reviews map
        Map<String, Review> reviews = new HashMap<>();

        ItemActivity newItem = new ItemActivity(
                itemId,
                foodNameEditText.getText().toString().trim(),
                categorySpinner.getSelectedItem().toString(),
                imageUrlEditText.getText().toString().trim(),
                parseIngredients(ingredientsEditText.getText().toString().trim()),
                recipeEditText.getText().toString().trim(),
                0, // initial likes
                reviews
        );

        databaseReference.child(itemId).setValue(newItem)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(Add.this, "Recipe added successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(Add.this, "Failed to add recipe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<String> parseIngredients(String ingredientsText) {
        List<String> ingredients = new ArrayList<>();
        String[] items = ingredientsText.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                ingredients.add(trimmed);
            }
        }
        return ingredients;
    }
}
