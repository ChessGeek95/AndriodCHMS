package com.iogroup.chms.app.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iogroup.chms.app.R;
import com.iogroup.chms.app.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ProgressNoteFragment extends Fragment {

    private String id;

    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
    private String url;
    //-------------------------
    private String user_id;
    private String description;
    private int Success;
    private String DATE;
    private String dr_name = null;

    private ArrayList<NameValuePair> post2 = new ArrayList<NameValuePair>();
    private String url2;
    //-------------------------

    private SharedPreferences sharedPreferences;

    private ListView progressList;
    private ArrayList<ProgressNote> Items;
    private ProgressListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress_note, container, false);

        setHasOptionsMenu(true);

        url = getResources().getString(R.string.IP) + "/app/get_progress_notes";
        url2 = getResources().getString(R.string.IP) + "/app/set_progress_note";

        sharedPreferences = getActivity().getSharedPreferences("MyPreference" ,getActivity().MODE_PRIVATE);
        id = sharedPreferences.getString("id" ,"");
        user_id = sharedPreferences.getString("user_id" ,"");

        progressList = (ListView) view.findViewById(R.id.progress_listview);

        Items = new ArrayList<ProgressNote>();
        adapter = new ProgressListAdapter(getActivity().getApplicationContext(),Items);
        progressList.setAdapter(adapter);

        //-----------------
        post.add(new BasicNameValuePair("id" ,id));
        new GetProNoteAsyncTsk().execute();

        return view;
    }

    //------- dialog
    private void order_dialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.progress_dialog, null);

        Button Cancel_btn = (Button)view.findViewById(R.id.pro_cancel);
        Button OK_btn = (Button)view.findViewById(R.id.pro_OK);
        final EditText edt = (EditText)view.findViewById(R.id.pro_description_edt);
        builder.setView(view);
        final Dialog dialog = builder.create();

        OK_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edt.getText().toString().isEmpty()){
                    description = edt.getText().toString();
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    DATE = df.format(c.getTime());

                    Log.e("**********",user_id+" "+id);

                    post2.add(new BasicNameValuePair("id" ,id));
                    post2.add(new BasicNameValuePair("user_id" ,user_id));
                    post2.add(new BasicNameValuePair("description" ,description));
                    post2.add(new BasicNameValuePair("date" ,DATE));
                    new AddProgressAsyncTsk().execute();

                    dialog.dismiss();
                }
            }
        });

        Cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //----------------------------

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.progress, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_addPro) {
            order_dialog();
        }
        return super.onOptionsItemSelected(item);
    }

    //=================== physician's order class
    public class ProgressNote{
        String orderDescription;
        String date;
        String dr_name;

        public ProgressNote(){}
        public ProgressNote(String orderDescription ,String date ,String dr_name){
            this.orderDescription = orderDescription;
            this.date = date;
            this.dr_name = dr_name;
        }

        public String getDr_name() {
            return dr_name;
        }

        public void setDr_name(String dr_name) {
            this.dr_name = dr_name;
        }

        public String getDate() {
            return date;
        }

        public String getProgressDescription() {
            return orderDescription;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setOrderDescription(String orderDescription) {
            this.orderDescription = orderDescription;
        }

    }

    //----------------------physician's order adapter
    //============================
    public class ProgressListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<ProgressNote> Items;

        public ProgressListAdapter(Context context, ArrayList<ProgressNote> Items) {
            this.context = context;
            this.Items = Items;
        }

        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public Object getItem(int position) {
            return Items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.progress_list_item, null);
            }

            TextView description = (TextView) convertView.findViewById(R.id.progress_description);
            TextView date = (TextView) convertView.findViewById(R.id.progress_date);
            TextView dr = (TextView) convertView.findViewById(R.id.progress_dr_name);

            description.setText(Items.get(position).getProgressDescription());
            date.setText(Items.get(position).getDate());
            dr.setText("dr."+Items.get(position).getDr_name());

            return convertView;
        }

        public void addProgress(String des, String date ,String dr) {
            Items.add(0 ,new ProgressNote(des ,date ,dr));
        }
    }

    //======================== Async task
    private class GetProNoteAsyncTsk extends AsyncTask<Void, Void, Void> {

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
                    JSONObject MainjsonObj= new JSONObject(jsonStr);
                    JSONArray jsonArray = MainjsonObj.getJSONArray("notes");

                    String order ,date ,dr_name;

                    for (int i=0;i<jsonArray.length();i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);

                        order = jsonObj.getString("treatment_progress");
                        date = jsonObj.getString("date");
                        dr_name = jsonObj.getString("dr_name");

                        adapter.addProgress(order, date, dr_name);
                    }

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
            progressList.setAdapter(adapter);
        }

    }

    //======================== Async task
    private class AddProgressAsyncTsk extends AsyncTask<Void, Void, Void> {

        String jsonStr = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            try {
                jsonStr = sh.makeServiceCall(url2, ServiceHandler.POST ,post2);
                Log.e("jsonStr", jsonStr);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj= new JSONObject(jsonStr);
                    Success = jsonObj.getInt("success");

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

            if (Success == 1){
                adapter.addProgress(description, DATE, dr_name);
                progressList.setAdapter(adapter);
            }
            else{
                if(jsonStr.contains("no physician with this id")){
                    Toast.makeText(getActivity().getApplicationContext(),
                            "You can't add note", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Please try again", Toast.LENGTH_LONG).show();
                }
            }

        }

    }

}
