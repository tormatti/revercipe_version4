package com.example.recipe.userProfile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;
import com.example.recipe.model.User;
import com.squareup.picasso.Picasso;


public class UserProfileFragment extends Fragment {

    View view;
    User user = null;
    ImageView photoImv;
    TextView usernameTv;
      UserProfileAdapter adapter;
    UserProfileViewModel viewModel;
    SwipeRefreshLayout swipeRefresh;

     @Override
     public void onAttach(@NonNull Context context) {
         super.onAttach(context);
         viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
     }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        String uEmail = UserProfileFragmentArgs.fromBundle(getArguments()).getUserEmail();
        Model.instance.getUserByEmail(uEmail, new Model.GetUserByEmailListener() {
            @Override
            public void onComplete(User u) {
                user = u;
                setUserDetails();
                Model.instance.refreshUserPostsList(user.getEmail());
            }
        });

        swipeRefresh = view.findViewById(R.id.user_profilePostslist_swiperefresh);
        usernameTv = view.findViewById(R.id.frag_userprofile_usern_tv);
        photoImv = view.findViewById(R.id.frag_user_profile_imv);


        viewModel.getPostsData().observe(getViewLifecycleOwner(), list1 -> refresh());

        swipeRefresh.setOnRefreshListener(() -> Model.instance.refreshUserPostsList(user.getEmail()));
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

        // rv :

        RecyclerView list = view.findViewById(R.id.userprofile_rv);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserProfileAdapter();
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Log.d("click","its ok");
            }
        });


        return view;
    }
    private void refresh() {
        adapter.notifyDataSetChanged();
    }

    private void setUserDetails() {

        usernameTv.setText(user.getUserName());
        photoImv.setImageResource(R.drawable.splash_bg1);

        if (user.getProImageUrl() != null && user.getProImageUrl() != "") {
            Picasso.get()
                    .load(user.getProImageUrl())
                    .into(photoImv);
        }

    }




    /* ************************************ interface *********************************************** */


    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }




    /* ************************************ view holder ********************************************* */


    class UserProfileViewHolder extends RecyclerView.ViewHolder {

        TextView desc_tv;
        TextView textViewtitle;
        ImageView post_imv;
        TextView postUser_tv;
        TextView buttonDelete, buttonDetails;

        public UserProfileViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);


            post_imv = itemView.findViewById(R.id.imageViewRecipe);
            desc_tv = itemView.findViewById(R.id.textEmail);
            textViewtitle = itemView.findViewById(R.id.textViewtitle);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);

            buttonDetails.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });
        }

        void bind(Recipe post, User user_) {

            post_imv.setImageResource(R.drawable.splash_bg1);
            desc_tv.setText(post.getDescription());

            if (post.getImageUrl() != null) {
                Picasso.get()
                        .load(post.getImageUrl())
                        .into(post_imv);
            }

        }

    }


    /* ************************************ adapter ************************************************* */


    class UserProfileAdapter extends RecyclerView.Adapter<UserProfileViewHolder> {

        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public UserProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.feed_recipe_row, parent, false);
            UserProfileViewHolder holder = new UserProfileViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserProfileViewHolder holder, int position) {
            Recipe post = viewModel.getPostsData().getValue().get(position);
            holder.bind(post, user);
        }

        @Override
        public int getItemCount() {

            if(user!=null){
                Log.d("user",user.getUserName());
                if (viewModel.getPostsData().getValue() == null) {
                    return 0;
                }
                else
                    return viewModel.getPostsData().getValue().size();
            }

            return 0;
        }
    }




}