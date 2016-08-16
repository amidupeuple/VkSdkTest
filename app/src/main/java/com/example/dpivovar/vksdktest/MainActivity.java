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
import android.util.Base64;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private Button headerLeftButton;
    private Button headerRightButton;
    private Button headerCenterButton;
    private TextView headerText;
    private TableLayout imageTable;

    ArrayList<String> imageList = new ArrayList<String>();
    ArrayList<Drawable> imageDrawable = new ArrayList<Drawable>();
    List<Bitmap> imageBitmap = new ArrayList<>();
    private String path = "";
    private Context context;
    private Activity activity;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

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
                        Log.d(TAG, "Photo: " + imageList.get(0));
                        String path = "/sdcard/Download/Mr_Twinkle.jpg";
                        File f = new File(path);
                        f.exists();
                        VKUploadService uploadService = ServiceGenerator.createService(VKUploadService.class, uploadUrl);
                        TypedFile typedFile = new TypedFile("multipart/form-data", f);

                        Uri uri = Uri.parse(uploadUrl);
                        Set<String> queryParameterNames = uri.getQueryParameterNames();
                        HashMap<String,String> queryMap = new HashMap<>();
                        Iterator<String> iterator = queryParameterNames.iterator();

                        while(iterator.hasNext()){
                            String queryName = iterator.next();
                            String queryParameter = uri.getQueryParameter(queryName);
                            queryMap.put(queryName, queryParameter);
                        }

                        uploadService.upload(queryMap, typedFile, new Callback<JSONObject>() {
                            @Override
                            public void success(JSONObject jsonObject, Response response) {
                                Log.d(TAG, "success");
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.e(TAG, error.getMessage());
                            }
                        });

                        /*byte[] arr = convertBitmapToByteArr(imageBitmap.get(0));

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        try {
                            dos.writeBytes(twoHyphens + boundary + lineEnd);
                            dos.writeBytes("Content-Disposition: form-data; name=\"photo\"" + lineEnd);
                            dos.writeBytes(lineEnd);

                            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(arr);
                            int bytesAvailable = fileInputStream.available();

                            int maxBufferSize = 1024 * 1024;
                            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            byte[] buffer = new byte[bufferSize];

                            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                            while (bytesRead > 0) {
                                dos.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            }

                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }

                        MultipartRequest multipartRequest = new MultipartRequest(uploadUrl, null, mimeType, bos.toByteArray(), new com.android.volley.Response.Listener<NetworkResponse>() {

                            @Override
                            public void onResponse(NetworkResponse response) {
                                Toast.makeText(context, "Upload successfully!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "##SucResponse: " + new String(response.data));
                            }
                        }, new com.android.volley.Response.ErrorListener(){

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, "Upload failed!\r\n" + error.toString(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "##Response: " + new String(error.networkResponse.data));
                            }
                        });
                        try {
                            Log.d(TAG, "###Header: " + multipartRequest.getHeaders());
                            Log.d(TAG, "###Body: " + new String(multipartRequest.getBody()));
                        } catch (AuthFailureError authFailureError) {
                            authFailureError.printStackTrace();
                        }
                        Volley.newRequestQueue(context).add(multipartRequest);*/
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

    private byte[] convertBitmapToByteArr(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArr = outputStream.toByteArray();
        return byteArr;
    }

    private String convertImageToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArr = outputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArr, Base64.DEFAULT);
        return encoded;
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
                imageBitmap.add(bitmap);
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
