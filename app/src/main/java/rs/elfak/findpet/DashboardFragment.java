package rs.elfak.findpet;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Objects;

import rs.elfak.findpet.Adapters.PostsRecViewAdapter;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.Location;
import rs.elfak.findpet.data_models.Pet;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class DashboardFragment extends Fragment {
    private RecyclerView postsRecView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsRecView = getView().findViewById(R.id.posts_rec_view);

        ArrayList<Post> posts = new ArrayList<>();
        posts = HardcodePosts();
        //TODO Populate posts from db

        PostsRecViewAdapter adapter = new PostsRecViewAdapter();
        adapter.setPosts(posts);

        postsRecView.setAdapter(adapter);
        postsRecView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private ArrayList<Post> HardcodePosts() {
        ArrayList<Post> posts = new ArrayList<>();
        Post post = new Post();
        post.user = new User();
        post.pet = new Pet();
        post.location = new Location();
        post.user.username = "Nemanja";
        post.user.phoneNumber="0648546006";
        post.location = new Location("43.3191867", "21.9121003");
        post.text = "Izgubljen je pas star oko 2 godine, odaziva se na ime Džeki.";
        post.timestamp = new Date(System.currentTimeMillis());
        post.image = Helpers.drawableFromUrl("https://images.unsplash.com/photo-1583337130417-3346a1be7dee?ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8cGV0c3xlbnwwfHwwfHw%3D&ixlib=rb-1.2.1&w=1000&q=80");
        post.user.profilePicture = Helpers.drawableFromUrl("https://images.unsplash.com/photo-1529665253569-6d01c0eaf7b6?ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8cHJvZmlsZXxlbnwwfHwwfHw%3D&ixlib=rb-1.2.1&w=1000&q=80");
        posts.add(post);
        post = new Post();
        post.user = new User();
        post.pet = new Pet();
        post.location = new Location();
        post.user.username = "Zeljko";
        post.user.phoneNumber="0648546006";
        post.location = new Location("43.3191867", "21.9121003");
        post.text = "Izgubljen je pas star.";
        post.timestamp = new Date(System.currentTimeMillis());
        post.image = Helpers.drawableFromUrl("https://images.indianexpress.com/2021/04/puppy-1903313_1280.jpg");
        post.user.profilePicture = Helpers.drawableFromUrl("https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg");
        posts.add(post);

        return posts;

    }
}
