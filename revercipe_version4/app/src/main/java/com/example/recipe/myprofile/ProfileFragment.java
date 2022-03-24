package com.example.recipe.myprofile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;
import com.example.recipe.model.User;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {


    View view;
    User user;
    ImageView photoImv;
    TextView usernameTv;
    ProfileAdapter adapter;
    ProfileViewModel viewModel;
    Button edit_btn,addNewRecipe;
    TextView logout_btn;

    SwipeRefreshLayout swipeRefresh;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        user = Model.instance.getActiveUser();
        photoImv = view.findViewById(R.id.frag_profile_imv);
        edit_btn = view.findViewById(R.id.button2);
        usernameTv = view.findViewById(R.id.frag_profile_usern_tv);
        logout_btn = view.findViewById(R.id.textViewLogout);
        addNewRecipe = view.findViewById(R.id.fragEditAccount_save_btn);
        swipeRefresh = view.findViewById(R.id.postslist_swiperefresh);

        viewModel.getPostsData().observe(getViewLifecycleOwner(), list1 -> refresh());
        setUserDetails();


        swipeRefresh.setOnRefreshListener(() -> {
            Model.instance.refreshPostsList();
            Model.instance.refreshPostsList();
        });
        swipeRefresh.setRefreshing(
                Model.instance.getPostsListLoadingState()
                        .getValue() == Model.PostsListLoadingState.loading
        );

        Model.instance.getPostsListLoadingState().observe(getViewLifecycleOwner(), postsListLoadingState -> {
            if (postsListLoadingState == Model.PostsListLoadingState.loading) {
                swipeRefresh.setRefreshing(true);
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });


        edit_btn.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_global_editPostFragment));


        logout_btn.setOnClickListener(v -> logout());

        addNewRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_global_addPostFragment);
            }
        });


        // rv :

        RecyclerView list = view.findViewById(R.id.profile_rv);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileAdapter();
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        adapter.setOnItemClickListener((v, position,type) -> {
            Recipe recipe = viewModel.getPostsData().getValue().get(position);
            String pId = String.valueOf(recipe.getId());

            if(type == 1) {
                Navigation.findNavController(view)
                        .navigate(ProfileFragmentDirections.actionProfileFragmentToPostDetailsFragment(pId));
            }else{
                deletePost(recipe);
            }
        });



        return view;
    }


    private void deletePost(Recipe recipe) {
        Model.instance.refreshPostsList();
        Model.instance.deletePost(recipe, () -> {
            viewModel.getPostsData().observe(getViewLifecycleOwner(), list1 -> refresh());
        });
    }


    private void setUserDetails() {
        photoImv.setImageResource(R.drawable.splash_bg1);
        usernameTv.setText(user.getUserName());
        if (user.getProImageUrl() != null && user.getProImageUrl() != "") {
            Picasso.get()
                    .load(Model.instance.getActiveUser().getProImageUrl())
                    .into(photoImv);
        }
    }

    private void logout() {
        Model.instance.logout(() -> {
            Navigation.findNavController(view).navigate(R.id.action_global_loginFragment);
        });
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
    }

    /* ************************************ interface *********************************************** */

    interface OnItemClickListener {
        void onItemClick(View v, int position, int type);
    }



    /* ************************************ view holder ********************************************* */

    class ProfileViewHolder extends RecyclerView.ViewHolder {

        TextView desc_tv;
        TextView textViewtitle;
        ImageView post_imv;
        TextView postUser_tv;
        TextView buttonDelete, buttonDetails;

        public ProfileViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            post_imv = itemView.findViewById(R.id.imageViewRecipe);
            desc_tv = itemView.findViewById(R.id.textEmail);
            textViewtitle = itemView.findViewById(R.id.textViewtitle);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);

            buttonDetails.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos, 1);
            });

            buttonDelete.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                listener.onItemClick(view, pos, 2);
            });
        }

        void bind(Recipe post, String userName, String userImage) {
            desc_tv.setText(post.getDescription());
            textViewtitle.setText(post.getRecipeName());
            post_imv.setImageResource(R.drawable.splash_bg1);
//            postUser_tv.setText(userName);
//            if (post.getImageUrl() != null) {
//                Picasso.get()
//                        .load(post.getImageUrl())
//                        .into(post_imv);
//            }

        }

    }

    /* ************************************ adapter ************************************************* */

    class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {

        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.feed_recipe_row, parent, false);
            ProfileViewHolder holder = new ProfileViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
            Recipe post = viewModel.getPostsData().getValue().get(position);
            User u = Model.instance.getActiveUser();
            String postUserName = u.getUserName();
            String postUserImage = u.getProImageUrl();


            holder.bind(post, postUserName, postUserImage);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getPostsData().getValue() == null) {
                return 0;
            }
            return viewModel.getPostsData().getValue().size();
        }
    }


}