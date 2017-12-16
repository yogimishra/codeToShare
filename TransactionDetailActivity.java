import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

@SuppressWarnings({"FieldCanBeLocal"})
public class TransactionDetailActivity extends LoggedInBaseActivity implements DrawerLayout.DrawerListener   {

    private final String TAG = "GT.TransactionDetailActivity";

    private ActionBar mSupportActionBar;

    private DrawerLayout mDrawerLayout;

    private OpenBetsFragment mOpenBetsFragment;

    private TextView mTransaction_detail_balance_currency_type;

    private static Transaction transaction;


    public static Transaction getTransaction() {
        return transaction;
    }

    public static void setTransaction(Transaction transaction) {
        TransactionDetailActivity.transaction = transaction;
    }

    @Override
    protected void onLoggedInCreate(Bundle savedInstanceState) {
        Crashlytics.setString("onCreate", "TransactionDetailActivity");
        EventTracker.trackFlurryEvent(this, "TransactionDetailPage");

        setContentView(R.layout.activity_transaction_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        mTransaction_detail_balance_currency_type = (TextView)findViewById(R.id.text_user_balance_currency_type);
        mTransaction_detail_balance_currency_type.setText(Utils.getCurrency(mCache));

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            if (transaction != null) {
                mSupportActionBar.setTitle(getString(R.string.label_transaction) + " #" + transaction.getId());
            } else {
                mSupportActionBar.setTitle(R.string.label_transaction + Utils.getCurrency(mCache));
            }
        }

        //set open bets fragment
        mOpenBetsFragment = (OpenBetsFragment)getSupportFragmentManager().findFragmentById(R.id.right_drawer);

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
    public void onLoggedInPause(){
        Crashlytics.setString("onPause", "TransactionDetailAcitivity");
    }

    @Override
    protected void onLoggedInStop() {
        //Do nothing.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public void onOpenBetMenuSelection(MenuItem item) {

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
        supportFinishAfterTransition();
        return parentIntent;
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
    public void onBackPressed() {

        if (mOpenBetsFragment != null && mOpenBetsFragment.getView() != null) {
            if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView())) {
                //close it
                mDrawerLayout.closeDrawers();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void updateBalance() {

    }
}
