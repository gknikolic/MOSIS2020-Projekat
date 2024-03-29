package rs.elfak.findpet.Repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.RepositoryEventListeners.PostsListEventListener;
import rs.elfak.findpet.data_models.PetFilterModel;
import rs.elfak.findpet.data_models.Post;
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
import java.util.Iterator;
import java.util.Map;

public class PostsData {
    private static final String TAG = "PostsData";
    private ArrayList<Post> posts;
    private HashMap<String, Integer> postsKeyIndexMapping;
    private final DatabaseReference dbReference;
    private static final String FIREBASE_CHILD = "posts";
    public static final String FIRESTORE_CHILD = "postsImages";
    private static final long MAX_SIZE = 1024 * 1024 * 4;
    private ArrayList<PostsListEventListener> updateListeners = new ArrayList<>();;
    private final StorageReference storageReference;

    private final ChildEventListener childEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String postKey = snapshot.getKey();
            if (!postsKeyIndexMapping.containsKey(postKey)) {
                Post post = snapshot.getValue(Post.class);
                post.key = postKey;
                try{
                    fetchPostImage(post, false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            String postKey = snapshot.getKey();
            Post post = snapshot.getValue(Post.class);
            post.key = postKey;

            if(postsKeyIndexMapping.containsKey(postKey)){
                fetchPostImage(post, true);
            }
            else{
                fetchPostImage(post, false);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            String postKey = snapshot.getKey();
            if (postsKeyIndexMapping.containsKey(postKey)) {
                int index = postsKeyIndexMapping.get(postKey);
                posts.remove(index);
                recreateKeyIndexMapping();
            }
            notifyPostsListUpdated();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void addPost(Post post){
        posts.add(post);
        postsKeyIndexMapping.put(post.key, posts.size() - 1);
        notifyPostsListUpdated();
    }

    private void updateLocalPost(Post post){
        int index = postsKeyIndexMapping.get(post.key);
        posts.set(index, post);
        notifyPostsListUpdated();
    }

    private void fetchPostImage(Post post, boolean updateExisting){
            storageReference.child(FIRESTORE_CHILD).child(post.key + Constants.IMAGE_FORMAT).getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    post.image= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (updateExisting){
                        updateLocalPost(post);
                    }
                    else {
                        addPost(post);
                    }
                    Log.i("POST IMAGE FETCHED", "=========================== POST KEY: " + post.key);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("OnFailure", "############################## GET POST PICTURE FAILED ############################");
                    e.printStackTrace();
                    if(updateExisting) {
                        updateLocalPost(post);
                    }
                    else {
                        addPost(post);
                    }
                }
            });
    }

    private void recreateKeyIndexMapping() {
        postsKeyIndexMapping.clear();
        for (int i = 0; i < posts.size(); i++) {
            postsKeyIndexMapping.put(posts.get(i).key, i);
        }
    }

    private final ValueEventListener parentEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            notifyPostsListUpdated();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private PostsData() {
        posts = new ArrayList<>();
        postsKeyIndexMapping = new HashMap<>();
        dbReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
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

    public void updateLocation(Location lastLocation, String postKey) {
        dbReference.child(FIREBASE_CHILD).child(postKey).child("location").child("latitude")
                .setValue(String.valueOf(lastLocation.getLatitude()));
        dbReference.child(FIREBASE_CHILD).child(postKey).child("location").child("longitude")
                .setValue(String.valueOf(lastLocation.getLongitude()));
    }

    private static class SingletonHolder {
        public static final PostsData instance = new PostsData();
    }

    public static PostsData getInstance() {
        return SingletonHolder.instance;
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void addNewPost(Post post, Context context) {
        DatabaseReference newPostRef = dbReference.child(FIREBASE_CHILD).push();
        String key = newPostRef.getKey();
        post.key = key;
        posts.add(post);
        postsKeyIndexMapping.put(key, posts.size() - 1);
        newPostRef.setValue(post);
        if(post.image != null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            post.image.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
    //        storageReference.child(FIRESTORE_CHILD).child(currentUserUID + Constants.IMAGE_FORMAT).delete();
            storageReference.child(FIRESTORE_CHILD).child(post.key + Constants.IMAGE_FORMAT).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Post picture uploaded");
                    Toast.makeText(context, "Post data successfully added", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("", "Error occurred! Post picture not added.");
                    Toast.makeText(context, "Post data added, picture failed to add!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public Post getPost(int index) {
        return posts.get(index);
    }

    public Post getPost(String key) {
        if (postsKeyIndexMapping.containsKey(key))
            return getPost(postsKeyIndexMapping.get(key));
        return null;
    }

    public void updatePost(String key, Post postUpdated) {
        dbReference.child(FIREBASE_CHILD).child(key).setValue(postUpdated);
    }

    public ArrayList<Post> filterPosts(PetFilterModel filterModel){
//        HashMap<String, Boolean> filteredResults = new HashMap<String, Boolean>();
//        for(Post post: posts){
//            if(filterModel.petType == null)
//                filteredResults.put(post.key, true);
//            else if(post.pet.type == filterModel.petType)
//                filteredResults.put(post.key, true);
//        }
//        if(filterModel.caseType != null) {
//            Iterator<String> iterator = filteredResults.keySet().iterator();
//            while (iterator.hasNext()) {
//                String key = iterator.next();
//                int index = postsKeyIndexMapping.get(key);
//                Post post = posts.get(index);
//                if (post.caseType != filterModel.caseType) {
//                    iterator.remove();
//                }
//            }
//        }
//        if(filterModel.name != null) {
//            Iterator<String> iterator = filteredResults.keySet().iterator();
//            while (iterator.hasNext()) {
//                String key = iterator.next();
//
//                int index = postsKeyIndexMapping.get(key);
//                Post post = posts.get(index);
//                if (post.pet.name != filterModel.name) {
//                    iterator.remove();
//                }
//            }
//        }
//        if(filterModel.radius != -1) {
//            Iterator<String> iterator = filteredResults.keySet().iterator();
//            while (iterator.hasNext()) {
//                String key = iterator.next();
//                int index = postsKeyIndexMapping.get(key);
//                Post post = posts.get(index);
//                double distance = measureCurrentUserToPostDistance(post);
//                if (distance > filterModel.radius) {
//                    iterator.remove();
//                }
//            }
//        }
        ArrayList<Post> filteredResults = new ArrayList<>();
        for(Post post: posts){
            if(filterModel.caseType != null){
                if(filterModel.caseType != post.caseType){
                    continue;
                }
            }
            if(filterModel.petType != null){
                if(filterModel.petType != post.pet.type){
                    continue;
                }
            }
            if(filterModel.name != null){
                if(!filterModel.name.equals(post.pet.name)){
                    continue;
                }
            }
            if(filterModel.radius != -1){
                double distance = measureCurrentUserToPostDistance(post);
                if (distance > (double)filterModel.radius) {
                    continue;
                }
            }
            filteredResults.add(post);
        }
        return filteredResults;
    }

    private double measureCurrentUserToPostDistance(Post post) {
        float[] distance = new float[1];
        User user = UsersData.getInstance().getCurrentLoggedUser();
        Location.distanceBetween(
                post.location.latitude,
                post.location.longitude,
                user.location.latitude,
                user.location.longitude,
                distance
        );
        return distance[0];
    }

    public void addUpdateListener(PostsListEventListener listener) {
        updateListeners.add(listener);
    }

    private void notifyPostsListUpdated() {
        for (PostsListEventListener listener: updateListeners) {
            listener.OnPostsListUpdated();
        }
    }

}
