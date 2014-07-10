package com.mie.rrobles.tracker;

import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
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

public class homography extends Activity implements OnTouchListener{

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
                    //Features_Homography();
                    Mat target = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
                    		"/MIETracker/target01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
                    Mat scene = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
                    		"/MIETracker/scene01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
                    image = Features2DHomografia(target, scene);
                    Bitmap img = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.ARGB_8888);        
                    Utils.matToBitmap(image, img);
                    
                    ImageView imgView = (ImageView) findViewById(R.id.imageView);
                    imgView.setImageBitmap(img);
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
    
    
    public static Mat Features2DHomografia(Mat Mat1, Mat Mat2)
    {
 	   Mat warpimg = new Mat();
 	   Mat H = new Mat();
 	   //Mat1 = new Mat();
 	   //Mat1.convertTo(Mat1,CvType.CV_8U);
 	  // Mat2 = new Mat();
 	   //Mat2.convertTo(Mat2,CvType.CV_8U);
 	   //Utils.bitmapToMat(bmp1, Mat1);
 	   //Utils.bitmapToMat(bmp2, Mat2);

 	   MatOfDMatch matches = new MatOfDMatch();
 	   MatOfDMatch gm = new MatOfDMatch();
 	   LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
 	   LinkedList<Point> objList = new LinkedList<Point>();
 	   LinkedList<Point> sceneList = new LinkedList<Point>();
 	   MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
 	   MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();
 	   Mat descriptors_object = new Mat();
 	   Mat descriptors_scene = new Mat();
 	   MatOfPoint2f obj = new MatOfPoint2f();
 	   MatOfPoint2f scene = new MatOfPoint2f();
 	   
 	   

 	   Object fd = FeatureDetector.create(FeatureDetector.ORB);

 	   ((FeatureDetector) fd).detect( Mat1, keypoints_object );
 	   ((FeatureDetector) fd).detect( Mat2, keypoints_scene );
 	 
 	   //– Step 2: Calculate descriptors (feature vectors)
 	   //DescriptorExtractor extractor = DescriptorExtractor.create(con);
 	   DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
 	   extractor.compute( Mat1, keypoints_object, descriptors_object );
 	   extractor.compute( Mat2, keypoints_scene, descriptors_scene );
 	   DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

 	   matcher.match( descriptors_object, descriptors_scene, matches);

 	   double max_dist = 0; double min_dist = 100;
 	   List<DMatch> matchesList = matches.toList();

 	   //– Quick calculation of max and min distances between keypoints
 	   for( int i = 0; i < descriptors_object.rows(); i++ )
 	   {
 	   Double dist = (double) matchesList.get(i).distance;
 	   if( dist < min_dist ) min_dist = dist;
 	   if( dist > max_dist ) max_dist = dist;
 	   }

 	   for(int i = 0; i < descriptors_object.rows(); i++){
 	   if(matchesList.get(i).distance < 3*min_dist){
 	   good_matches.addLast(matchesList.get(i));
 	   }
 	   }

 	   gm.fromList(good_matches);

 	   List<KeyPoint> keypoints_objectList = keypoints_object.toList();
 	   List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

 	   for(int i = 0; i<good_matches.size(); i++){
 	   objList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
 	   sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
 	   }
 	   obj.fromList(objList);

 	   scene.fromList(sceneList);
 	   if(good_matches.size() >=4){
 	   H = Calib3d.findHomography(obj, scene,Calib3d.RANSAC, 10);
 	  
       warpimg = Mat2.clone();

       Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
       Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);

     obj_corners.put(0, 0, new double[] {0,0});
     obj_corners.put(1, 0, new double[] {Mat1.cols(),0});
     obj_corners.put(2, 0, new double[] {Mat1.cols(),Mat1.rows()});
     obj_corners.put(3, 0, new double[] {0,Mat1.rows()});
     //obj_corners:input
     Core.perspectiveTransform(obj_corners, scene_corners, H);

     Core.line(warpimg, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
     Core.line(warpimg, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
     Core.line(warpimg, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
     Core.line(warpimg, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);
 	   
 	   
// 	   warpimg = Mat1.clone();
// 	   org.opencv.core.Size ims = new org.opencv.core.Size(Mat1.cols(),Mat1.rows());
 	   //Imgproc.warpPerspective(Mat1, warpimg,H, ims);
// 	   Imgproc.warpPerspective(Mat1, warpimg,H, ims, 0);
 	  
 	   	   
 	   }
 	   
 	   
 	return warpimg;
 	   
 	   
    }

}