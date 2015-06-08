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

public class UnitSummery extends Fragment {

    String id;
    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

    String url;
    //-----------------------
    private SharedPreferences sharedPreferences;

    TextView CCAPD ,FD ,MASP ,ROPE ,DP , RAD ,PCOD ,dr_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_summery, container, false);

        url = getResources().getString(R.string.IP) + "/app/get_unit_summary";

        sharedPreferences = getActivity().getSharedPreferences("MyPreference" ,getActivity().MODE_PRIVATE);
        id = sharedPreferences.getString("id" ,"");

        CCAPD = (TextView)view.findViewById(R.id.CCAPD);
        FD = (TextView)view.findViewById(R.id.FD);
        MASP = (TextView)view.findViewById(R.id.MASP);
        ROPE = (TextView)view.findViewById(R.id.ROPE);
        DP = (TextView)view.findViewById(R.id.DP2);
        RAD = (TextView)view.findViewById(R.id.RAD);
        PCOD = (TextView)view.findViewById(R.id.PCOD);
        dr_name = (TextView)view.findViewById(R.id.dr_name);

        post.add(new BasicNameValuePair("id" ,id));
        new UnitSummAsyncTsk().execute();

        return view;
    }

    private void setData(String CCAPDi ,String FDi ,String MASPi ,String ROPEi ,
                         String DPi ,String RADi ,String PCODi ,String dr){

        CCAPD.setText(CCAPDi);
        FD.setText(FDi);
        MASP.setText(MASPi);
        ROPE.setText(ROPEi);
        DP.setText(DPi);
        RAD.setText(RADi);
        PCOD.setText(PCODi);
        dr_name.setText("Doctor: " + dr);
    }

    //======================== Async task
    private class UnitSummAsyncTsk extends AsyncTask<Void, Void, Void> {

        String SCCAPD ,SFD ,SMASP ,SROPE ,Sdp ,SRAD ,SPCOD ,Sdr_name;

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

                    SCCAPD = jsonObj.getString("ccapd");
                    SFD =jsonObj.getString("fd");
                    SMASP =jsonObj.getString("maps");
                    SROPE =jsonObj.getString("rope");
                    Sdp =jsonObj.getString("dp");
                    SRAD =jsonObj.getString("ra");
                    SPCOD =jsonObj.getString("pd");
                    Sdr_name =jsonObj.getString("dr_name");

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
            setData(SCCAPD ,SFD ,SMASP ,SROPE ,Sdp ,SRAD ,SPCOD ,Sdr_name);
        }

    }

}
