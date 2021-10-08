package rs.elfak.findpet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;

public class NotFriendsRecViewAdapter extends RecyclerView.Adapter<NotFriendsRecViewAdapter.MyViewHolder> {

    private ArrayList<User> notFriendsList = new ArrayList<>();
    Context context;

    public NotFriendsRecViewAdapter(Context context){
        this.context = context;
    }

    public void setNotFriendsList(ArrayList<User> notFriendsList) {
        this.notFriendsList = notFriendsList;
    }

    public ArrayList<User> getNotFriendsList() {
        return notFriendsList;
    }

    @NonNull
    @Override
    public NotFriendsRecViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.not_friends_item, parent, false);
        NotFriendsRecViewAdapter.MyViewHolder holder = new NotFriendsRecViewAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotFriendsRecViewAdapter.MyViewHolder holder, int position) {
        holder.fullName.setText(notFriendsList.get(position).fullName);
        holder.email.setText(notFriendsList.get(position).email);
        holder.profileImage.setImageBitmap(notFriendsList.get(position).profilePicture);

        holder.sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersData.getInstance().sendFriendRequest(notFriendsList.get(position).key, context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notFriendsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fullName;
        TextView email;
        ImageView profileImage;
        Button sendFriendRequestBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.notFriends_fullname);
            email = itemView.findViewById(R.id.notFriends_email);
            profileImage = itemView.findViewById(R.id.notFriends_profilePicture);
            sendFriendRequestBtn = itemView.findViewById(R.id.notFriends_btnSendRequest);
        }
    }
}
