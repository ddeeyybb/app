package com.example.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.adapters.ReviewAdapter;
import com.example.app.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView foodImage, likeButton;
    private TextView foodName, categoryText, ingredientsText, recipeText, likeCountText;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitReviewButton;
    private RecyclerView reviewRecyclerView;

    private String itemId;
    private int currentLikes = 0;
    private boolean isLikedByCurrentUser = false;

    private DatabaseReference foodRef;
    private FirebaseUser currentUser;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Food Details");
        }

        // Bind UI components
        foodImage = findViewById(R.id.foodImage);
        foodName = findViewById(R.id.foodName);
        categoryText = findViewById(R.id.categoryText);
        ingredientsText = findViewById(R.id.ingredientsText);
        recipeText = findViewById(R.id.recipeText);
        likeButton = findViewById(R.id.likeButton);
        likeCountText = findViewById(R.id.likeCountText);
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);

        // Setup RecyclerView with GridLayoutManager
        int spanCount = 2; // 2 columns
        reviewRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecyclerView.setAdapter(reviewAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get intent extras
        itemId = getIntent().getStringExtra("itemId");
        String name = getIntent().getStringExtra("itemName");
        String imageUrl = getIntent().getStringExtra("itemImageUrl");
        String category = getIntent().getStringExtra("itemCategory");
        String recipe = getIntent().getStringExtra("itemRecipe");
        List<String> ingredients = getIntent().getStringArrayListExtra("itemIngredients");

        // Set UI content
        foodName.setText(name != null ? name : "No Name");
        categoryText.setText(category != null ? category : "Not Available");
        recipeText.setText(recipe != null && !recipe.isEmpty() ? recipe : "Not Available");

        if (ingredients != null && !ingredients.isEmpty()) {
            ingredientsText.setText(String.join(", ", ingredients));
        } else {
            ingredientsText.setText("Not Available");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(foodImage);
        } else {
            foodImage.setImageResource(R.drawable.placeholder_image);
        }

        foodRef = FirebaseDatabase.getInstance().getReference("Foods").child(itemId);

        loadLikeCount();
        loadReviews();

        likeButton.setOnClickListener(view -> {
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to like.", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleLike();
        });

        submitReviewButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in to submit a review.", Toast.LENGTH_SHORT).show();
                return;
            }

            int rating = (int) ratingBar.getRating();
            String comment = commentEditText.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Please enter a comment.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            long timestamp = System.currentTimeMillis();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = deriveFallbackUsername();
                    }
                    submitReview(new Review(rating, comment, username, timestamp, uid));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    String username = deriveFallbackUsername();
                    submitReview(new Review(rating, comment, username, timestamp, uid));
                }
            });
        });

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String deriveFallbackUsername() {
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            return currentUser.getDisplayName();
        } else if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
            return currentUser.getEmail().split("@")[0];
        } else {
            return "Anonymous";
        }
    }

    private void submitReview(Review review) {
        foodRef.child("reviews").push().setValue(review)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(DetailActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    ratingBar.setRating(0);
                    commentEditText.setText("");
                    loadReviews();
                })
                .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Failed to submit review.", Toast.LENGTH_SHORT).show());
    }

    private void loadLikeCount() {
        foodRef.child("likes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer likes = task.getResult().getValue(Integer.class);
                currentLikes = (likes != null) ? likes : 0;
                likeCountText.setText(currentLikes + " likes");
            } else {
                currentLikes = 0;
                likeCountText.setText("0 likes");
            }
        });

        if (currentUser != null) {
            String uid = currentUser.getUid();
            foodRef.child("likedUsers").child(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    isLikedByCurrentUser = true;
                    likeButton.setColorFilter(getResources().getColor(R.color.red));
                } else {
                    isLikedByCurrentUser = false;
                    likeButton.clearColorFilter();
                }
            });
        }
    }

    private void toggleLike() {
        String uid = currentUser.getUid();

        if (isLikedByCurrentUser) {
            foodRef.child("likedUsers").child(uid).removeValue().addOnSuccessListener(unused -> {
                currentLikes = Math.max(0, currentLikes - 1);
                foodRef.child("likes").setValue(currentLikes);
                likeCountText.setText(currentLikes + " likes");
                likeButton.clearColorFilter();
                isLikedByCurrentUser = false;
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to remove like.", Toast.LENGTH_SHORT).show()
            );
        } else {
            foodRef.child("likedUsers").child(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().exists()) {
                    currentLikes++;
                    foodRef.child("likes").setValue(currentLikes);
                    foodRef.child("likedUsers").child(uid).setValue(true);
                    likeCountText.setText(currentLikes + " likes");
                    likeButton.setColorFilter(getResources().getColor(R.color.red));
                    isLikedByCurrentUser = true;
                } else {
                    Toast.makeText(this, "You already liked this.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadReviews() {
        foodRef.child("reviews").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                    Review review = reviewSnap.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                Collections.reverse(reviewList); // Show latest first
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to load reviews.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
