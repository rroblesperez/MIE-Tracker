package com.mie.rrobles.tracker;


import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

public class camera_homography extends Activity implements CvCameraViewListener2 {
    private static final String    TAG = "camera_homography::Activity";


    public int                    mViewMode;
    private Mat                   mRgba;
	public Mat                    image = new Mat();
	public Size                   resolution;
	
	public MatOfKeyPoint          keypoints_object = new MatOfKeyPoint();
	public Mat                    descriptors_object = new Mat();
	public Object                 fd = FeatureDetector.create(FeatureDetector.ORB);
	public DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);


	
    private camera_view   mOpenCvCameraView;

    public Mat target = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
    		"/MIETracker/target01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
    public Mat scene = Highgui.imread(Environment.getExternalStorageDirectory().getPath() +
    		"/MIETracker/scene01.jpg", Highgui.CV_LOAD_IMAGE_UNCHANGED);
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public camera_homography() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_layout);
        
        
//        resolution.height = 640;
//        resolution.width = 480;
//        mOpenCvCameraView.setResolution(resolution);
        
        targetFeatures(target);

        mOpenCvCameraView = (camera_view) findViewById(R.id.camera_layout);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

            // input frame has RGBA format
            mRgba = inputFrame.rgba();
            image = Features2DHomografia(mRgba);

            //FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

        return image;
    }

    
    public void targetFeatures(Mat target)
    {
  	   ((FeatureDetector) fd).detect( target, keypoints_object );
 	   extractor.compute( target, keypoints_object, descriptors_object );

    	
    }
    
    
    public Mat Features2DHomografia(Mat Mat2)
    {
 	   Mat warpimg = new Mat();
 	   Mat H = new Mat();
//
//
 	   MatOfDMatch matches = new MatOfDMatch();
// 	   MatOfDMatch gm = new MatOfDMatch();
// 	   
 	   LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
 	   LinkedList<Point> objList = new LinkedList<Point>();
 	   LinkedList<Point> sceneList = new LinkedList<Point>();
 	   
 	
 	   MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();
 	   
 	   Mat descriptors_scene = new Mat();
 	   
 	   MatOfPoint2f obj = new MatOfPoint2f();
 	   MatOfPoint2f scene = new MatOfPoint2f(); 	   
 	   


 	   ((FeatureDetector) fd).detect( Mat2, keypoints_scene );
 	  
 	   //Log.i(TAG, "Keypoints target " + keypoints_object.size());
       //Log.i(TAG, "Keypoints scene " + keypoints_scene.size());
 	   
 	   //Calculate descriptors
 	   extractor.compute( Mat2, keypoints_scene, descriptors_scene );
 	   
 	  //Log.i(TAG, "Descriptor target " + descriptors_object.size());
      //Log.i(TAG, "Descriptor scene " + descriptors_scene.size());
   
 	   DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
 	   if ((descriptors_object.type() == descriptors_scene.type()) && (descriptors_object.cols() == descriptors_scene.cols())) {
 		   matcher.match(descriptors_object, descriptors_scene, matches);
 	   }
 	  
 	  double max_dist = 0; double min_dist = 100; double num_iter = 0;
	   List<DMatch> matchesList = matches.toList();
 	  
// 	   if (matches.size().height > 10)
// 	   {
// 	 	   Log.i(TAG, "Matches: "+ matchesList.size());
//
//// 		   Toast.makeText(this, "Detectado", Toast.LENGTH_SHORT).show();  
// 	   }
////
// 	   
//       Log.i(TAG, "Object rows: " + descriptors_object.rows());
//       Log.i(TAG, "Scene  rows: " + descriptors_scene.rows());
//       Log.i(TAG, "Object cols: " + descriptors_object.cols());
//       Log.i(TAG, "Scene  cols: " + descriptors_scene.cols());

 	   //– Quick calculation of max and min distances between keypoints
       if (descriptors_scene.rows() < descriptors_object.rows()){
    	   num_iter = descriptors_scene.rows();
       } else {
    	   num_iter = descriptors_object.rows();
       }
       
 	   for( int i = 0; i < num_iter; i++ )
 	   {
	 	   Double dist = (double) matchesList.get(i).distance;
	 	   if( dist < min_dist ) min_dist = dist;
	 	   if( dist > max_dist ) max_dist = dist;
 	   }
 	   
       Log.i(TAG, "Matched calculated. Max: " + max_dist + " Min: " + min_dist);


 	   for(int i = 0; i < num_iter; i++){
	 	   if(matchesList.get(i).distance < 3*min_dist){
	 		   good_matches.addLast(matchesList.get(i));
	 	   }
 	   }
 	   

 	   
 	   Log.i(TAG, "Good matches: " + good_matches.size());


//       Features2d.drawMatches(Mat2, keypoints_scene, Mat1, keypoints_object, good_matches, warpimg);

// 	   gm.fromList(good_matches);

 	   List<KeyPoint> keypoints_objectList = keypoints_object.toList();
 	   List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

 	   for(int i = 0; i<good_matches.size(); i++){
	 	   objList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
	 	   sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
 	   }
 	   obj.fromList(objList);

 	   scene.fromList(sceneList);

       warpimg = Mat2.clone();

 	   if(good_matches.size() >=25){
//
// 		   Toast.makeText(this, "Detectado", Toast.LENGTH_SHORT).show();
//
 		   H = Calib3d.findHomography(obj, scene,Calib3d.RANSAC, 10);
	 	  
	
	       Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
	       Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
	
	       obj_corners.put(0, 0, new double[] {0,0});
	       obj_corners.put(1, 0, new double[] {target.cols(),0});
	       obj_corners.put(2, 0, new double[] {target.cols(),target.rows()});
	       obj_corners.put(3, 0, new double[] {0,target.rows()});
	       //obj_corners:input
	       Core.perspectiveTransform(obj_corners, scene_corners, H);
	
	       Core.line(warpimg, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
	       Core.line(warpimg, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
	       Core.line(warpimg, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
	       Core.line(warpimg, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);	 	   
 	 }
 	   
 	   
 	return warpimg;
 	   
 	   
    }

    
}
