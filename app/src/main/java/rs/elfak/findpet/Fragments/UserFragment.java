package rs.elfak.findpet.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.Services.UserLocationService;
import rs.elfak.findpet.data_models.User;

public class UserFragment extends Fragment {
    private User currentUser;
    private EditText fullNameEditText;
    private EditText phoneNumberEditText;
    private ImageView profileImageImageView;
    private Button btnUpdateUser, btnChangeProfilePicture;
    private Button btnEnableLocationService, btnDisableLocationService;
    private SwitchCompat networkBasedLocationSwitch;

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
                UsersData.getInstance().updateUser(currentUser.key, getContext());
            }
        });

        btnChangeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDialog();
            }
        });

        btnEnableLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();
                User currentUser = UsersData.getInstance().getCurrentLoggedUser();
                if(currentUser != null){
                    currentUser.locationEnabled = true;
                }
            }
        });

        btnDisableLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent service = new Intent(getActivity(), UserLocationService.class);
                getActivity().stopService(service);
                User currentUser = UsersData.getInstance().getCurrentLoggedUser();
                if(currentUser != null){
                    currentUser.locationEnabled = false;
                }
                Toast.makeText(getActivity(),"Foreground location service stopped",Toast.LENGTH_LONG).show();
            }
        });
    }


    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                getActivity());
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(ActivityCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    Constants.STORAGE_PERMISSION_CODE);
                        }
                        else{
                            Intent pictureActionIntent = null;

                            pictureActionIntent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(
                                    pictureActionIntent,
                                    Constants.STORAGE_REQUEST_CODE);

                        }

                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(ActivityCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(
                                    new String[]{Manifest.permission.CAMERA},
                                    Constants.CAMERA_PERMISSION_CODE);
                        }
                        else{
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE);
                        }
                    }
                });
        myAlertDialog.show();
    }

//    private void startGallery() {
//        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        galleryIntent.setType("image/*");
//        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//            startActivityForResult(galleryIntent, Constants.STORAGE_REQUEST_CODE);
//        }
//    }

    private void pickImage() {
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getActivity());
        //todo activity problem
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == Constants.STORAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri returnUri = data.getData();
            Bitmap bitmapImage = null;
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bitmapImage != null){
                Bitmap.createScaledBitmap(bitmapImage, 400, 400, true);
                UsersData.getInstance().changeUserProfilePicture(bitmapImage, getContext());
                UsersData.getInstance().getCurrentLoggedUser().profilePicture=bitmapImage;
                profileImageImageView.setImageBitmap(bitmapImage);
            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            UsersData.getInstance().changeUserProfilePicture(photo, getContext());
            UsersData.getInstance().getCurrentLoggedUser().profilePicture=photo;
            profileImageImageView.setImageBitmap(photo);
        }
    }

    private void initializeUI(View view) {
        fullNameEditText = view.findViewById(R.id.userSettings_fullName);
        phoneNumberEditText = view.findViewById(R.id.userSettings_phoneNumber);
        profileImageImageView = view.findViewById(R.id.userSettings_profilePicture);
        btnUpdateUser = view.findViewById(R.id.userSettings_btnUpdateUser);
        btnChangeProfilePicture = view.findViewById(R.id.userSettings_btnChangeProfilePicture);
        btnEnableLocationService = view.findViewById(R.id.userSettings_btnEnableService);
        btnDisableLocationService = view.findViewById(R.id.userSettings_btnDisableService);
        networkBasedLocationSwitch = view.findViewById(R.id.userSettings_NetworkBasedLocationSwitch);
    }

    void startLocationService() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.LOCATION_PERMISSION_CODE
            );
        } else {
            startService(!this.networkBasedLocationSwitch.isChecked());
        }
    }

    private void startService(boolean useGps){
        Intent service = new Intent(getActivity().getApplicationContext(), UserLocationService.class);
        service.putExtra("useGps", useGps);
        ContextCompat.startForegroundService(getActivity().getApplicationContext(), service);
        Toast.makeText(getActivity(),"Foreground location service started",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.LOCATION_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }
            else{
                Toast.makeText(
                        getActivity(),
                        "To use location you must grant location permissions",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
