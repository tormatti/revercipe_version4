package com.example.recipe.feed;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.Recipe;
import com.example.recipe.myprofile.ProfileFragmentDirections;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;


public class FeedRvFragment extends Fragment {

    View view;
    String user_email;
    FeedAdapter adapter;
    FeedViewModel viewModel;
    SwipeRefreshLayout swipeRefresh;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_feed, container, false);


        user_email = FeedRvFragmentArgs.fromBundle(getArguments()).getUserEmail();
//
        viewModel.getPostsData().observe(getViewLifecycleOwner(), list1 -> refresh());

        swipeRefresh = view.findViewById(R.id.postslist_swiperefresh);

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

        // rv :

        RecyclerView list = view.findViewById(R.id.feed_rv);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedAdapter();
        list.setHasFixedSize(true);
        list.setAdapter(adapter);



        adapter.setOnItemClickListener((v, position) -> {
            String pId = String.valueOf(viewModel.getPostsData().getValue().get(position).getId());
            Navigation.findNavController(view)
                    .navigate(FeedRvFragmentDirections.actionFeedRvFragmentToPostDetailsFragment(pId));
        });

        return view;
    }


    private void refresh() {
        adapter.notifyDataSetChanged();
    }

    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {

        TextView desc_tv;
        TextView textViewtitle;
        ImageView post_imv;
        TextView buttonDelete, buttonDetails;

        public FeedViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);


            post_imv = itemView.findViewById(R.id.imageViewRecipe);
            desc_tv = itemView.findViewById(R.id.textEmail);
            textViewtitle = itemView.findViewById(R.id.textViewtitle);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);
            buttonDelete.setVisibility(View.GONE);
            buttonDetails.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });
        }

        void bind(Recipe post) {

            post_imv.setImageResource(R.drawable.splash_bg1);
            desc_tv.setText(post.getDescription());
            textViewtitle.setText(post.getRecipeName());

            if (post.getImageUrl() != null) {
                Picasso.get()
                        .load(post.getImageUrl())
                        .into(post_imv);
            }


        }
    }


    class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {

        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.feed_recipe_row, parent, false);
            FeedViewHolder holder = new FeedViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
            Recipe post = viewModel.getPostsData().getValue().get(position);
            String recipeName = "";
            String recipeDesc = "";

            holder.bind(post);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getPostsData().getValue() == null) {
                return 0;
            }
            return viewModel.getPostsData().getValue().size();
        }
    }


    /* **************************************** Menu ************************************************ */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {

                case R.id.menu_about:
                    Navigation.findNavController(this.view).navigate(R.id.action_global_aboutFragment);
                    break;

                case R.id.menu_profile:
                    if(Model.instance.getActiveUser().getEmail()!=null){
                        Model.instance.refreshUserPostsList(Model.instance.getActiveUser().getEmail());
                    }
                    Navigation.findNavController(this.view).navigate(R.id.action_global_profileFragment);
                    break;

                case R.id.menu_addPost:
                    Navigation.findNavController(this.view).navigate(R.id.action_global_addPostFragment);
                    break;

                case R.id.menu_friends:
                    Navigation.findNavController(this.view).navigate(R.id.action_global_searchFragment);
                    break;

            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();


    }
}