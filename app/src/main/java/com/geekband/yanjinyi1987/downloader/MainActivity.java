package com.geekband.yanjinyi1987.downloader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mOKButton;
    private Button mCancelButton;
    private ListView mProgressBarList;
    private List<MyListItem> downloader_progresses;
    private EditText mUrlsInput;
    private MyDownloadTask myDownloadTask[];
    private MyProgressBarAdapter myProgressBarAdapter;
    private int urls_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mOKButton = (Button) findViewById(R.id.ok_button);
        mCancelButton = (Button) findViewById(R.id.cancle_button);
        mProgressBarList = (ListView) findViewById(R.id.downloader_lists);
        mUrlsInput = (EditText) findViewById(R.id.urls_input);
        downloader_progresses = new ArrayList<>();
        mCancelButton.setEnabled(false);


        myProgressBarAdapter = new MyProgressBarAdapter(MainActivity.this,0, downloader_progresses);
        mProgressBarList.setAdapter(myProgressBarAdapter);
        
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloader_progresses.clear();
                String[] urls_test = {"http://mirrors.ustc.edu.cn/eclipse/aether/aether-core/1.0.1/dist/aether-1.0.1-bin.zip",
                        "http://mirrors.ustc.edu.cn/eclipse/aether/aether-core/1.0.1/dist/aether-1.0.1-src.zip",
                        "http://w.xavbt.com/7/uploads/2016/09/1_141113064612.jpg"};

                mUrlsInput.setText(urls_test[0]+"\n"+urls_test[1]+"\n"+urls_test[2]);
                String[] urls = getUrlsFromEditText();

                //urls = urls_test;
                List<String> urls_list = new ArrayList<>();
                urls_count = urls.length;
                for (int i = 0; i < urls_count; i++) {
                    if(!urls[i].equals("")) {
                        downloader_progresses.add(new MyListItem(0));
                        urls_list.add(urls[i]);
                        Log.i("show String", urls[i]);
                    }
                }
                urls_count = urls_list.size();
                myProgressBarAdapter.notifyDataSetChanged();

                mOKButton.setEnabled(false);
                mUrlsInput.setEnabled(false);
                mCancelButton.setEnabled(true);

                myDownloadTask = new MyDownloadTask[urls_count];
                for (int i = 0; i < urls_count; i++) {
                    myDownloadTask[i]=new MyDownloadTask();
                    myDownloadTask[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new InputParameters(urls_list.get(i),
                            downloader_progresses.get(i),
                            i));
                }

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOKButton.setEnabled(true);
                mUrlsInput.setEnabled(true);
                mCancelButton.setEnabled(false);
                downloader_progresses.clear();
                myProgressBarAdapter.notifyDataSetChanged();
                for (int i = 0; i < downloader_progresses.size(); i++) {
                    myDownloadTask[i].cancel(true);
                }

            }
        });
    }

    private String[] getUrlsFromEditText() {
        ArrayList<String> urls = new ArrayList<>();
        String text = mUrlsInput.getText().toString();
        Log.i("File", Environment.getDownloadCacheDirectory().getAbsolutePath());
        return text.split("\n");
    }

    class InputParameters {
        public String url;
        public MyListItem myListItem;
        public int position;

        public InputParameters(String url, MyListItem myListItem,int position) {
            this.url = url;
            this.myListItem = myListItem;
            this.position = position;
        }
    }

    public static Integer count=0;

    boolean checkFinishedAll() {
        synchronized (count) {
            count++;
            if(count!=urls_count) {
                return false;
            }
            else {
                count=0;
                mOKButton.setEnabled(true);
                mUrlsInput.setEnabled(true);
                mCancelButton.setEnabled(false);
                downloader_progresses.clear();
                myProgressBarAdapter.notifyDataSetChanged();
                return true;
            }
        }
    }

    public class MyDownloadTask extends AsyncTask<InputParameters,Integer,Boolean> {


        public static final String TAG = "MyDownloadTask";
        InputParameters inputParameters;
        private Downloader downloader;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG,"onPreExecute");

        }


        @Override
        protected Boolean doInBackground(InputParameters... params) {
            boolean result=false;
            inputParameters = params[0];
            Log.i(TAG,"doInBackground"+inputParameters.position);
            downloader = new Downloader(MainActivity.this);
            try {
                result = downloader.httpDownloader(params[0].url,this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                return result;
            }
        }

        public void setProgress(int i) {
            publishProgress(i);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //Log.i(TAG,"onProgressUpdate");
            inputParameters.myListItem.progress=values[0];
            myProgressBarAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            inputParameters.myListItem.mStopButton.setText("Done");
            checkFinishedAll();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(TAG,"onCancelled");
            downloader.setBreak();
        }
    }
}

class MyListItem {
    ProgressBar mProgressBar;
    TextView mStopButton;
    public int progress;

    public MyListItem(int progress) {
        this.progress = progress;
    }
}
class MyProgressBarAdapter extends ArrayAdapter {
    List<MyListItem> mExternalListItems;
    public MyProgressBarAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        mExternalListItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int progress = ((MyListItem)getItem(position)).progress;
        View view;
        ProgressBar mProgressBar;
        TextView mStopButton;
        if(convertView==null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
            mProgressBar = (ProgressBar) view.findViewById(R.id.download_progress);
            mStopButton = (TextView) view.findViewById(R.id.stop_image_button);
            ((MyListItem)getItem(position)).mProgressBar = mProgressBar;
            ((MyListItem)getItem(position)).mStopButton = mStopButton;
            ViewHolder viewHolder = new ViewHolder(mProgressBar,mStopButton);
            view.setTag(viewHolder);
            mProgressBar.setProgress(progress);
        }
        else {
            view = convertView;
            ((ViewHolder)view.getTag()).mProgressBar.setProgress(progress);
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        for (int i = 0; i < getCount(); i++) {
            View view = getView(i,null,null);
//            mExternalListItems.get(i).mProgressBar = ((ViewHolder)view.getTag()).mProgressBar;
//            mExternalListItems.get(i).mStopButton = ((ViewHolder)view.getTag()).mStopButton;
        }
    }

    class ViewHolder {
        public ProgressBar mProgressBar;
        public TextView mStopButton;

        public ViewHolder(ProgressBar mProgressBar, TextView mStopButton) {
            this.mProgressBar = mProgressBar;
            this.mStopButton = mStopButton;
        }
    }
}
