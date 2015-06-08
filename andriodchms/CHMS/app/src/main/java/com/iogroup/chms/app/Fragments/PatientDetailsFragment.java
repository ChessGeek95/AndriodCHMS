package com.iogroup.chms.app.Fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iogroup.chms.app.CircularImageView;
import com.iogroup.chms.app.R;
import com.iogroup.chms.app.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class PatientDetailsFragment extends Fragment {

    String id;
    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

    String url;
    //--------------------
    private SharedPreferences sharedPreferences;

    Bitmap bitmap;
    CircularImageView picture;
    TextView name ,father_name ,birthday ,admission_date ,ward ,room ,bed ,university ,medical_center;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_details, container, false);

        url = getResources().getString(R.string.IP) + "/app/get_patient_details";

        sharedPreferences = getActivity().getSharedPreferences("MyPreference" ,getActivity().MODE_PRIVATE);
        id = sharedPreferences.getString("id" ,"");

        picture = (CircularImageView)view.findViewById(R.id.profile_img);
        name = (TextView)view.findViewById(R.id.profile_name);
        father_name = (TextView)view.findViewById(R.id.profile_father_name);
        birthday = (TextView)view.findViewById(R.id.profile_birthday);
        admission_date = (TextView)view.findViewById(R.id.profile_admission_date);
        ward = (TextView)view.findViewById(R.id.profile_ward);
        room = (TextView)view.findViewById(R.id.profile_room);
        bed = (TextView)view.findViewById(R.id.profile_bed);

        post.add(new BasicNameValuePair("id" ,id));
        Log.e("***patient_id" ,id);

        new PatientDetailsAsyncTsk().execute();

        return view;
    }

    private void setData(String imgURL ,String n ,String ftr_name ,String bith ,String admi ,
                         String w ,String r ,String b){
        /*
        String url = "http://www.learn2crack.com/wp-content/uploads/2014/04/node-cover-720x340.png";
        new LoadImage(picture).execute(url);
        */

        name.setText("Name: "+n);
        father_name.setText("Father: "+ftr_name);
        birthday.setText("Birthday: "+bith);
        admission_date.setText("Admission Date: "+admi);
        ward.setText("Ward: "+w);
        room.setText("Room: "+r);
        bed.setText("Bed: "+b);

    }


    //====== loading image
    public void loadingImageFromUrl(ImageView imageView,String Url) throws IOException {
        URL url = new URL(Url);
        Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        imageView.setImageBitmap(bmp);
    }

    //====== image loader
    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        ImageView img;
        public LoadImage(){}
        public LoadImage(ImageView img){
            this.img = img;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null)
                img.setImageBitmap(image);
        }
    }

    //======================== Async task
    private class PatientDetailsAsyncTsk extends AsyncTask<Void, Void, Void> {

        String imgurl ,Sname ,Sfather_name ,Sbirthday ,Sadmission_date ,Sward ,Sroom ,Sbed;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            ServiceHandler sh = new ServiceHandler();
            String jsonStr =null;
            // Making a request to url and getting response
            try {
                jsonStr = sh.makeServiceCall(url, ServiceHandler.POST ,post );
                Log.e("jsonStr", jsonStr);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj= new JSONObject(jsonStr);

                    imgurl = "";
                    Sname = jsonObj.getString("name");
                    Sfather_name =jsonObj.getString("father_name");
                    Sbirthday =jsonObj.getString("birthday");
                    Sadmission_date =jsonObj.getString("add_date");
                    Sward =jsonObj.getString("ward");
                    Sroom =jsonObj.getString("room");
                    Sbed =jsonObj.getString("bed");

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
/*
            if (success==0){
                Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
            }
*/
            setData(imgurl ,Sname ,Sfather_name ,Sbirthday ,Sadmission_date ,
                    Sward ,Sroom ,Sbed);
        }

    }
}
