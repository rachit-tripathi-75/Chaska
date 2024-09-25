package com.deificdigital.chaska;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.deificdigital.chaska.app.App;
import com.deificdigital.chaska.common.ActivityBase;

import java.io.File;

public class CapturePreviewActivity extends ActivityBase {

    private Toolbar mToolbar;
    private StyledPlayerView mVideoView;
    private ProgressBar mProgressBar;
    private ExoPlayer mPlayer;
    private MediaController mController;
    private MaterialRippleLayout mNextButton;

    private File videoFile;
    private String imagePath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture_preview);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Video File

        videoFile = new File(App.getInstance().getDirectory(), VIDEO_SRC_FILE);

        // Intent

        Intent i = getIntent();

        imagePath = i.getStringExtra("imagePath");

        if (imagePath != null && imagePath.length() != 0) {

            videoFile = new File(imagePath);
        }

        if (!videoFile.exists()) {

            Toast.makeText(getApplicationContext(), getString(R.string.msg_play_video_error), Toast.LENGTH_SHORT).show();

            finish();
        }

        //

        mNextButton = findViewById(R.id.next_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent();
                setResult(100, i);

                finish();
            }
        });

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mVideoView = findViewById(R.id.video_view);

        mPlayer = new ExoPlayer.Builder(this).build();

        mVideoView.setUseController(true);
        mVideoView.requestFocus();
        mVideoView.setPlayer(mPlayer);
        mVideoView.setControllerAutoShow(false);
        mVideoView.setShowNextButton(false);
        mVideoView.setShowPreviousButton(false);
        mVideoView.setShowFastForwardButton(false);
        mVideoView.setShowRewindButton(false);
        mVideoView.setShowMultiWindowTimeBar(false);
        mVideoView.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_NEVER);
        mVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        MediaItem mediaItem = MediaItem.fromUri(videoFile.getPath());

        if (mPlayer != null) {

            mPlayer.addMediaItem(mediaItem);
            mPlayer.prepare();
            mPlayer.setPlayWhenReady(true);

            mPlayer.addListener(new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(@Player.State int state) {

                    if (state == Player.STATE_BUFFERING) {

                        mProgressBar.setVisibility(View.VISIBLE);

                    } else {

                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {

                    Toast.makeText(getApplicationContext(), getString(R.string.msg_play_video_error), Toast.LENGTH_SHORT).show();
                }
            });
        }

        updateView();
    }

    private void updateView() {

        mNextButton.setVisibility(View.GONE);

        if (imagePath == null || imagePath.length() == 0) {

            mNextButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        if (imagePath == null || imagePath.length() == 0) {

            getMenuInflater().inflate(R.menu.menu_share, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case android.R.id.home: {

                if (mPlayer != null) {

                    mPlayer.setPlayWhenReady(false);
                    mPlayer.stop();
                }

                finish();

                return true;
            }

            case R.id.action_share: {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("video/*");
                Uri uri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", videoFile);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);

                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        if (mPlayer != null) {

            mPlayer.setPlayWhenReady(false);
            mPlayer.stop();
        }

        finish();
    }
}
