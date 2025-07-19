package com.kundevahal.lostnfound;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import static android.app.Activity.RESULT_OK;

public class HandoverFormFragment extends Fragment {

    private static final String ARG_ITEM = "item";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Item item;
    private EditText userNameEditText, userPhoneEditText;
    private Button confirmHandoverButton, takePhotoButton;
    private ImageView itemImageView, capturedImageView;
    private TextView itemNameTextView, itemLocationTextView, itemDateTimeTextView;
    private ProgressBar progressBar;
    private View formContainer;
    private Bitmap capturedBitmap;

    public static HandoverFormFragment newInstance(Item item) {
        HandoverFormFragment fragment = new HandoverFormFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (Item) getArguments().getSerializable(ARG_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handover_form, container, false);

        // Initialize views
        userNameEditText = view.findViewById(R.id.editTextUserName);
        userPhoneEditText = view.findViewById(R.id.editTextUserPhone);
        confirmHandoverButton = view.findViewById(R.id.buttonConfirmHandover);
        takePhotoButton = view.findViewById(R.id.buttonTakePhoto);
        itemImageView = view.findViewById(R.id.imageViewItem);
        itemNameTextView = view.findViewById(R.id.textViewItemName);
        itemLocationTextView = view.findViewById(R.id.textViewItemLocation);
        itemDateTimeTextView = view.findViewById(R.id.textViewItemDateTime);
        progressBar = view.findViewById(R.id.progressBar);
        formContainer = view.findViewById(R.id.formContainer);
        capturedImageView = view.findViewById(R.id.imageViewCaptured);

        // Show ProgressBar while data loads
        showLoading(true);

        // Set item details in the UI
        if (item != null) {
            itemNameTextView.setText(item.getName());
            itemLocationTextView.setText(item.getLocation());
            itemDateTimeTextView.setText(item.getDateTime());

            // Load item image using Glide
            Glide.with(this).load(item.getItemImageUrl()).into(itemImageView);

            // Once data is set, hide ProgressBar and show the actual views
            showLoading(false);
        }

        takePhotoButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        confirmHandoverButton.setOnClickListener(v -> {
            showLoading(true); // Show the loading progress bar when confirming the handover
            confirmHandover();
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedBitmap = (Bitmap) extras.get("data");
            capturedImageView.setImageBitmap(capturedBitmap);
            capturedImageView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void confirmHandover() {
        String userName = userNameEditText.getText().toString().trim();
        String userPhone = userPhoneEditText.getText().toString().trim();

        // Check if all fields are filled
        if (userName.isEmpty() || userPhone.isEmpty() || capturedBitmap == null) {
            Toast.makeText(getContext(), "Please fill in all fields and take a photo.", Toast.LENGTH_SHORT).show();
            showLoading(false); // Hide loading if validation fails
            return;
        } else if (userName.length()>20) {
            Toast.makeText(getContext(), "Invaid! Too long person name", Toast.LENGTH_SHORT).show();
            showLoading(false); // Hide loading if validation fails
            return;
        }

        // Check if the phone number matches
        if (!userPhone.equals(item.getPhoneNumber())) {
            Toast.makeText(getContext(), "Phone number does not match the owner's phone number.", Toast.LENGTH_SHORT).show();
            showLoading(false); // Hide loading if validation fails
            return; // Exit the method if the phone numbers do not match
        }

        uploadImageAndSaveRecord();
    }

    private void uploadImageAndSaveRecord() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("handover_photos/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();
                    fetchAcceptedItemDataAndSave(photoUrl);
                });
            } else {
                Toast.makeText(getContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show();
                showLoading(false); // Hide loading if upload fails
            }
        });
    }

    private void fetchAcceptedItemDataAndSave(String photoUrl) {
        DatabaseReference acceptedItemRef = FirebaseDatabase.getInstance().getReference("accepted_items").child(item.getItemId());
        acceptedItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve data from accepted_items
                    String location = dataSnapshot.child("location").getValue(String.class);
                    String email = dataSnapshot.child("userEmail").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);

                    // Save the handover record with additional data
                    saveRecordToDatabase(photoUrl, location, email, phoneNumber, description);
                } else {
                    Toast.makeText(getContext(), "Failed to fetch accepted item details.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch accepted item details.", Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }

    private void saveRecordToDatabase(String photoUrl, String location, String email, String phoneNumber, String description) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("handover_records");
        String recordId = databaseRef.push().getKey();

        // Create the handover item to save
        HandoverItem handoverItem = new HandoverItem(recordId, item.getName(), userNameEditText.getText().toString().trim(), userPhoneEditText.getText().toString().trim(), photoUrl, location, email, phoneNumber, description);

        if (recordId != null) {
            databaseRef.child(recordId).setValue(handoverItem).addOnCompleteListener(task -> {
                showLoading(false); // Hide loading after saving
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Handover record saved successfully.", Toast.LENGTH_SHORT).show();
                    deleteAcceptedItem(); // Call method to delete the accepted item from the list
                    Intent intent = new Intent(getContext(), AuthenticatorMainActivity.class);
                    startActivity(intent);

                    // Finish the current activity to prevent going back to it
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to save handover record.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                showLoading(false); // Hide loading on failure
                Toast.makeText(getContext(), "Failed to save handover record.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void deleteAcceptedItem() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("accepted_items").child(item.getItemId());
        databaseRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Item removed from accepted list.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to remove item from accepted list.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
