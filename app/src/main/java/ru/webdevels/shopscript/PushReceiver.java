package ru.webdevels.shopscript;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import ru.evotor.pushNotifications.PushNotificationReceiver;

public class PushReceiver extends PushNotificationReceiver {
    @Override
    public void onReceivePushNotification(@NotNull Context context, @NotNull Bundle data, long l) {

        if (data.containsKey("frontend_url")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("frontend_url", data.getString("frontend_url")).apply();
        }

        Intent intent = new Intent(context, ReceiptLauncher.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .putExtras(data);
        context.startActivity(intent);
    }
}
