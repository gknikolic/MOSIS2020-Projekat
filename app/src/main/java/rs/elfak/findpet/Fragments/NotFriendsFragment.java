package rs.elfak.findpet.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import rs.elfak.findpet.Adapters.FriendRequestsRecViewAdapter;
import rs.elfak.findpet.Adapters.NotFriendsRecViewAdapter;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;

public class NotFriendsFragment extends Fragment {

    private RecyclerView notFriendsRecyclerView;
    NotFriendsRecViewAdapter adapter;


    public NotFriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_not_friends, container, false);

        notFriendsRecyclerView = view.findViewById(R.id.notFriends_recView);
        adapter = new NotFriendsRecViewAdapter(getContext());

        ArrayList<User> notFriendUsers = new ArrayList<>();
        User currentLoggedUser = UsersData.getInstance().getCurrentLoggedUser();
        for(User user: UsersData.getInstance().getUsers()){
            if(currentLoggedUser.key.equals(user.key))
                continue;
            if(!currentLoggedUser.friends.containsKey(user.key)){
                notFriendUsers.add(user);
            }
        }
        adapter.setNotFriendsList(notFriendUsers);
        notFriendsRecyclerView.setAdapter(adapter);
        notFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}