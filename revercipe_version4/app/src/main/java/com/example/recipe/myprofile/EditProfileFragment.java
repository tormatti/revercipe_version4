package com.example.recipe.myprofile;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.User;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;


public class EditProfileFragment extends Fragment {


    ProgressBar progressBar;
    TextView username;
    Button saveBtn;
    View view;
    ImageView backArrow;

    Bitmap imageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        saveBtn = view.findViewById(R.id.frag_editProfile_save_btn);
        username = view.findViewById(R.id.frag_editProfile_username_et);
        progressBar = view.findViewById(R.id.frag_editProfile_progressBar);
        backArrow = view.findViewById(R.id.backArrow);

        progressBar.setVisibility(View.GONE);


        setUserDetails();

        saveBtn.setOnClickListener(v -> save());

        backArrow.setOnClickListener(view -> {
            backClick();
        });

        return view;
    }

    private void backClick(){
        getActivity().onBackPressed();
    }





    /* ************************************ init **************************************************** */

    private void setUserDetails() {
        username.setText(Model.instance.getActiveUser().getUserName());
    }

    /* ************************************ save **************************************************** */

    private void save() {

        Model.instance.checkUserName(username.getText().toString(), flag -> {
            // it`s ok , userName is available
            if (flag == true) {


                saveBtn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                User user = Model.instance.getActiveUser();

                if (imageBitmap != null) {
                    Model.instance.saveUserImage(imageBitmap, user.getEmail() + ".jpg",
                            url -> {
                                user.setProImageUrl(url);
                                Model.instance.saveUpdateUser(username.getText().toString(),
                                        () -> Navigation.findNavController(view).navigateUp());
                            });
                } else {
                    Model.instance.saveUpdateUser(username.getText().toString(),
                            () -> Navigation.findNavController(view).navigateUp());
                }

            } else {
                username.setError("Username is not available");
                username.requestFocus();
            }
        });

    }


}