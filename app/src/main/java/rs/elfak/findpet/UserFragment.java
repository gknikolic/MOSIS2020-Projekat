package rs.elfak.findpet;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Repositories.UsersData;
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
        initializeUI(getView());

        fullNameEditText.setText(currentUser.fullName);
        phoneNumberEditText.setText(currentUser.phoneNumber);
        profileImageImageView.setImageBitmap(currentUser.profilePicture);

        btnUpdateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = fullNameEditText.getText().toString();
                String phoneNumber = phoneNumberEditText.getText().toString();
                currentUser.fullName = fullName;
                currentUser.phoneNumber = phoneNumber;
                UsersData.getInstance().updateUser(currentUser.key);
            }
        });

        btnChangeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2000);
                }
                else {
                    startGallery();
                }
            }
        });
    }

    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                Uri returnUri = data.getData();
                Bitmap bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                UsersData.getInstance().changeUserProfilePicture(bitmapImage, getContext());
                UsersData.getInstance().getCurrentLoggedUser().profilePicture=bitmapImage;
                profileImageImageView.setImageBitmap(bitmapImage);
            }
        }
    }

    private void initializeUI(View view) {
        fullNameEditText = view.findViewById(R.id.userSettings_fullName);
        phoneNumberEditText = view.findViewById(R.id.userSettings_phoneNumber);
        profileImageImageView = view.findViewById(R.id.userSettings_profilePicture);
        btnUpdateUser = view.findViewById(R.id.userSettings_btnUpdateUser);
        btnChangeProfilePicture = view.findViewById(R.id.userSettings_btnChangeProfilePicture);
    }
}
