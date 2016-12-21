/*
Copyright (c) 2011, Sony Mobile Communications Inc.
Copyright (c) 2014, Sony Corporation

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Mobile Communications Inc.
 nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.sony.smarteyeglass.extension.helloworld;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Hello World activity provides a button on the phone that starts
 * the  SmartEyeglass app.
 *
 * For demonstration, this also displays messages sent in the intent.
 */
public final class HelloWorldActivity extends Activity implements OnClickListener{

    private static final String TAG = "HelloWorldActivity";

    private static boolean mPause = true;

    private ImageView mPlay = null;
    private TextView mTitle = null;
    private TextView mText = null;
    private RelativeLayout mLayout = null;
    private IntentFilter filter = null;
    private static final String mSong = "Can't stop the feeling";
    private static final String mAlbum = "Justin Timberlake";
    private static String playPath = null;
    private static String pausePath = null;

    //path to dynamically get the images from the sdacard
    private File path = null;
    private String image = null;
    private List<String> buttons = new ArrayList<String>();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonelayout);
        mLayout = (RelativeLayout) findViewById(R.id.spotify_phone);
        mTitle = (TextView) findViewById(R.id.phn_title);
        mText = (TextView) findViewById(R.id.phn_text);
        mPlay = (ImageView) findViewById(R.id.phn_play_btn);
        mPlay.setOnClickListener(this);

        // Now get the images from the sdcard

        path = new File(Environment.getExternalStorageDirectory(), "a.png");
        if(path.exists()){
            image = path.getPath();
        }else{
            Log.d(TAG, "there is no file in the sdcard");
            finish();
        }

        path = new File(Environment.getExternalStorageDirectory(),"play.png");
        if(path.exists()){
            playPath = path.getPath();
        }else{
            Log.d(TAG, "there is no file in the sdcard");
            finish();
        }

        path = new File(Environment.getExternalStorageDirectory(),"pause.png");
        if(path.exists()){
            pausePath = path.getPath();
        }else{
            Log.d(TAG, "there is no file in the sdcard");
            finish();
        }


        /*
         * Make sure ExtensionService of your SmartEyeglass app has already
         * started.
         * This is normally started automatically when user enters your app
         * on SmartEyeglass, although you can initialize it early using
         * request intent.
         */
        if (HelloWorldExtensionService.Object == null) {
            Intent intent = new Intent(Registration.Intents
                    .EXTENSION_REGISTER_REQUEST_INTENT);
            Context context = getApplicationContext();
            intent.setClass(context, HelloWorldExtensionService.class);
            context.startService(intent);
        }
        //Start the extension as soon as teh service starts
        startExtension();


        Log.d(TAG, " onCreate: Starting the APP");
        //Now build the layout based on the images in the sdcard
        Bitmap bmpImage = BitmapFactory.decodeFile(image);
        Drawable bckgrnd = new BitmapDrawable(getResources(),bmpImage);
        mLayout.setBackground(bckgrnd);

        //Song name and title
        mTitle.setText(null);
        mText.setText(null);

        //add the image view for the play button
        Bitmap bmpButton = BitmapFactory.decodeFile(pausePath);
        Drawable  play = new BitmapDrawable(getResources(),bmpButton);
        mPlay.setImageDrawable(play);
    }

    /**
     *  Start the app with the message "Hello SmartEyeglass"
     */
    public void startExtension() {
        Log.d(TAG, "startExtension ");
        // Check ExtensionService is ready and referenced
        if (HelloWorldExtensionService.Object != null) {
            Log.d(TAG, "startExtension: ExtensionService is ready and referenced ");
            HelloWorldExtensionService.Object
                    .sendMessageToExtension("Hello SmartEyeglass Song App");
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                Bundle extras = intent.getExtras();
                if(extras != null){
                    //add the image views
                    mPause = intent.getBooleanExtra("state", true);
                    changeState();
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        Log.d(TAG, " onClick ");
        switch (v.getId()){
            case R.id.phn_play_btn:
                toggleState();
                break;
            default:
                Log.d(TAG, "Wrong Option");
                break;
        }
    }

    private void changeState(){
        Bitmap bmp = null;

        if(mPause){
            Log.d(TAG, "Path = " + playPath);
            bmp = BitmapFactory.decodeFile(playPath);
        }else{
            Log.d(TAG, "Path = " + pausePath);
            bmp = BitmapFactory.decodeFile(pausePath);
        }

        if(bmp == null){
            Log.d(TAG, "bmp  = null");
            finish();
        }

        Drawable  play = new BitmapDrawable(getResources(),bmp);
        mPlay.setImageDrawable(play);
        Log.d(TAG, "Sending to Glass Pause = " + mPause);
        HelloWorldExtensionService.Object.sendStateToExtension(bmp,mPause);
    }

    //change the state of the song
    private void toggleState(){
        Bitmap bmp = null;

        if(mPause){
            Log.d(TAG, "Path = " + playPath);
            bmp = BitmapFactory.decodeFile(playPath);
        }else{
            Log.d(TAG, "Path = " + pausePath);
            bmp = BitmapFactory.decodeFile(pausePath);
        }

        if(bmp == null){
            Log.d(TAG, "bmp  = null");
            finish();
        }

        Drawable  play = new BitmapDrawable(getResources(),bmp);
        mPlay.setImageDrawable(play);
        mPause = !mPause;
        HelloWorldExtensionService.Object.sendStateToExtension(bmp,mPause);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(filter == null){
            Log.d(TAG, "onResume, Rgistering the filter and observer");
            filter = new IntentFilter();
            filter.addAction("com.example.sony.smarteyeglass.extension.helloworld.CHANGE_STATE");
            registerReceiver(broadcastReceiver,filter);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, unRegistering the filter and observer");
        unregisterReceiver(broadcastReceiver);
    }
}
