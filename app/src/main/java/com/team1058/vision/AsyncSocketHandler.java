package com.team1058.vision;

import android.os.AsyncTask;
import android.os.Process;

/**
 * Created by Austin on 4/25/2017.
 */

public class AsyncSocketHandler extends AsyncTask<String, Void, String> {
    private boolean done;
    private OnTaskCompleted listener;
    public AsyncSocketHandler(OnTaskCompleted listener, String newHostName, int newPort){
        this.listener = listener;
    }
    public interface OnTaskCompleted{
        void onTaskCompleted(boolean connected);
    }
    @Override
    protected String doInBackground(String... s) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
        //Process.setThreadPriority(1);
        SocketSender sender = SocketSender.getInstance();
        done = sender.send(s);
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        listener.onTaskCompleted(done);

    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}
