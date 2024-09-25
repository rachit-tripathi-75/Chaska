package com.deificdigital.chaska;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.google.android.material.snackbar.Snackbar;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.deificdigital.chaska.app.App;
import com.deificdigital.chaska.constants.Constants;
import com.deificdigital.chaska.util.CountingRequestBody;
import com.deificdigital.chaska.util.CustomRequest;
import com.deificdigital.chaska.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import github.ankushsachdevaemojicon.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;

public class AddPhotoFragment extends Fragment implements Constants {

    public static final int RESULT_OK = -1;

    private ProgressDialog pDialog;

    private EmojiconEditText mTextEditor;
    private ImageView mThumbnail, mActionIcon;
    private ProgressBar mProgressView;
    private ImageButton mDeleteButton;
    private Button mPublishButton;

    private SwitchCompat mModeSwitch;

    String commentText = "", imgUrl = "", videoUrl = "", originImgUrl = "", previewImgUrl = "",  postArea = "", postCountry = "", postCity = "", postLat = "", postLng = "";
    private String selectedImagePath = "";

    private int postMode = 0;
    private int itemType = 0;
    private int itemShowInStream = 1;

    private Boolean loading = false;
    private Boolean uploading = false;
    private Boolean compressing = false;

    //

    private ActivityResultLauncher<String> recordAudioPermissionLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> videoPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> videoFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> videoFromCameraActivityResultLauncher;

    private ActivityResultLauncher<String[]> videoCapturePermissionLauncher;

    public AddPhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(false);

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_photo, container, false);

        //

        videoFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        selectedImagePath = Helper.getRealPath(result.getData().getData());

                        File videoFile = new File(selectedImagePath);

                        if (videoFile.length() > VIDEO_FILE_MAX_SIZE) {

                            selectedImagePath = "";

                            Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), Toast.LENGTH_SHORT).show();

                        } else {

                            Helper helper = new Helper(App.getInstance().getApplicationContext());
                            helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                            mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                            itemType = GALLERY_ITEM_TYPE_VIDEO;
                        }

                        updateView();
                    }
                }
            }
        });

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        Helper helper = new Helper(App.getInstance().getApplicationContext());
                        helper.createImage(result.getData().getData(), IMAGE_FILE);

                        selectedImagePath = new File(App.getInstance().getDirectory(), IMAGE_FILE).getPath();

                        mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                        itemType = GALLERY_ITEM_TYPE_IMAGE;

                        updateView();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    selectedImagePath = new File(App.getInstance().getDirectory(), IMAGE_FILE).getPath();

                    mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), IMAGE_FILE)));

                    itemType = GALLERY_ITEM_TYPE_IMAGE;

                    updateView();
                }
            }
        });

        videoFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == 100) {

                    File file =  new File(App.getInstance().getDirectory(), VIDEO_SRC_FILE);

                    if (file.exists()) {

                        Log.e("MyApp file size: ", Long.toString(file.length()));

                        selectedImagePath = file.getPath();

                        Helper helper = new Helper(getActivity().getApplicationContext());
                        helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                        mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                        itemType = GALLERY_ITEM_TYPE_VIDEO;
                    }

                    updateView();
                }
            }
        });

        videoCapturePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                captureVideo();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_or_video_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_and_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                selectMedia();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        videoPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                selectMedia();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_read_video_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_read_video_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                captureVideo();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_record_audio_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_record_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                selectMedia();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        if (loading) {

            showpDialog();
        }

        mTextEditor = rootView.findViewById(R.id.text_editor);
        mActionIcon = rootView.findViewById(R.id.action_add);

        //


        mThumbnail = rootView.findViewById(R.id.thumbnail);

        mThumbnail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (selectedImagePath.length() == 0) {

                    Helper helper = new Helper(getActivity());

                    if (!helper.checkStoragePermission()) {

                        requestStoragePermission();

                    } else {

                        selectMedia();
                    }

                } else {

                    if (itemType == GALLERY_ITEM_TYPE_VIDEO) {

                        Intent i = new Intent(getActivity(), CapturePreviewActivity.class);
                        i.putExtra("imagePath", selectedImagePath);
                        startActivity(i);
                    }
                }
            }
        });

        //

        mProgressView = rootView.findViewById(R.id.progress_view);
        mProgressView.setVisibility(View.GONE);

        //

        mDeleteButton = rootView.findViewById(R.id.delete_button);
        mDeleteButton.setVisibility(View.GONE);

        mDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(getText(R.string.action_remove));

                alertDialog.setMessage(getText(R.string.label_delete_item));
                alertDialog.setCancelable(true);

                alertDialog.setNeutralButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.setPositiveButton(getText(R.string.action_remove), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        mThumbnail.setImageURI(null);

                        selectedImagePath = "";

                        mDeleteButton.setVisibility(View.GONE);
                        mActionIcon.setVisibility(View.VISIBLE);
                        mActionIcon.setVisibility(View.VISIBLE);

                        updateView();

                        dialog.cancel();

                        itemType = GALLERY_ITEM_TYPE_IMAGE;
                    }
                });

                alertDialog.show();
            }
        });

        //

        mPublishButton = rootView.findViewById(R.id.publish_button);

        mPublishButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (App.getInstance().isConnected()) {

                    commentText = mTextEditor.getText().toString();
                    commentText = commentText.trim();

                    itemShowInStream = 1;

                    if (!mModeSwitch.isChecked()) {

                        itemShowInStream = 0;
                    }

                    if (selectedImagePath != null && selectedImagePath.length() > 0) {

                        if (itemType == GALLERY_ITEM_TYPE_IMAGE) {

                            File f = new File(selectedImagePath);

                            uploadFile(METHOD_GALLERY_UPLOAD_IMG, f);

                        } else {

                            File f = new File(selectedImagePath);
                            File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                            if (f.length() > VIDEO_FILE_MAX_SIZE) {

                                compressingVideoFile(f, new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE));

                            } else {

                                uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, f, f_thumb);
                            }
                        }

                    } else {

                        Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_photo), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                } else {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        //

        mModeSwitch = rootView.findViewById(R.id.mode_switch);

        mModeSwitch.setOnCheckedChangeListener(null);

        mModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    itemShowInStream = 1;

                } else {

                    itemShowInStream = 0;
                }
            }
        });

        //

        mTextEditor.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                if (cnt == 0) {

                    getActivity().setTitle(getText(R.string.title_activity_new_photo));

                } else {

                    getActivity().setTitle(Integer.toString(140 - cnt));
                }
            }

        });

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateView() {

        mDeleteButton.setVisibility(View.GONE);
        mActionIcon.setVisibility(View.VISIBLE);
        mActionIcon.setImageResource(R.drawable.ic_plus);

        if (itemShowInStream == 1) {

            mModeSwitch.setChecked(true);

        } else {

            mModeSwitch.setChecked(false);
        }

        if (selectedImagePath != null && selectedImagePath.length() > 0) {

            mActionIcon.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.VISIBLE);

            if (itemType == GALLERY_ITEM_TYPE_IMAGE) {

                mThumbnail.setImageURI(Uri.fromFile(new File(selectedImagePath)));

            } else {

                File f = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                if (f.exists()) {

                    mThumbnail.setImageURI(Uri.fromFile(new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE)));

                } else {

                    mThumbnail.setImageResource(R.drawable.ic_video_preview);
                }

                mActionIcon.setImageResource(R.drawable.ic_play);
                mActionIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (pDialog == null) {

            initpDialog();
        }

        if (uploading) {

            pDialog.setMessage(getString(R.string.msg_uploading));
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        } else {

            if (compressing) {

                pDialog.setMessage(getString(R.string.msg_compressing_video));
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            } else {

                pDialog.setMessage(getString(R.string.msg_loading));
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
        }

        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void selectMedia() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));
        arrayAdapter.add(getString(R.string.action_video_capture));
        arrayAdapter.add(getString(R.string.action_video_gallery));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");

                        imgFromGalleryActivityResultLauncher.launch(intent);

                        break;
                    }

                    case 1: {

                        Helper helper = new Helper(getActivity());

                        if (helper.checkPermission(Manifest.permission.CAMERA)) {

                            createPhoto();

                        } else {

                            requestCameraPermission();
                        }

                        break;
                    }

                    case 2: {

                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                            captureVideo();

                        } else {

                            requestVideoCapturePermission();
                        }

                        break;
                    }

                    default: {

                        Helper helper = new Helper(getActivity());

                        if (helper.checkVideoPermission()) {

                            selectVideo();

                        } else {

                            requestVideoPermission();
                        }

                        break;
                    }
                }

            }
        });

        /** Creating the alert dialog window using the builder class */
        AlertDialog d = builderSingle.create();

        /** Return the alert dialog window */
        d.show();
    }

    public void selectVideo() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/mp4");

        videoFromGalleryActivityResultLauncher.launch(intent);
    }

    public void createPhoto() {

        try {

            Uri selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(App.getInstance().getDirectory(), IMAGE_FILE));

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imgFromCameraActivityResultLauncher.launch(cameraIntent);

        } catch (Exception e) {

            Log.e("Dimon", e.getMessage());

            Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    public void captureVideo() {

        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        //intent.putExtra("profileId", obj.getId());
        videoFromCameraActivityResultLauncher.launch(intent);
    }

    public void sendPost() {

        loading = true;

        showpDialog();

        imgUrl = imgUrl.replace("../","");
        videoUrl = videoUrl.replace("../","");
        originImgUrl = originImgUrl.replace("../","");
        previewImgUrl = previewImgUrl.replace("../","");

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GALLERY_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;
                            uploading = false;
                            compressing = false;

                            hidepDialog();

                            sendSuccess();

                            Log.e("Result", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;
                uploading = false;
                compressing = false;

                hidepDialog();

                sendSuccess();

//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("accessMode", Integer.toString(postMode));
                params.put("itemType", Integer.toString(itemType));
                params.put("itemShowInStream", Integer.toString(itemShowInStream));
                params.put("comment", commentText);
                params.put("imgUrl", imgUrl);
                params.put("videoUrl", videoUrl);
                params.put("originImgUrl", originImgUrl);
                params.put("previewImgUrl", previewImgUrl);
                params.put("postArea", postArea);
                params.put("postCountry", postCountry);
                params.put("postCity", postCity);
                params.put("postLat", postLat);
                params.put("postLng", postLng);

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(15), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendSuccess() {

        loading = false;

        hidepDialog();

        Intent i = new Intent();
        getActivity().setResult(RESULT_OK, i);

        Toast.makeText(getActivity(), getText(R.string.msg_gallery_item_added), Toast.LENGTH_SHORT).show();

        getActivity().finish();

        // Interstitial ad

        if (App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewGalleryItem() != 0 && App.getInstance().getAdmob() == ADMOB_DISABLED) {

            App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewGalleryItem(App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewGalleryItem() + 1);

            if (App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewGalleryItem() >= App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewGalleryItem()) {

                App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewGalleryItem(0);

                App.getInstance().showInterstitialAd(null);
            }

            App.getInstance().saveData();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public Boolean uploadFile(String serverURL, File file) {

        loading = true;
        uploading = true;

        showpDialog();

        final CountingRequestBody.Listener progressListener = new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesRead, long contentLength) {

                if (bytesRead >= contentLength) {

                    if (isAdded() && getActivity() != null && pDialog != null) {

                        requireActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                //            progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                } else {

                    if (contentLength > 0) {

                        final int progress = (int) (((double) bytesRead / contentLength) * 100);

                        if (isAdded() && getActivity() != null && pDialog != null) {

                            requireActivity().runOnUiThread(new Runnable() {

                                public void run() {

                                    pDialog.setProgress(progress);
//                                    progressBar.setVisibility(View.VISIBLE);
//                                    progressBar.setProgress(progress);
                                }
                            });
                        }

                        if (progress >= 100) {

                            if (isAdded() && getActivity() != null && pDialog != null) {

                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            }
                        }

                        Log.e("uploadProgress called", progress+" ");
                    }
                }
            }
        };

        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().addNetworkInterceptor(new Interceptor() {

                    @NonNull
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {

                        okhttp3.Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {

                            return chain.proceed(originalRequest);
                        }

                        okhttp3.Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    }
                })
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), okhttp3.RequestBody.create(file, okhttp3.MediaType.parse(mime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            originImgUrl = result.getString("originPhotoUrl");
                            imgUrl = result.getString("normalPhotoUrl");
                            previewImgUrl = result.getString("previewPhotoUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        uploading = false;
                        loading = false;
                        compressing = false;

                        hidepDialog();

                        sendPost();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    private void compressingVideoFile(File src, File dest) {

        loading = true;
        compressing = true;

        showpDialog();

        Transcoder.into(dest.getPath())
                .addDataSource(src.getPath()) // or...
                .setListener(new TranscoderListener() {
                    public void onTranscodeProgress(double progress) {

                        double prgs = progress * 100;

                        pDialog.setProgress((int) prgs);

                        Log.e("Dimon", "onTranscodeProgress:" + Integer.valueOf((int) prgs).toString());
                    }
                    public void onTranscodeCompleted(int successCode) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();

                        Log.e("compressing", "onTranscodeCompleted");
                        Log.e("compressing src", Long.toString(src.length()));
                        Log.e("compressing dest", Long.toString(dest.length()));

                        File f = new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE);
                        File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                        uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, f, f_thumb);
                    }
                    public void onTranscodeCanceled() {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        Log.e("compressing", "onTranscodeCanceled");
                        pDialog.hide();
                    }
                    public void onTranscodeFailed(@NonNull Throwable exception) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();
                        Log.e("compressing", exception.toString());
                    }
                }).transcode();
    }

    public Boolean uploadVideoFile(String serverURL, File videoFile,  File videoThumb) {

        loading = true;
        uploading = true;

        showpDialog();

        final CountingRequestBody.Listener progressListener = new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesRead, long contentLength) {

                if (bytesRead >= contentLength) {

                    if (isAdded() && getActivity() != null && pDialog != null) {

                        requireActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                //            progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                } else {

                    if (contentLength > 0) {

                        final int progress = (int) (((double) bytesRead / contentLength) * 100);

                        if (isAdded() && getActivity() != null && pDialog != null) {

                            requireActivity().runOnUiThread(new Runnable() {

                                public void run() {

                                    pDialog.setProgress(progress);
//                                    progressBar.setVisibility(View.VISIBLE);
//                                    progressBar.setProgress(progress);
                                }
                            });
                        }

                        if (progress >= 100) {

                            if (isAdded() && getActivity() != null && pDialog != null) {

                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            }
                        }

                        Log.e("uploadProgress called", progress+" ");
                    }
                }
            }
        };

        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().addNetworkInterceptor(new Interceptor() {

                    @NonNull
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {

                        okhttp3.Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {

                            return chain.proceed(originalRequest);
                        }

                        okhttp3.Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    }
                })
                .connectTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .readTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String vidFileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(videoFile).toString());
        String thumbFileExtension = MimeTypeMap.getFileExtensionFromUrl( Uri.fromFile(videoThumb).toString());
        String vidMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(vidFileExtension.toLowerCase());
        String thumbMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(thumbFileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", videoThumb.getName(), okhttp3.RequestBody.create(videoThumb, okhttp3.MediaType.parse(thumbMime)))
                    .addFormDataPart("uploaded_video_file", videoFile.getName(), okhttp3.RequestBody.create(videoFile, okhttp3.MediaType.parse(vidMime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            videoUrl = result.getString("videoFileUrl");
                            previewImgUrl = result.getString("imgFileUrl");
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        uploading = false;
                        loading = false;
                        compressing = false;

                        hidepDialog();

                        sendPost();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});

        } else {

            storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void requestVideoPermission() {

        videoPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO);
    }

    private void requestRecordAudioPermission() {

        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
    }

    private void requestVideoCapturePermission() {

        videoCapturePermissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
    }
}