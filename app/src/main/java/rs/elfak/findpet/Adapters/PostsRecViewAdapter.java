package rs.elfak.findpet.Adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.findpet.Fragments.Communicators.FragmentCommunicator;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.PetFilterModel;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class PostsRecViewAdapter extends RecyclerView.Adapter<PostsRecViewAdapter.ViewHolder> {

    private ArrayList<Post> posts = new ArrayList<Post>();

    //for relationship
    private HashMap<String, User> userOfPost = new HashMap<>();

    //event listener
    private FragmentCommunicator communicator;

    public PostsRecViewAdapter() {

    }

    public void setCommunicator(FragmentCommunicator communicator) {
        this.communicator = communicator;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //HACK: Avoid this error
        //Do not treat position as fixed; only use immediately and call `holder.getAdapterPosition()` to look it up later
        int i = position;
        String postKey = posts.get(i).key;
        String userKey = posts.get(i).userKey;

        //relationship
        userOfPost.put(posts.get(i).key, UsersData.getInstance().getUser(userKey));

        //header
        Bitmap userProfileImage = getUserProfileImage(i);
        holder.userImage.setImageBitmap(userProfileImage);
        holder.userName.setText(userOfPost.get(postKey).username);
        holder.timestamp.setText(Helpers.formatDate(posts.get(i).timestamp));

        //post details
        holder.postText.setText(posts.get(i).text);
        holder.postImage.setImageBitmap(posts.get(i).image);

        //buttons and clickable elements
        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + userOfPost.get(postKey).phoneNumber));
                view.getContext().startActivity(callIntent);
            }
        });
        holder.btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:" + userOfPost.get(postKey).phoneNumber));
                sendIntent.putExtra("sms_body", "");
                view.getContext().startActivity(sendIntent);
            }
        });
        holder.btnShowOnMap.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PetFilterModel filterModel = new PetFilterModel();
                filterModel.name = posts.get(i).pet.name;
                filterModel.petType = posts.get(i).pet.type;
                filterModel.caseType = posts.get(i).caseType;
                filterModel.postKey = posts.get(i).key;

                //HACK
                //there are not other way to get FragmentManager and go to other fragment
                communicator.showPetOnMap(filterModel);

//                Bundle bundle = new Bundle();
//                bundle.putSerializable(Constants.USER_KEY, userOfPost.get(posts.get(i).userKey));
////                bundle.putSerializable(Constants.FREINDS_KEY, users);
//                PetsFragment petsFragment = new PetsFragment(new PetFilterModel());
//                petsFragment.setArguments(bundle);
//                AppCompatActivity activity = (AppCompatActivity) view.getContext().gets;
//                ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, petsFragment).commit();

            }
        });
        holder.profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO delegate to user profile review
            }
        });

    }



//    private String getUserUsername(int position) {
//        String userKey = posts.get(position).userKey;
//        User user = UsersData.getInstance().getUser(userKey);
//        if(user != null)
//            return user.username;
//        return null;
//    }

    private String getPhoneNumber(int position) {
        String userKey = posts.get(position).userKey;
        User user = UsersData.getInstance().getUser(userKey);
        if(user != null)
            return user.username;
        return null;
    }

    private Bitmap getUserProfileImage(int position) {
        String userKey = posts.get(position).userKey;
        User user = UsersData.getInstance().getUser(userKey);
        if(user != null)
            return user.profilePicture;
        return null;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged(); //refresh recView with new data
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView userImage;
        private TextView userName;
        private TextView timestamp;
        private TextView postText;
        private ImageView postImage;
        private Button btnCall;
        private Button btnMessage;
        private Button btnShowOnMap;
        private RelativeLayout profileCard; //TODO on click show profile review

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initUIElements(itemView);

        }

        private void initUIElements(@NonNull View itemView) {
            userImage = itemView.findViewById(R.id.post_userImage);
            userName = itemView.findViewById(R.id.post_userName);
            timestamp = itemView.findViewById((R.id.post_time));
            postText = itemView.findViewById(R.id.post_text);
            btnCall = itemView.findViewById(R.id.post_btnCall);
            btnMessage = itemView.findViewById(R.id.post_btnMessage);
            btnShowOnMap = itemView.findViewById(R.id.post_btnShowOnMap);
            postImage = itemView.findViewById(R.id.post_postImage);
            profileCard = itemView.findViewById((R.id.post_profileCard));


        }
    }
}
