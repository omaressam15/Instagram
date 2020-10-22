package com.omaressam.instagram.view.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.omaressam.instagram.R;
import com.omaressam.instagram.models.Post;
import com.omaressam.instagram.models.User;
import com.omaressam.instagram.utils.OnLikeClicked;
import com.omaressam.instagram.view.home.Post.PostAdapter;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class HomeFragment extends Fragment implements OnLikeClicked {

    RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> posts;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setUpView(view);
        getPosts();

        return view;
    }

    private void setUpView(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        posts = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Posts");

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

    }

    private void getPosts() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    final Post post = snapshot.getValue(Post.class);
                    assert post != null;
                    post.setId(id);
                    DatabaseReference usersRef = database.getReference("Users").child(post.getUserId());

                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            post.setUser(user);
                            posts.add(post);
                            postAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                postAdapter = new PostAdapter(posts, HomeFragment.this);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClicked(final int position) {

        Post post = posts.get(position);

        final DatabaseReference likeRef = database.getReference("UserLikes").child(firebaseUser.getUid()).child(post.getId()).child("didLike");

        final DatabaseReference myRef = database.getReference("Posts").child(post.getId()).child("numberOfLikes");
        final int numberOfLiked = post.getNumberOfLikes();

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean didLike = dataSnapshot.getValue(Boolean.class);
                if (didLike != null && didLike) {
                    myRef.setValue(numberOfLiked - 1);
                    posts.get(position).setNumberOfLikes(numberOfLiked - 1);

                    likeRef.setValue(false);
                } else {
                    myRef.setValue(numberOfLiked + 1);
                    posts.get(position).setNumberOfLikes(numberOfLiked + 1);

                    likeRef.setValue(true);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
