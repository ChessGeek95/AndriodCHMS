package com.iogroup.chms.app.Fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iogroup.chms.app.R;
import com.iogroup.chms.app.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MedicalHistory extends Fragment {

    String id ;
    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

    String url;
    //-----------------------
    private SharedPreferences sharedPreferences;

    TextView CF ,HOPI ,PDH ,CDT ,AT ,FH ,SUMM ,PDX , DR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medical_history, container, false);

        sharedPreferences = getActivity().getSharedPreferences("MyPreference" ,getActivity().MODE_PRIVATE);
        id = sharedPreferences.getString("id" ,"");

        url = getResources().getString(R.string.IP) + "/app/get_medical_history";

        CF = (TextView)view.findViewById(R.id.CF);
        HOPI = (TextView)view.findViewById(R.id.HOPI);
        PDH = (TextView)view.findViewById(R.id.PDH2);
        CDT = (TextView)view.findViewById(R.id.CDTAOA2);
        AT = (TextView)view.findViewById(R.id.AT2);
        FH = (TextView)view.findViewById(R.id.FH2);
        SUMM = (TextView)view.findViewById(R.id.summ2);
        PDX = (TextView)view.findViewById(R.id.PDX);
        DR = (TextView)view.findViewById(R.id.dr_name2);

        post.add(new BasicNameValuePair("id" ,id));
        new MedicalAsyncTsk().execute();

        return view;
    }

    private void setData(String CFi ,String HOPIi ,String PDHi ,String CDTi ,
                         String ATi ,String FHi ,String SUMMi ,String PDXi ,String DRi){

        CF.setText(CFi);
        HOPI.setText(HOPIi);
        PDH.setText(PDHi);
        CDT.setText(CDTi);
        AT.setText(ATi);
        FH.setText(FHi);
        SUMM.setText(SUMMi);
        PDX.setText(PDXi);
        DR.setText("Doctor: " + DRi);
    }

    //======================== Async task
    private class MedicalAsyncTsk extends AsyncTask<Void, Void, Void> {

        String SCF ,SHOPI ,SPDH ,SCDT ,SAT ,SFH ,SSUMM ,SPDX ,SDR;

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

                    SCF = jsonObj.getString("cf");
                    SHOPI =jsonObj.getString("hopi");
                    SPDH =jsonObj.getString("pdh");
                    SCDT =jsonObj.getString("crtoa");
                    SAT =jsonObj.getString("at");
                    SFH =jsonObj.getString("fh");
                    SSUMM =jsonObj.getString("summary");
                    SPDX =jsonObj.getString("pxd");
                    SDR =jsonObj.getString("dr_name");

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
            setData(SCF ,SHOPI ,SPDH ,SCDT ,SAT ,SFH ,SSUMM ,SPDX ,SDR);
        }

    }

}
