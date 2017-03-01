package com.team1058.vision;

/**
 * Created by Austin on 2/24/2017.
 */
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
public class GearTracking {
    //Outputs
    private Mat resizeImageOutput = new Mat();
    private Mat hslThresholdOutput = new Mat();
    private ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
    private ArrayList<MatOfPoint> filterContoursOutput = new ArrayList<MatOfPoint>();

    //Sources
    private Mat source0;

    /**
     * This constructor sets up the pipeline
     */
    public GearTracking() {
    }

    /**
     * This is the primary method that runs the entire pipeline and updates the outputs.
     */
    public void process(Mat source) {
        //Step  Resize_Image0:
        source0 = source;
        Mat resizeImageInput = source0;
        double resizeImageWidth = 1280;
        double resizeImageHeight = 720;
        int resizeImageInterpolation = Imgproc.INTER_CUBIC;
        resizeImage(resizeImageInput, resizeImageWidth, resizeImageHeight, resizeImageInterpolation, resizeImageOutput);

        //Step  HSL_Threshold0:
        Mat hslThresholdInput = resizeImageOutput;
        double[] hslThresholdHue = {49.808928122858994, 177.59049808872928};
        double[] hslThresholdSaturation = {160.52158273381295, 255.0};
        double[] hslThresholdLuminance = {103.45323741007192, 200.0};
        hslThreshold(hslThresholdInput, hslThresholdHue, hslThresholdSaturation, hslThresholdLuminance, hslThresholdOutput);

        //Step  Find_Contours0:
        Mat findContoursInput = hslThresholdOutput;
        boolean findContoursExternalOnly = false;
        findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);

        //Step  Filter_Contours0:
        ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 20.0;
        double filterContoursMinPerimeter = 0.0;
        double filterContoursMinWidth = 0.0;
        double filterContoursMaxWidth = 1000.0;
        double filterContoursMinHeight = 0.0;
        double filterContoursMaxHeight = 1000.0;
        double[] filterContoursSolidity = {0.0, 100.0};
        double filterContoursMaxVertices = 1000000;
        double filterContoursMinVertices = 0;
        double filterContoursMinRatio = 0;
        double filterContoursMaxRatio = 1000;
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);

    }

    public void setsource0(Mat source0) {
        this.source0 = source0;
    }

    /**
     * This method is a generated getter for the output of a Resize_Image.
     * @return Mat output from Resize_Image.
     */
    public Mat resizeImageOutput() {
        return resizeImageOutput;
    }

    /**
     * This method is a generated getter for the output of a HSL_Threshold.
     * @return Mat output from HSL_Threshold.
     */
    public Mat hslThresholdOutput() {
        return hslThresholdOutput;
    }

    /**
     * This method is a generated getter for the output of a Find_Contours.
     * @return ArrayList<MatOfPoint> output from Find_Contours.
     */
    public ArrayList<MatOfPoint> findContoursOutput() {
        return findContoursOutput;
    }

    /**
     * This method is a generated getter for the output of a Filter_Contours.
     * @return ArrayList<MatOfPoint> output from Filter_Contours.
     */
    public ArrayList<MatOfPoint> filterContoursOutput() {
        return filterContoursOutput;
    }


    /**
     * Scales and image to an exact size.
     * @param input The image on which to perform the Resize.
     * @param width The width of the output in pixels.
     * @param height The height of the output in pixels.
     * @param interpolation The type of interpolation.
     * @param output The image in which to store the output.
     */
    private void resizeImage(Mat input, double width, double height,
                             int interpolation, Mat output) {
        Imgproc.resize(input, output, new Size(width, height), 0.0, 0.0, interpolation);
    }

    private void hslThreshold(Mat input, double[] hue, double[] sat, double[] lum,
                              Mat out) {
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HLS);
        Core.inRange(out, new Scalar(hue[0], lum[0], sat[0]),
                new Scalar(hue[1], lum[1], sat[1]), out);
    }

    private void findContours(Mat input, boolean externalOnly,
                              List<MatOfPoint> contours) {
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if (externalOnly) {
            mode = Imgproc.RETR_EXTERNAL;
        }
        else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);
    }
    private void filterContours(List<MatOfPoint> inputContours, double minArea,
                                double minPerimeter, double minWidth, double maxWidth, double minHeight, double
                                        maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double
                                        minRatio, double maxRatio, List<MatOfPoint> output) {
        final MatOfInt hull = new MatOfInt();
        output.clear();
        //operation
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) continue;
            if (bb.height < minHeight || bb.height > maxHeight) continue;
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) continue;
            if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) continue;
            Imgproc.convexHull(contour, hull);
            MatOfPoint mopHull = new MatOfPoint();
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int)hull.get(j, 0)[0];
                double[] point = new double[] { contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            final double solid = 100 * area / Imgproc.contourArea(mopHull);
            if (solid < solidity[0] || solid > solidity[1]) continue;
            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount)	continue;
            final double ratio = bb.width / (double)bb.height;
            if (ratio < minRatio || ratio > maxRatio) continue;
            output.add(contour);
        }
    }
}
