package ru.webdevels.shopscript;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import ru.evotor.pushNotifications.PushNotificationReceiver;

public class PushReceiver extends PushNotificationReceiver {
    @Override
    public void onReceivePushNotification(@NotNull Context context, @NotNull Bundle data, long l) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : data.keySet()) {
            editor.putString(key, data.getString(key));
        }
        editor.apply();

        if (data.getString("order_id") != null) {
            Intent intent = new Intent(context, ReceiptLauncher.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .putExtras(data);
            context.startActivity(intent);
        }
    }
}
