package rs.elfak.findpet.Adapters;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.Hold;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;

public class FriendRequestsRecViewAdapter  extends RecyclerView.Adapter<FriendRequestsRecViewAdapter.MyViewHolder>{

    private ArrayList<String> usersKeyList = new ArrayList<>();
    Context context;

    public ArrayList<String> getUsersKeyList() {
        return usersKeyList;
    }

    public void setUsersKeyList(ArrayList<String> usersKeyList) {
        this.usersKeyList = usersKeyList;
    }

    public FriendRequestsRecViewAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String userKey = usersKeyList.get(position);
        User user = UsersData.getInstance()
                .getUser(userKey);
        holder.fullname.setText(user.fullName);
        holder.email.setText(user.email);
        holder.profileImage.setImageBitmap(user.profilePicture);

        holder.acceptRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersData.getInstance().acceptFriendRequest(userKey, context);
            }
        });

        holder.refuseRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersData.getInstance().removeFriendRequest(userKey, context);

            }
        });
    }

    @Override
    public int getItemCount() {
        int size;
        HashMap<String, Boolean> friendRequests = UsersData.getInstance()
                .getCurrentLoggedUser().friendRequests;
        if(friendRequests != null)
            return UsersData.getInstance()
                    .getCurrentLoggedUser()
                    .friendRequests.size();
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView fullname;
        TextView email;
        ImageView profileImage;
        Button acceptRequestBtn;
        Button refuseRequestBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.friendRequest_fullname);
            email = itemView.findViewById(R.id.friendRequest_email);
            profileImage = itemView.findViewById(R.id.friendRequests_profilePicture);
            acceptRequestBtn = itemView.findViewById(R.id.friendRequests_btnAccept);
            refuseRequestBtn = itemView.findViewById(R.id.friendRequests_btnRefuse);
        }
    }
}
