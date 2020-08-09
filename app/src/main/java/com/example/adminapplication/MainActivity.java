package com.example.adminapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {


    private Button selectFile, upload;
    private EditText imagename;
    private Uri pdfUri;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;


    private DatabaseReference databaseReference;

    private final int PICK_IMAGE_REQUEST = 71;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Data").child("project");


        selectFile = (Button) findViewById(R.id.selectFile);

        upload = (Button) findViewById(R.id.send);
        imagename = (EditText) findViewById(R.id.edittxt);



        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose Source file"), PICK_IMAGE_REQUEST);

            }
        });


        upload.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                if (pdfUri != null) {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setTitle("Uploading file...");
                    progressDialog.setProgress(0);
                    progressDialog.show();


                    StorageReference storageReference = mStorageRef.child("url" + System.currentTimeMillis() + "." + getFileExtension(pdfUri));

                    storageReference.putFile(pdfUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    final Task<Uri> firebasedatabaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                    firebasedatabaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();
                                            Upload upload = new Upload(imagename.getText().toString().trim(), downloadUrl);

                                            String uploadId = databaseReference.push().getKey();


                                            databaseReference.child(uploadId).setValue(upload).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {


                                                        Toast.makeText(MainActivity.this, "File successfully uploaded ", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();


                                                    } else {
                                                        Toast.makeText(MainActivity.this, "File Not successfully uploaded ", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(MainActivity.this, "File is Not successfully uploaded ", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double currentProgress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded" + ((int) currentProgress) + "%...");
                        }
                    });
                } else {

                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }

            private String getFileExtension(Uri uri) {
                ContentResolver cR = MainActivity.this.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                return mime.getExtensionFromMimeType(cR.getType(uri));
            }


        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode ==RESULT_OK  && data !=null ){

            pdfUri = data.getData();

        }
        else {

            Toast.makeText(MainActivity.this,"Please Select an File",Toast.LENGTH_SHORT).show();
        }








    }
}
