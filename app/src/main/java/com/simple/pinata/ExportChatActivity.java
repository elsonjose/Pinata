package com.simple.pinata;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ExportChatActivity extends AppCompatActivity {

    private Uri uri;
    private String filepath,filename;
    private TextView MessageTextView;
    private Dialog progressDialog;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chat);

        MessageTextView = findViewById(R.id.message_textview);
        MessageTextView.setMovementMethod(new ScrollingMovementMethod());

        mScrollView= findViewById(R.id.message_scrollview);
        mScrollView.post(new Runnable()
        {
            public void run()
            {
                mScrollView.smoothScrollTo(0, MessageTextView.getTop());
            }
        });



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

    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            uri = imageUris.get(0);
            filepath = getFilePathForN(uri, getApplicationContext());
            String paths[]=  filepath.split("/");
            filename=paths[paths.length-1];


            try {
                FileInputStream inputStream = openFileInput(filename);
                int c;
                String RevisedString="",temp= "",orgString= "";
                while((c=inputStream.read()) != -1)
                {

                    String cc= Character.toString((char)c);

                    orgString=orgString+Character.toString((char)c);

                    if(!cc.equals("\n"))
                    {
                        temp = temp + Character.toString((char)c);

                    }
                    else
                    {
                        RevisedString=RevisedString + temp.substring(temp.lastIndexOf(":") + 1);
                    }

                }


                String messages = orgString.trim();
                MessageTextView.setText(messages);

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
