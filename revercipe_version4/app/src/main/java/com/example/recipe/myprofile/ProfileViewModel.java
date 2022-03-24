package com.example.recipe.myprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    LiveData<List<Recipe>> data;

    public ProfileViewModel() {
        data = Model.instance.getAllPostsForUser(Model.instance.getActiveUser().getEmail());
    }

    public LiveData<List<Recipe>> getPostsData() {
        return data;
    }


}
