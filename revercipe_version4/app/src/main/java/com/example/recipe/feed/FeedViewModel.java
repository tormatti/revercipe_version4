package com.example.recipe.feed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;
import com.example.recipe.model.User;
import java.util.List;

public class FeedViewModel extends ViewModel {

    LiveData<List<Recipe>> data;
    LiveData<List<User>> user_data;

    public FeedViewModel() {
        data = Model.instance.getAllPosts();
        user_data = Model.instance.getAllUsers();
    }

    public LiveData<List<Recipe>> getPostsData() {
        return data;
    }

    public LiveData<List<User>> getAllUsersData() {
        user_data = Model.instance.getAllUsers();
        return user_data;
    }



}
