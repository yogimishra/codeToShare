import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;


/**
 * <p>This class acts as a container for all web views to be loaded. This should
 * be used to load all Web views.</p>
 * @author Yogesh Mishra
 * @version 2.0
 */
public class WebViewActivity extends BaseActionBarActivity implements DrawerLayout.DrawerListener {

    private static final String TAG = "WebViewActivity";
    private DrawerLayout mDrawerLayout;

    private OpenBetsFragment mOpenBetsFragment;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(null);
        }

        // URL to be loaded should be passed as bundle parameter
        String url = getIntent().getStringExtra(BundleKey.WEBVIEW_URL);

        WebView webView = (WebView)findViewById(R.id.webcontent);
        // A different layout to be used in case of KITKAT devices. Which are
        // having issues to load text contents in web view.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            webView.setVisibility(View.GONE);
            webView = (WebView) findViewById(R.id.webcontent_19);
            ScrollView webViewContainer = (ScrollView) findViewById(R.id.webview_container);
            webViewContainer.setVisibility(WebView.VISIBLE);
        }

        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(true);

            WebViewClient webViewClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    String schemeURL = getString(R.string.scheme) + "://";
                    if (url.equalsIgnoreCase(schemeURL)) {
                        finish();
                        return true;
                    } else
                        return super.shouldOverrideUrlLoading(view, url);
                }
            };

            webView.setWebViewClient(webViewClient);

            webView.setInitialScale(100);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);

            webView.loadUrl(url);
        }

        mOpenBetsFragment = (OpenBetsFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);

        // Drawer Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOpenBetMenuSelection(MenuItem item) {
        if(!SessionController.getInstance().isAuthorized()){
            DialogUtils.showDialogToContactSupport(this);
        }
        else if(mDrawerLayout != null && mOpenBetsFragment != null && mOpenBetsFragment.getView() != null) {
            if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView()))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(mOpenBetsFragment.getView());
        }
    }

    @Override
    public void onDrawerSlide(View view, float v) {

    }

    @Override
    public void onDrawerOpened(View view) {
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        Log.d(TAG, "onDrawer opened !!!");
        if (mOpenBetsFragment != null && mOpenBetsFragment.isVisible())
            mOpenBetsFragment.onResume();
    }

    @Override
    public void onDrawerClosed(View view) {
        if (mOpenBetsFragment != null)
            mOpenBetsFragment.reset();
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public void onBackPressed() {
        if (mOpenBetsFragment != null && mOpenBetsFragment.getView() != null) {
            if (mDrawerLayout.isDrawerOpen(mOpenBetsFragment.getView()))
                mDrawerLayout.closeDrawers();
            else
                super.onBackPressed();
        }
        else
            super.onBackPressed();
    }
}
