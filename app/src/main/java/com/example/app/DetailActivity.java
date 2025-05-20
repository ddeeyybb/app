package com.example.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.app.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class DetailActivity extends AppCompatActivity {

    private ImageView foodImage, likeButton;
    private TextView foodName, categoryText, ingredientsText, recipeText, likeCountText;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitReviewButton;
    private ListView reviewListView;

    private String itemId;
    private int currentLikes = 0;
    private boolean isLikedByCurrentUser = false;

    private DatabaseReference foodRef;
    private FirebaseUser currentUser;

    private ReviewListAdapter reviewListAdapter;
    private final List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Food Details");
        }

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
        reviewListView = findViewById(R.id.reviewListView);

        reviewListAdapter = new ReviewListAdapter(reviewList);
        reviewListView.setAdapter(reviewListAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        itemId = getIntent().getStringExtra("itemId");

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        foodRef = FirebaseDatabase.getInstance().getReference("Foods").child(itemId);

        loadFoodDetails();
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

            submitReviewButton.setEnabled(false);
            submitReviewWithUsername(rating, comment);
        });

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadFoodDetails() {
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailActivity.this, "Food item not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                String category = snapshot.child("category").getValue(String.class);
                String recipe = snapshot.child("recipe").getValue(String.class);

                List<String> ingredients = new ArrayList<>();
                for (DataSnapshot ingredientSnap : snapshot.child("ingredients").getChildren()) {
                    String ingredient = ingredientSnap.getValue(String.class);
                    if (ingredient != null) {
                        ingredients.add(ingredient);
                    }
                }

                foodName.setText(name != null ? name : "No Name");
                categoryText.setText(category != null ? category : "Not Available");
                recipeText.setText(recipe != null ? recipe : "Not Available");

                ingredientsText.setText(!ingredients.isEmpty()
                        ? String.join(", ", ingredients)
                        : "Not Available");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(DetailActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(foodImage);
                } else {
                    foodImage.setImageResource(R.drawable.placeholder_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to load food details.", Toast.LENGTH_SHORT).show();
            }
        });
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
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to remove like.", Toast.LENGTH_SHORT).show();
            });
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
        foodRef.child("reviews").orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot reviewSnap : snapshot.getChildren()) {
                    Review review = reviewSnap.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                Collections.reverse(reviewList); // Show newest first
                reviewListAdapter.notifyDataSetChanged();

                if (!reviewList.isEmpty()) {
                    reviewListView.smoothScrollToPosition(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Failed to load reviews.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReviewWithUsername(int rating, String comment) {
        String uid = currentUser.getUid();
        long timestamp = System.currentTimeMillis();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                if (username == null || username.trim().isEmpty()) {
                    username = getFallbackUsername();
                }
                submitReview(new Review(rating, comment, username, timestamp, uid));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String username = getFallbackUsername();
                submitReview(new Review(rating, comment, username, timestamp, uid));
            }
        });
    }

    private String getFallbackUsername() {
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            return currentUser.getDisplayName();
        } else if (currentUser.getEmail() != null) {
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
                    submitReviewButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailActivity.this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
                    submitReviewButton.setEnabled(true);
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ReviewListAdapter for ListView
    private class ReviewListAdapter extends BaseAdapter {

        private final List<Review> reviews;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        ReviewListAdapter(List<Review> reviews) {
            this.reviews = reviews;
        }

        @Override
        public int getCount() {
            return reviews.size();
        }

        @Override
        public Object getItem(int position) {
            return reviews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.item_review, parent, false);
                holder = new ViewHolder();
                holder.usernameTextView = convertView.findViewById(R.id.reviewUsername);
                holder.ratingBar = convertView.findViewById(R.id.reviewRatingBar);
                holder.commentTextView = convertView.findViewById(R.id.reviewCommentText);
                holder.uidTextView = convertView.findViewById(R.id.reviewUid);
                holder.dateTextView = convertView.findViewById(R.id.reviewDate);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Review review = reviews.get(position);

            holder.usernameTextView.setText(review.getUsername() != null ? review.getUsername() : "Anonymous");
            holder.ratingBar.setRating(review.getRating() != null ? review.getRating() : 0);
            holder.commentTextView.setText(review.getComment() != null ? review.getComment() : "");

            if (review.getUid() != null && !review.getUid().isEmpty()) {
                holder.uidTextView.setVisibility(View.VISIBLE);
                holder.uidTextView.setText("User ID: " + review.getUid());
            } else {
                holder.uidTextView.setVisibility(View.GONE);
            }

            if (review.getTimestamp() != null) {
                holder.dateTextView.setText(dateFormat.format(new Date(review.getTimestamp())));
            } else {
                holder.dateTextView.setText("");
            }

            return convertView;
        }

        class ViewHolder {
            TextView usernameTextView;
            RatingBar ratingBar;
            TextView commentTextView;
            TextView uidTextView;
            TextView dateTextView;
        }
    }
}
