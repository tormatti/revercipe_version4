package com.example.recipe.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.recipe.MyApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ModelFirebase {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();


    public ModelFirebase() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
    }


    /* ************************************ init user login+register ******************************** */

    public void checkUserName(String userName, Model.CheckUserNameListener listener) {
        db.collection(User.COLLECTION_NAME)
                .document(userName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        boolean flag = true;
                        if (task.isSuccessful() & task.getResult() != null) {
                            if (task.getResult().getData() != null) {
                                if (userName.equals(Model.instance.activeUser.getUserName())) {
                                    flag = true;
                                } else {
                                    flag = false;
                                }

                            }
                        }
                        listener.onComplete(flag);
                    }
                });


    }

    public void registerUser(String email, String password, String fullName, String username, Model.RegisterListener listener) {

        // authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(fullName, username, email);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(MyApplication.getContext(),
                                        "User has been registered successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MyApplication.getContext(),
                                        "Failed to register, try again!!", Toast.LENGTH_LONG).show();
                            }
                        });

                        //add to users  collection
                        Map<String, Object> json = user.toJson();
                        db.collection(User.COLLECTION_NAME)
                                .document(user.getUserName())
                                .set(json)
                                .addOnSuccessListener(unused -> listener.onAddUser())
                                .addOnFailureListener(e -> listener.onAddUser());

                        db.collection(User.COLLECTION_EMAIL_NAME)
                                .document(user.getEmail())
                                .set(json)
                                .addOnSuccessListener(unused -> listener.onAddUser())
                                .addOnFailureListener(e -> listener.onAddUser());


                        listener.onComplete();
                    } else {
                        Toast.makeText(MyApplication.getContext(),
                                "Failed to register, try again!!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void loginUser(String email, String password, Model.LoginListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onComplete();
                    } else {
                        Toast.makeText(MyApplication.getContext(),
                                "Failed to login", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void logout(Model.LogoutListener listener) {
        mAuth.signOut();
        listener.onComplete();
    }


    /* ************************************ users *************************************************** */

    public void getCurrentUser(Model.GetCurrentUserListener listener) {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // User is signed in
            String em = user.getEmail();
            getUserByUserEmail(em, user1 -> listener.onComplete(user1));
        } else {
            listener.onComplete(null);
        }

    }

    public void deleteUserImage(String proImageName, Model.DeleteUserImageListener listener) {

        // delete from storage , users-imgUrl:null


        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("user_images/" + proImageName + ".jpg");

        imgRef.delete().addOnSuccessListener(unused -> {

            Model.instance.getActiveUser().setProImageUrl(null);
            User u = Model.instance.getActiveUser();
            Map<String, Object> json = u.toJson();

            db.collection(User.COLLECTION_EMAIL_NAME)
                    .document(u.getEmail())
                    .set(json);

            // update the new user details
            db.collection(User.COLLECTION_NAME)
                    .document(u.getUserName())
                    .set(json)
                    .addOnSuccessListener(unused1 -> listener.onComplete(true))
                    .addOnFailureListener(e -> listener.onComplete(false));

        }).addOnFailureListener(e -> listener.onComplete(false));

    }

    public void getUserByUserEmail(String email, Model.GetUserByUserName listener) {

        db.collection(User.COLLECTION_EMAIL_NAME)
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        User user = null;
                        if (task.isSuccessful() & task.getResult() != null) {
                            if (task.getResult().getData() != null) {
                                user = User.create(task.getResult().getData());
                            }
                        }
                        listener.onComplete(user);
                    }
                });

    }

    public void saveUserImage(Bitmap imageBitmap, String imageName, Model.SaveImageUserListener listener) {
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("user_images/" + imageName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> listener.onComplete(null))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Uri downloadUrl = uri;
                            listener.onComplete(downloadUrl.toString());
                        });
                    }
                });


    }

    public void saveUpdateUser(String user_name, Model.SaveUserChangeListener listener) {

        db.collection(User.COLLECTION_NAME)
                .document(Model.instance.getActiveUser().getUserName())
                .delete()
                .addOnSuccessListener(unused -> Log.d("TAG", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.d("TAG", "Error deleting document"));

        Model.instance.getActiveUser().setUserName(user_name);
        User u = Model.instance.getActiveUser();
        Map<String, Object> json = u.toJson();


        db.collection(User.COLLECTION_EMAIL_NAME)
                .document(u.getEmail())
                .set(json);

        // update the new user details
        db.collection(User.COLLECTION_NAME)
                .document(u.getUserName())
                .set(json)
                .addOnSuccessListener(unused -> listener.onComplete())
                .addOnFailureListener(e -> listener.onComplete());

    }


    public void getAllUsers(GetAllUsersListener listener) {
        db.collection(User.COLLECTION_EMAIL_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    List<User> list = new LinkedList<User>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = User.create(doc.getData());
                            if (user != null) {
                                list.add(user);
                            }
                        }
                    }
                    listener.onComplete(list);
                });
    }

    /* ************************************ posts *************************************************** */

    public void addPost(Recipe post, Model.AddPostListener listener) {
        Map<String, Object> json = post.toJson();
        db.collection(Recipe.COLLECTION_NAME)
                .document(String.valueOf(post.getId()))
                .set(json)
                .addOnSuccessListener(unused -> listener.onComplete())
                .addOnFailureListener(e -> listener.onComplete());

    }

    public void getPostById(String pId, Model.GetPostByIdListener listener) {
        db.collection(Recipe.COLLECTION_NAME)
                .document(pId)
                .get()
                .addOnCompleteListener(task -> {
                    Recipe post = null;
                    if (task.isSuccessful() & task.getResult() != null) {
                        post = Recipe.create(task.getResult().getData());
                    }
                    listener.onComplete(post);
                });
    }

    public void getAllPosts(Long lastUpdateData, GetAllPostsListener listener) {
        db.collection(Recipe.COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("updateData", new Timestamp(lastUpdateData, 0))
                .get()
                .addOnCompleteListener(task -> {
                    List<Recipe> list = new LinkedList<Recipe>();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Recipe post = Recipe.create(doc.getData());
                            if (post != null) {
                                list.add(post);
                            }
                        }
                    }
                    listener.onComplete(list);
                });
    }

    public void savePostImage(Bitmap imageBitmap, String imageName, Model.SaveImagePostListener listener) {
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("post_images/" + imageName);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bas);
        byte[] data = bas.toByteArray();
        UploadTask uploadTask = imgRef.putBytes(data);
        uploadTask.addOnFailureListener(
                exception -> {
                    Log.e(" exception ", " ==> "+exception.getLocalizedMessage());
                    listener.onComplete(null);
                }
        )
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Uri downloadUrl = uri;
                            listener.onComplete(downloadUrl.toString());
                        }));
    }

    public void changePostId(Model.GetPostIdListener listener) {
        db.collection("RecipeId")
                .document("id")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.getData() != null) {
                        Map<String, Object> json = new HashMap<String, Object>();
                        Long i = new Long(0);
                        Number tmp = null;
                        json = documentSnapshot.getData();
                        tmp = (Number) json.get("id");
                        i = Long.valueOf(Integer.parseInt(String.valueOf(tmp)) + 1);
                        json.put("id", i);
                        db.collection("RecipeId")
                                .document("id")
                                .set(json);
                        listener.onComplete(i);
                    } else {
                        Map<String, Object> json = new HashMap<String, Object>();
                        json.put("id", new Long(1));
                        db.collection("RecipeId")
                                .document("id")
                                .set(json)
                                .addOnSuccessListener(unused -> listener.onComplete(new Long(1)))
                                .addOnFailureListener(ea -> listener.onComplete(new Long(1)));
                    }
                })
                .addOnFailureListener(e -> listener.onComplete(new Long(1)));
    }


    public void create_postDelete(Recipe post) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("id", post.id);
        db.collection(Recipe.COLLECTION_DELETE_NAME)
                .document("id")
                .set(json)
                .addOnSuccessListener(unused -> Log.d("TAG", "success create delete post ducument"))
                .addOnFailureListener(e -> Log.d("TAG", "unsuccessful create delete post document"));
    }

    public void get_postDelete(DelPostListener listener) {

        db.collection(Recipe.COLLECTION_DELETE_NAME)
                .document("id")
                .get()
                .addOnCompleteListener(task -> {
                    Long id_ = new Long(0);
                    if (task.isSuccessful() & task.getResult() != null) {
                        Map<String, Object> json = task.getResult().getData();
                        if(json != null && json.get("id") !=null)
                        id_ = (Long) json.get("id");
                    }

                    listener.onComplete(id_);
                });
    }


    public void deletePost(Recipe post, Model.DeletePostListener listener) {

        //create_postDelete(post);
        // delete the post from firebase
        db.collection(Recipe.COLLECTION_NAME)
                .document(String.valueOf(post.getId()))
                .delete()
                .addOnCompleteListener(task -> {
                    // delete the image from storage
                    StorageReference storageRef = storage.getReference();
                    StorageReference imgRef = storageRef.child("post_images/" + post.getId() + ".jpg");
                    imgRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("TAG", "image delete is successful");
                            })
                            .addOnFailureListener(exception -> {
                                Log.d("TAG", "image delete is fail");
                            });

                    //  secondListener.onComplete();
                    listener.onComplete();
                }).addOnSuccessListener(unused -> Log.d("TAG", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.d("TAG", "Error deleting document"));
    }

    public void deletePostImage(Recipe postT, Model.DeletePostImageListener listenerA, DelPostImageListener listenerB) {

        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("post_images/" + postT.getId() + ".jpg");

        // delete from app localdb && from firebase

        imgRef.delete().addOnSuccessListener(unused -> {

            postT.setImageUrl(null);
            Map<String, Object> json = postT.toJson();

            db.collection(Recipe.COLLECTION_NAME)
                    .document(String.valueOf(postT.getId()))
                    .set(json)
                    .addOnSuccessListener(unused1 -> {
                        listenerA.onComplete(true);
                        listenerB.onComplete();
                    })
                    .addOnFailureListener(e -> listenerA.onComplete(false));


        }).addOnFailureListener(e -> listenerA.onComplete(false));


    }




    /* ************************************ interface *********************************************** */

    public interface GetAllUsersListener {
        void onComplete(List<User> list);
    }

    public interface GetAllPostsListener {
        void onComplete(List<Recipe> list);
    }

    public interface DelPostListener {
        void onComplete(Long postId);
    }

    public interface DelPostImageListener {
        void onComplete();
    }
}
