package com.example.recipe.post;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;
import com.squareup.picasso.Picasso;

public class RecipeDetailsFragment extends Fragment {

    View view;
    Recipe post_;
    TextView textViewName, textViewTime, textViewIn, textViewDes;
    ImageView postImage_iv, backArrow;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_recipe_details, container, false);

        postImage_iv = view.findViewById(R.id.imageView);
        textViewName = view.findViewById(R.id.textViewName);
        textViewTime = view.findViewById(R.id.textViewTime);
        textViewIn = view.findViewById(R.id.textViewIn);
        textViewDes = view.findViewById(R.id.textViewDes);
        backArrow = view.findViewById(R.id.backArrow);

        String pId = RecipeDetailsFragmentArgs.fromBundle(getArguments()).getPostId();

        Model.instance.getPostById(pId, post -> {
            post_ = post;
            textViewName.setText(post.getRecipeName());
            textViewIn.setText(post.getRecipeIngredients());
            textViewDes.setText(post.getDescription());
            textViewTime.setText(post.getRecipeTime());

            if (post.getImageUrl() != null)
                Picasso.get()
                        .load(post.getImageUrl())
                        .into(postImage_iv);
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backArrow();
            }
        });


        return view;
    }

    private void backArrow() {
        getActivity().onBackPressed();
    }
}