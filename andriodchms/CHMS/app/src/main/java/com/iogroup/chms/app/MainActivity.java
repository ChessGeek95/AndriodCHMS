package com.iogroup.chms.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private String success;
    private String user_type;
    private String user_id;

    private ArrayList<NameValuePair> user = new ArrayList<NameValuePair>();
    private String url;
    private LoginAsyncTsk loginAsyncTsk;
    //------------------------

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText username_txt ,password_txt;
    private String username ,password;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = getResources().getString(R.string.IP)+"/app/login";
        //--------------------
        sharedPreferences = getSharedPreferences("MyPreference" ,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ImageView logo_img = (ImageView)findViewById(R.id.login_logo);
        username_txt = (EditText)findViewById(R.id.login_username);
        password_txt = (EditText)findViewById(R.id.login_password);
        Button login_btn = (Button)findViewById(R.id.login_btn);

        //---------------------
        getSupportActionBar().hide();

        if(isNetworkAvailable()) {
            if(!sharedPreferences.getString("username" ,"").isEmpty()){
                username = sharedPreferences.getString("username" ,"");
                password = sharedPreferences.getString("password" ,"");
                user.add(new BasicNameValuePair("username" ,username));
                user.add(new BasicNameValuePair("password" ,password));
                loginAsyncTsk = new LoginAsyncTsk();
                loginAsyncTsk.execute();
            }
        }else{
            Toast.makeText(getApplicationContext() ,"Network connection unavailable" ,Toast.LENGTH_LONG).show();
        }

        //---------------------
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!username_txt.getText().toString().isEmpty() &&
                        !password_txt.getText().toString().isEmpty()){
                    if(isNetworkAvailable()) {
                        user.add(new BasicNameValuePair("username", username_txt.getText().toString()));
                        user.add(new BasicNameValuePair("password", password_txt.getText().toString()));
                        loginAsyncTsk = new LoginAsyncTsk();
                        loginAsyncTsk.execute();
                    }else{
                        Toast.makeText(getApplicationContext() ,"Network connection unavailable" ,Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }


    @Override
    public void onPause(){
        super.onPause();
        if(loginAsyncTsk != null && loginAsyncTsk.getStatus() == AsyncTask.Status.RUNNING)
            loginAsyncTsk.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }


    //======================== login Async task
    private class LoginAsyncTsk extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            ServiceHandler sh = new ServiceHandler();
            String jsonStr =null;
            // Making a request to url and getting response
            try {
                jsonStr = sh.makeServiceCall(url, ServiceHandler.POST ,user );
                Log.e("jsonStr", jsonStr);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj= new JSONObject(jsonStr);

                    success = jsonObj.getString("success");
                    user_type = jsonObj.getString("type");
                    user_id = jsonObj.getString("user_id");

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
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (success.equals("1")){
                editor.putString("user_id" ,user_id);
                editor.putString("user_type" ,user_type);
                if(!username_txt.getText().toString().isEmpty()) {
                    editor.putString("username", username_txt.getText().toString());
                    editor.putString("password", password_txt.getText().toString());
                }
                editor.commit();

                Intent intent = new Intent(getApplicationContext() ,NavigationActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
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
