package com.example.recipe.feed;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.recipe.R;
import com.example.recipe.model.Model;
import com.example.recipe.myprofile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainFragment  extends Fragment {

    private BottomNavigationView navigationView;

    String user_email;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
          View view = inflater.inflate(R.layout.main_layout,container,false);
          navigationView = view.findViewById(R.id.activity_main_bottom_navigation_view);
          initData();
          return view;
    }

    private void initData(){
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        user_email = FeedRvFragmentArgs.fromBundle(getArguments()).getUserEmail();
        Bundle args = new Bundle();
        args.putString("userEmail",user_email);
        FeedRvFragment feedRvFragment = new FeedRvFragment ();
        feedRvFragment.setArguments(args);
        loadFragment(feedRvFragment);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.recipesFragment:
                    Bundle args = new Bundle();
                    args.putString("userEmail",user_email);
                    FeedRvFragment feedRvFragment = new FeedRvFragment ();
                    feedRvFragment.setArguments(args);
                    loadFragment(feedRvFragment);
                    return true;
                case R.id.mypageFragment:
                    Bundle args1 = new Bundle();
                    args1.putString("userEmail",user_email);
                    ProfileFragment profileFragment = new ProfileFragment ();
                    profileFragment.setArguments(args1);
                    loadFragment(profileFragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment  fragment){
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrag, fragment).commit();
    }


    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigationView.setSelectedItemId(R.id.recipesFragment);

            }
        },1500);
    }
}
