package com.team1058.vision;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    Mat mRgba;
    Mat hsvmat;
    Mat mMaskMat;
    Mat heirarchy;
    Mat mDilatedMat;
    List<MatOfPoint> contours;
    GearTracking sGearTracking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setMaxFrameSize(1920,1080);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
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

        mRgba = inputFrame.rgba();
        hsvmat = new Mat(mRgba.rows(), mRgba.cols(), mRgba.type());
        mMaskMat = new Mat(mRgba.rows(), mRgba.cols(), mRgba.type());
        heirarchy = new Mat(mRgba.rows(), mRgba.cols(), mRgba.type());

        contours = new ArrayList<MatOfPoint>();
        mDilatedMat = new Mat();
        int channelCount = 0;
        sGearTracking.process(mRgba);
        contours = sGearTracking.filterContoursOutput();
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            if(contours.get(contourIdx).size().area()>100)  // Minimum size allowed for consideration
            {
                Imgproc.drawContours (mRgba, contours, contourIdx, new Scalar( 0, 255, 0 ), 8);
            }
        }
        return mRgba;
    }
}
