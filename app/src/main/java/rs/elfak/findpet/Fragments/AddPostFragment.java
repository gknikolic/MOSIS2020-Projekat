package rs.elfak.findpet.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Enums.PetType;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.PostsData;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.Pet;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class AddPostFragment extends Fragment {
    private User currentUser;
    private ImageView imageViewAddPicture;
    private Button btnAddPicture;
    private Button btnAddNewPost;
    private EditText editTextPetName;
    private EditText editTextDescription;
    private Spinner spinnerCaseType;
    private Spinner spinnerPetType;
    private Post newPost;
    private int petTypeIndex = -1;
    private int caseTypeIndex = -1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
//        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);
        currentUser = UsersData.getInstance().getCurrentLoggedUser();
        newPost = new Post();
        return inflater.inflate(R.layout.fragment_add_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI(getView());
        spinnerCaseType.setAdapter(new ArrayAdapter<CaseType>(getContext(), R.layout.spinner_item, CaseType.values()));
        spinnerPetType.setAdapter(new ArrayAdapter<PetType>(getContext(), R.layout.spinner_item, PetType.values()));

        btnAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGetPictureDialog();
            }
        });

        btnAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFieldsValues()){
                    newPost.pet = new Pet();
                    newPost.caseType = CaseType.values()[caseTypeIndex];
                    newPost.pet.name = editTextPetName.getText().toString();
                    newPost.pet.type = PetType.values()[petTypeIndex];
                    newPost.IsFinished = false;
                    newPost.location = currentUser.location;
                    newPost.text = editTextDescription.getText().toString();
                    newPost.timestamp = getCurrentDate();
                    newPost.userKey = currentUser.key;
                    PostsData.getInstance().addNewPost(newPost, getContext());
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new DashboardFragment())
                            .commit();
                }
            }
        });

        spinnerCaseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                caseTypeIndex = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                caseTypeIndex = -1;
            }
        });

        spinnerPetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                petTypeIndex = i;

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                petTypeIndex = -1;
            }
        });
    }

    private Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    private void startGetPictureDialog() {
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
                newPost.image = bitmapImage;
                this.imageViewAddPicture.setImageBitmap(bitmapImage);

            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if(bitmap != null){
                newPost.image = bitmap;
                this.imageViewAddPicture.setImageBitmap(bitmap);
            }
        }
    }

    private void initializeUI(View view) {
        imageViewAddPicture = view.findViewById(R.id.addPost_petPicture);
        btnAddPicture= view.findViewById(R.id.addPost_btnAddPicture);
        btnAddNewPost= view.findViewById(R.id.addPost_btnAddNewPost);
        editTextPetName= view.findViewById(R.id.addPost_petName);
        editTextDescription= view.findViewById(R.id.addPost_description);
        spinnerCaseType= view.findViewById(R.id.addPost_caseTypeSpinner);
        spinnerPetType= view.findViewById(R.id.addPost_petTypeSpinner);
    }

    private boolean checkFieldsValues() {
        String petName, description;
        petName = editTextPetName.getText().toString();
        description = editTextDescription.getText().toString();

        if (TextUtils.isEmpty(petName) || TextUtils.isEmpty(description)) {
            Toast.makeText(getActivity(), "Please enter pet name and short description.", Toast.LENGTH_LONG).show();
            return false;
        }else if(imageViewAddPicture.getDrawable() == null){
            Toast.makeText(getActivity(), "Please get pet picture.", Toast.LENGTH_LONG).show();
            return false;
        }else if(caseTypeIndex == -1 || petTypeIndex == -1){
            Toast.makeText(getActivity(), "Please select values from dropdown menu.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}