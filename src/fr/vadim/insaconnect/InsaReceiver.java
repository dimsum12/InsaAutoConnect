package fr.vadim.insaconnect;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;


public class InsaReceiver extends BroadcastReceiver {
	Context c;

	@Override
	public void onReceive(Context context, Intent intent) {
		c = context;
		Log.d("INFO", "Entered OnReceive " + this.getClass().getName());
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		
		if( info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.getState() == State.CONNECTED){
			Intent i = new Intent(context, fr.vadim.insaconnect.ServiceConnector.class);
			
			context.stopService(i);
			context.startService(i);		
		}
	}
}
