package com.omaressam.instagram.view.addPost;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.omaressam.instagram.R;
import com.omaressam.instagram.models.Post;
import com.omaressam.instagram.utils.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class AddPostFragment extends Fragment implements View.OnClickListener {

    private static final int GALLERY_PICK = 100;

    private static final int GALLERY_PERMISSION = 200;

    private Button addPost;

    private ImageView Post;


    private Bitmap selectedImage;
    private Uri imageUri;


    private EditText rightAboutImage;

    private NavController navController;

    private FirebaseAuth mAuth;


    private StorageReference mStorageRef;

    public AddPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_posr, container, false);
        setupView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        checkAccessImagesPermission();

    }

    private void setupView(View view) {
        addPost = view.findViewById(R.id.addPost_button);
        Post = view.findViewById(R.id.addPost_imageView);
        rightAboutImage = view.findViewById(R.id.addPost_title_editText);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        addPost.setOnClickListener(this);

    }
    private void savePost() {
        final String title = rightAboutImage.getText().toString();
        final String imagePath = UUID.randomUUID().toString() + ".jpg";

        mStorageRef.child("postImages").child(imagePath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mStorageRef.child("postImages").child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        Post post = new Post();
                        post.setImage(imageURL);
                        post.setTitle(title);

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        assert currentUser != null;
                        post.setUserId(currentUser.getUid());
                        post.setDate(Utilities.getCurrentDate());
                        savePostToDB(post);
                    }
                });
            }
        });
    }



    private void savePostToDB(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts");
        String id = myRef.push().getKey();
        assert id != null;
        myRef.child(id).setValue(post);
        Toast.makeText(getActivity(), "Post added!", Toast.LENGTH_SHORT).show();

        navController.popBackStack();
    }

    private void checkAccessImagesPermission() {
        int permission = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery();
            } else {
                Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                assert imageUri != null;
                InputStream imageStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);

                Post.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), R.string.you_havent_picked_image, Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onClick(View v) {
        savePost();
    }
}
