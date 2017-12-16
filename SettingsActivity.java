import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends LoggedInBaseActivity implements DrawerLayout.DrawerListener{

    private final String TAG = "GT.SettingsActivity";

    @SuppressWarnings("FieldCanBeLocal")
    private ActionBar mSupportActionBar;

    private DrawerLayout mDrawerLayout;

    private OpenBetsFragment mOpenBetsFragment;

    private ProgressDialog progressDialog;

    private boolean isPaused;

    private final String TAG_MODIFY_REQUEST = "TAG_MODIFY_REQUEST";
    private final String TAG_GETCHANGELIMIT_REQUEST = "TAG_GETCHANGELIMIT_REQUEST";

    private Integer mDepositLimit;
    private Integer mNewDepositLimit;
    private Integer mTimeLimit;
    private Integer mDepositLimitPeriod; // variable to store integer constant value for navigateToDeposit limit period (String)
    private Integer mNewDepositLimitPeriod;
    private View rootView;
    private boolean timeLimitAccepted = true;
    private boolean activateDepositLimitField = false;


    @Override
    protected void onLoggedInCreate(Bundle savedInstanceState) {
        Log.d("Intent data", "You shouldn't see this when logged out.");
        EventTracker.trackFlurryEvent(this, "SettingsPage");
        Crashlytics.setString("onCreate", "SettingsActivity");
        setContentView(R.layout.activity_settings);

        SessionController sessionController = SessionController.getInstance();
        boolean isLoggedIn = sessionController.isValidSession() && !sessionController.isAnonymousUser();
        if(!isLoggedIn) {
            finish();
            DeepLinkUtils.launchRedirectActivity(this, SplashActivity.class, uri);
            return;
        }

        rootView = findViewById(R.id.root_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            mSupportActionBar.setTitle(R.string.drawer_preferences);
        }

        // Deep Link implementation
        if (uri != null && uri.getHost().equals(DeepLinkUtils.HOST_SETTINGS)) {
            String pathString = uri.getPath();
            if(pathString != null  && pathString.length() > 0) {
                String depositLimitString = pathString.substring(1);
                if (depositLimitString != null && depositLimitString.equals("deposit_limit"))
                    activateDepositLimitField = true;
            }
        }

        //set open bets fragment
        mOpenBetsFragment = (OpenBetsFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);

        // Drawer Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);

        DepositLimitPreferences depositLimitPreferences = SessionController.getInstance().getDepositLimitPreferences();
        mDepositLimit = depositLimitPreferences.getAmount();
        switch (depositLimitPreferences.getTimePeriod())
        {
            case DAILY:
                mDepositLimitPeriod = 0;
                break;
            case WEEKLY:
                mDepositLimitPeriod = 1;
                break;
            case MONTHLY:
                mDepositLimitPeriod = 2;
                break;
            default: //Default is daily.
                mDepositLimitPeriod = 0;
                break;
        }
        mTimeLimit = SessionController.getInstance().getTimeLimitInMinutes();
    }

    @Override
    protected void onLoggedInStart() {
        if(activateDepositLimitField) {
            SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.accountsfragment);
            settingsFragment.activateDepositLimit();
        }
    }

    /**
     * <p>Hides the keyboard.</p>
     */
    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    }

    /**
     * <p>Shows the time limit in minutes dialog when needed.</p>
     */
    private void setTimeLimitInMinutesDialog()
    {
        final int value = SessionController.getInstance().getTimeLimitInMinutes();
        final SettingsFragment settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.accountsfragment);
        mTimeLimit = settingsFragment.getTimeLimitInMins();
        if (mTimeLimit < 20 && mTimeLimit != 0 && mTimeLimit != SessionController.getInstance().getTimeLimitInMinutes())
        {
            //Show dialog
            CustomDialogBuilder builder = DialogUtils.getAlertDialogBuilder(this);
            builder.setTitle(R.string.daily_time_limit_title);
            builder.setMessage(getString(R.string.daily_time_limit_message, mTimeLimit));
            builder.setPositiveButton(R.string.label_dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SessionController.getInstance().setTimeLimitInMinutes(mTimeLimit); //If the user confirms, we set the reset variable to the new value.
                }
            });
            builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    settingsFragment.setTimeLimitInMins(value);
                    mTimeLimit = value;
                    timeLimitAccepted = false;
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
        else {
            SessionController.getInstance().setTimeLimitInMinutes(mTimeLimit);
        }
    }

    @Override
    protected void onLoggedInStop() {
        NetworkManager networkManager = NetworkManager.getInstance(this);
        if (networkManager != null) {
            networkManager.cancelAll(TAG_MODIFY_REQUEST);
            networkManager.cancelAll(TAG_GETCHANGELIMIT_REQUEST);
        }
    }

    @Override
    protected void onLoggedInResume() {
        isPaused = false;

    }

	@Override
    protected void onLoggedInPause() {
        isPaused = true;
        updateSettingsToServer();
    }

    @SuppressWarnings("Convert2Diamond")
    private void postSetTimeLimit(int mins) {

        Log.d(TAG, " Setting time Limits");
        String url = EndPointConfiguration.getApiHost() + EndPointConfiguration.getBaseUrl() + EnvironmentConstants.MODIFY_URL + String.valueOf(mins);
        ModifiedUserResponseListener modifiedUserResponseListener = new ModifiedUserResponseListener();
        ModifiedUserErrorResponseListener modifiedUserErrorResponseListener = new ModifiedUserErrorResponseListener();

        GsonRequest<ModifyUserResponse> modifyTimeLimitRequest = new GsonRequest<ModifyUserResponse>(
                Request.Method.GET,
                url,
                null,//jsonRequest object
                ModifyUserResponse.class,
                modifiedUserResponseListener,
                modifiedUserErrorResponseListener,
                this
        );

        modifyTimeLimitRequest.setTag(TAG_MODIFY_REQUEST);
        NetworkManager networkRequestManager = NetworkManager.getInstance(this);
        networkRequestManager.add(modifyTimeLimitRequest);
    }

    @SuppressWarnings("Convert2Diamond")
    private void postSetDepositLimit(int limit, String period) {

        try {
            String modifyChangeDepositLimitUrl = EndPointConfiguration.getApiHost() + EndPointConfiguration.getBaseUrl() + EnvironmentConstants.MODIFY_CHANGE_DEPOSIT_LIMIT_URL;
            Log.d(TAG, "modifyChangeDepositLimitUrl :" + modifyChangeDepositLimitUrl);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("limit", limit);
            jsonObject.put("period", period);

            GetChangeLimitResponseListener mGetChangeLimitResponseListener = new GetChangeLimitResponseListener();
            GetChangeLimitErrorResponseListener mGetChangeLimitErrorListener = new GetChangeLimitErrorResponseListener();

            GsonRequest<ChangeDepositLimitResponse> changeDepositLimitRequest = new GsonRequest<ChangeDepositLimitResponse>(
                    Request.Method.POST,
                    modifyChangeDepositLimitUrl,
                    jsonObject,//jsonRequest object
                    ChangeDepositLimitResponse.class,
                    mGetChangeLimitResponseListener,
                    mGetChangeLimitErrorListener,
                    this
            );

            changeDepositLimitRequest.setTag(TAG_GETCHANGELIMIT_REQUEST);
            NetworkManager networkRequestManager = NetworkManager.getInstance(this);
            networkRequestManager.add(changeDepositLimitRequest);

        } catch (JSONException e) {
            //An error occured
            e.printStackTrace();
        }
    }

    private void showProgress() {
        //Show progress dialog
        progressDialog = DialogUtils.getProgressDialog(this, getResources().getString(R.string.label_dialog_saving_settings), getResources().getString(R.string.label_please_wait), false);
        progressDialog.show();
    }

    private void updateSettingsToServer() {
        hideKeyboard();
        if(!isPaused)
            showProgress();

        SettingsFragment settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.accountsfragment);
        if (settingsFragment.getTimeLimitInMins() != SessionController.getInstance().getTimeLimitInMinutes())
            setTimeLimitInMinutesDialog();

        //Get reality check information
        if (settingsFragment.getView() != null
                && settingsFragment.getView().findViewById(R.id.reality_check_layout) != null)
        {
            boolean newTimeCheckEnabled = ((ViewTogglerLayout) settingsFragment.getView().findViewById(R.id.reality_check_layout)).isChecked();
            boolean oldTimeCheckEnabled = SessionController.getInstance().isRealityCheckEnabled();
            long oldTimeCheckTime = SessionController.getInstance().getRealityCheckTime();
            if (oldTimeCheckEnabled != newTimeCheckEnabled) {
                SessionController.getInstance().setRealityCheckEnabled(newTimeCheckEnabled);
                RealityCheckService.getInstance().refresh();
            }
            if (newTimeCheckEnabled) //The time is only updated when the checking is active
            {
                Spinner realityCheckSpinner = (Spinner) ((ViewTogglerLayout) settingsFragment.getView().findViewById(R.id.reality_check_layout)).getInnerView();
                long newTime = (Long) realityCheckSpinner.getSelectedItem();
                if (oldTimeCheckTime != newTime) {
                    SessionController.getInstance().setRealityCheckTime(newTime);
                    RealityCheckService.getInstance().refresh();
                }
            }
        }

        Integer timeLimit = SessionController.getInstance().getTimeLimitInMinutes();

        //Log.d("DEPOSIT LIMIT", "timeLimit updateSettingsToServer =" + timeLimit);
        DepositLimitPreferences depositLimitPreferences = settingsFragment.getDepositLimitPreferences();
        mNewDepositLimit =  depositLimitPreferences.getAmount();
        switch (depositLimitPreferences.getTimePeriod())
        {
            case DAILY:
                mNewDepositLimitPeriod = 0;
                break;
            case WEEKLY:
                mNewDepositLimitPeriod = 1;
                break;
            case MONTHLY:
                mNewDepositLimitPeriod = 2;
                break;
        }


        if (mDepositLimit == null)
            mDepositLimit = 0;

        if (mDepositLimitPeriod == null)
            mDepositLimitPeriod = 0;

        if(!mTimeLimit.equals(timeLimit)) {
            postSetTimeLimit(timeLimit);
        }
        else if(!mDepositLimit.equals(mNewDepositLimit) || !mDepositLimitPeriod.equals(mNewDepositLimitPeriod)) {
            //getChangeLimitResult(mNewDepositLimit);
            String depositLimitPeriodString = Utils.getDepositLimitPeriodString(mNewDepositLimitPeriod);
            postSetDepositLimit(mNewDepositLimit, depositLimitPeriodString);
        }
        else
            onSettingsSaved();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                updateSettingsToServer();
                break;
            default:
                if (mOpenBetsFragment.getView() != null) {
                    if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
                        mDrawerLayout.closeDrawer(mOpenBetsFragment.getView());
                    } else {
                        mDrawerLayout.openDrawer(mOpenBetsFragment.getView());
                    }
                }
        }
        return true;
    }

    private void onSettingsSaved() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if(!isPaused) {
            if(!mDepositLimit.equals(mNewDepositLimit) || !mDepositLimitPeriod.equals(mNewDepositLimitPeriod))
                showDepositLimitDialog(mNewDepositLimitPeriod);
            else if (mTimeLimit == SessionController.getInstance().getTimeLimitInMinutes()) {
                if (timeLimitAccepted)
                    launchDashboard();
                else
                    timeLimitAccepted = true;
            }
        }

    }

    /**
     * <p>Launches the dashboard.</p>
     */
    private void launchDashboard() {
        if(uri != null) { // Deep link
            if (isIndirectLaunch) {
                finish();
                Intent parentIntent = new Intent(this, DashboardActivity.class);
                startActivity(parentIntent);
            }
            else
                finish();
        }
        else {
            Intent launchIntent = new Intent(this, DashboardActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launchIntent);
        }
    }

    @Override
    public void onOpenBetMenuSelection(MenuItem item) {
        Log.d(TAG, "onOpenBetMenuSelection");

        if (mOpenBetsFragment != null && mOpenBetsFragment.getView() != null) {
            if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
                //if already open, close it
                mDrawerLayout.closeDrawers();
            } else {
                //open right drawer
                mDrawerLayout.openDrawer(mOpenBetsFragment.getView());
            }
        }

    }

    @Override
    public Intent getSupportParentActivityIntent() {
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


    @Override
    public void onBackPressed() {
        if (mOpenBetsFragment != null && mOpenBetsFragment.getView() != null) {
            if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
                //close it
                mDrawerLayout.closeDrawers();
            } else {
                updateSettingsToServer();
                //super.onBackPressed();
            }
        }
    }

    private void showDepositLimitDialog(int depositLimitPeriod) {
        final AlertDialog.Builder dialogBuilder = DialogUtils.getAlertDialogBuilder(this);
        AlertDialog depositDialog;
        dialogBuilder.setTitle(getResources().getString(R.string.label_warning));
        String depositLimitPeriodString = Utils.getDepositLimitPeriodString(depositLimitPeriod);
        String depositLimitMessage = getResources().getString(R.string.msg_deposit_limit_changed, depositLimitPeriodString);
        dialogBuilder.setMessage(depositLimitMessage);
        dialogBuilder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (!isPaused) {
                    launchDashboard();
                }
            }
        });
        dialogBuilder.setInverseBackgroundForced(true);
        dialogBuilder.setCancelable(false);
        depositDialog = dialogBuilder.create();
        depositDialog.show();
    }

    @Override
    public void onDrawerSlide(View view, float v) {

    }

    @Override
    public void onDrawerOpened(View view) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        Log.d(TAG, "onDrawer opened !!!");
        if(mOpenBetsFragment != null && mOpenBetsFragment.isVisible()){
            mOpenBetsFragment.onResume();
        }

    }

    @Override
    public void onDrawerClosed(View view) {

        if(mOpenBetsFragment != null){
            mOpenBetsFragment.reset();
        }

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public void updateBalance() {

    }

    /**
     * Class for handling the response of timelimit in mins*
     */
    class ModifiedUserResponseListener implements Response.Listener<ModifyUserResponse> {

        @Override
        public void onResponse(ModifyUserResponse response) {

            String depositLimitPeriodString = Utils.getDepositLimitPeriodString(mNewDepositLimitPeriod);
            SettingsFragment settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.accountsfragment);

            if (SessionController.getInstance().getDepositLimitPreferences().getAmount() != settingsFragment.getDepositLimitPreferences().getAmount()
                    || SessionController.getInstance().getDepositLimitPreferences().getTimePeriod() != settingsFragment.getDepositLimitPreferences().getTimePeriod())
                postSetDepositLimit(mNewDepositLimit, depositLimitPeriodString);
            else
                onSettingsSaved();
        }
    }

    /**
     * Class for handling the response of change in navigateToDeposit *
     */
    class GetChangeLimitResponseListener implements Response.Listener<ChangeDepositLimitResponse> {


        @Override
        public void onResponse(ChangeDepositLimitResponse response) {

            if (response.isSuccess()) {
                Log.d(TAG, "Result Deposit Limit: " + response.getResult());

                onSettingsSaved();
            }

        }
    }

    class ModifiedUserErrorResponseListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {

            Snackbar.make(rootView, R.string.error_msg_update_failed, Snackbar.LENGTH_LONG).show();

            Log.d(TAG, "Modification unsuccessful   " + error.getCause());

            String depositLimitPeriodString = Utils.getDepositLimitPeriodString(mNewDepositLimitPeriod);
            postSetDepositLimit(mNewDepositLimit, depositLimitPeriodString);
            //getChangeLimitResult(mNewDepositLimit);

        }
    }

    class GetChangeLimitErrorResponseListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {

            Log.d(TAG, "GetChangeLimit Unsuccessful  " + error.getCause());
            Snackbar.make(rootView, R.string.error_msg_update_failed, Snackbar.LENGTH_LONG).show();

            onSettingsSaved();

        }
    }
}
