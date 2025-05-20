package com.example.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.models.Review;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviewList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ReviewAdapter(List<Review> reviewList) {
        // Sort initial list by timestamp descending (newest first)
        Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.usernameTextView.setText(review.getUsername() != null ? review.getUsername() : "Anonymous");
        holder.commentTextView.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating() != null ? review.getRating() : 0);

        if (review.getUid() != null && !review.getUid().isEmpty()) {
            holder.uidTextView.setVisibility(View.VISIBLE);
            holder.uidTextView.setText("User ID: " + review.getUid());
        } else {
            holder.uidTextView.setVisibility(View.GONE);
        }

        if (review.getTimestamp() != null && review.getTimestamp() > 0) {
            String formattedDate = dateFormat.format(new Date(review.getTimestamp()));
            holder.dateTextView.setText(formattedDate);
        } else {
            holder.dateTextView.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    /**
     * Adds a new review at the top (position 0) and refreshes the adapter.
     * Newest reviews appear first.
     */
    public void addReviewAtTop(Review newReview) {
        reviewList.add(0, newReview);
        notifyItemInserted(0);
    }

    /**
     * Adds multiple new reviews at the top and refreshes.
     * Newest reviews appear first.
     */
    public void addReviewsAtTop(List<Review> newReviews) {
        reviewList.addAll(0, newReviews);
        notifyItemRangeInserted(0, newReviews.size());
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView, uidTextView, dateTextView;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.reviewUsername);
            commentTextView = itemView.findViewById(R.id.reviewCommentText);
            ratingBar = itemView.findViewById(R.id.reviewRatingBar);
            uidTextView = itemView.findViewById(R.id.reviewUid);
            dateTextView = itemView.findViewById(R.id.reviewDate);
        }
    }
}
