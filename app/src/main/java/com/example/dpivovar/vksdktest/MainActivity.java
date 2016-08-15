package com.example.dpivovar.vksdktest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private Button headerLeftButton;
    private Button headerRightButton;
    private Button headerCenterButton;
    private TextView headerText;
    private TableLayout imageTable;

    ArrayList<String> imageList = new ArrayList<String>();
    ArrayList<Drawable> imageDrawable = new ArrayList<Drawable>();
    private String path = "";
    private Context context;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VKSdk.initialize(this);

        context = this;
        activity = this;
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);

        headerLeftButton = (Button) findViewById(R.id.header_left_btn);
        headerRightButton = (Button) findViewById(R.id.header_right_btn);
        headerCenterButton = (Button) findViewById(R.id.header_center_btn);
        headerText = (TextView) findViewById(R.id.header_text);
        imageTable = (TableLayout) findViewById(R.id.image_table);

        headerText.setText("Image Table");
        headerLeftButton.setText("Select");
        headerRightButton.setText("Clear");
        headerCenterButton.setText("Send");
        registerForContextMenu(headerLeftButton);

        headerLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openContextMenu(headerLeftButton);
            }
        });

        headerRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageList.clear();
                imageDrawable.clear();;
                deletePhotos();
                updateImageTable();
            }
        });

        headerCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "try to receive upload server url");
                VKApi.photos().getWallUploadServer().executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Log.d(TAG, "Response received!");
                        String uploadUrl = null;
                        try {
                            uploadUrl = response.json.getJSONObject("response").getString("upload_url");
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        Log.d(TAG, "Upload url: " + uploadUrl);


                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        super.attemptFailed(request, attemptNumber, totalAttempts);
                        Log.d(TAG, "enter attemptFailed()");
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Log.d(TAG, "enter onError()");
                    }

                    @Override
                    public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                        super.onProgress(progressType, bytesLoaded, bytesTotal);
                        Log.d(TAG, "enter  onProgress()");
                    }
                });
            }
        });
        VKSdk.login(activity, VKScope.PHOTOS);
    }

    private void updateImageTable() {
        imageTable.removeAllViews();

        if(imageDrawable.size() > 0) {
            for(int i=0; i < imageDrawable.size(); i++) {
                TableRow tableRow=new TableRow(this);
                tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                tableRow.setPadding(5, 5, 5, 5);
                for(int j=0; j<1; j++)
                {
                    ImageView image=new ImageView(this);
                    image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    /*Bitmap bitmap = BitmapFactory.decodeFile(image_list.get(i).toString().trim());
                    bitmap = Bitmap.createScaledBitmap(bitmap,500, 500, true);
                    Drawable d=loadImagefromurl(bitmap);*/
                    image.setBackgroundDrawable(imageDrawable.get(i));

                    tableRow.addView(image, 200, 200);
                }
                imageTable.addView(tableRow);
            }
        }
    }

    private void deletePhotos() {
        String folder = Environment.getExternalStorageState() + "/LoadImg";
        File f = new File(folder);
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            Log.d(TAG, "Files to delete >>>>>>>> " + files.length);
            for (int i = 0; i < files.length; i++) {
                String fpath = folder + File.separator + files[i].getName().toString().trim();
                System.out.println("File Full Path======>>> " + fpath);
                File nf = new File(fpath);
                if (nf.exists()) {
                    nf.delete();
                }
            }
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Post Image");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_take_photo:
                takePhoto();
                break;
        }

        return true;
    }

    private void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File folder = new File(Environment.getExternalStorageDirectory() + "/LoadImg");

        if(!folder.exists()) {
            folder.mkdir();
        }

        final Calendar c = Calendar.getInstance();
        String new_Date= c.get(Calendar.DAY_OF_MONTH)+"-"+((c.get(Calendar.MONTH))+1)   +"-"+c.get(Calendar.YEAR) +" " + c.get(Calendar.HOUR) + "-" + c.get(Calendar.MINUTE)+ "-"+ c.get(Calendar.SECOND);
        path=String.format(Environment.getExternalStorageDirectory() +"/LoadImg/%s.png","LoadImg("+new_Date+")");
        File photo = new File(path);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        startActivityForResult(intent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d(TAG, "Success");
            }
            @Override
            public void onError(VKError error) {
                Log.d(TAG, "auth error");
            }
        }));

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==2) {
            Log.v("Load Image", "Camera File Path=====>>>"+path);
            imageList.add(path);
            Log.v("Load Image", "Image List Size=====>>>"+imageList.size());
            //updateImageTable();
            new GetImages().execute();
        }


    }

    public class GetImages extends AsyncTask<Void, Void, Void> {
        public ProgressDialog progDialog=null;

        protected void onPreExecute() {
            progDialog=ProgressDialog.show(context, "", "Loading...",true);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            imageDrawable.clear();
            for(int i=0; i<imageList.size(); i++)
            {
                Bitmap bitmap = BitmapFactory.decodeFile(imageList.get(i).toString().trim());
                bitmap = Bitmap.createScaledBitmap(bitmap,500, 500, true);
                Drawable d=loadImagefromurl(bitmap);

                imageDrawable.add(d);
            }
            return null;
        }

        protected void onPostExecute(Void result)
        {
            if(progDialog.isShowing())
            {
                progDialog.dismiss();
            }
            updateImageTable();
        }
    }

    public Drawable loadImagefromurl(Bitmap icon) {
        Drawable d=new BitmapDrawable(icon);

        return d;
    }
}
