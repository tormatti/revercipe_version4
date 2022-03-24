package com.example.recipe.friends;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.model.User;
import com.squareup.picasso.Picasso;

public class FriendsFragment extends Fragment {

    View view;
    RecyclerView list;
    FriendsViewModel viewModel;
    SearchUserAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_friends, container, false);

        list = view.findViewById(R.id.search_rv);


        adapter = new SearchUserAdapter();
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        viewModel.getDataList().observe(getViewLifecycleOwner(), list1 -> refresh());


        adapter.setOnItemClickListener((v, position) -> {
            String uEmail = String.valueOf(viewModel.getDataList().getValue().get(position).getEmail());

            if (uEmail.equals(Model.instance.getActiveUser().getEmail())) {
                Navigation.findNavController(view)
                        .navigate(FriendsFragmentDirections
                                .actionGlobalProfileFragment());
            } else {
                Navigation.findNavController(view)
                        .navigate(FriendsFragmentDirections
                                .actionSearchFragmentToUserFragment(uEmail));
            }

        });


        return view;
    }

    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
    }

    class SearchUserViewHolder extends RecyclerView.ViewHolder {

        ImageView user_iv;
        TextView userName_tv;


        public SearchUserViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            user_iv = itemView.findViewById(R.id.searchUser_row_userImage_iv);
            userName_tv = itemView.findViewById(R.id.searchUser_row_userName_tv);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                listener.onItemClick(v, pos);
            });

        }

        void bind(User user) {
            userName_tv.setText(user.getUserName());

            if (user.getProImageUrl() != null&&user.getProImageUrl() != "") {
                Picasso.get()
                        .load(user.getProImageUrl())
                        .into(user_iv);
            }
        }

    }

    class SearchUserAdapter extends RecyclerView.Adapter<FriendsFragment.SearchUserViewHolder> {

        OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.friends_user_row, parent, false);
            SearchUserViewHolder holder = new SearchUserViewHolder(view, listener);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position) {
            User user = viewModel.getDataList().getValue().get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            if (viewModel.getDataList().getValue() == null) {
                return 0;
            }
            return viewModel.getDataList().getValue().size();
        }
    }


}