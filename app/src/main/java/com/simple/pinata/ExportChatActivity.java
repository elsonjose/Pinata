package com.simple.pinata;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ExportChatActivity extends AppCompatActivity {

    private Uri uri;
    private String filepath, filename;
    private RelativeLayout RootMessageView;
    private String messageString = "";
    private final int SELECT_PHOTO = 1;
    private ImageView ExportImageView;
    private Dialog ImagePickerDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chat);

        RootMessageView = findViewById(R.id.root_message);
        ExportImageView = findViewById(R.id.export_imageview);

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction.equals(Intent.ACTION_SEND_MULTIPLE)) {

            if (receivedType.startsWith("text/")) {
                String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);

                if (receivedText != null) {
                    handleSendMultipleImages(receivedIntent);
                } else {
                    Toast.makeText(this, "received text null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "received other type", Toast.LENGTH_SHORT).show();
            }

        }


        ImagePickerDialog = new Dialog(this);
        ImagePickerDialog.setContentView(R.layout.image_pick_dialog_layout);
        ImagePickerDialog.setCanceledOnTouchOutside(false);
        ImagePickerDialog.setCancelable(false);
        ImagePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button ImageDialogNoBtn = ImagePickerDialog.findViewById(R.id.forgotpassword_cancel_btn);
        Button ImageDialogYesBtn = ImagePickerDialog.findViewById(R.id.forgotpassword_reset_btn);

        ImageDialogNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePickerDialog.dismiss();

            }
        });

        ImageDialogYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);


            }
        });

        ImagePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        ExportImageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            uri = imageUris.get(0);
            filepath = getFilePathForN(uri, getApplicationContext());
            String paths[] = filepath.split("/");
            filename = paths[paths.length - 1];

            BufferedReader reader = null;

            try {
                FileInputStream inputStream = openFileInput(filename);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String sinleLine = "";
                String line = reader.readLine();
                while (line != null) {
                    if (line.contains(":") && !line.contains("Tap for more info.")) {
                        if (line.contains("-")) {
                            sinleLine = line.substring(line.indexOf("-") + 1);
                            messageString += sinleLine.substring(sinleLine.indexOf(":") + 1);
                        }
                    }
                    line = reader.readLine();
                }
                messageString.replace("<Media omitted>", "");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private static String getFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

}
