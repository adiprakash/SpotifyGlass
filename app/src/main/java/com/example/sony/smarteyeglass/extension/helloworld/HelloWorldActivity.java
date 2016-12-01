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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ImageView;

import com.sonyericsson.extras.liveware.aef.registration.Registration;

/**
 * The Hello World activity provides a button on the phone that starts
 * the  SmartEyeglass app.
 *
 * For demonstration, this also displays messages sent in the intent.
 */
public final class HelloWorldActivity extends Activity implements OnClickListener{

    private static final String TAG = "HelloWorldActivity";

    private static  int mIdx = 0;
    private static boolean mState = false;

    private ImageView mPlay = null;
    private ImageView mNext = null;
    private TextView mTitle = null;
    private TextView mText = null;

    private IntentFilter filter = null;

    private String[] mSong = {" Don't let me down", " Can't stop the feeling",
            " Don't let it go", " Panda's in my home", " Counting Stars"};

    private String[] mAlbum = {" Chain Smokers", " Justin timberlake",
            " Chain Smoker", " Bryan Adam's", "Band of boys"};

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonelayout);

        mTitle = (TextView) findViewById(R.id.phn_title);
        mText = (TextView) findViewById(R.id.phn_text);
        mNext = (ImageView) findViewById(R.id.phn_next_btn);
        mPlay = (ImageView) findViewById(R.id.phn_play_btn);
        mPlay.setOnClickListener(this);
        mNext.setOnClickListener(this);


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


        //Start the extension service as soon the Activity starts
        startExtension();

        Log.d(TAG, " onCreate: Starting the Song first time");
        updateSong();
        mIdx++;
        mState = !mState;

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                Log.d(TAG,"BroadcastReciever: Action =" + intent.getAction());
                Bundle extras = intent.getExtras();
                if(extras != null){
                    mState = extras.getBoolean("SongState");
                    Log.d(TAG, " New State: " + mState);
                    toggleState();
                }
            }

        }
    };

    @Override
    public void onClick(View v) {
        Log.d(TAG, "startExtension ");
        switch (v.getId()){
            case R.id.phn_play_btn:
                toggleState();
                HelloWorldExtensionService.SmartEyeglassControl.updateState(mState);
                mState = !mState;
                //need to send the state to the glass too.


                break;
            case R.id.phn_next_btn:
                //mState = !mState;
                updateSong();
                HelloWorldExtensionService.SmartEyeglassControl.updateState(mState);
                HelloWorldExtensionService.SmartEyeglassControl.updateSong(mIdx);
                mIdx++;
                //mState = !mState;
                //need to send the data to glass
                break;
            default:
                Log.d(TAG, "Wrong Option");
                break;
        }
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

    //change the state of the song
    private void toggleState(){
        Log.d(TAG, "Toggle Song: " + mState);
        if(mState)
            mPlay.setImageResource(R.drawable.pause);
        else
            mPlay.setImageResource(R.drawable.play);
    }

    //change the song by clicking the next button
    private void updateSong(){
        Log.d(TAG, "Update Song: ");
        mTitle.setText(mAlbum[(mAlbum.length+mIdx)%(mAlbum.length)]);
        mText.setText(mSong[(mSong.length+mIdx)%(mSong.length)]);
        toggleState();
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
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause, unRegistering the filter and observer");
        unregisterReceiver(broadcastReceiver);
    }
}
