package rs.elfak.findpet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Date;
import java.util.ArrayList;

import rs.elfak.findpet.data_models.Post;

public class DashboardFragment extends Fragment {
    private RecyclerView postsRecView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postsRecView = getView().findViewById(R.id.posts_rec_view);

        ArrayList<Post> posts = new ArrayList<>();

        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

}
