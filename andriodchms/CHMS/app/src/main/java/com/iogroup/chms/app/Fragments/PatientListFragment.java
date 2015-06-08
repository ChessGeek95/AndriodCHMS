package com.iogroup.chms.app.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.iogroup.chms.app.NavigationActivity;
import com.iogroup.chms.app.PatientListItem;
import com.iogroup.chms.app.R;
import com.iogroup.chms.app.ServiceHandler;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientListFragment extends Fragment implements SearchView.OnQueryTextListener{

    private String user_id;
    private String user_type;

    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
    private String url;
    //------------------------

    private ArrayList<PatientListItem> Items;
    private PatientListAdapter adapter;
    private ListView patientList;
    private SearchView mSearchView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_list, container, false);
        setHasOptionsMenu(true);
        getActivity().openOptionsMenu();

        url = getResources().getString(R.string.IP)+"/app/get_list";

        patientList = (ListView) view.findViewById(R.id.patient_listview2);

        sharedPreferences = getActivity().getSharedPreferences("MyPreference" ,getActivity().MODE_PRIVATE);
        editor = sharedPreferences.edit();
        user_id = sharedPreferences.getString("user_id" ,"");
        user_type = sharedPreferences.getString("user_type" ,"");

        Items = new ArrayList<PatientListItem>();
        adapter = new PatientListAdapter(getActivity().getApplicationContext(),Items);
        patientList.setAdapter(adapter);

        patientList.setTextFilterEnabled(true);

        //-----------------
        Log.e("user_id",user_id+" "+user_type);
        post.add(new BasicNameValuePair("user_id", user_id));
        post.add(new BasicNameValuePair("type" ,user_type));
        new GetListAsyncTsk().execute();

        patientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                editor.putString("id" ,Items.get(i).getId());
                editor.commit();

                Intent intent = new Intent(getActivity().getApplicationContext(), NavigationActivity.class);
                intent.putExtra("Display", true);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.patient_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();

        mSearchView.setOnQueryTextListener(this);

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.getFilter().filter(s);
        return false;
    }


    //============================
    public class PatientListAdapter extends BaseAdapter implements Filterable{

        private Context context;
        private ArrayList<PatientListItem> Items;
        private ArrayList<PatientListItem> filteredData;
        private ItemFilter mFilter = new ItemFilter();

        public PatientListAdapter(Context context, ArrayList<PatientListItem> Items) {
            this.context = context;
            this.Items = Items;
            this.filteredData = Items;
        }

        @Override
        public int getCount() {
            return filteredData.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredData.get(position);
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
                convertView = mInflater.inflate(R.layout.patient_list_item, null);
            }

            TextView name = (TextView) convertView.findViewById(R.id.patient_name);
            TextView ward = (TextView) convertView.findViewById(R.id.patient_ward);
            TextView room = (TextView) convertView.findViewById(R.id.patient_room);
            TextView bed = (TextView) convertView.findViewById(R.id.patient_bed);

            name.setText(filteredData.get(position).getName());
            ward.setText("W: " + filteredData.get(position).getWard());
            room.setText("R: " + filteredData.get(position).getRoom());
            bed.setText("B: " + filteredData.get(position).getBed());

            return convertView;
        }

        public void addPatient(String id, String name, String ward, String room, String bed) {
            Items.add(new PatientListItem(id, name, ward, room, bed));
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String filterString = constraint.toString().toLowerCase();

                FilterResults results = new FilterResults();

                final List<PatientListItem> list = Items;

                int count = list.size();
                final ArrayList<PatientListItem> nlist = new ArrayList<PatientListItem>(count);

                String filterableString ;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i).getName();
                    if (filterableString.toLowerCase().contains(filterString)) {
                        nlist.add(list.get(i));
                    }
                }

                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<PatientListItem>) results.values;
                notifyDataSetChanged();
            }

        }
    }

    //======================== Async task
    private class GetListAsyncTsk extends AsyncTask<Void, Void, Void> {

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
                    JSONArray jsonArray = MainjsonObj.getJSONArray("response");

                    String id, name ,ward ,room ,bed;

                    for (int i=0;i<jsonArray.length();i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);

                        id = jsonObj.getString("id");
                        name = jsonObj.getString("name");
                        ward = jsonObj.getString("ward");
                        room = jsonObj.getString("room");
                        bed = jsonObj.getString("bed");

                        adapter.addPatient(id ,name ,ward ,room ,bed);
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
            patientList.setAdapter(adapter);
        }

    }

}
