package com.gb.optimize_battery;

import static android.content.Context.POWER_SERVICE;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;


public class OptimizeBatteryPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {


    private static final int OPTIMIZATION_CODE = 9090;
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    @Nullable private ActivityPluginBinding pluginBinding;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "optimize_battery");
        channel.setMethodCallHandler(this);
        //Set flutter context
        context = flutterPluginBinding.getApplicationContext();

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "isIgnoringBatteryOptimizations":
                boolean isIgnoring = isIgnoringBatteryOptimizations();
                result.success(isIgnoring);
                break;
            case "stopOptimizingBatteryUsage":
                Boolean response = stopOptimizingBatteryUsage();
                result.success(response);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    Boolean stopOptimizingBatteryUsage() {
        try {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            activity.startActivityForResult(intent, OPTIMIZATION_CODE);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    boolean isIgnoringBatteryOptimizations() {
        String packageName = context.getPackageName();
        PowerManager mPowerManager = (PowerManager) (context.getSystemService(POWER_SERVICE));
        return mPowerManager.isIgnoringBatteryOptimizations(packageName);
    }

    //Activity aware methods
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        registerListeners();
    }


    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        if (pluginBinding != null) {
            pluginBinding = null;
        }
        activity = null;
    }

    private void registerListeners() {
        if (pluginBinding != null) {
            pluginBinding.addActivityResultListener((requestCode, resultCode, data) -> {
                Log.d("OptimizeBatteryPlugin", "onActivityResult: " + requestCode + " " + resultCode + " " + data);
                if (requestCode == OPTIMIZATION_CODE) {
                    if (isIgnoringBatteryOptimizations()) {
                        channel.invokeMethod("BatteryOptimizationDenied", true);
                    } else {
                        channel.invokeMethod("BatteryOptimizationDenied", false);
                    }
                    return true;
                }
                return false;
            });
        }
    }

}

enum Status {
    OK,
    NO_ACTIVITY,
    ACTIVITY_NOT_FOUND,
}
