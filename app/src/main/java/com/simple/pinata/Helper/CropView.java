package com.simple.pinata.Helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.simple.pinata.ExportChatActivity;
import com.simple.pinata.R;

import java.util.ArrayList;
import java.util.List;

public class CropView extends View implements View.OnTouchListener {
    private Paint paint;
    private List<Point> points;

    int DIST = 2;
    boolean flgPathDraw = true;

    Point mfirstpoint = null;
    boolean bfirstpoint = false;

    Point mlastpoint = null;

    Bitmap bitmap;

    Context mContext;
    private Dialog FinishCropDialog;


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public CropView(Context context) {
        super(context);
        mContext = context;
        this.bitmap = bitmap;

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();

        bfirstpoint = false;

        FinishCropDialog = new Dialog(context);
    }

    public CropView(Context c, Bitmap bitmap) {
        super(c);

        mContext = c;
        this.bitmap = bitmap;

        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();

        bfirstpoint = false;

        FinishCropDialog = new Dialog(c);
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);

        points = new ArrayList<Point>();
        bfirstpoint = false;

        this.setOnTouchListener(this);
    }

    public void onDraw(Canvas canvas) {

            /*Rect dest = new Rect(0, 0, getWidth(), getHeight());

     paint.setFilterBitmap(true); canvas.drawBitmap(bitmap, null, dest, paint);*/
        canvas.drawBitmap(bitmap, 0, 0, null);

        Path path = new Path();
        boolean first = true;

        for (int i = 0; i < points.size(); i += 2) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else if (i < points.size() - 1) {
                Point next = points.get(i + 1);
                path.quadTo(point.x, point.y, next.x, next.y);
            } else {
                mlastpoint = points.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, paint);
    }

    public boolean onTouch(View view, MotionEvent event) {
        // if(event.getAction() != MotionEvent.ACTION_DOWN)
        // return super.onTouchEvent(event);
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        if (flgPathDraw) {

            if (bfirstpoint) {

                if (comparepoint(mfirstpoint, point)) {
                    // points.add(point);
                    points.add(mfirstpoint);
                    flgPathDraw = false;
                    showcropdialog();
                } else {
                    points.add(point);
                }
            } else {
                points.add(point);
            }

            if (!(bfirstpoint)) {

                mfirstpoint = point;
                bfirstpoint = true;
            }
        }

        invalidate();
        Log.e("Hi  ==>", "Size: " + point.x + " " + point.y);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("Action up*****~~>>>>", "called");
            mlastpoint = point;
            if (flgPathDraw) {
                if (points.size() > 12) {
                    if (!comparepoint(mfirstpoint, mlastpoint)) {
                        flgPathDraw = false;
                        points.add(mfirstpoint);
                        showcropdialog();
                    }
                }
            }
        }

        return true;
    }

    private boolean comparepoint(Point first, Point current) {
        int left_range_x = (int) (current.x - 3);
        int left_range_y = (int) (current.y - 3);

        int right_range_x = (int) (current.x + 3);
        int right_range_y = (int) (current.y + 3);

        if ((left_range_x < first.x && first.x < right_range_x)
                && (left_range_y < first.y && first.y < right_range_y)) {
            if (points.size() < 10) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public void fillinPartofPath() {
        Point point = new Point();
        point.x = points.get(0).x;
        point.y = points.get(0).y;

        points.add(point);
        invalidate();
    }

    public void resetView() {
        points.clear();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.RED);

        points = new ArrayList<Point>();
        bfirstpoint = false;

        flgPathDraw = true;
        invalidate();
    }

    private void showcropdialog() {

        FinishCropDialog.setContentView(R.layout.image_pick_dialog_layout);
        FinishCropDialog.setCanceledOnTouchOutside(false);
        FinishCropDialog.setCancelable(false);
        FinishCropDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button DialogNoBtn = FinishCropDialog.findViewById(R.id.forgotpassword_cancel_btn);
        Button DialogYesBtn = FinishCropDialog.findViewById(R.id.forgotpassword_reset_btn);

        TextView HeaderTextView,MessageTextView;

        HeaderTextView = FinishCropDialog.findViewById(R.id.forgotpassword_header);
        MessageTextView = FinishCropDialog.findViewById(R.id.forgotpassword_ET);

        HeaderTextView.setText("Finish Crop");
        MessageTextView.setText("Are you sure that you want to use the cropped area for your word  cloud ?");

        DialogNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetView();
                FinishCropDialog.dismiss();

            }
        });

        DialogYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ((ExportChatActivity) mContext).cropImage();
                FinishCropDialog.dismiss();


            }
        });

        FinishCropDialog.show();

    }

    public List<Point> getPoints() {
        return points;
    }
}