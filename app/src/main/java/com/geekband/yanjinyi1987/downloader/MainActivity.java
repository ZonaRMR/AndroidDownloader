package com.geekband.yanjinyi1987.downloader;

import android.content.Context;
import android.os.AsyncTask;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mOKButton;
    private Button mCancelButton;
    private ListView mProgressBarList;
    private List<MyListItem> downloader_progresses;
    private EditText mUrlsInput;
    private MyDownloadTask myDownloadTask[];

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


        final MyProgressBarAdapter myProgressBarAdapter = new MyProgressBarAdapter(MainActivity.this,0, downloader_progresses);
        mProgressBarList.setAdapter(myProgressBarAdapter);
        
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloader_progresses.clear();
                String[] urls = getUrlsFromEditText();
                int urls_count = urls.length;
                for (int i = 0; i < urls_count; i++) {
                    downloader_progresses.add(new MyListItem(0));
                }
                //myProgressBarAdapter.notifyDataSetChanged();

                mOKButton.setEnabled(false);
                mUrlsInput.setEnabled(false);
                mCancelButton.setEnabled(true);

                myDownloadTask = new MyDownloadTask[urls_count];
                for (int i = 0; i < urls_count; i++) {
                    myDownloadTask[i].execute(urls[i]);
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
                myDownloadTask.cancel(true);
            }
        });
    }

    private String[] getUrlsFromEditText() {
        ArrayList<String> urls = new ArrayList<>();
        String text = mUrlsInput.getText().toString();
        return text.split("\n");
    }

    private class MyDownloadTask extends AsyncTask<String,Integer,String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return new String();
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

class MyListItem {
//    ProgressBar mProgressBar;
//    ImageButton mStopButton;
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
        ImageButton mStopButton;
        if(convertView==null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
            mProgressBar = (ProgressBar) view.findViewById(R.id.download_progress);
            mStopButton = (ImageButton) view.findViewById(R.id.stop_image_button);
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
        public ImageButton mStopButton;

        public ViewHolder(ProgressBar mProgressBar, ImageButton mStopButton) {
            this.mProgressBar = mProgressBar;
            this.mStopButton = mStopButton;
        }
    }
}
