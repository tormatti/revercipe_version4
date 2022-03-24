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
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;


public class EditRecipeFragment extends Fragment {

    View view;
    Recipe post_;
    Button save_btn;
    Bitmap imageBitmap;
    EditText postDesc_et;
    ImageView postImage_iv;
    ProgressBar progressBar;
    ImageButton camBtn, galleryBtn, delImage;


    static final int REQUESTS_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_GALLERY = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        delImage = view.findViewById(R.id.frag_editP_del_btn);
        camBtn = view.findViewById(R.id.frag_editP_cam_btn);
        postImage_iv = view.findViewById(R.id.editPostFrag_img);
        save_btn = view.findViewById(R.id.editPostFrag_save_btn);
        postDesc_et = view.findViewById(R.id.editPostFrag_desc_et);
        galleryBtn = view.findViewById(R.id.frag_editP_gallery_btn);
        progressBar = view.findViewById(R.id.frag_editPost_progressBar);

        progressBar.setVisibility(View.GONE);
        String pId = EditRecipeFragmentArgs.fromBundle(getArguments()).getPostId();

        Model.instance.getPostById(pId, post -> {
            post_ = post;
            postDesc_et.setText(post.getDescription());
            if (post.getImageUrl() != null) {
                Picasso.get().load(post.getImageUrl()).into(postImage_iv);
            }
        });


        camBtn.setOnClickListener(v -> openCam());
        save_btn.setOnClickListener(v -> savePost());
        delImage.setOnClickListener(v -> deleteImage());
        galleryBtn.setOnClickListener(v -> openGallery());

        return view;
    }

    private void deleteImage() {
        Model.instance.deletePostImage(post_, new Model.DeletePostImageListener() {
            @Override
            public void onComplete(Boolean flag) {
                if (flag == true) {
                    Navigation.findNavController(view).navigateUp();
                }
            }
        });
    }


    private void savePost() {


        post_.setDescription(postDesc_et.getText().toString());

        if (imageBitmap != null) {
            Model.instance.savePostImage(imageBitmap, post_.getId() + ".jpg", url -> {
                post_.setImageUrl(url);
                Model.instance.addPost(post_, () -> {
                    Model.instance.refreshPostsList();
                    camBtn.setEnabled(false);
                    save_btn.setEnabled(false);
                    galleryBtn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    Model.instance.refreshUserPostsList(Model.instance.getActiveUser().getEmail());
                    Navigation.findNavController(view).navigateUp();
                    Model.instance.refreshUserPostsList(Model.instance.getActiveUser().getEmail());

                });
            });
        } else {
            Model.instance.addPost(post_, () -> {
                Model.instance.refreshPostsList();
                camBtn.setEnabled(false);
                save_btn.setEnabled(false);
                galleryBtn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                Navigation.findNavController(view).navigateUp();
            });
        }
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
                Bundle extars = data.getExtras();
                imageBitmap = (Bitmap) extars.get("data");
                postImage_iv.setImageBitmap(imageBitmap);
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
                postImage_iv.setImageBitmap(imageBitmap);
            }
        }
    }
}