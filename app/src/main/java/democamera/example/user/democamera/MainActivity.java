package democamera.example.user.democamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "CropBG";
    private Uri outPutFileUri;
    private int TAKE_PHOTO_REQUEST_CODE = 1;

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    setTheme(android.R.style.Theme_Holo_Light_Dialog);
                    setContentView(R.layout.activity_main);
                    ImageView ImgView = (ImageView) findViewById(R.id.ImgView);

                    CropBG co = new CropBG();
                    int a = R.drawable.bak;

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test08);
                    Bitmap return_img = co.cropIMG(bitmap, getApplicationContext(), a);
                    //ImgView.setImageBitmap(return_img);

                    String filename = "pippo.png";
                    File sd = Environment.getExternalStorageDirectory();
                    File dest = new File(sd, filename);
                    try {
                        FileOutputStream out = new FileOutputStream(dest);
                        return_img.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.flush();
                        out.close();
                        //ImgView.setImageBitmap(return_img);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mOpenCVCallBack))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }else{
            Log.i(TAG, "opencv successfull");
            System.out.println(java.lang.Runtime.getRuntime().maxMemory());
        }
    }

    public void btnTakePhoto(View v){
        File file = new File(Environment.getExternalStorageDirectory(),"test.jpg");
        outPutFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutFileUri);
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK){
            Toast.makeText(this,outPutFileUri.toString(),Toast.LENGTH_LONG).show();

            ImageView ImgView = (ImageView) findViewById(R.id.ImgView);

            CropBG co = new CropBG();
            int a = R.drawable.bak;


            //Bitmap bMap = BitmapFactory.decodeFile(outPutFileUri);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),bMap);
            //Bitmap return_img = co.cropIMG(bMap, getApplicationContext(), a);
            ImgView.setImageURI(outPutFileUri);

            Bitmap bmapS = ((BitmapDrawable) ImgView.getDrawable()).getBitmap();
            Bitmap return_img = co.cropIMG(bmapS, getApplicationContext(), a);
            ImgView.setImageBitmap(return_img);
        }


    }
}
