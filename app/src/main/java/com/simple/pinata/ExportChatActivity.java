package com.simple.pinata;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simple.pinata.Helper.CropView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
    private String messageString = "";
    private ImageView ExportImageView;
    private Dialog ImagePickerDialog;
    private Bitmap mBitmap;
    private CropView mCropView;
    private ImageButton ExportBackBtn,ExportPreviewBtn,ExportDoneAllBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.cropview_layout);

        RootMessageView = findViewById(R.id.root_message);

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

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(ExportChatActivity.this);

                ImagePickerDialog.dismiss();

            }
        });

        ImagePickerDialog.show();
        

    }


    public void cropImage() {

        setContentView(R.layout.activity_export_chat);

        ExportImageView = findViewById(R.id.export_imageview);
        ExportBackBtn = findViewById(R.id.export_back_btn);
        ExportPreviewBtn = findViewById(R.id.export_preview_btn);
        ExportDoneAllBtn = findViewById(R.id.export_done_all_btn);
        
        ExportBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.cropview_layout);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                mCropView = new CropView(ExportChatActivity.this, mBitmap);
                LinearLayout CoverOverlay = findViewById(R.id.cropview_overlay);
                ViewGroup.LayoutParams layoutParams = CoverOverlay.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = width;
                CoverOverlay.setLayoutParams(layoutParams);
                CoverOverlay.addView(mCropView);

                findViewById(R.id.cropview_change_img_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ImagePickerDialog.show();


                    }
                });


            }
        });

        ExportPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ExportChatActivity.this, "todo", Toast.LENGTH_SHORT).show();
                
            }
        });
        
        ExportDoneAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ExportChatActivity.this, "todo", Toast.LENGTH_SHORT).show();
                
            }
        });
        

        Bitmap fullScreenBitmap =
                Bitmap.createBitmap(mCropView.getWidth(), mCropView.getHeight(), mBitmap.getConfig());

        Canvas canvas = new Canvas(fullScreenBitmap);

        Path path = new Path();
        List<Point> points = mCropView.getPoints();
        for (int i = 0; i < points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }

        // Cut out the selected portion of the image...
        Paint paint = new Paint();
        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mBitmap, 0, 0, paint);

        // Create a bitmap with just the cropped area.
        Region region = new Region();
        Region clip = new Region(0, 0, fullScreenBitmap.getWidth(), fullScreenBitmap.getHeight());
        region.setPath(path, clip);
        Rect bounds = region.getBounds();
        Bitmap croppedBitmap =
                Bitmap.createBitmap(fullScreenBitmap, bounds.left, bounds.top,
                        bounds.width(), bounds.height());

        ExportImageView.setImageBitmap(croppedBitmap);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                try {

                    final InputStream imageStream = getContentResolver().openInputStream(resultUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int width = displayMetrics.widthPixels;

                    mBitmap=Bitmap.createScaledBitmap(selectedImage, width, width, false);

                    mCropView = new CropView(this, mBitmap);
                    LinearLayout CoverOverlay = findViewById(R.id.cropview_overlay);
                    ViewGroup.LayoutParams layoutParams = CoverOverlay.getLayoutParams();
                    layoutParams.width = width;
                    layoutParams.height = width;
                    CoverOverlay.setLayoutParams(layoutParams);
                    CoverOverlay.addView(mCropView);

                    findViewById(R.id.cropview_change_img_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ImagePickerDialog.show();


                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
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
