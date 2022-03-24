package com.example.recipe.userProfile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;

import java.util.List;

public class UserProfileViewModel extends ViewModel {
    LiveData<List<Recipe>> data;

    public UserProfileViewModel() {
        data = Model.instance.getAllPostsForUser(Model.instance.getActiveUser().getEmail());

    }

    public LiveData<List<Recipe>> getPostsData() {
        data = Model.instance.getAllPostsForUser(Model.instance.getActiveUser().getEmail());
        if(data.getValue()!=null){
            Log.d("getpostdata", String.valueOf(data.getValue().size()));
            for (Recipe p : data.getValue()){
                Log.d("getpostdata",p.getPostUser());
            }
        }


        return data;
    }



}
