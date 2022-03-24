package com.example.recipe.connect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipe.R;
import com.example.recipe.model.Model;


public class RegisterFragment extends Fragment {

    Button register_btn;
    TextView account_btn;
    EditText fullNameEt, usernameEt, emailEt, passwordEt, cpasswordEt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        fullNameEt = view.findViewById(R.id.fragRegister_fullName_et);
        usernameEt = view.findViewById(R.id.fragRegister_userName_et);
        emailEt = view.findViewById(R.id.fragRegister_email_et);
        passwordEt = view.findViewById(R.id.fragRegister_password_et);
        cpasswordEt = view.findViewById(R.id.fragRegister_confpassword_et);

        register_btn = view.findViewById(R.id.fragRegister_register_btn);
        account_btn = view.findViewById(R.id.fragRegister_forgotPassword_tv);


        account_btn.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        register_btn.setOnClickListener(v -> {
            registerUser(view);
        });


        return view;
    }

    private void registerUser(View view) {

        String fullName = fullNameEt.getText().toString();
        String username = usernameEt.getText().toString();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();
        String cPassword = cpasswordEt.getText().toString();

        if (fullName.isEmpty()) {
            fullNameEt.setError("Full name is required");
            fullNameEt.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEt.setError("Email is required");
            emailEt.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            usernameEt.setError("Username is required");
            usernameEt.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Please provide valid email");
            emailEt.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordEt.setError("Password is required");
            passwordEt.requestFocus();
            return;
        }
        if (cPassword.isEmpty()) {
            passwordEt.setError("Confirm Password is required");
            passwordEt.requestFocus();
            return;
        }

        if (password.length() < 6 || cPassword.length() < 6) {
            passwordEt.setError("Minimum password length should be 6 characters!");
            passwordEt.requestFocus();
            return;
        }

        if(!password.equals(cPassword)){
            Toast.makeText(getContext(),"Password mismatch ", Toast.LENGTH_LONG).show();
            return;
        }


        Model.instance.checkUserName(usernameEt.getText().toString(), flag -> {
            // it`s ok , userName is available
            if (flag == true) {
                Model.instance.registerUser(email, password, fullName, username, new Model.RegisterListener() {
                    @Override
                    public void onComplete() {
                        Navigation.findNavController(view)
                                .navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment());
                    }

                    @Override
                    public void onAddUser() {
                        Log.d("TAG", "onComplete -  register");
                    }
                });

            } else {
                usernameEt.setError("Username is not available");
                usernameEt.requestFocus();
            }
        });


    }


}