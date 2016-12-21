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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
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
    private static long mStart;
    private static long mEnd;
    private static final String TAG = "SpotifyControl";
    private static final String mSong = "Can't stop the feeling";
    private static final String mAlbum = "Justin Timberlake";

    //False means the play button and true means pause button
    private static boolean mpause = true;

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
            initLayout();
        }
    }


    /**
     *  Update the display with the dynamic message text.
     */
    private void initLayout() {
        showLayout(R.layout.spotify_layout, null);
        sendText(R.id.title, mAlbum);
        sendText(R.id.text, mSong);
        sendImage(R.id.play_btn,R.drawable.pause);
    }


    public void updatePlay(Bitmap bmp, boolean state){
        Log.d(TAG, "update play");
        sendImage(R.id.play_btn, bmp);
        mEnd = SystemClock.elapsedRealtime();
        Log.d(TAG, "Time taken = " + (mEnd-mStart));
        mpause = state;
    }
    /**
     * Provides a public method for ExtensionService and Activity to call in
     * order to request start.
     */
    public void requestExtensionStart() {
        startRequest();
    }

    /**
     * Process Touch events.
     * This starts the Android Activity for the app, passing a startup message.
     */
    @Override
    public void onTouch(final ControlTouchEvent event) {
        super.onTouch(event);
        if (event.getAction() == Control.Intents.TOUCH_ACTION_PRESS) {
            mpause = !mpause;
            mStart = SystemClock.elapsedRealtime();
            HelloWorldExtensionService.Object
                    .sendStateToActivity(mpause);
        }
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

    @Override
    public void onResume() {
        Log.d(Constants.LOG_TAG, "onResume: InitLayout called");
        initLayout();
        super.onResume();

    }

    // Clean up data structures on termination.
    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, "onDestroy: HelloWorldControl");
        utils.deactivate();
    }
}
