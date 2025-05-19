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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
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

        // Show username or "Anonymous" if null
        String userInfo = review.getUsername() != null ? review.getUsername() : "Anonymous";
        if (review.getUid() != null && !review.getUid().isEmpty()) {
            userInfo += " (" + review.getUid() + ")";
        }
        holder.usernameTextView.setText(userInfo);

        holder.commentTextView.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());

        String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date(review.getTimestamp()));
        holder.dateTextView.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView usernameTextView, commentTextView, dateTextView;
        final RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.reviewUsername);
            commentTextView = itemView.findViewById(R.id.reviewCommentText);
            ratingBar = itemView.findViewById(R.id.reviewRatingBar);
            dateTextView = itemView.findViewById(R.id.reviewDate);
        }
    }
}
