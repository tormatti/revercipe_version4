package com.example.recipe.model;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.recipe.MyApplication;

@Database(entities = {Recipe.class}, version = 1)

abstract class AppLocalDbRepository extends RoomDatabase {
    public abstract RecipeDao postDao();
}

public class AppLocalDb{
    static public AppLocalDbRepository db =
            Room.databaseBuilder(MyApplication.getContext(),
                    AppLocalDbRepository.class,
                    "Db_FileName.db")
                    .fallbackToDestructiveMigration()
                    .build();
}
