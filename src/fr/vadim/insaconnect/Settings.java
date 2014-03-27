package fr.vadim.insaconnect;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        
        //Get Layout Elements
        final EditText et_login = (EditText) findViewById(R.id.et_login);
        final EditText et_password = (EditText) findViewById(R.id.et_password);
        final CheckBox cb_password = (CheckBox) findViewById(R.id.checkBox1);
        Button btn_submit = (Button) findViewById(R.id.save);
        Button btn_logout = (Button) findViewById(R.id.btn_logout);
        
        //Preparing to save Data
        Context context = this;
        final SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        
        et_login.setText(sharedPref.getString("login", ""));
        et_password.setText(sharedPref.getString("password", ""));
        cb_password.setChecked(sharedPref.getBoolean("savePassword", false));
        
        
        
        btn_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//Retrieve and Save the login and password
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("login", et_login.getText().toString());
				editor.putString("password", et_password.getText().toString());
				editor.putBoolean("savePassword", cb_password.isChecked());
				editor.commit();
				
				Toast.makeText(getApplicationContext(), getText(R.string.save_ok), Toast.LENGTH_LONG).show();
				Context context = getApplicationContext();
				
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = cm.getActiveNetworkInfo();
				
				if( info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.getState() == State.CONNECTED){
						Intent i = new Intent(context, fr.vadim.insaconnect.ServiceConnector.class);
						
						context.stopService(i);
						context.startService(i);
						Toast.makeText(context, getText(R.string.try_connect).toString(), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, getText(R.string.no_connect).toString(), Toast.LENGTH_LONG).show();
				}
				
				finish();
				
			}
		});
        
        btn_logout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://securelogin.arubanetworks.com/cgi-bin/login?cmd=logout")));
				//finish();
			}
		});
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    
    private void saveSettings(String login, String password) {
    	
    	
    	
    }
    
    
    
}
