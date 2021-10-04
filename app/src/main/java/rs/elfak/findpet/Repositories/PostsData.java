//package rs.elfak.findpet.Repositories;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.location.Location;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//
//
//import rs.elfak.findpet.RepositoryEventListeners.PostsListEventListener;
//import rs.elfak.findpet.data_models.Post;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class PostsData {
//    private static final String TAG = "PostsData";
//    private ArrayList<Post> posts;
//    private HashMap<String, Integer> postsKeyIndexMapping;
//    private DatabaseReference dbReference;
//    private static final String FIREBASE_CHILD = "posts";
//    private PostsListEventListener updateListener;
//    private String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//
//    private final ChildEventListener childEventListener = new ChildEventListener() {
//        private static final long ONE_MEGABYTE = 1024 * 1024;
//
//        @Override
//        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            String postKey = snapshot.getKey();
//            if (!postsKeyIndexMapping.containsKey(postKey)) {
//                Post post = snapshot.getValue(Post.class);
//                post.key = postKey;
//                posts.add(post);
//                postsKeyIndexMapping.put(postKey, posts.size() - 1);
//                if (updateListener != null) {
//                    updateListener.OnPostsListUpdated();
//                }
//                //todo handle profile picture
//                storageReference.child("images").child(post.key + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        post.image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        posts.add(post);
//                        postsKeyIndexMapping.put(postKey, posts.size() - 1);
//                        if (updateListener != null) {
//                            updateListener.OnPostsListUpdated();
//                        }
//                    }
//                });
//            }
//        }
//
//        @Override
//        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//            String userKey = snapshot.getKey();
//            User user = snapshot.getValue(User.class);
//            user.key = userKey;
//            //todo handle profile picture
//            if(usersKeyIndexMapping.containsKey(userKey)){
//                int index = usersKeyIndexMapping.get(userKey);
//                users.set(index, user);
//            }
//            else{
//                users.add(user);
//                usersKeyIndexMapping.put(userKey, users.size() - 1);
//            }
//            if (updateListener != null) {
//                updateListener.OnUsersListUpdated();
//            }
//        }
//
//        @Override
//        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//            String userKey = snapshot.getKey();
//            if (usersKeyIndexMapping.containsKey(userKey)) {
//                int index = usersKeyIndexMapping.get(userKey);
//                users.remove(index);
//                recreateKeyIndexMapping();
//            }
//            if (updateListener != null) {
//                updateListener.OnUsersListUpdated();
//            }
//        }
//
//        @Override
//        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//    };
//
//    private void recreateKeyIndexMapping() {
//        usersKeyIndexMapping.clear();
//        for (int i = 0; i < users.size(); i++) {
//            usersKeyIndexMapping.put(users.get(i).key, i);
//        }
//    }
//
//    private final ValueEventListener parentEventListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            if (updateListener != null)
//                updateListener.OnUsersListUpdated();
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//    };
//
//    public PostsData() {
//        users = new ArrayList<>();
//        usersKeyIndexMapping = new HashMap<>();
//        dbReference = FirebaseDatabase.getInstance().getReference();
//        currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
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
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        startListeners();
//    }
//
//    public void startListeners() {
//        dbReference.child(FIREBASE_CHILD).addChildEventListener(childEventListener);
//        dbReference.child(FIREBASE_CHILD).addListenerForSingleValueEvent(parentEventListener);
//    }
//
//    public void destroy() {
//        dbReference.child(FIREBASE_CHILD).removeEventListener(childEventListener);
//        dbReference.child(FIREBASE_CHILD).removeEventListener(parentEventListener);
//    }
//
//
//    public void updateLocation(Location lastLocation, String postKey) {
//        if (!currentUserUID.equals("")) {
//            dbReference.child(FIREBASE_CHILD).child(postKey).child("location").child("latitude")
//                    .setValue(String.valueOf(lastLocation.getLatitude()));
//            dbReference.child(FIREBASE_CHILD).child(postKey).child("location").child("longitude")
//                    .setValue(String.valueOf(lastLocation.getLongitude()));
//        }
//    }
//
//    private static class SingletonHolder {
//        public static final PostsData instance = new PostsData();
//    }
//
//    public static PostsData getInstance() {
//        return SingletonHolder.instance;
//    }
//
//    public ArrayList<Post> getPosts() {
//        return posts;
//    }
//
//    public void addNewPost(Post post) {
//        String key = user.key;
//        users.add(user);
//        usersKeyIndexMapping.put(key, users.size() - 1);
//        DatabaseReference newUserRef = dbReference.child(FIREBASE_CHILD).child(key);
//        newUserRef.setValue(user);
//
////        ByteArrayOutputStream baos = new ByteArrayOutputStream();
////        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_icon);
////        korisnik.setDrawable(context.getResources().getDrawable(R.drawable.user_icon, context.getTheme()));
////        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
////        byte[] data = baos.toByteArray();
////        storageReference.child(key + ".png").putBytes(data).addOnFailureListener(new OnFailureListener() {
////            @Override
////            public void onFailure(@NonNull @NotNull Exception e) {
////                Toast.makeText(context, "Doslo je do greske! Slika nije postavljena!", Toast.LENGTH_SHORT).show();
////            }
////        });
//    }
//
//    public User getUser(int index) {
//        return users.get(index);
//    }
//
//    public User getUser(String key) {
//        if (usersKeyIndexMapping.containsKey(key))
//            return getUser(usersKeyIndexMapping.get(key));
//        return null;
//    }
//
//    public void updatePost(String key, Post postUpdate) {
//        Post post =
//        dbReference.child(FIREBASE_CHILD).child(key).setValue(username);
//    }
//
//    public void setUpdateListener(PostsListEventListener listener) {
//        this.updateListener = listener;
//    }
//
//}
