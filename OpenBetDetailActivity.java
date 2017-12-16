import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import org.parceler.Parcels;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class OpenBetDetailActivity extends AppCompatActivity {


    private OpenBetDetails openBetDetails;

    private final String TAG = "GT.OpenBetDetailActivity";

    private CacheManager.ICache mCache = CacheManager.getInstance();
    private BalanceFragment balanceFragment;
    private LightStreamerManager lightStreamerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.setString("onCreate", "OpenBetDetails");
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(Utils.CACHE_KEY)){
                mCache= Parcels.unwrap(savedInstanceState.getParcelable(Utils.CACHE_KEY));
            }
            if(savedInstanceState.containsKey(Utils.CACHE_KEY_OPEN_BETS)){
                openBetDetails = savedInstanceState.getParcelable(Utils.CACHE_KEY_OPEN_BETS);
            }
        }
        setContentView(R.layout.activity_open_bet_detail);

        Bundle bundle = getIntent().getExtras();
        openBetDetails = bundle.getParcelable(BundleKey.OPEN_BET_DETAILS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        balanceFragment = (BalanceFragment) getSupportFragmentManager().findFragmentByTag("BALANCE_FRAGMENT");
        lightStreamerManager = new LightStreamerManager(this, TAG);
    }
    @Override
    protected void onResume() {
        super.onResume();
        EventTracker.resumeAdjust(this);
        if (balanceFragment != null)
            balanceFragment.update();
        if (!lightStreamerManager.isBound())
            bindLightStreamer();
    }
    @Override
    protected void onPause() {
        super.onPause();
        EventTracker.pauseAdjust(this);
        if (lightStreamerManager.isBound())
            unbindLightStreamer();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Parcelable parcelable = Parcels.wrap(mCache);
        outState.putParcelable(Utils.CACHE_KEY, parcelable);
        outState.putParcelable(Utils.CACHE_KEY_OPEN_BETS, openBetDetails);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.containsKey(Utils.CACHE_KEY)){
            mCache=Parcels.unwrap(savedInstanceState.getParcelable(Utils.CACHE_KEY));
            CacheManager.updateCache(mCache);
        }
        if(savedInstanceState.containsKey(Utils.CACHE_KEY_OPEN_BETS)){
            openBetDetails = savedInstanceState.getParcelable(Utils.CACHE_KEY_OPEN_BETS);
        }
    }


    public OpenBetDetails getOpenBetDetails() {
        return openBetDetails;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
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

    /**
     * <p>Updates the balance.</p>
     * @param data contains the data regarding the balance.
     */
    private void updateBalance(Bundle data)
    {
        String balance = data.getString(Utils.LS_BALANCE_FIELD_BALANCE);
        //update nenx cache with latest balance
        if (balance != null)
            mCache.setAccountBalance(Float.valueOf(balance));

        if (balanceFragment != null)
            balanceFragment.update();
    }

    /**
     * <p>Binds the light streamer service and subscribes all the necessary events.</p>
     */
    private void bindLightStreamer()
    {
        lightStreamerManager.bind();
        lightStreamerManager.subscribeEvent(Utils.MSG_BALANCE_UPDATE, new LightStreamerManager.Listener() {
            @Override
            public void update(Bundle data) {
                updateBalance(data);
            }
        });
    }

    /**
     * <p>Unbinds the service and unsubscribes all events.</p>
     */
    private void unbindLightStreamer()
    {
        lightStreamerManager.unSubscribeEvent(Utils.MSG_BALANCE_UPDATE);
        lightStreamerManager.unBind();
    }
}
