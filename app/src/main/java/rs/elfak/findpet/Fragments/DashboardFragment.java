package rs.elfak.findpet.Fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Objects;

import rs.elfak.findpet.Adapters.PostsRecViewAdapter;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.PostsData;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.RepositoryEventListeners.PostsListEventListener;
import rs.elfak.findpet.data_models.Location;
import rs.elfak.findpet.data_models.Pet;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class DashboardFragment extends Fragment implements PostsListEventListener {
    private RecyclerView postsRecView;
    private TextView username;
    private FloatingActionButton fab;
//    private PostsRecViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPostFragment addPostFragment = new AddPostFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addPostFragment).commit();
            }
        });
        PostsData.getInstance().setUpdateListener(this);
        postsRecView = getView().findViewById(R.id.posts_rec_view);
        PostsRecViewAdapter adapter = new PostsRecViewAdapter();
        adapter.setPosts(PostsData.getInstance().getPosts());

        postsRecView.setAdapter(adapter);
        postsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        this.username = getView().findViewById(R.id.nav_user);
    }

    @Override
    public void OnPostsListUpdated() {
        PostsRecViewAdapter adapter = new PostsRecViewAdapter();
        adapter.setPosts(PostsData.getInstance().getPosts());

        postsRecView.setAdapter(adapter);
        postsRecView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
