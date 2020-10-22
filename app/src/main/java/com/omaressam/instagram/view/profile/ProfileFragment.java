package com.omaressam.instagram.view.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.omaressam.instagram.R;
import com.omaressam.instagram.databinding.FragmentProfileBinding;
import com.omaressam.instagram.models.Post;
import com.omaressam.instagram.models.User;
import com.omaressam.instagram.utils.Utilities;
import com.omaressam.instagram.view.home.HomeFragment;
import com.omaressam.instagram.view.home.Post.PostAdapter;
import com.omaressam.instagram.view.splash.SplashActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private ImageView imageProfile;
    private Button buttonProfile;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private Button savePro;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private Bitmap selectedImage;
    private User user;
    private Uri imageUri;
    private static final int GALLERY_PICK = 100;
    private static final int GALLERY_PERMISSION = 200;

    private BookDetailsViewModel bookDetailsViewModel;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        FragmentProfileBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false);

        bookDetailsViewModel = ViewModelProviders.of(requireActivity()).get(BookDetailsViewModel.class);

        binding.setLifecycleOwner(getActivity());

        binding.setViewModel(bookDetailsViewModel);


        bookDetailsViewModel.user.setValue(user);

        satUpView(binding.getRoot());

        return binding.getRoot();

    }

    private void satUpView(View view) {



        imageProfile = view.findViewById(R.id.profile_imageView);

        buttonProfile = view.findViewById(R.id.profile_sign_out_button);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        buttonProfile.setOnClickListener(this);

        savePro = view.findViewById(R.id.choseProfile);
        savePro.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        checkAccessImagesPermission();
        get();
       // savePost();
    }

    private void get() {

        myRef = database.getReference("Users").child(firebaseUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userObject = dataSnapshot.getValue(User.class);


                assert userObject != null;
                Picasso.get()
                        .load(userObject.getImage())
                        .placeholder(R.drawable.img_placeholder)
                        .into(imageProfile);
                      //  savePostToDB(userObject);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "" + error.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePost() {
        final AlertDialog dialog = new SpotsDialog
                .Builder()
                .setContext(getContext())
                .setTheme(R.style.Custom)
                .build();
        dialog.show();

        final String imagePath = UUID.randomUUID().toString() + ".jpg";

        mStorageRef.child("ImageProfile").child(imagePath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageRef.child("ImageProfile").child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        User user = new User();
                        user.setImage(imageURL);

                        Picasso.get()
                                .load(user.getImage())
                                .placeholder(R.drawable.img_placeholder)
                                .into(imageProfile);

                        savePostToDB(imageURL);
                        dialog.dismiss();

                    }
                });
            }
        });
    }

    private void savePostToDB(String image) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(firebaseUser.getUid()).child("image");
        String id = myRef.push().getKey();
        assert id != null;
        myRef.setValue(image);

    }


    private String getResizedBase64(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
    private void checkAccessImagesPermission() {
        int permission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        } else {
            getImageFromGallery();
        }
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_PICK);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                assert imageUri != null;
                InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imageProfile.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), R.string.you_havent_picked_image, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_sign_out_button) {
            mAuth.signOut();
            requireActivity().startActivity(new Intent(getActivity(), SplashActivity.class));

        }else {
          if  (v.getId()==R.id.choseProfile) {
                savePost();
            }
        }
    }
}



