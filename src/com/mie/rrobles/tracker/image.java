package com.mie.rrobles.tracker;

import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
//import org.opencv.samples.tutorial3.Tutorial3Activity.AutoFocusCallBackImpl;

public class image extends Activity implements OnTouchListener{

	private static final String    TAG = "image::Activity";
	Mat                  image = new Mat();

	//private camera_view   mOpenCvCameraView;

	
	private BaseLoaderCallback  iLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("mixed_sample");
                    //displayImage();
                    Features_Homography();
                  
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
    	
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, iLoaderCallback);
    	
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.image_layout);
        
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, iLoaderCallback);
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {

    	Toast.makeText(this, "Tocado", Toast.LENGTH_SHORT).show();
    	//finish();
    	return false;
    }
    
    public void loadTarget() {
    	String filePath = Environment.getExternalStorageDirectory().getPath() +
                "/MIETracker/target.jpg";
        Mat mTarget = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_UNCHANGED);
    }
    
    public void displayImage() {
        
        String filePath = Environment.getExternalStorageDirectory().getPath() +
                "/MIETracker/scene.jpg";
        Mat imgMat = Highgui.imread(filePath, Highgui.CV_LOAD_IMAGE_UNCHANGED);
//        if (imgMat == null) {
//        	Log.e(TAG, "error loading file: " + filePath);
//        } else {
//                Log.d(TAG, "Ht: " + imgMat.height() + " Width: " + imgMat.width());
//                String outPath = filePath + "_gray.jpg";
//                Log.d(TAG, outPath);
//                Highgui.imwrite(outPath, imgMat);
//        }

        Bitmap img = Bitmap.createBitmap(imgMat.cols(), imgMat.rows(),Bitmap.Config.ARGB_8888);        
        Utils.matToBitmap(imgMat, img);
        
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageBitmap(img);
    }
    
    public Mat Features_Homography(){       

        MatOfKeyPoint templateKeypoints = new MatOfKeyPoint();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        MatOfDMatch matches = new MatOfDMatch();

        Mat Object = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
        		"/MIETracker/target01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
        Mat Resource = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
        		"/MIETracker/scene01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
        Mat imageOut = Resource.clone();        

        //Don't use "FeatureDetector.FAST" result was too bad
        //"FeatureDetector.SURF""FeatureDetector.SURF" non-free method
        //"FeatureDetector.ORB" is better for now
        //FeatureDetector.SIFT cna't use
        FeatureDetector myFeatures = FeatureDetector.create(FeatureDetector.ORB);    
        myFeatures.detect(Resource, keypoints);
        myFeatures.detect(Object, templateKeypoints);

        Log.i(TAG, "Features detected");
        
        DescriptorExtractor Extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();
        Extractor.compute(Resource, keypoints, descriptors1);
        Extractor.compute(Object, templateKeypoints, descriptors2);

        Log.i(TAG, "Descriptor computed");

        //add Feature descriptors
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(descriptors1, descriptors2, matches);

        List<DMatch> matches_list = matches.toList();
        
        Log.i(TAG, "Matches: " + matches_list.size());

        
        double max_dist = 0; double min_dist = 999;

        //-- Quick calculation of max and min distances between keypoints
        for( int i = 0; i < descriptors1.rows(); i++ )
        { 
            double dist = matches_list.get(i).distance;
            if( dist < min_dist )
                min_dist = dist;
            if( dist > max_dist )
                max_dist = dist;
        }

        Log.i(TAG, "Matched calculated. Max: " + max_dist + "Min: " + min_dist);
       
        MatOfDMatch good_matches = new MatOfDMatch();
        
        //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
        for( int i = 0; i < descriptors1.rows(); i++ )
        { 
            if( matches_list.get(i).distance < 3*min_dist ){
                MatOfDMatch temp = new MatOfDMatch();
                temp.fromArray(matches.toArray()[i]);
                 good_matches.push_back(temp);
            }
        }
        
        Log.i(TAG, "Only good matches. Num:" + good_matches.size());
        
        Features2d.drawMatches(Resource, keypoints, Object, templateKeypoints, good_matches, imageOut);

        Log.i(TAG, "Draw matches");

        Highgui.imwrite(Environment.getExternalStorageDirectory().getPath() +
        		"/MIETracker/result_match.jpg", imageOut);

        Log.i(TAG, "Resultado escrito");

        Bitmap img = Bitmap.createBitmap(imageOut.cols(), imageOut.rows(),Bitmap.Config.ARGB_8888);        
        Utils.matToBitmap(imageOut, img);
        
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageBitmap(img);

        Log.i(TAG, "Resultado mostrado");
        
        return imageOut;

    }
    

}