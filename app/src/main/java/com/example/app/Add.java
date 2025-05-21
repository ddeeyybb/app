package com.example.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.app.models.Review;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Add extends AppCompatActivity {

    private EditText foodNameEditText, ingredientsEditText, recipeEditText;
    private Spinner categorySpinner;
    private Button submitButton, pickImageButton;
    private ProgressBar progressBar;
    private ImageView imageViewPreview;

    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Replace with your current ImgBB API key
    private final String imgbbApiKey = "360b35b0c006824ab5d6046b49700fb5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        initViews();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        foodNameEditText = findViewById(R.id.editTextFoodName);
        ingredientsEditText = findViewById(R.id.editTextIngredients);
        recipeEditText = findViewById(R.id.editTextRecipe);
        categorySpinner = findViewById(R.id.spinnerCategory);
        submitButton = findViewById(R.id.buttonSubmit);
        pickImageButton = findViewById(R.id.buttonPickImage);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList("Dessert", "Main dish", "Drinks");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadImageToImgBB();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageViewPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
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

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void uploadImageToImgBB() {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        try {
            // Convert image to Base64 string
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            byte[] imageBytes = readBytesFromInputStream(inputStream);
            inputStream.close();

            // Encode without newlines (NO_WRAP)
            String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            String url = "https://api.imgbb.com/1/upload?key=" + imgbbApiKey;

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String imageUrl = jsonResponse.getJSONObject("data").getString("url");
                            saveRecipeToDatabase(imageUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showUploadError();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            String errorMsg = new String(error.networkResponse.data);
                            System.err.println("Upload Error: " + errorMsg);
                        }
                        error.printStackTrace();
                        showUploadError();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("image", encodedImage);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                    return headers;
                }
            };

            queue.add(postRequest);

        } catch (IOException e) {
            e.printStackTrace();
            showUploadError();
        }
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void showUploadError() {
        progressBar.setVisibility(View.GONE);
        submitButton.setEnabled(true);
        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
    }

    private void saveRecipeToDatabase(String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Foods");
        String itemId = UUID.randomUUID().toString();

        long currentTime = System.currentTimeMillis();

        Map<String, Review> emptyReviews = new HashMap<>();

        ItemActivity newItem = new ItemActivity(
                itemId,
                foodNameEditText.getText().toString().trim(),
                categorySpinner.getSelectedItem().toString(),
                imageUrl,
                parseIngredients(ingredientsEditText.getText().toString().trim()),
                recipeEditText.getText().toString().trim(),
                0,
                emptyReviews,
                currentTime
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
