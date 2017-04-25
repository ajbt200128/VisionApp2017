package com.team1058.vision;

import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Austin on 4/25/2017.
 */

public class SocketValues {
    public HashMap<String,Double> values;
    public HashMap<String,MatFunction> functions;

    public static abstract class MatFunction{
        public abstract double calc(ArrayList<MatOfPoint> mats);
    }

    public SocketValues(){
        values = new HashMap<>();
        functions = new HashMap<>();
    }

    public void findValues(ArrayList<MatOfPoint> mats){
        for (String currKey: functions.keySet()) {
            MatFunction currFunction = functions.get(currKey);
            values.put(currKey,currFunction.calc(mats));
        }
        //Java 8 :(((
        //functions.forEach((k,v)-> values.put(k,v.calc(mats)));
    }



}
