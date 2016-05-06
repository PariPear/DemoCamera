package democamera.example.user.democamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 25/4/2559.
 */
public class CropBG {

    private String TAG = "CropBG";
    private Bitmap bitmap;
    private Canvas canvas;
    private Scalar color = new Scalar(255, 0, 0, 255);
    private Point tl, br;
    private int counter;
    private Bitmap bitmapResult;
    private Bitmap bitmapBackground;
    private Mat dst;

    public Bitmap cropIMG(Bitmap imgBit , Context context,int background_r){
        dst = new Mat();
        Bitmap bitmap = imgBit;
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap,img);

        int r = img.rows();
        int c = img.cols();

        Point p1 = new Point(c/5, r/5);
        Point p2 = new Point(c-c/5, r-r/8);
        Rect rect = new Rect(p1,p2);
        Mat mask = new Mat();

        mask.setTo(new Scalar(125));
        Mat fgdModel = new Mat();
        fgdModel.setTo(new Scalar(255, 255, 255));
        Mat bgdModel = new Mat();
        bgdModel.setTo(new Scalar(255, 255, 255));

        Mat imgC3 = new Mat();
        Imgproc.cvtColor(img, imgC3, Imgproc.COLOR_RGBA2RGB);
        Log.d(TAG, "imgC3: " + imgC3);

        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 5, Imgproc.GC_INIT_WITH_RECT);
        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));

        Core.compare(mask, source, mask, Core.CMP_EQ);
        Mat foreground = new Mat(img.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        img.copyTo(foreground, mask);
        Imgproc.rectangle(img, p1, p2, color);

        Mat background = new Mat();
        try {
            background = Utils.loadResource(context,background_r);
            Log.d(TAG, "-------------------9999");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat tmp = new Mat();
        Imgproc.resize(background, tmp, img.size());
        background = tmp;

        Mat tempMask = new Mat(foreground.size(), CvType.CV_8UC1, new Scalar(255, 255, 255));
        Imgproc.cvtColor(foreground, tempMask,6);

        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        dst = new Mat();
        background.setTo(vals, tempMask);
        Imgproc.resize(foreground, tmp, mask.size());
        foreground = tmp;
        Core.add(background, foreground, dst, tempMask);

        //convert to Bitmap
        Log.d(TAG, "Convert to Bitmap");
        Utils.matToBitmap(dst, bitmap);
        return makeBlackTransparent(bitmap);

    }

    private static Bitmap makeBlackTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);

        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);

        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);

        // threshold the image to create alpha channel with complete transparency in black background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, 100, 255, Imgproc.THRESH_BINARY);

        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);

        // Create the final result by merging three single channel and alpha(BGRA order)
        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);

        // convert matrix to output bitmap
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;
    }


}
