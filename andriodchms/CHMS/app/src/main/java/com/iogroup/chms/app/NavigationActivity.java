package com.iogroup.chms.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iogroup.chms.app.Fragments.MedicalHistory;
import com.iogroup.chms.app.Fragments.PatientDetailsFragment;
import com.iogroup.chms.app.Fragments.PatientListFragment;
import com.iogroup.chms.app.Fragments.PhysicianOrderFragment;
import com.iogroup.chms.app.Fragments.ProgressNoteFragment;
import com.iogroup.chms.app.Fragments.UnitSummery;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class NavigationActivity extends Activity{
    private DrawerLayout mDrawerLayout;
    private LinearLayout Drawer_linLayout;
    private ListView mDrawerList;
    private ListView PatientListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout patientList_layout;

    // nav drawer title
    private CharSequence mDrawerTitle;

    private int BackCount = 2;
    private int Position = 0;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private ArrayList<NavDrawerItem> navDrawerItems2;
    private NavDrawerListAdapter adapter;
    private NavDrawerListAdapter adapter2;
    boolean display_patient_list = false;

    TextView DR_NAME ,DR_SPEC;
    CircularImageView DR_PIC;
    Bitmap bitmap;
    //---------------------------------
    private String user_id;
    private String user_type;
    private String DR_name ,DR_speciality ,DR_pic ,url;
    private ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
    private DrDetailsAsyncTsk drDetailsAsyncTsk;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_navigation);

        sharedPreferences = getSharedPreferences("MyPreference" ,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        user_id = sharedPreferences.getString("user_id" ,"");
        user_type = sharedPreferences.getString("user_type","");

        try {
            display_patient_list = getIntent().getExtras().getBoolean("Display");

        } catch (Exception e) {
            display_patient_list = false;
        }

        url = getResources().getString(R.string.IP) + "/app/get_doctor_details";
        //=============================================
        mTitle = mDrawerTitle = "CHMS";
        getActionBar().setTitle("CHMS");
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.green)));

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Drawer_linLayout = (LinearLayout) findViewById(R.id.drawer_linLyout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        PatientListView = (ListView) findViewById(R.id.patient_listview);
        patientList_layout = (LinearLayout) findViewById(R.id.patientList_layout);
        DR_NAME = (TextView) findViewById(R.id.nav_profile_name);
        DR_SPEC = (TextView) findViewById(R.id.nav_profile_speciality);
        DR_PIC = (CircularImageView) findViewById(R.id.nav_profile_img);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));

        navDrawerItems2 = new ArrayList<NavDrawerItem>();

        navDrawerItems2.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems2.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems2.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navDrawerItems2.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1)));
        navDrawerItems2.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(7, -1)));

        navMenuIcons.recycle();

        if(isNetworkAvailable()) {
            post.add(new BasicNameValuePair("dr_id" ,user_id));
            post.add(new BasicNameValuePair("type" ,user_type));
            drDetailsAsyncTsk = new DrDetailsAsyncTsk();
            drDetailsAsyncTsk.execute();
        }else{
            Toast.makeText(getApplicationContext() ,"Network connection unavailable" ,Toast.LENGTH_LONG).show();
        }

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        PatientListView.setOnItemClickListener(new SlideMenuClickListener2());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext() ,navDrawerItems);
        mDrawerList.setAdapter(adapter);
        adapter2 = new NavDrawerListAdapter(getApplicationContext() ,navDrawerItems2);
        PatientListView.setAdapter(adapter2);



        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        /////////////////////////////////////////////////////////////////////

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (display_patient_list) {
            displayView(3);
        } else{
            displayView(0);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(drDetailsAsyncTsk != null && drDetailsAsyncTsk.getStatus() == AsyncTask.Status.RUNNING)
            drDetailsAsyncTsk.cancel(true);
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    private class SlideMenuClickListener2 implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position+3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayView(int position) {
        Position = position;
        Fragment fragment = null;
        switch (position) {
            case 0:
                patientList_layout.setVisibility(View.GONE);
                fragment = new PatientListFragment();
                break;
            case 1:
                // setting
                break;
            case 2:
                editor.remove("username");
                editor.remove("password");
                editor.commit();
                Intent intent = new Intent(getApplicationContext() ,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case 3:
                patientList_layout.setVisibility(View.VISIBLE);
                fragment = new PatientDetailsFragment();
                break;
            case 4:
                patientList_layout.setVisibility(View.VISIBLE);
                fragment = new UnitSummery();
                break;
            case 5:
                patientList_layout.setVisibility(View.VISIBLE);
                fragment = new PhysicianOrderFragment();
                break;
            case 6:
                patientList_layout.setVisibility(View.VISIBLE);
                fragment = new ProgressNoteFragment();
                break;
            case 7:
                patientList_layout.setVisibility(View.VISIBLE);
                fragment = new MedicalHistory();
                break;

            default:
                patientList_layout.setVisibility(View.GONE);
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();


            // update selected item and title, then close the drawer
            if(position<3) {
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
            }else {
                PatientListView.setItemChecked(position-3 ,true);
                PatientListView.setSelection(position-3);
            }
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(Drawer_linLayout);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //======================== Async task
    private class DrDetailsAsyncTsk extends AsyncTask<Void, Void, Void> {

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

                    DR_name = jsonObj.getString("name");
                    DR_speciality =jsonObj.getString("speciality");
                    DR_pic =jsonObj.getString("photo");

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

            DR_NAME.setText(DR_name);
            DR_SPEC.setText(DR_speciality);

            String url = getResources().getString(R.string.IP) + DR_pic;
            new LoadImage(DR_PIC).execute(url);
        }

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

    @Override
    public void onBackPressed() {
        if(Position > 0){
            displayView(0);
        }
        else if(BackCount == 2)
        {
            Toast.makeText(getBaseContext(), "press again to exit", Toast.LENGTH_SHORT).show();
            BackCount = 0;
            new T().start();
        }
        else if(BackCount == 0)
        {
            super.onBackPressed();
        }
    }

    class T extends Thread
    {

        @Override
        public void run() {
            try {
                sleep(3000);// the flag will expire in three seconds
                BackCount = 2;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
