package rs.elfak.findpet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.data_models.User;

public class UserFragment extends Fragment {
    private User currentUser;
    private EditText fullNameEditText;
    private EditText phoneNumberEditText;
    private ImageView profileImageImageView;
    private Button btnUpdateUser, btnChangeProfilePicture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //from main activity
        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);

        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instalizeUI(getView());

        fullNameEditText.setText(currentUser.fullName);
        phoneNumberEditText.setText(currentUser.phoneNumber);
        profileImageImageView.setImageBitmap(currentUser.profilePicture);

        btnUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnChangeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void instalizeUI(View view) {
        fullNameEditText = view.findViewById(R.id.userSettings_fullName);
        phoneNumberEditText = view.findViewById(R.id.userSettings_phoneNumber);
        profileImageImageView = view.findViewById(R.id.userSettings_profilePicture);
        btnUpdateUser = view.findViewById(R.id.userSettings_btnUpdateUser);
        btnChangeProfilePicture = view.findViewById(R.id.userSettings_btnChangeProfilePicture);
    }
}
