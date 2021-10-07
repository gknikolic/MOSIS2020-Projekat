package rs.elfak.findpet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import rs.elfak.findpet.Adapters.PostsRecViewAdapter;
import rs.elfak.findpet.Fragments.Communicators.FragmentCommunicator;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.PostsData;
import rs.elfak.findpet.RepositoryEventListeners.PostsListEventListener;
import rs.elfak.findpet.data_models.PetFilterModel;

public class DashboardFragment extends Fragment implements PostsListEventListener {
    private RecyclerView postsRecView;
    private TextView username;
    private FloatingActionButton fab;
    private PostsRecViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        postsRecView = view.findViewById(R.id.posts_rec_view);
        adapter = new PostsRecViewAdapter();
        adapter.setPosts(PostsData.getInstance().getPosts());
        adapter.setCommunicator(new FragmentCommunicator() {
            @Override
            public void showPetOnMap(PetFilterModel filterModel) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.USER_KEY, filterModel.userKey);
                //bundle.putSerializable(Constants.FREINDS_KEY, users);
                PetsFragment petsFragment = new PetsFragment(filterModel);
                petsFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, petsFragment).commit();
            }
        });

        postsRecView.setAdapter(adapter);
        postsRecView.setLayoutManager(new LinearLayoutManager(getContext()));

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PostsData.getInstance().addUpdateListener(this);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPostFragment addPostFragment = new AddPostFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addPostFragment).commit();
            }
        });


        this.username = getView().findViewById(R.id.nav_user);
    }

    @Override
    public void OnPostsListUpdated() {
        //PostsRecViewAdapter adapter = new PostsRecViewAdapter();
        adapter.setPosts(PostsData.getInstance().getPosts());

        postsRecView.setAdapter(adapter);
        postsRecView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
