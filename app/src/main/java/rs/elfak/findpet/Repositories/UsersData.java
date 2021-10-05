package rs.elfak.findpet.Repositories;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.data_models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class UsersData {
    private static final String TAG = "UsersData";
    private ArrayList<User> users;
    private HashMap<String, Integer> usersKeyIndexMapping;
    private DatabaseReference dbReference;
    private static final String FIREBASE_CHILD = "users";
    private UsersListEventListener updateListener;
    private String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    public static final String IMAGE_FORMAT = ".jpg";


    private final ChildEventListener childEventListener = new ChildEventListener() {
        private static final long MAX_SIZE = 1028 * 1028 * 20;

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String userKey = snapshot.getKey();
            if (!usersKeyIndexMapping.containsKey(userKey)) {
                User user = snapshot.getValue(User.class);
                user.key = userKey;
                try{
                    if (user.profilePictureUploaded){
                        storageReference.child("profilePictures").child(user.key + IMAGE_FORMAT).getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                user.profilePicture= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                users.add(user);
                                usersKeyIndexMapping.put(userKey, users.size() - 1);
                                if (updateListener != null) {
                                    updateListener.OnUsersListUpdated();
                                }
                                Log.i("USER ON ADD", "===========================ADDED USER WITH PROFILE PICTURE " + user.username);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                users.add(user);
                                usersKeyIndexMapping.put(userKey, users.size()-1);
                                Log.i("OnFailure", "##############################FAILED############################");
                                if (updateListener != null) {
                                    updateListener.OnUsersListUpdated();
                                }
                            }
                        });
                    }
                    else{
                        users.add(user);
                        usersKeyIndexMapping.put(userKey, users.size()-1);
                        Log.i("USER ON ADD", "===========================ADDED USER WITHOUT PROFILE PICTURE " + user.username);
                        if (updateListener != null) {
                            updateListener.OnUsersListUpdated();
                        }
                    }

                }catch (Exception e){
                    users.add(user);
                    usersKeyIndexMapping.put(userKey, users.size()-1);
                    if (updateListener != null) {
                        updateListener.OnUsersListUpdated();
                    }
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String userKey = snapshot.getKey();
            User user = snapshot.getValue(User.class);
            user.key = userKey;

            if(usersKeyIndexMapping.containsKey(userKey)){
                if (user.profilePictureUploaded) {
                    storageReference.child("profilePictures").child(user.key + IMAGE_FORMAT).getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            user.profilePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            int index = usersKeyIndexMapping.get(userKey);
                            users.set(index, user);
                            if (updateListener != null) {
                                updateListener.OnUsersListUpdated();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int index = usersKeyIndexMapping.get(userKey);
                            users.set(index, user);
                            if (updateListener != null) {
                                updateListener.OnUsersListUpdated();
                            }
                        }
                    });
                }
                else {
                    int index = usersKeyIndexMapping.get(userKey);
                    users.set(index, user);
                    if (updateListener != null) {
                        updateListener.OnUsersListUpdated();
                    }
                }
            }
            else{
                if (user.profilePictureUploaded){
                    storageReference.child("profilePictures").child(user.key + IMAGE_FORMAT).getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            user.profilePicture= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            users.add(user);
                            usersKeyIndexMapping.put(userKey, users.size() - 1);
                            if (updateListener != null) {
                                updateListener.OnUsersListUpdated();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            users.add(user);
                            usersKeyIndexMapping.put(userKey, users.size() - 1);
                            if (updateListener != null) {
                                updateListener.OnUsersListUpdated();
                            }
                        }
                    });
                }else {
                    users.add(user);
                    usersKeyIndexMapping.put(userKey, users.size() - 1);
                    if (updateListener != null) {
                        updateListener.OnUsersListUpdated();
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            String userKey = snapshot.getKey();
            if (usersKeyIndexMapping.containsKey(userKey)) {
                int index = usersKeyIndexMapping.get(userKey);
                users.remove(index);
                recreateKeyIndexMapping();
            }
            if (updateListener != null) {
                updateListener.OnUsersListUpdated();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void recreateKeyIndexMapping() {
        usersKeyIndexMapping.clear();
        for (int i = 0; i < users.size(); i++) {
            usersKeyIndexMapping.put(users.get(i).key, i);
        }
    }

    private final ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (updateListener != null)
                updateListener.OnUsersListUpdated();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public UsersData() {
        users = new ArrayList<>();
        usersKeyIndexMapping = new HashMap<>();
        dbReference = FirebaseDatabase.getInstance().getReference();
        currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        Query query = dbReference.child("users");
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot ds: snapshot.getChildren()){
//                    String userKey = ds.getKey();
//                    if(!usersKeyIndexMapping.containsKey(userKey)){
//                        User user = ds.getValue(User.class);
//                        user.key = userKey;
//                        users.add(user);
//                        usersKeyIndexMapping.put(userKey, users.size() - 1);
//                        Log.i("USER IN CTOR", "===========================ADDED USER  " + user.username);
////                        todo insert code for profile picture retrieving
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        startListeners();
    }

    public void startListeners() {
        dbReference.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
        dbReference.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
    }

    public void destroy() {
        dbReference.child(FIREBASE_CHILD).removeEventListener(childEventListener);
        dbReference.child(FIREBASE_CHILD).removeEventListener(parentEventListener);
    }

    public void changeUserProfilePicture(Bitmap bitmap, Context context) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        if(getCurrentLogedUser().profilePictureUploaded)
            storageReference.child("profilePictures").child(currentUserUID + IMAGE_FORMAT).delete();
        storageReference.child("profilePictures").child(currentUserUID + IMAGE_FORMAT).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getUser(currentUserUID).profilePicture = bitmap;
                getCurrentLogedUser().profilePictureUploaded = true;
                updateUser(currentUserUID);
                Toast.makeText(context, "Profile picture changed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error occurred! Profile picture not changed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateLocation(Location lastLocation) {
        if (!currentUserUID.equals("")) {
            dbReference.child("users").child(currentUserUID).child("location").child("latitude")
                    .setValue(String.valueOf(lastLocation.getLatitude()));
            dbReference.child("users").child(currentUserUID).child("location").child("longitude")
                    .setValue(String.valueOf(lastLocation.getLongitude()));
        }
    }

    private static class SingletonHolder {
        public static final UsersData instance = new UsersData();
    }

    public static UsersData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addNewUser(User user) {
        String key = user.key;
        user.profilePictureUploaded = false;
        users.add(user);
        usersKeyIndexMapping.put(key, users.size() - 1);
        DatabaseReference newUserRef = dbReference.child(FIREBASE_CHILD).child(key);
        newUserRef.setValue(user);

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.avatar);
//        user.profilePicture = bitmap;
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//        storageReference.child(key + ".png").putBytes(data).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(context, "Doslo je do greske! Slika nije postavljena!", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public User getUser(int index) {
        return users.get(index);
    }

    public User getUser(String key) {
        if (usersKeyIndexMapping.containsKey(key))
            return getUser(usersKeyIndexMapping.get(key));
        return null;
    }

    public void updateUser(String uid) {
        User user = getUser(uid);
        dbReference.child(FIREBASE_CHILD).child(user.key).setValue(user);
    }

    public void setUpdateListener(UsersListEventListener listener) {
        updateListener = listener;
    }

    public User getCurrentLogedUser(){
        return getUser(currentUserUID);
    }
}
