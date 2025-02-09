package com.deificdigital.chaska;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.deificdigital.chaska.common.ActivityBase;
import com.deificdigital.chaska.dialogs.GenderSelectDialog;
import com.deificdigital.chaska.dialogs.MsgImageChooseDialog;
import com.deificdigital.chaska.dialogs.SexOrientationSelectDialog;

public class SignupActivity extends ActivityBase implements GenderSelectDialog.AlertPositiveListener, MsgImageChooseDialog.AlertPositiveListener, SexOrientationSelectDialog.AlertPositiveListener {

    Toolbar mToolbar;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {

            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");

        } else {

            fragment = new SignupFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "currentFragment", fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onSexOrientationSelect(int position) {

        SignupFragment p = (SignupFragment) fragment;
        p.getSexOrientation(position);
    }

    @Override
    public void onGenderSelect(int position) {

        SignupFragment p = (SignupFragment) fragment;
        p.getGender(position);
    }

    @Override
    public void onImageFromGallery() {

        SignupFragment p = (SignupFragment) fragment;
        p.imageFromGallery();
    }

    @Override
    public void onImageFromCamera() {

        SignupFragment p = (SignupFragment) fragment;
        p.imageFromCamera();
    }
}
