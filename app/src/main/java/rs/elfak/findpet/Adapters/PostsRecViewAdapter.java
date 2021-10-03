package rs.elfak.findpet.Adapters;

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

import rs.elfak.findpet.R;
import rs.elfak.findpet.data_models.Post;

public class PostsRecViewAdapter extends RecyclerView.Adapter<PostsRecViewAdapter.ViewHolder> {

    private ArrayList<Post> posts = new ArrayList<Post>();

    public PostsRecViewAdapter() {

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
        holder.userName.setText(posts.get(position).userName);
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
