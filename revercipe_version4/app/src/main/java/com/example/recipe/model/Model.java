package com.example.recipe.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.recipe.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Model {

    Long delete_postId;
    User activeUser = null;
    public static final Model instance = new Model();
    ModelFirebase modelFirebase = new ModelFirebase();
    MutableLiveData<List<Recipe>> postsList = new MutableLiveData<List<Recipe>>();
    MutableLiveData<List<User>> usersList = new MutableLiveData<List<User>>();
    public Executor executor = Executors.newFixedThreadPool(1);
    public Handler mainThread = HandlerCompat.createAsync(Looper.getMainLooper());
    MutableLiveData<List<Recipe>> postsListForUser = new MutableLiveData<List<Recipe>>();

    MutableLiveData<PostsListLoadingState> postsListLoadingState = new MutableLiveData<PostsListLoadingState>();

    private Model() {
        postsListLoadingState.setValue(PostsListLoadingState.loaded);
    }

    /* ************************************ enum loading posts ************************************** */

    public enum PostsListLoadingState {
        loading,
        loaded
    }

    /* ************************************ init user login+register ******************************** */

    public void checkUserName(String userName, CheckUserNameListener listener) {
        modelFirebase.checkUserName(userName, listener);
    }

    public void registerUser(String email, String password, String fullName, String username, RegisterListener listener) {
        modelFirebase.registerUser(email, password, fullName, username, listener);
    }

    public void loginUser(String email, String password, LoginListener listener) {
        modelFirebase.loginUser(email, password, listener);
        modelFirebase.getUserByUserEmail(email, user -> activeUser = user);
    }

    public void logout(LogoutListener listener) {
        modelFirebase.logout(listener);
    }


    /* ************************************ users *************************************************** */

    public void getUserByEmail(String eMail,GetUserByEmailListener listener) {
        modelFirebase.getUserByUserEmail(eMail, new GetUserByUserName() {
            @Override
            public void onComplete(User user) {
                listener.onComplete(user);
            }
        });
    }


    public void getCurrentUser(GetCurrentUserListener listener) {
        modelFirebase.getCurrentUser(listener);
    }

    public void deleteUserImage(String proImageName, DeleteUserImageListener listener) {
        modelFirebase.deleteUserImage(proImageName, listener);
    }


    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public void saveUserImage(Bitmap imageBitmap, String imageName, SaveImageUserListener listener) {
        modelFirebase.saveUserImage(imageBitmap, imageName, listener);
    }

    public void saveUpdateUser(String user_name, SaveUserChangeListener listener) {
        modelFirebase.saveUpdateUser(user_name, listener);
    }

    public LiveData<List<User>> getAllUsers() {
        modelFirebase.getAllUsers(list -> usersList.postValue(list));
        return usersList;
    }

    public LiveData<List<Recipe>> getAllPostsForUser(String userEmail_) {
        if (postsListForUser.getValue() == null) {
            refreshUserPostsList(userEmail_);
        }
        return postsListForUser;
    }


    public void refreshUserPostsList(String userEmail_) {

        postsListLoadingState.setValue(PostsListLoadingState.loading);

        Long lastUpdateData = MyApplication.getContext()
                .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                .getLong("PostsLastUpdateData", 0);

        modelFirebase.getAllPosts(lastUpdateData, list ->
                executor.execute(() -> {

                    Long lud = new Long(0);
                    for (Recipe post : list) {

                        AppLocalDb.db.postDao().insertAll(post);
                        if (lud < post.getUpdateData()) {
                            lud = post.getUpdateData();
                        }
                    }
                    MyApplication.getContext()
                            .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                            .edit()
                            .putLong("PostsLastUpdateData", lud)
                            .commit();

                    List<Recipe> postList = AppLocalDb.db.postDao().getAll();
                    List<Recipe> userPostList = new ArrayList<Recipe>();

                    for (Recipe post : postList) {
                        if (post.getPostUser().equals(userEmail_)) {
                            Log.d("TAG", "email active user is " + post.getPostUser());
                            userPostList.add(post);
                        }
                    }

                    // sort the posts lists in descending order
                    Collections.sort(userPostList, Collections.reverseOrder());
                    postsListForUser.postValue(userPostList);
                    postsListLoadingState.postValue(PostsListLoadingState.loaded);

                }));
    }


    /* ************************************ posts *************************************************** */

    public void deletePostImage(Recipe post, DeletePostImageListener listener) {
        modelFirebase.deletePostImage(post, listener, () -> executor.execute(() -> AppLocalDb.db.postDao().delete(post)));
    }


    public void deletePost(Recipe post, DeletePostListener listener) {
        modelFirebase.create_postDelete(post);
        modelFirebase.get_postDelete(postId -> {
            modelFirebase.deletePost(post, listener);
            delete_postId = postId;
        });
    }

    public LiveData<PostsListLoadingState> getPostsListLoadingState() {
        return postsListLoadingState;
    }

    public LiveData<List<Recipe>> getAllPosts() {
        if (postsList.getValue() == null) {
            refreshPostsList();
        }
        return postsList;
    }

    public void refreshPostsList() {
        postsListLoadingState.setValue(PostsListLoadingState.loading);

        Long lastUpdateData = MyApplication.getContext()
                .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                .getLong("PostsLastUpdateData", 0);

        modelFirebase.getAllPosts(lastUpdateData, list ->
                executor.execute(() -> {
                    Log.d("pos", "size app local sb is: " + AppLocalDb.db.postDao().getAll().size());
                    modelFirebase.get_postDelete(postId -> delete_postId = postId);

                    // ---------------------------------------------------------------------------------------------------------------------------------- //
                    for (Recipe p : AppLocalDb.db.postDao().getAll()) {

                        if (delete_postId != null) {
                            if (p.getId() == delete_postId) {
                                Log.d("pos", "size before delete is : " + AppLocalDb.db.postDao().getAll().size());
                                AppLocalDb.db.postDao().delete(p);
                                Log.d("pos", "size after delete is : " + AppLocalDb.db.postDao().getAll().size());
                                p.setDisplay(false);
                                AppLocalDb.db.postDao().insertAll(p);
                                Log.d("pos", "size after delete and insert is : " + AppLocalDb.db.postDao().getAll().size());

                            }
                        }
                        // ------------------------------------------------------------------------- //
                        Log.d("pos", "here: " + p.getId());
                        if (p.getId() > 30) {
                            Log.d("pos", "id: " + p.getId());
                            Log.d("pos", "desc: " + p.getDescription());
                            Log.d("pos", "display: " + p.getDisplay());
                        }
                    }

                    // ---------------------------------------------------------------------------------------------------------------------------------- //


                    Long lud = new Long(0);
                    for (Recipe post : list) {
                        AppLocalDb.db.postDao().insertAll(post);
                        if (lud < post.getUpdateData()) {
                            lud = post.getUpdateData();
                        }
                    }
                    MyApplication.getContext()
                            .getSharedPreferences("TAG", Context.MODE_PRIVATE)
                            .edit()
                            .putLong("PostsLastUpdateData", lud)
                            .commit();

                    Log.d("pos", "2 size app local sb is: " + AppLocalDb.db.postDao().getAll().size());
                    List<Recipe> tmpList = AppLocalDb.db.postDao().getAll();

                    List<Recipe> postList = new ArrayList<>();

                    for (Recipe npo : tmpList) {

                        if (npo.getDisplay().booleanValue() == false) {
                            AppLocalDb.db.postDao().delete(npo);
                            Log.d("pos", "display is false: " + npo.getDisplay().booleanValue());
                        } else {
                            if (delete_postId != null) {
                                if (npo.getId() == delete_postId) {
                                    AppLocalDb.db.postDao().delete(npo);
                                    npo.setDisplay(false);
                                    AppLocalDb.db.postDao().insertAll(npo);
                                } else {
                                    postList.add(npo);
                                }
                            } else {
                                postList.add(npo);
                            }
                        }
                    }
                    Log.d("pos", "3 size app local sb is: " + AppLocalDb.db.postDao().getAll().size());
                    // sort the posts lists in descending order
                    Collections.sort(postList, Collections.reverseOrder());
                    postsList.postValue(postList);
                    postsListLoadingState.postValue(PostsListLoadingState.loaded);
                }));
    }

    public void savePostImage(Bitmap imageBitmap, String imageName, SaveImagePostListener listener) {
        modelFirebase.savePostImage(imageBitmap, imageName, listener);
    }

    public void addPost(Recipe post, AddPostListener listener) {
        modelFirebase.addPost(post, listener);
    }

    public void getPostId(GetPostIdListener listener) {
        modelFirebase.changePostId(listener);
    }


    public Recipe getPostById(String pId, GetPostByIdListener listener) {
        modelFirebase.getPostById(pId, listener);
        return null;
    }


    /* ************************************ interface *********************************************** */

    public interface GetUserByEmailListener {
        void onComplete(User user);
    }

    public interface DeletePostImageListener {
        void onComplete(Boolean flag);
    }

    public interface DeleteUserImageListener {
        void onComplete(Boolean flag);
    }

    public interface GetCurrentUserListener {
        void onComplete(User user);
    }

    public interface DeletePostListener {
        void onComplete();
    }

    public interface GetPostByIdListener {
        void onComplete(Recipe post);
    }

    public interface SaveUserChangeListener {
        void onComplete();
    }

    public interface AddPostListener {
        void onComplete();
    }

    public interface SaveImagePostListener {
        void onComplete(String url);
    }

    public interface SaveImageUserListener {
        void onComplete(String url);
    }

    public interface CheckUserNameListener {
        void onComplete(boolean flag);
    }

    public interface RegisterListener {
        void onComplete();

        void onAddUser();
    }

    public interface LoginListener {
        void onComplete();
    }

    public interface LogoutListener {
        void onComplete();
    }

    public interface GetUserByUserName {
        void onComplete(User user);
    }

    public interface GetPostIdListener {
        void onComplete(Long id);

    }


}
