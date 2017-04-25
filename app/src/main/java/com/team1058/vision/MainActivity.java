package com.team1058.vision;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.gson.Gson;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2, AsyncSocketHandler.OnTaskCompleted {
    private CameraBridgeViewBase mOpenCvCameraView;

    private GearTracking sGearTracking;

    private AsyncSocketHandler mAsyncSocketHandler;
    private SocketValues mValues;

    private boolean connected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setMaxFrameSize(1920,1080);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mAsyncSocketHandler = new AsyncSocketHandler(this,"10.0.0.12",1058);

        mValues = new SocketValues();


        mValues.functions.put("Distance",new SocketValues.MatFunction(){
            @Override
            public double calc(ArrayList<MatOfPoint> mats){
                    if(mats.size() >= 2){
                        Rect r1 = Imgproc.boundingRect(mats.get(0));
                        Rect r2 = Imgproc.boundingRect(mats.get(1));
                        Rect temp;
                        if(r2.x < r1.x){
                            temp = r1;
                            r1 = r2;
                            r2 = temp;
                        }
                        return ((r2.x + (r1.x + r1.width))/2)-1080;

                    }
               return 0.0;
            }
        });
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Log.i(TAG, "OpenCV loaded");
                    mOpenCvCameraView.enableView();
                    sGearTracking  = new GearTracking();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat mRgba = inputFrame.rgba();

        ArrayList<MatOfPoint> contours = findContours(mRgba);

        if(mAsyncSocketHandler.getStatus() != AsyncTask.Status.RUNNING){
            mAsyncSocketHandler = new AsyncSocketHandler(this,"10.0.0.12",1058);
            mValues.findValues(contours);
            mAsyncSocketHandler.execute(new Gson().toJson(mValues.values));
        }

        Log.d("AsyncTask: ",mAsyncSocketHandler.getStatus().toString());

        Imgproc.putText(mRgba,"Connection status: "+String.valueOf(connected),new Point(33.0,80),1,3.0,new Scalar(255,0,0));
        return mRgba;
    }

    @Override
    public void onTaskCompleted(boolean done){
        connected = done;
    }

    public ArrayList<MatOfPoint> findContours(Mat mRgba){
        ArrayList<MatOfPoint> contours;
        sGearTracking.process(mRgba);
        contours = sGearTracking.filterContoursOutput();
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            if(contours.get(contourIdx).size().area()>100)  // Minimum size allowed for consideration
            {
                Imgproc.drawContours (mRgba, contours, contourIdx, new Scalar( 0, 255, 0 ), 8);
            }
        }
        return contours;
    }
}
