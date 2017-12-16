package com.gametech.ui.activities.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.gametech.logic.controllers.session.SessionController;
import com.gametech.logic.events.tracker.EventTracker;
import com.gametech.logic.utils.Utils;
import com.gametech.ui.R;
import com.gametech.ui.activities.core.LoggedInBaseActivity;
import com.gametech.ui.activities.users.SplashActivity;
import com.gametech.ui.fragments.account.AccountFragment;
import com.gametech.ui.fragments.play.OpenBetsFragment;
import com.gametech.ui.utils.DeepLinkUtils;


public class AccountActivity extends LoggedInBaseActivity implements DrawerLayout.DrawerListener {

    private final String TAG = "GT.AccountActivity";

    private ActionBar mSupportActionBar;

    private DrawerLayout mDrawerLayout;

    private OpenBetsFragment mOpenBetsFragment;

    private AccountFragment accountsFragment;


    @Override
    protected void onLoggedInCreate(Bundle savedInstanceState) {
        EventTracker.trackFlurryEvent(this, "AccountPage");
        setContentView(R.layout.activity_account);
        Crashlytics.setString("onCreate", "AccountActivity");

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        SessionController sessionController = SessionController.getInstance();
        boolean isLoggedIn = sessionController.isValidSession() && !sessionController.isAnonymousUser();
        if(!isLoggedIn) {
            finish();
            DeepLinkUtils.launchRedirectActivity(this, SplashActivity.class, uri);
            return;
        }

        mSupportActionBar = getSupportActionBar();
        mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        if(Utils.isProoptionBuild())
            mSupportActionBar.setTitle(null);
        else
            mSupportActionBar.setTitle(R.string.label_account);

        //set open bets fragment
        mOpenBetsFragment = (OpenBetsFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);
        accountsFragment = (AccountFragment) getSupportFragmentManager().findFragmentById(R.id.accountsfragment);

        // Drawer Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

    }

    @Override
    protected void onLoggedInStart() {
        //Do nothing.
    }

    @Override
    protected void onLoggedInResume() {
        //Do nothing.
    }

    @Override
    protected void onLoggedInPause() {
        //Do nothing.
    }

    @Override
    protected void onLoggedInStop() {
        //Do nothing.
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOpenBetMenuSelection(MenuItem item) {
        if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
            //if already open, close it
            mDrawerLayout.closeDrawers();
        } else {
            //open right drawer
            mDrawerLayout.openDrawer(mOpenBetsFragment.getView());
        }

    }

    @Override
    public Intent getSupportParentActivityIntent() {
        if(uri != null) // Deep link
            return DeepLinkUtils.getParentActivityIntentWithDeepLinks(this, isIndirectLaunch);
        else {
            Intent parentIntent = null;
            try {
                Class className = Class.forName(getIntent().getStringExtra(Utils.PARENT_ACTIVITY_NAME));
                parentIntent = new Intent(this, className);
                parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return parentIntent;
        }
    }

    @Override
    public void onDrawerSlide(View view, float v) {

    }

    @Override
    public void onDrawerOpened(View view) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        Log.d(TAG, "onDrawer opened !!!");
        if (mOpenBetsFragment != null && mOpenBetsFragment.isVisible()) {
            mOpenBetsFragment.onResume();
        }

    }

    @Override
    public void onDrawerClosed(View view) {

        if (mOpenBetsFragment != null) {
            mOpenBetsFragment.reset();
        }
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
            //close it
            mDrawerLayout.closeDrawers();
        } else {
            finish();
        }
    }


    @Override
    public void updateBalance() {
        accountsFragment.updateBalance();
    }



}
