package com.example.app;

import com.example.app.models.Review;
import java.util.List;
import java.util.Map;

public class ItemActivity {

    private String id;
    private String name;
    private String category;
    private String imageUrl;
    private List<String> ingredients;
    private String recipe;
    private int likes;
    private Map<String, Review> reviews;

    public ItemActivity() {
        // Required empty constructor for Firebase
    }

    public ItemActivity(String id, String name, String category, String imageUrl,
                        List<String> ingredients, String recipe, int likes,
                        Map<String, Review> reviews) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.likes = likes;
        this.reviews = reviews;
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
    public void setLikes(int likes) { this.likes = likes; }

    public Map<String, Review> getReviews() { return reviews; }
    public void setReviews(Map<String, Review> reviews) { this.reviews = reviews; }

    // Helper method
    public void incrementLikes() {
        this.likes++;
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
                '}';
    }
}
