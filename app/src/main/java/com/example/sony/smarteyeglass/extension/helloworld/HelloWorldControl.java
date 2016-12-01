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

import android.content.Context;
import android.util.Log;
import com.sony.smarteyeglass.SmartEyeglassControl;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sony.smarteyeglass.extension.util.SmartEyeglassControlUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

/**
 * Demonstrates how to communicate between an Android activity and its
 * corresponding SmartEyeglass app.
 *
 */
public final class HelloWorldControl extends ControlExtension {

    /** Instance of the SmartEyeglass Control Utility class. */
    private final SmartEyeglassControlUtils utils;

    public HelloWorldControl controlObject;

    /** The SmartEyeglass API version that this app uses */
    private static final int SMARTEYEGLASS_API_VERSION = 1;

    /* The image icon to be displayed */
    private static boolean iconImage;

    private static final String TAG = "HelloWorldControl";

    // keeps the index of the song being played
    private static  int mIdx = 0;

    //False means the play button and true means pause button
    private static boolean mToggle = false;

    private String[] mSong = {" Don't let me down", " Can't stop the feeling",
            " Don't let it go", " Panda's in my home", " Counting Stars"};

    private String[] mAlbum = {" Chain Smokers", " Justin timberlake",
            " Chain Smoker", " Bryan Adam's", "Band of boys"};

    /**
     * Shows a simple layout on the SmartEyeglass display and sets
     * the text content dynamically at startup.
     * Tap on the device controller touch pad to start the Android activity
     * for this app on the phone.
     * Tap the Android activity button to run the SmartEyeglass app.
     *
     * @param context            The context.
     * @param hostAppPackageName Package name of SmartEyeglass host application.
     */
    public HelloWorldControl(final Context context,
            final String hostAppPackageName, final String message) {
        super(context, hostAppPackageName);
        utils = new SmartEyeglassControlUtils(hostAppPackageName, null);
        utils.setRequiredApiVersion(SMARTEYEGLASS_API_VERSION);
        utils.activate(context);
        controlObject = this;

        /*
         * Set reference back to this Control object
         * in ExtensionService class to allow access to SmartEyeglass Control
         */
        HelloWorldExtensionService.SmartEyeglassControl = this;

        /*
         * Show the message that was set Iif any) when this Control started
         */
        if (message != null) {
            showToast(message);
        } else {
            Log.d(TAG, "Starting the spotify");
            updateLayout();
            //updateSong();
        }

    }

    /**
     * Provides a public method for ExtensionService and Activity to call in
     * order to request start.
     */
    public void requestExtensionStart() {
        startRequest();
    }

    //The public method to toggle the state
    public void updateState(boolean state){
        Log.d(TAG, "Update the state");
        mToggle = state;
        toggleSong();
    }

    //The public method to change the song
    public void updateSong(int index){
        Log.d(TAG, "Updating the song");
        mIdx = index;
        updateSong();
    }

    // Update the SmartEyeglass display when app becomes visible
    @Override
    public void onResume() {
        resumeSong();
        super.onResume();
    }

    // Clean up data structures on termination.
    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, "onDestroy: HelloWorldControl");
        utils.deactivate();
    };

    /**
     * Process Touch events.
     * This starts the Android Activity for the app, passing a startup message.
     */
    @Override
    public void onTouch(final ControlTouchEvent event) {
        super.onTouch(event);
        Log.d(TAG, "Touch Action: " + event.getAction());
        if (event.getAction() == Control.Intents.TOUCH_ACTION_PRESS) {
            toggleSong();
            HelloWorldExtensionService.Object
                    .sendMessageToActivity(mToggle);
            mToggle = !mToggle;
            Log.d(TAG, "Sending State: " + mToggle);

        }
    }

    /**
     *  Update the display with the dynamic message text.
     */
    private void updateLayout() {
        Log.d(TAG, "Update Layout ");
        showLayout(R.layout.spotify_layout, null);
        sendText(R.id.title, mAlbum[(mAlbum.length+mIdx)%(mAlbum.length)]);
        sendText(R.id.text, mSong[(mSong.length+mIdx)%(mSong.length)]);
        if(mToggle)
            sendImage(R.id.play_btn,R.drawable.pause);
        else
            sendImage(R.id.play_btn,R.drawable.play);
    }

    //Toggle the song state upon touch
    private void toggleSong(){
        Log.d(TAG, "Toggle Song: " + mToggle);
        if(mToggle)
            sendImage(R.id.play_btn,R.drawable.pause);
        else
            sendImage(R.id.play_btn,R.drawable.play);
    }

    // update the song upon Tap
    private void updateSong(){
        Log.d(TAG, "update Song: " + mIdx);
        updateLayout();
        mIdx++;
        mToggle = !mToggle;
    }

    private void resumeSong(){
        Log.d("HelloWorldControl", "resume Song: ");
        updateSong();
    }

    /**
     * Timeout dialog messages are similar to Toast messages on
     * Android Activities
     * This shows a timeout dialog with the specified message.
     */
    public void showToast(final String message) {
        Log.d(Constants.LOG_TAG, "Timeout Dialog : HelloWorldControl");
        utils.showDialogMessage(message,
                SmartEyeglassControl.Intents.DIALOG_MODE_TIMEOUT);
    }
}
