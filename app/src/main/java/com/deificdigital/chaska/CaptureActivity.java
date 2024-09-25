package com.deificdigital.chaska;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.filter.Filters;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;
import com.deificdigital.chaska.app.App;
import com.deificdigital.chaska.common.ActivityBase;

import java.io.File;

public class CaptureActivity extends ActivityBase {

    private final CameraLogger LOG = CameraLogger.create("CaptureApp");
    private final Boolean USE_FRAME_PROCESSOR = false;
    private final Boolean DECODE_BITMAP = false;

    private int currentFilter = 0;
    private final Filters[] allFilters = Filters.values();

    private CameraView mCameraView;
    private ProgressBar mProgressBar;
    private ImageButton mCloseButton;
    private FloatingActionButton mFabCapture, mFabToggle, mFabAudio, mFabTimeMenu, mFabTimeShortVideo, mFabTimeMediumVideo, mFabTimeNormalVideo;

    private int currentProgress = 0;
    private int duration = VIDEO_SHORT_CAPTURE_MILLISECONDS;

    private CountDownTimer countdownTimer;
    public Boolean isTimerRunning = false;

    private Animation fabOpenAnimation;
    private Animation fabCloseAnimation;
    private boolean isFabMenuOpen = false;
    private ConstraintLayout mConstraintLayout;
    private LinearLayout mNormalVideoLayout, mMediumVideoLayout, mShortVideoLayout;
    private RelativeLayout mActionsMenuLayout;

    private ActivityResultLauncher<Intent> previewCaptureActivityResultLauncher;


    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture);

        //

        mActionsMenuLayout = findViewById(R.id.actions_menu_layout);

        //

        mConstraintLayout = findViewById(R.id.time_menu_layout);

        mConstraintLayout.setOnTouchListener((view, motionEvent) -> {

            if (isFabMenuOpen) {

                collapseFabMenu();
            }

            return false;
        });


        mNormalVideoLayout = findViewById(R.id.normal_video_layout);
        mMediumVideoLayout = findViewById(R.id.medium_video_layout);
        mShortVideoLayout = findViewById(R.id.short_video_layout);

        mFabTimeShortVideo = findViewById(R.id.fab_time_short_video);

        mFabTimeShortVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                duration = VIDEO_SHORT_CAPTURE_MILLISECONDS;

                if (isFabMenuOpen) {

                    collapseFabMenu();

                } else {

                    expandFabMenu();
                }
            }
        });

        mFabTimeMediumVideo = findViewById(R.id.fab_time_medium_video);

        mFabTimeMediumVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                duration = VIDEO_MEDIUM_CAPTURE_MILLISECONDS;

                if (isFabMenuOpen) {

                    collapseFabMenu();

                } else {

                    expandFabMenu();
                }
            }
        });

        mFabTimeNormalVideo = findViewById(R.id.fab_time_normal_video);

        mFabTimeNormalVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                duration = VIDEO_NORMAL_CAPTURE_MILLISECONDS;

                if (isFabMenuOpen) {

                    collapseFabMenu();

                } else {

                    expandFabMenu();
                }
            }
        });

        mFabTimeMenu = findViewById(R.id.fab_time_menu);

        mFabTimeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isFabMenuOpen) {

                    collapseFabMenu();

                } else {

                    expandFabMenu();
                }
            }
        });

        fabOpenAnimation = AnimationUtils.loadAnimation(this, R.animator.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(this, R.animator.fab_close);

        //

        previewCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == 100) {

                    Intent i = new Intent();
                    setResult(100, i);

                    finish();
                }
            }
        });

        mCameraView = findViewById(R.id.camera);
        mProgressBar = findViewById(R.id.progressBar);

        //

        mCloseButton = findViewById(R.id.closeButton);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCameraView.stopVideo();

                finish();
            }
        });

        //

        mFabCapture = findViewById(R.id.fab_capture);

        mFabCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                captureVideo();
            }
        });

        mFabToggle = findViewById(R.id.fab_toggle);

        mFabToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                toggleCamera();
            }
        });

        mFabAudio = findViewById(R.id.fab_audio);

        mFabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleAudio();
            }
        });

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);

        // StatusBar

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(Color.TRANSPARENT);

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {

            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mProgressBar.getLayoutParams();
        layoutParams.setMargins(16, statusBarHeight + 16, 16, 0);
        mProgressBar.setLayoutParams(layoutParams);

        CoordinatorLayout.LayoutParams layoutParams2 = (CoordinatorLayout.LayoutParams) mCloseButton.getLayoutParams();
        layoutParams2.setMargins(0, statusBarHeight - 16, 0, 0);
        mCloseButton.setLayoutParams(layoutParams2);

        //

        mCameraView.setLifecycleOwner(this);

        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {

                super.onCameraOpened(options);
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {

                super.onVideoTaken(result);

                if (currentProgress + 1000 >= duration) {

                    if (result.getFile().exists()) {

                        Intent i = new Intent(CaptureActivity.this, CapturePreviewActivity.class);
                        previewCaptureActivityResultLauncher.launch(i);
                    }
                }

                stopTimer();
            }

            @Override
            public void onVideoRecordingStart() {

                super.onVideoRecordingStart();

                startTimer();
            }



            @Override
            public void onVideoRecordingEnd() {

                super.onVideoRecordingEnd();

                stopTimer();
            }
        });

        mCameraView.addFrameProcessor(new FrameProcessor() {

            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {

                long time = frame.getTime();
                Size size = frame.getSize();
                int format = frame.getFormat();
                int userRotation = frame.getRotationToUser();
                int viewRotation = frame.getRotationToView();

                if (frame.getDataClass() == byte[].class) {

                    byte[] data = frame.getData();
                    // Process byte array...
                } else if (frame.getDataClass() == Image.class) {

                    Image data = frame.getData();
                    // Process android.media.Image...
                }
            }
        });

        if (App.getInstance().getTooltipsSettings().isAllowShowCaptureVideoTimeTooltip()) {

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setIcon(R.drawable.ic_time);
            alertDialog.setTitle(getString(R.string.label_tooltip));
            alertDialog.setMessage(getString(R.string.label_tooltip_capture_video_time));

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_understand), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    App.getInstance().getTooltipsSettings().setShowCaptureVideoTimeTooltip(false);
                    App.getInstance().getTooltipsSettings().saveTooltipsSettings();

                    dialog.dismiss();
                }
            });

            alertDialog.show();
        }

        updateView();
    }

    private void updateView() {

        if (mCameraView.isTakingPicture() || mCameraView.isTakingVideo()) {

            mFabCapture.setImageResource(R.drawable.ic_capture_stop);
            mProgressBar.setVisibility(View.VISIBLE);
            mFabAudio.setVisibility(View.GONE);
            mFabToggle.setVisibility(View.GONE);
            mCloseButton.setVisibility(View.GONE);
            mFabTimeMenu.setVisibility(View.GONE);

        } else {

            mFabCapture.setImageResource(R.drawable.ic_capture_start);
            mProgressBar.setVisibility(View.GONE);
            mFabAudio.setVisibility(View.VISIBLE);
            mFabToggle.setVisibility(View.VISIBLE);
            mCloseButton.setVisibility(View.VISIBLE);
            mFabTimeMenu.setVisibility(View.VISIBLE);
        }

        if (mCameraView.getAudio() == Audio.ON) {

            mFabAudio.setImageResource(R.drawable.ic_capture_micro_on);

        } else {

            mFabAudio.setImageResource(R.drawable.ic_capture_micro_off);
        }
    }

    private void toggleAudio() {

        if (mCameraView.getAudio() == Audio.ON) {

            mCameraView.setAudio(Audio.OFF);

        } else {

            mCameraView.setAudio(Audio.ON);
        }

        updateView();
    }

    private void toggleCamera() {

        if (mCameraView.isTakingPicture() || mCameraView.isTakingVideo()) return;

        if (mCameraView.getFacing() == Facing.BACK) {

            //mCameraView.setFacing(Facing.FRONT);

        } else {

            //mCameraView.setFacing(Facing.BACK);
        }

        mCameraView.toggleFacing();
    }

    private void captureVideo() {

        if (mCameraView.isTakingVideo()) {

            mCameraView.stopVideo();

            stopTimer();

        } else {

            File file =  new File(App.getInstance().getDirectory(), VIDEO_SRC_FILE);

            mCameraView.takeVideo(file, duration);
        }
    }

    private void startTimer() {

        mCloseButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        currentProgress = 0;

        mProgressBar.setProgress(currentProgress);
        mProgressBar.setMax(duration);

        countdownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {

                mProgressBar.setProgress(currentProgress + 1000);
                currentProgress = mProgressBar.getProgress();
            }

            @Override
            public void onFinish() {

                mCloseButton.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                countdownTimer.cancel();
                isTimerRunning = false;
                mCameraView.stopVideo();

                updateView();
            }
        };

        countdownTimer.start();
        isTimerRunning = true;

        updateView();
    }

    private void stopTimer() {

        mCloseButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        if (countdownTimer != null) {

            countdownTimer.cancel();
        }

        isTimerRunning = false;

        updateView();
    }

    @Override
    public void onBackPressed() {

        if (isFabMenuOpen) {

            collapseFabMenu();

        } else {

            super.onBackPressed();
        }
    }

    private void expandFabMenu() {

        mActionsMenuLayout.startAnimation(fabCloseAnimation);

        mFabTimeMenu.setImageResource(R.drawable.ic_plus);

        ViewCompat.animate(mFabTimeMenu).rotation(45.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        mShortVideoLayout.startAnimation(fabOpenAnimation);
        mMediumVideoLayout.startAnimation(fabOpenAnimation);
        mNormalVideoLayout.startAnimation(fabOpenAnimation);
        isFabMenuOpen = true;
    }

    private void collapseFabMenu() {

        mActionsMenuLayout.startAnimation(fabOpenAnimation);

        ViewCompat.animate(mFabTimeMenu).rotation(0.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).withEndAction(new Runnable() {
            @Override
            public void run() {

                mFabTimeMenu.setImageResource(R.drawable.ic_time);
            }
        }).start();

        mShortVideoLayout.startAnimation(fabCloseAnimation);
        mMediumVideoLayout.startAnimation(fabCloseAnimation);
        mNormalVideoLayout.startAnimation(fabCloseAnimation);

        isFabMenuOpen = false;
    }
}

