package com.simple.pinata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.simple.pinata.Helper.MaskModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExportChatActivity extends AppCompatActivity {

    private Uri uri;
    private String filepath, filename;
    private RelativeLayout RootMessageView;
    private String messageString ="";
    private RecyclerView ExportRecyclerview;
    private Button ExportPreviewBtn;
    private List<MaskModel> MaskList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chat);

        RootMessageView = findViewById(R.id.root_message);
        ExportRecyclerview = findViewById(R.id.export_recyclerview);
        ExportRecyclerview.setHasFixedSize(true);
        ExportRecyclerview.setLayoutManager(new GridLayoutManager(this,2));
        ExportPreviewBtn = findViewById(R.id.export_preview_btn);

        //Snackbar.make(RootMessageView, "Please wait until chats are loaded.", Snackbar.LENGTH_LONG).show();

        MaskList = new ArrayList<>();
        MaskList.add(new MaskModel("Batman",R.drawable.batman_mask,0));
        MaskList.add(new MaskModel("Gentleman",R.drawable.gentleman_mask,0));
        MaskList.add(new MaskModel("Joker",R.drawable.joker_mask,0));
        MaskList.add(new MaskModel("Love",R.drawable.love_mask,0));
        MaskList.add(new MaskModel("Storm Trooper",R.drawable.stormtrooper_mask,0));
        MaskList.add(new MaskModel("User Choice",R.drawable.ic_add,1));

        MaskAdapter maskAdapter = new MaskAdapter();
        ExportRecyclerview.setAdapter(maskAdapter);


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

        ExportPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Snackbar.make(RootMessageView, "To do.", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    public class MaskAdapter extends RecyclerView.Adapter<MaskAdapter.MaskViewHolder>
    {

        @NonNull
        @Override
        public MaskAdapter.MaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MaskAdapter.MaskViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.export_mask_layout,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MaskAdapter.MaskViewHolder holder, final int position) {

            Glide.with(getApplicationContext()).load(MaskList.get(position).getId()).into(holder.MaskImageview);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(MaskList.get(position).getType() == 0)
                    {
                        Snackbar.make(RootMessageView, MaskList.get(position).getName(), Snackbar.LENGTH_LONG).show();
                    }
                    else
                    {
                        Snackbar.make(RootMessageView, "To do.", Snackbar.LENGTH_LONG).show();
                    }

                }
            });


        }

        @Override
        public int getItemCount() {
            return MaskList.size();
        }

        public class MaskViewHolder extends RecyclerView.ViewHolder {

            ImageView MaskImageview;

            public MaskViewHolder(@NonNull View itemView) {
                super(itemView);

                MaskImageview = itemView.findViewById(R.id.export_mask_imageview);
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
                String sinleLine ="";
                String line = reader.readLine();
                while(line != null){
                    if(line.contains(":") && !line.contains("Tap for more info."))
                    {
                        if(line.contains("-"))
                        {
                            sinleLine=line.substring(line.indexOf("-") + 1);
                            messageString +=sinleLine.substring(sinleLine.indexOf(":")+1);
                        }
                    }
                    line = reader.readLine();
                }
                messageString.replace("<Media omitted>","");

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
