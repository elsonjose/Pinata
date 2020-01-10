package com.simple.pinata;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.simple.pinata.Helper.PaintView;
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
    private String messageString ="";
    private PaintView paintView;
    private ImageButton maskSaveBtn,maskPreviewBtn,maskUndoBtn,maskBackBtn,maskBrushBtn,maskRedoBtn;

    private Dialog brushOptionsDialog;
    private RadioButton typeNormal,typeEmboss,typeBlur;
    private SeekBar strokeSeekbar;
    private ImageButton closeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chat);

        RootMessageView = findViewById(R.id.root_message);

        paintView = (PaintView) findViewById(R.id.export_paintview);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
        paintView.normal();

        brushOptionsDialogInit();

        maskSaveBtn = findViewById(R.id.export_mask_save_btn);
        maskPreviewBtn  = findViewById(R.id.export_mask_preview_btn);
        maskUndoBtn = findViewById(R.id.export_mask_undo_btn);
        maskBackBtn = findViewById(R.id.export_mask_back_btn);
        maskBrushBtn  = findViewById(R.id.export_mask_brush_btn);
        maskRedoBtn = findViewById(R.id.export_mask_redo_btn);

        maskRedoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paintView.redoCanvas();

            }
        });

        maskUndoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paintView.undoCanvas();

            }
        });

        maskBrushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!paintView.isEmboss() && !paintView.isBlur())
                {
                    typeNormal.setChecked(true);
                    typeEmboss.setChecked(false);
                    typeBlur.setChecked(false);
                }
                else if(paintView.isEmboss() && !paintView.isBlur())
                {
                    typeNormal.setChecked(false);
                    typeEmboss.setChecked(true);
                    typeBlur.setChecked(false);
                }
                else if(!paintView.isEmboss() && paintView.isBlur())
                {
                    typeNormal.setChecked(false);
                    typeEmboss.setChecked(false);
                    typeBlur.setChecked(true);
                }

                strokeSeekbar.setProgress(paintView.getStrokeWidth());

                brushOptionsDialog.show();
                
                
                typeNormal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        typeNormal.setChecked(true);
                        typeEmboss.setChecked(false);
                        typeBlur.setChecked(false);
                        paintView.setEmboss(false);
                        paintView.setBlur(false);
                    }
                });
                
                typeEmboss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        typeNormal.setChecked(false);
                        typeEmboss.setChecked(true);
                        typeBlur.setChecked(false);
                        paintView.setEmboss(true);
                        paintView.setBlur(false);
                        
                    }
                });
                
                typeBlur.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        typeNormal.setChecked(false);
                        typeEmboss.setChecked(false);
                        typeBlur.setChecked(true);
                        paintView.setEmboss(false);
                        paintView.setBlur(true);
                    }
                });

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

    private void brushOptionsDialogInit() {

        brushOptionsDialog = new Dialog(this,android.R.style.Theme_Light_NoTitleBar);
        brushOptionsDialog.setCancelable(true);
        brushOptionsDialog.setCanceledOnTouchOutside(true);
        brushOptionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        brushOptionsDialog.setContentView(R.layout.brush_export_layout);
        brushOptionsDialog.getWindow().getAttributes().windowAnimations = R.style.BottomUpSlideDialogAnimation;

        Window brushOptionsDialogwindow = brushOptionsDialog.getWindow();
        brushOptionsDialogwindow.setGravity(Gravity.BOTTOM);
        brushOptionsDialogwindow.setLayout(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.WRAP_CONTENT);
        brushOptionsDialogwindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        brushOptionsDialogwindow.setDimAmount(0.75f);

        typeNormal = brushOptionsDialog.findViewById(R.id.brush_type_normal_radio_btn);
        typeEmboss = brushOptionsDialog.findViewById(R.id.brush_type_emboss_radio_btn);
        typeBlur = brushOptionsDialog.findViewById(R.id.brush_type_blur_radio_btn);

        strokeSeekbar = brushOptionsDialog.findViewById(R.id.brush_export_seekbar);

        closeDialog = brushOptionsDialog.findViewById(R.id.brush_export_closebtn);

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                brushOptionsDialog.dismiss();

            }
        });

        brushOptionsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {


                if(!paintView.isEmboss() && !paintView.isBlur())
                {
                    paintView.setBlur(false);
                    paintView.setEmboss(false);
                }
                else if(paintView.isEmboss() && !paintView.isBlur())
                {
                    paintView.setBlur(false);
                    paintView.setEmboss(true);
                }
                else if(!paintView.isEmboss() && paintView.isBlur())
                {
                    paintView.setBlur(true);
                    paintView.setEmboss(false);
                }


                paintView.setStrokeWidth(strokeSeekbar.getProgress());


            }
        });

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
