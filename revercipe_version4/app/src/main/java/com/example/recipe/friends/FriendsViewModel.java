package com.example.recipe.friends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipe.model.Model;
import com.example.recipe.model.User;

import java.util.Collections;
import java.util.List;

public class FriendsViewModel extends ViewModel {

    LiveData<List<User>> data;


    public FriendsViewModel() {
        data = Model.instance.getAllUsers();
    }

    public LiveData<List<User>> getDataList() {
        Collections.sort(data.getValue());
        return data;
    }





}
