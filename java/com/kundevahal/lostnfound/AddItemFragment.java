package com.kundevahal.lostnfound;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddItemFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_ITEM_IMAGE_PICK = 5;
    private static final int REQUEST_ADHAR_IMAGE_PICK = 4;
    private static final int REQUEST_ADHAR_IMAGE_PICK_CAMERA = 6;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private ImageView itemPhoto, adharPhoto;
    private EditText itemName, itemLocation, adharNumber, itemDescription;
    private TextView itemDateTime,userName, phoneNumber;
    private Button submitButton, cancelButton;

    private Uri imageUri, adharImageUri,photoUri;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference,userdatbaseref;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;
    private FirebaseUser user;
    private Bitmap capturedBitmap;
    private Bitmap capturedBitmapadhar;
    private ChipGroup chipGroupImageType;
    private Chip chipOriginalImage, chipLookAlike;
    String firstNameStr,middleNameStr,lastNameStr,mobileNumberStr,fullname;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items");
        storageReference = FirebaseStorage.getInstance().getReference("item_images");
        userdatbaseref = FirebaseDatabase.getInstance().getReference("Users");

        itemPhoto = view.findViewById(R.id.item_photo);
        adharPhoto = view.findViewById(R.id.adhar_card_photo);
        itemName = view.findViewById(R.id.item_name);
        itemLocation = view.findViewById(R.id.item_location);
        adharNumber = view.findViewById(R.id.adhar_number);
        itemDescription = view.findViewById(R.id.item_description);
        userName = view.findViewById(R.id.user_name);
        phoneNumber = view.findViewById(R.id.phone_number);
        itemDateTime = view.findViewById(R.id.item_datetime);
        submitButton = view.findViewById(R.id.submit_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        chipGroupImageType = view.findViewById(R.id.chipGroupImageType);
        chipOriginalImage = view.findViewById(R.id.chipOriginalImage);
        chipLookAlike = view.findViewById(R.id.chipLookAlike);

        NotificationHelper.createNotificationChannel(getActivity());

        // Set user email
        if (user != null) {
            String email = user.getEmail();

            // Fetch additional user details from the database using email as key
            Query userQuery = userdatbaseref.orderByChild("email").equalTo(email);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            firstNameStr = userSnapshot.child("firstname").getValue(String.class);
                            middleNameStr = userSnapshot.child("middlename").getValue(String.class);
                            lastNameStr = userSnapshot.child("lastname").getValue(String.class);
                            mobileNumberStr = userSnapshot.child("contact").getValue(String.class);
                            fullname = firstNameStr+" "+middleNameStr+" "+lastNameStr;

                            // Only set userName when data is fully retrieved
                            userName.setText(fullname);
                            phoneNumber.setText(mobileNumberStr);


                        }
                    } else {
                        Log.e(TAG, "No user data found for email: " + email);
                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: ", error.toException());
                    Toast.makeText(getContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });

        }

        // Set a listener for chip selection changes
        chipGroupImageType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == chipOriginalImage.getId()) {
                Toast.makeText(getContext(), "Original Item Selected", Toast.LENGTH_SHORT).show();
                // Handle selection of "Original Item"
            } else if (checkedId == chipLookAlike.getId()) {
                Toast.makeText(getContext(), "Look Alike Selected", Toast.LENGTH_SHORT).show();
                // Handle selection of "Look Alike"
            }
        });

        itemPhoto.setOnClickListener(v -> showImageSelectionDialog());
        adharPhoto.setOnClickListener(v -> showAdharImageSelectionDialog());
        itemDateTime.setOnClickListener(v -> showDateTimePicker());
        submitButton.setOnClickListener(v -> submitItem());
        cancelButton.setOnClickListener(v -> {
            Fragment homeFragment = new HomeFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, homeFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });



        return view;
    }

    private void showImageSelectionDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Item Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_PICK);
                        } else {
                            openCamera();
                        }
                    } else {
                        openGallery();
                    }
                }).show();
    }

    private void showAdharImageSelectionDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Aadhar Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_ADHAR_IMAGE_PICK);
                        } else {
                            openAdharCamera();
                        }
                    } else {
                        openAdharGallery();
                    }
                }).show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getActivity().getPackageManager())!=null)
        {
            startActivityForResult(cameraIntent, REQUEST_ITEM_IMAGE_PICK);
        }

    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }

    private void openAdharCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_ADHAR_IMAGE_PICK_CAMERA);
    }

    private void openAdharGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_ADHAR_IMAGE_PICK);
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        final Calendar maxDateTime = Calendar.getInstance(); // Current date and time for validation

        // DatePickerDialog to select the date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // TimePickerDialog to select the time
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Validate the selected date and time
                                if (calendar.after(maxDateTime)) {
                                    Toast.makeText(getContext(), "Future date and time are not allowed.", Toast.LENGTH_SHORT).show();
                                    itemDateTime.setText(""); // Reset the field
                                } else {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    itemDateTime.setText(dateFormat.format(calendar.getTime()));
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Restrict DatePicker to not allow future dates
        datePickerDialog.getDatePicker().setMaxDate(maxDateTime.getTimeInMillis());
        datePickerDialog.show();
    }

    private void submitItem() {
        String name = itemName.getText().toString().trim();
        String location = itemLocation.getText().toString().trim();
        String adhar = adharNumber.getText().toString().trim();
        String description = itemDescription.getText().toString().trim();
        String user = userName.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String dateTime = itemDateTime.getText().toString().trim();
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : ""; // Get the current user email
        String imageType;
        if (chipGroupImageType.getCheckedChipId() == chipOriginalImage.getId()) {
            imageType = "Original Item";
        } else if (chipGroupImageType.getCheckedChipId() == chipLookAlike.getId()) {
            imageType = "Look Alike";
        } else {
            Toast.makeText(getActivity(), "Please select the image type", Toast.LENGTH_SHORT).show();
            return;
        }
        String finalImageType = imageType;

        if (name.isEmpty() || location.isEmpty() || adhar.isEmpty() || description.isEmpty() ||  dateTime.isEmpty() || imageUri==null || adharImageUri==null){
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(adhar.length()!=12)
        {
            Toast.makeText(getActivity(), "Invalid Adhar Number", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(itemDescription.length()>60)
        {
            Toast.makeText(getActivity(), "Discription overloaded", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(itemName.length()>30)
        {
            Toast.makeText(getActivity(), "Item Name overloaded", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(itemLocation.length()>60)
        {
            Toast.makeText(getActivity(), "Location overloaded", Toast.LENGTH_SHORT).show();
            return;
        }


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Submitting...");
        progressDialog.show();

        // Upload item image
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("item_images").child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Upload Aadhar image
                        if (adharImageUri != null) {
                            StorageReference adharFileReference = storageReference.child("adhar_images").child(System.currentTimeMillis() + ".jpg");
                            adharFileReference.putFile(adharImageUri)
                                    .addOnSuccessListener(taskSnapshot1 -> adharFileReference.getDownloadUrl().addOnSuccessListener(adharUri -> {
                                        String adharImageUrl = adharUri.toString();
                                        saveItemDetails(name, location, adhar, description, user, phone, dateTime, imageUrl, adharImageUrl, userEmail,finalImageType);
                                    }))
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Failed to upload Aadhar image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            //Toast.makeText(getActivity(), "Adhaar Image is Empty", Toast.LENGTH_SHORT).show();
                            saveItemDetails(name, location, adhar, description, user, phone, dateTime, imageUrl, null, userEmail,finalImageType);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to upload item image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            //Toast.makeText(getActivity(), "Item Image is Empty", Toast.LENGTH_SHORT).show();
            saveItemDetails(name, location, adhar, description, user, phone, dateTime, null, null, userEmail,finalImageType);
        }
    }

    private void saveItemDetails(String name, String location, String adhar, String description, String user, String phone, String dateTime, String imageUrl, String adharImageUrl, String userEmail, String finalImageType) {
        String itemId = databaseReference.push().getKey();
        Item item = new Item(itemId, name, location, dateTime, imageUrl, adharImageUrl, description, adhar, user, phone, userEmail,finalImageType);
        databaseReference.child(itemId).setValue(item).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(getActivity(), "Item submitted successfully", Toast.LENGTH_SHORT).show();
                //listenForNewLostItems();

                Fragment homeFragment = new HomeFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, homeFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            } else {
                Toast.makeText(getActivity(), "Failed to submit item", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ITEM_IMAGE_PICK) {


                    if (data != null && data.getExtras() != null) {
                        Bitmap itmbitmap = (Bitmap) data.getExtras().get("data");
                        itemPhoto.setImageBitmap(itmbitmap);
                        imageUri = getImageUri(getContext(),itmbitmap);
                        //capturedBitmap = bitmap;// Save bitmap for further use

                    }

            } else if (requestCode == REQUEST_ADHAR_IMAGE_PICK_CAMERA) {
                if (data != null && data.getExtras() != null) {
                    Bitmap adhrbitmap = (Bitmap) data.getExtras().get("data");
                    adharPhoto.setImageBitmap(adhrbitmap);
                    adharImageUri = getImageUri(getContext(),adhrbitmap);
                   // capturedBitmapadhar = bitmap;// Save bitmap for further use
                }
            }
        } else {
            Toast.makeText(getContext(), "Photo capture canceled", Toast.LENGTH_SHORT).show();
        }
    }
    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }



}
