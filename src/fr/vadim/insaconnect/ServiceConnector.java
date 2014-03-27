package fr.vadim.insaconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
/*
 * Service that connect to the captive portal of Insa
 */
public class ServiceConnector extends Service {
	Context c;
	static final String protocol    = "http://";
	static final String expectedURL = "a6000.insa-lyon.fr";

	
	/*
	 * Async Task
	 */
	class Async extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			Log.d("SER", "Service started");
			
			if( isINSAConnection()) {
				
				//Notify the user about the statuts of the connection
				if(connectInsa() && checkConnection()) {
					notifyUser(getText(R.string.notif_ok).toString());
				} else {
					notifyUser(getText(R.string.notif_erreur).toString());
				}
			}
			SharedPreferences pref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
			
			//Retrieving login and password from shared preferences
			if (!pref.getBoolean("savePassword", false)) {
				pref.edit().remove("password");
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.d("POE", "End of App");
		}	
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		c = getApplicationContext();
		Log.d("INFO", "Entered OnBind " + this.getClass().getName());
		new Async().execute("");
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d("INFO", "Entered onCreate " + this.getClass().getName());
		c = getApplicationContext();
		new Async().execute("");
		
	}
	private void notifyUser(String notifInfo) {
		Notification.Builder builder = new Notification.Builder(c);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(getText(R.string.notif_title));
		builder.setContentText(notifInfo);
		builder.setOnlyAlertOnce(true);
		builder.setAutoCancel(true);
		
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent;
		if(notifInfo.equals(getText(R.string.notif_erreur))) {
			resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com") );
		} else  {
			resultIntent = new Intent(this, Settings.class);
		}
		
		resultIntent.putExtra("fromservice", true);
		
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Settings.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		
		builder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(12121993, builder.getNotification());	
	}
	
	//Connect to INSA
	private boolean connectInsa() {
		SharedPreferences pref = c.getSharedPreferences(c.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		
		//Retrieving login and password from shared preferences
		String login = pref.getString("login", "");
		String password = pref.getString("password", "");
		
		//if there is no id set, we ask the user to fill them in a notification
		if(login == "" || password == "") {
			notifyUser(getText(R.string.notif_noid).toString());
			return false;
		}
		else {	
			//Send login information
			sendId(login, password);	
		}
		return true;
	}
	
	// Send the login and password to the login page
	private boolean sendId(String login, String password) {
		Log.d("INFO", "Entered sendID " + login + " " + password);
		HttpURLConnection connection = null;
		try {
			String params =
					"user="+URLEncoder.encode(login, "UTF-8")+
					"&password="+URLEncoder.encode(password, "UTF-8")+
					"&fqdn="+URLEncoder.encode("insa-lyon.fr", "UTF-8");
			
		
			
			
			URL url = new URL(protocol + expectedURL+"/cgi-bin/login");
			//URL url = new URL("http://vadimcaen.free.fr/testPost.php");
			Log.d("sendid", "Open Connection");
			
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Content-Length",  String.valueOf(params.length()));
	
			
			// Write Data
	        Log.d("sendid", "Write data");
	        OutputStream os = connection.getOutputStream();
	        os.write(params.getBytes());
	        
	        
	        
	        // Read Response
	        Log.d("sendid", "Read Reponse : ");
	        StringBuilder responseDB = new StringBuilder();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        
	        String line;
	        while( (line = br.readLine()) != null) {
	        	responseDB.append(line);
	        	Log.d("sendid", "    " + line);
	        }
	        
	        Log.d("sendid", "------ FIN REPONSE -----");
	        
	        //Close streams
	        br.close();
	        os.close();
	        connection.disconnect();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(connection != null) connection.disconnect();
		}
		Log.d("NOTIFY", "Notified");
		return true;
		
	}
	
	
	// Check if the user is connected to the internet
	private Boolean checkConnection() {
		Log.d("CheckConnection", "Entered checkConnection " + this.getClass().getName());
		HttpURLConnection httpconnection = null;
		Boolean result = false;
		try {
			URL url = new URL("http://www.google.com");
			httpconnection = (HttpURLConnection) url.openConnection();
			httpconnection.setConnectTimeout(10000);
			httpconnection.setReadTimeout(10000);
			httpconnection.setUseCaches(false);
			httpconnection.getInputStream();
			
			Log.d("CheckConnection", "HOST : " + httpconnection.getURL().getHost());
			if(httpconnection.getURL().getHost().matches(".*\\.google\\..*")) {
				Log.d("CHECK", "Matching OK");
				result = true;
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(httpconnection != null) httpconnection.disconnect();
		}
		
		return result;
	}
	
	private boolean isINSAConnection() {
		Log.d("RED","EnteredisInsaConnection " + this.getClass().getSimpleName());
		boolean redirect = false;
		HttpURLConnection httpconnection = null;
		Boolean result = false;
		try {
			URL url = new URL("http://client3.google.com/genreate_204");
			HttpURLConnection.setFollowRedirects(true);
			httpconnection = (HttpURLConnection) url.openConnection();
			httpconnection.setConnectTimeout(10000);
			httpconnection.setReadTimeout(10000);
			httpconnection.setUseCaches(false);
			//httpconnection.getInputStream();
			
			int status = httpconnection.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
		 
			Log.d("RED","Response Code ... " + status);
			Log.d("RED","Host Name ... " + httpconnection.getURL().getHost());
			
			
			if(httpconnection.getURL().getHost().matches(".*"+ expectedURL +".*")) {
				result = true;
				Log.d("RED","connected to Aruba Captiv portal");
				
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(httpconnection != null) httpconnection.disconnect();
		}
		
		return result;
	}

}
