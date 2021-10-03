package rs.elfak.findpet.Repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import rs.elfak.findpet.RegisterActivity;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.data_models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private final ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String userKey = snapshot.getKey();
            if (!usersKeyIndexMapping.containsKey(userKey)) {
                User user = snapshot.getValue(User.class);
                user.key = userKey;
                users.add(user);
                usersKeyIndexMapping.put(userKey, users.size() - 1);
                if (updateListener != null) {
                    updateListener.OnUsersListUpdated();
                }
                //todo handle profile picture
//                storage.child("images").child(user.email + ".jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        user.profilePicture=new byte[bytes.length];
//                        for (int i = 0; i < bytes.length; i++) {
//                            user.Uimage[i] = bytes[i];
//                        }
//                        users.add(user);
//                        usersKeyIndexMapping.put(userKey, users.size() - 1);
//                        if (updateListener != null) {
//                            updateListener.onListUpdated();
//                        }
//                    }
//                });
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String userKey = snapshot.getKey();
            User user = snapshot.getValue(User.class);
            user.key = userKey;
            //todo handle profile picture
            if(usersKeyIndexMapping.containsKey(userKey)){
                int index = usersKeyIndexMapping.get(userKey);
                users.set(index, user);
            }
            else{
                users.add(user);
                usersKeyIndexMapping.put(userKey, users.size() - 1);
            }
            if (updateListener != null) {
                updateListener.OnUsersListUpdated();
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

        Query query = dbReference.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String userKey = ds.getKey();
                    if(!usersKeyIndexMapping.containsKey(userKey)){
                        User user = ds.getValue(User.class);
                        user.key = userKey;
                        users.add(user);
                        usersKeyIndexMapping.put(userKey, users.size() - 1);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

//    public void uploadPicture(Bitmap bitmap, Park uploadPark, Context context) {
//        String key = database.child("korisnici").child(myUID).child("Slike").push().getKey();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//        storageReference.child(key + ".png").putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                HashMap<String, Boolean> m = new HashMap<>();
//                m.put("liked", false);
//                m.put("faved", false);
//                m.put("mine", true);
//                database.child("korisnici").child(myUID).child("Slike").child(key).setValue(m);
//
//                HashMap<String, Object> m2 = new HashMap<>();
//                m2.put("Owner", myUID);
//                m2.put("numOfLikes", 0);
//                m2.put("Park", uploadPark.key);
//                database.child("Pictures").child(key).setValue(m2);
//                Toast.makeText(context, "Slika postavljena!", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
//                Toast.makeText(context, "Doslo je do greske! Slika nije postavljena!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }

    public void changeUserProfilePicture(Bitmap bitmap, Context context) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        storageReference.child(currentUserUID + ".png").delete();
        storageReference.child(currentUserUID + ".png").putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getUser(currentUserUID).profilePicture = new BitmapDrawable(context.getResources(), bitmap);
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
            dbReference.child("users").child(currentUserUID).child("latitude")
                    .setValue(String.valueOf(lastLocation.getLatitude()));
            dbReference.child("users").child(currentUserUID).child("longitude")
                    .setValue(String.valueOf(lastLocation.getLongitude()));
        }
    }

    private static class SingletonHolder {
        public static final rs.elfak.findpet.Repositories.UsersData instance = new rs.elfak.findpet.Repositories.UsersData();
    }

    public static UsersData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addNewUser(User user) {
        String key = user.key;
        users.add(user);
        usersKeyIndexMapping.put(key, users.size() - 1);
        DatabaseReference newUserRef = dbReference.child(FIREBASE_CHILD).child(key);
        newUserRef.setValue(user);

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_icon);
//        korisnik.setDrawable(context.getResources().getDrawable(R.drawable.user_icon, context.getTheme()));
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] data = baos.toByteArray();
//        storageReference.child(key + ".png").putBytes(data).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
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

    public void updateUser(String uid, String username) {
        User user = getUser(uid);
        user.username = username;
        dbReference.child(FIREBASE_CHILD).child(user.key).child("username").setValue(username);
    }

    public void setUpdateListener(UsersListEventListener listener) {
        updateListener = listener;
    }

    public User getCurrentLogedUser(){
        return getUser(currentUserUID);
    }
}
