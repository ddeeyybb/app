package com.example.app;

import com.example.app.models.Review;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an item with details such as name, category, ingredients, likes, and reviews.
 */
public class ItemActivity {

    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private List<String> ingredients;
    private String recipe;
    private int likes;
    private Map<String, Review> reviews;
    private long uploadTime;

    /** Required empty constructor for Firebase */
    public ItemActivity() {
    }

    /**
     * Full constructor.
     */
    public ItemActivity(String id, String name, String category, String imageUrl,
                        List<String> ingredients, String recipe, int likes,
                        Map<String, Review> reviews, long uploadTime) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.likes = likes;
        this.reviews = reviews;
        this.uploadTime = uploadTime;
    }

    /**
     * Convenience constructor without uploadTime; sets uploadTime to current time.
     */
    public ItemActivity(String id, String name, String category, String imageUrl,
                        List<String> ingredients, String recipe, int likes,
                        Map<String, Review> reviews) {
        this(id, name, category, imageUrl, ingredients, recipe, likes, reviews, System.currentTimeMillis());
    }

    /**
     * Constructor without likes and reviews; likes default to 0, reviews default to null.
     */
    public ItemActivity(String id, String name, String category, String imageUrl,
                        List<String> ingredients, String recipe) {
        this(id, name, category, imageUrl, ingredients, recipe, 0, null, System.currentTimeMillis());
    }

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getRecipe() { return recipe; }
    public void setRecipe(String recipe) { this.recipe = recipe; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = Math.max(0, likes); }

    public Map<String, Review> getReviews() { return reviews; }
    public void setReviews(Map<String, Review> reviews) { this.reviews = reviews; }

    public long getUploadTime() { return uploadTime; }
    public void setUploadTime(long uploadTime) { this.uploadTime = uploadTime; }

    /**
     * Increment the likes count by one, ensuring it does not go below zero.
     */
    public void incrementLikes() {
        if (this.likes < 0) {
            this.likes = 0;
        }
        this.likes++;
    }

    /**
     * Calculate the average rating from reviews.
     * Returns 0 if there are no reviews.
     */
    public float getAverageRating() {
        if (reviews == null || reviews.isEmpty()) return 0f;
        float sum = 0f;
        int count = 0;
        for (Review review : reviews.values()) {
            if (review != null) {
                sum += review.getRating();
                count++;
            }
        }
        return count == 0 ? 0f : sum / count;
    }

    /**
     * Calculate popularity score based on likes and average rating.
     */
    public float getPopularityScore() {
        final float ratingWeight = 3.0f;
        return likes + (getAverageRating() * ratingWeight);
    }

    /**
     * Adds or updates a review in the reviews map.
     * Initialize reviews map if null.
     */
    public void addOrUpdateReview(String reviewId, Review review) {
        if (reviews == null) {
            // You can initialize a HashMap here or any Map implementation you prefer.
            reviews = new java.util.HashMap<>();
        }
        reviews.put(reviewId, review);
    }

    @Override
    public String toString() {
        return "ItemActivity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", ingredients=" + ingredients +
                ", recipe='" + recipe + '\'' +
                ", likes=" + likes +
                ", reviews=" + reviews +
                ", uploadTime=" + uploadTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemActivity)) return false;
        ItemActivity that = (ItemActivity) o;
        return likes == that.likes &&
                uploadTime == that.uploadTime &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(category, that.category) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(ingredients, that.ingredients) &&
                Objects.equals(recipe, that.recipe) &&
                Objects.equals(reviews, that.reviews);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, imageUrl, ingredients, recipe, likes, reviews, uploadTime);
    }
}
