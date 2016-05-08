package com.adr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class runs the service when an intent with the action
 * 'com.adr.intent.action.SERVICE' is broadcast.
 */
public class AdrBroadcastReceiver extends BroadcastReceiver
{
    public void onReceive(Context context,
			  Intent intent)
    {
	// NOTE: If the Adr Service has already been started,
	// this will not do anything. The Android takes care
	// of handling this case.
	context.startService(new Intent(context, Adr.class));
    }
}
