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
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;

public class FriendRequestsFragment extends Fragment {
    private RecyclerView requestsRecyclerView;
    FriendRequestsRecViewAdapter adapter;


    public FriendRequestsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        requestsRecyclerView = view.findViewById(R.id.friendRequests_recView);
        adapter = new FriendRequestsRecViewAdapter(getContext());
        HashMap<String, Boolean> friendRequests = UsersData.getInstance()
                .getCurrentLoggedUser()
                .friendRequests;
        ArrayList<String> usersKeysList;
        if(friendRequests != null){
            Set<String> usersKeySet = UsersData.getInstance()
                    .getCurrentLoggedUser()
                    .friendRequests.keySet();
            usersKeysList = new ArrayList<>(usersKeySet);
//            adapter.setUsersKeyList(usersKeysList);
        }else
            usersKeysList = new ArrayList<String>();
        adapter.setUsersKeyList(usersKeysList);
        requestsRecyclerView.setAdapter(adapter);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}