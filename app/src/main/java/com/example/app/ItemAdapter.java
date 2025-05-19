package com.example.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends BaseAdapter {

    private final Context context;
    private List<ItemActivity> foodList;
    private final OnItemClickListener onItemClickListener;
    private final LayoutInflater inflater;
    private final DatabaseReference foodsRef;

    // Constructor
    public ItemAdapter(Context context, List<ItemActivity> foodList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        this.inflater = LayoutInflater.from(context);

        // Reference to "Foods" node in Firebase Realtime Database
        foodsRef = FirebaseDatabase.getInstance().getReference("Foods");
    }

    @Override
    public int getCount() {
        return foodList.size();
    }

    @Override
    public ItemActivity getItem(int position) {
        return foodList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // If you want, you can return a stable id here, but for now just return position
        return position;
    }

    // ViewHolder pattern to improve performance by recycling views
    private static class ViewHolder {
        ImageView foodImage;
        TextView foodName;
        TextView foodCategory;
        TextView foodLikes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Inflate and set up ViewHolder if convertView is null
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_row, parent, false);
            holder = new ViewHolder();
            holder.foodImage = convertView.findViewById(R.id.foodImage);
            holder.foodName = convertView.findViewById(R.id.foodName);
            holder.foodCategory = convertView.findViewById(R.id.foodCategory);
            holder.foodLikes = convertView.findViewById(R.id.foodLikes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get current item
        ItemActivity item = getItem(position);

        // Bind data to views
        holder.foodName.setText(item.getName());
        holder.foodCategory.setText(item.getCategory());
        holder.foodLikes.setText("Likes: " + item.getLikes());

        // Load image with Glide, with fade and placeholder/error images
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_image) // Replace with your placeholder drawable
                    .error(R.drawable.error_image)             // Replace with your error drawable
                    .into(holder.foodImage);
        } else {
            holder.foodImage.setImageResource(R.drawable.error_image);
        }

        // Set click listener on the whole item view
        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });

        return convertView;
    }

    /**
     * Attach a Firebase listener to fetch food items from the "Foods" node
     * and update the adapter's list in real-time.
     */
    public void fetchFoodItems() {
        foodsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot foodSnapshot : snapshot.getChildren()) {
                    ItemActivity foodItem = foodSnapshot.getValue(ItemActivity.class);
                    if (foodItem != null) {
                        // Optional: set Firebase key as the ID
                        foodItem.setId(foodSnapshot.getKey());
                        foodList.add(foodItem);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Log error or handle failure to fetch data gracefully
                System.err.println("Error fetching food items: " + error.getMessage());
            }
        });
    }

    /**
     * Manually update the adapter's list and refresh the UI.
     * @param newList New list of food items.
     */
    public void updateFoodList(List<ItemActivity> newList) {
        this.foodList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Interface to handle item click callbacks.
     */
    public interface OnItemClickListener {
        void onItemClick(ItemActivity item);
    }
}
