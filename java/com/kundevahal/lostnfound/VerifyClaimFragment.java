package com.kundevahal.lostnfound;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VerifyClaimFragment extends Fragment {

    private static final String ARG_ITEM = "item";
    private Item item;
    private TextView itemDateTime;

    public VerifyClaimFragment() {
        // Required empty public constructor
    }

    public static VerifyClaimFragment newInstance(Item item) {
        VerifyClaimFragment fragment = new VerifyClaimFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (Item) getArguments().getSerializable(ARG_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_claim, container, false);
        itemDateTime = view.findViewById(R.id.handover_datetime);
        itemDateTime.setOnClickListener(v -> showDateTimePicker());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView tvItemName = view.findViewById(R.id.tvItemName);
        TextView tvItemLocation = view.findViewById(R.id.tvItemLocation);
        TextView tvItemDateTime = view.findViewById(R.id.tvItemDateTime);
        ImageView imgItemImage = view.findViewById(R.id.imgItemImage);
        ImageView imgAdharImage = view.findViewById(R.id.imgAdharImage);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvAdhar = view.findViewById(R.id.tvAdhar);
        TextView tvUser = view.findViewById(R.id.tvUser);
        TextView tvPhone = view.findViewById(R.id.tvPhone);
        //TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvImageType = view.findViewById(R.id.tvImageType);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnAccept = view.findViewById(R.id.btnAccept);

        // Set item details to views
        tvItemName.setText(item.getName());
        tvItemLocation.setText(item.getLocation());
        tvItemDateTime.setText(item.getDateTime());
        Glide.with(getContext()).load(item.getItemImageUrl()).into(imgItemImage);
        Glide.with(getContext()).load(item.getAdharImageUrl()).into(imgAdharImage);
        tvDescription.setText(item.getDescription());
        tvAdhar.setText(item.getAdharNumber());
        tvUser.setText(item.getUserName());
        tvPhone.setText(item.getPhoneNumber());
        //tvUserEmail.setText(item.getUserEmail());
        tvImageType.setText(item.getFinalImageType());


//        NotificationHelper.createNotificationChannel(getActivity());

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> navigateToMainActivity());

        // Handle accept button click
        btnAccept.setOnClickListener(v -> handleAccept());
    }

    private void handleAccept() {
        // Get the entered date and time
        String datenTime = itemDateTime.getText().toString().trim();

        // Check if the date and time field is empty
        if (datenTime.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and time before accepting.", Toast.LENGTH_SHORT).show();
            return; // Exit the method
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Acceptance")
                .setMessage("Are you sure you want to accept this item?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    // Proceed with acceptance if confirmed
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference lostItemRef = databaseRef.child("lost_items").child(item.getItemId());
                    DatabaseReference acceptedItemRef = databaseRef.child("accepted_items").child(item.getItemId());


                    String dateTime = itemDateTime.getText().toString().trim();
                    item.setDateTime(dateTime);

                    // Transfer item data to accepted_items
                    acceptedItemRef.setValue(item).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Delete the item from lost_items
                            lostItemRef.removeValue().addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "Item accepted successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getContext(), AuthenticatorMainActivity.class);
                                    startActivity(intent);

                                    // Finish the current activity to prevent going back to it
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Failed to remove item from lost_items", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Failed to accept item", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false) // Prevents dismissal on outside touch
                .show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getContext(), AuthenticatorMainActivity.class);
        startActivity(intent);
    }

    private void showDateTimePicker() {
        final Calendar currentDateTime = Calendar.getInstance(); // Current date and time for validation
        final Calendar selectedDateTime = Calendar.getInstance();

        // DatePickerDialog to select the date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // TimePickerDialog to select the time
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                            (view1, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);

                                // Validate the selected date and time
                                if (selectedDateTime.before(currentDateTime)) {
                                    Toast.makeText(getContext(), "Past date and time are not allowed.", Toast.LENGTH_SHORT).show();
                                    itemDateTime.setText(""); // Reset the field
                                } else {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    itemDateTime.setText(dateFormat.format(selectedDateTime.getTime()));
                                }
                            },
                            selectedDateTime.get(Calendar.HOUR_OF_DAY),
                            selectedDateTime.get(Calendar.MINUTE),
                            false);

                    // Allow time selection only if the selected date is today
                    if (selectedDateTime.get(Calendar.YEAR) == currentDateTime.get(Calendar.YEAR) &&
                            selectedDateTime.get(Calendar.DAY_OF_YEAR) == currentDateTime.get(Calendar.DAY_OF_YEAR)) {
                        timePickerDialog.updateTime(currentDateTime.get(Calendar.HOUR_OF_DAY), currentDateTime.get(Calendar.MINUTE));
                    }

                    timePickerDialog.show();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));

        // Restrict DatePicker to not allow past dates
        datePickerDialog.getDatePicker().setMinDate(currentDateTime.getTimeInMillis());
        datePickerDialog.show();
    }

}
