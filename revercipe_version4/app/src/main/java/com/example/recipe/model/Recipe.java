package com.example.recipe.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

@Entity
public class Recipe implements Comparable<Recipe> {

    final public static String COLLECTION_NAME = "Recipes";
    final public static String COLLECTION_DELETE_NAME = "DeleteRecipe";


    @PrimaryKey
    @NonNull
    Long id;
    String postUser;
    String imageUrl;
    String description;
    String recipeName;
    String recipeTime;
    String recipeIngredients;
    Long updateData = new Long(0);
    Boolean display=true;



    public Recipe() {
    }

    public Recipe(String description, Long id, String postUser, String recipeName, String recipeTime, String recipeIngredients) {
        this.description = description;
        this.id = id;
        this.recipeName = recipeName;
        this.recipeTime = recipeTime;
        this.recipeIngredients = recipeIngredients;
        this.postUser = postUser;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("updateData", FieldValue.serverTimestamp());
        json.put("imageUrl", imageUrl);
        json.put("description", description);
        json.put("id", id);
        json.put("postUser", postUser);
        json.put("recipeName", recipeName);
        json.put("recipeTime", recipeTime);
        json.put("recipeIngredients", recipeIngredients);

        return json;
    }

    public static Recipe create(Map<String, Object> json) {

        Long id = (Long) json.get("id");
        String imageUrl = (String) json.get("imageUrl");
        String postUser = (String) json.get("postUser");
        Timestamp ts = (Timestamp) json.get("updateData");
        String description = (String) json.get("description");
        String recipeName = (String) json.get("recipeName");
        String recipeTime = (String) json.get("recipeTime");
        String recipeIngredients = (String) json.get("recipeIngredients");

        Long update = ts.getSeconds();
        Recipe post = new Recipe(description, id, postUser,recipeName,recipeTime,recipeIngredients);
        post.setImageUrl(imageUrl);
        post.setUpdateData(update);
        return post;
    }

    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean flag) {
        this.display = flag;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPostUser() {
        return postUser;
    }

    public Long getUpdateData() {
        return updateData;
    }

    public String getDescription() {
        return description;
    }

    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUpdateData(Long updateData) {
        this.updateData = updateData;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeTime() {
        return recipeTime;
    }

    public void setRecipeTime(String recipeTime) {
        this.recipeTime = recipeTime;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    @Override
    public int compareTo(Recipe p) {
        return this.getId().compareTo(p.getId());
    }
}
