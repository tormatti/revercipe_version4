package com.example.recipe.post;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;

import java.io.FileNotFoundException;


public class AddRecipeFragment extends Fragment {


    ImageButton camBtn, galleryBtn;
    ProgressBar progressBar;
    ImageView imgPost;
    EditText rcName,rcTime,rcIngredient, rcDesc ;
    Button postBtn,cancelBtn;
    View view;


    static final int REQUESTS_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;
    Bitmap imageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_add_recipe, container, false);
        progressBar = view.findViewById(R.id.frag_addP_progressBar);
        galleryBtn = view.findViewById(R.id.frag_addP_gallery_btn);
        postBtn = view.findViewById(R.id.fragEditAccount_save_btn);
        cancelBtn = view.findViewById(R.id.fragEditAccount_cancel_btn);
        camBtn = view.findViewById(R.id.frag_addP_cam_btn);
        rcTime = view.findViewById(R.id.fragNewRecipe_userName_et2);
        rcName = view.findViewById(R.id.fragNewRecipe_userName_et);
        rcIngredient = view.findViewById(R.id.editTextIn);
        rcDesc = view.findViewById(R.id.fragNewRecipe_userName_et3);
        imgPost = view.findViewById(R.id.frag_addP_iv_p);

        progressBar.setVisibility(View.GONE);

        camBtn.setOnClickListener(v -> {
            openCam();
        });
        galleryBtn.setOnClickListener(v -> {
            openGallery();
        });
        postBtn.setOnClickListener(v -> {
            savePost();
        });


        return view;
    }

    private void openCam() {
        Intent openCamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCamIntent, REQUESTS_IMAGE_CAPTURE);
    }

    private void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, REQUEST_IMAGE_GALLERY);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTS_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                imgPost.setImageBitmap(imageBitmap);
            }
        } else if (requestCode == REQUEST_IMAGE_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                try {
                    imageBitmap = BitmapFactory.decodeStream(getContext()
                            .getContentResolver().openInputStream(uri));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                imgPost.setImageBitmap(imageBitmap);
            }
        }
    }

    private void savePost() {

        camBtn.setEnabled(false);
        postBtn.setEnabled(false);
        galleryBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String desc = rcDesc.getText().toString();
        String name = rcName.getText().toString();
        String time = rcTime.getText().toString();
        String ingredient = rcIngredient.getText().toString();

        Model.instance.getPostId(id -> {
            Recipe post = new Recipe(desc, id, Model.instance.getActiveUser().getEmail(), name,time,ingredient);
            if (imageBitmap != null) {
                Model.instance.savePostImage(imageBitmap, post.getId() + ".jpg", url -> {
                    post.setImageUrl(url);
                    Model.instance.addPost(post, () -> {
                        Model.instance.refreshPostsList();
                        Navigation.findNavController(view).navigateUp();
                    });
                });
            } else {
                Model.instance.addPost(post, () -> {
                    Model.instance.refreshPostsList();
                    Navigation.findNavController(view).navigateUp();
                });
            }
        });


    }
}