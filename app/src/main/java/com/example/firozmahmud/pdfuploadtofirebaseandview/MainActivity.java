package com.example.firozmahmud.pdfuploadtofirebaseandview;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private TextView tvNotification;
    private Uri pdfUri;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private String downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initVariable();
    }

    private void initVariable() {
        databaseReference = FirebaseDatabase.getInstance().getReference(DBConstants.FB_DATABASE);
        storageReference = FirebaseStorage.getInstance().getReference(DBConstants.FB_STORAGE);
        progressDialog = new ProgressDialog(this);
    }

    // ---- initialize view
    private void initView() {

        setContentView(R.layout.activity_main);
        tvNotification = findViewById(R.id.tvNotification);
    }


    // --- Button Click
    public void btnClick(View view) {

        if (view.getId() == R.id.getPDF) {
            // get the pdf file

            getPDFAction();

        } else if (view.getId() == R.id.upload) {
            // upload the pdf file to firebase

            uploadToFBStorage();
        } else if (view.getId() == R.id.viewPdf) {
            // -- view the pdf from url into webview
            getPdfFromFB();
        }
    }


    // ---- Get Pdf file link from firebase database
    private void getPdfFromFB() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                    downloadUri = snapshot.getValue().toString();
                }

                startActivity(new Intent(MainActivity.this, ViewActivity.class).putExtra("pdf", downloadUri));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void uploadToFBStorage() {

        if (pdfUri != null) {

            upload();
        } else {
            Toast.makeText(this, "Please select a pdf file", Toast.LENGTH_SHORT).show();
        }

    }

    // --- upload
    private void upload() {


        //  -- show the progress dialog
        progressDialog.setTitle("Uploading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();

        final String fileName = System.currentTimeMillis() + ".pdf";

        storageReference.child(fileName).putFile(pdfUri).

                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        downloadUri = taskSnapshot.getDownloadUrl().toString();


                        databaseReference.child("pdf").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if (task.isSuccessful()) {

                                    Toast.makeText(MainActivity.this, "Database Updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "ST: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                progressDialog.setProgress(progress);

                if (progress == 100) {
                    progressDialog.dismiss();

                    //Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    private void getPDFAction() {


        // at first check the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            // --- We have the permission to read external storage
            selectPDF();

        } else {   // --- We don't have the permission

            // --- We need to ask the user to grant the permission

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1010);

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == 1010 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // we have the permission
            selectPDF();

        } else {
            Toast.makeText(this, "Please provide permission to access file", Toast.LENGTH_SHORT).show();
        }

    }


    // select the pdf
    private void selectPDF() {

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);  // to fetch file
        startActivityForResult(intent, 1100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // check whether user successfully selected a file or not
        if (requestCode == 1100 && resultCode == RESULT_OK && data != null) {

            pdfUri = data.getData();   // return the uri of the selected file
            tvNotification.setText(pdfUri + "");

        } else {
            Toast.makeText(this, "Please select a pdf file", Toast.LENGTH_SHORT).show();
        }
    }
}
