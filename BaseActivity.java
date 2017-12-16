import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>This is the base activity for the application. Any activity will extend this activity in
 * order to get the most basic functionality. </p>
 * <p>The activity will bind the Light Streamer service by using {@link com.gametech.logic.lightstreamer.LightStreamerManager}.
 * Activities that extend this one will automatically bind the service and get balance and user status updates.
 * The activity will also display the relevant tutorials when needed.</p>
 * <p>The application has two tutorials. The founding tutorial is displayed when the user becomes active.
 * After displaying the tutorial, the user is asked if he wants to navigateToDeposit or not. After the user has
 * deposited, the betting tutorial is shown. </p>
 * <p>This activity will also subscribe and unsubscribe the event bus. The event bus is subscribed
 * on resume and unsubscribed on pause.</p>
 * @author Yogesh Mishra
 * @version 1.0
 */
public abstract class BaseActivity extends AppCompatActivity {
    public static String ASK_USER_TO_VERIFY = "ask_user_to_verify";
    protected static String VALIDATION_REQUIRED = "VALIDATION_REQUIRED";

    protected LightStreamerManager mLightStreamerManager;
    protected BaseActivityListener listener;
    protected boolean verifyAccountWarningShown;

    public BaseActivity() {
        listener = new BaseActivityListener(this);
    }

    // Deep link variables
    protected Uri uri;
    protected boolean isIndirectLaunch;

    /**
     * <p>This class allows the event bus to send events to the base activity.</p>
     */
    public class BaseActivityListener
    {
        private BaseActivity activity;
        private boolean registered;

        public BaseActivityListener(BaseActivity activity)
        {
            this.activity = activity;
        }

        /**
         * <p>This method is triggered when a show tutorial event is triggered.</p>
         * @param event is the triggered event.
         */
        @Subscribe
        @SuppressWarnings("unused")
        public void onShowTutorial(ShowTutorialEvent event)
        {
            activity.onShowTutorial(event);
        }

        /**
         * <p>Triggered when a bet is settled.</p>
         * @param event is the event that triggers the method.
         */
        @Subscribe
        @SuppressWarnings("unused")
        public void onBetSettled(BetStatusUpdateEvent event)
        {

            boolean showSnackbar = event.getStatus() == Bet.Status.WON || event.getStatus() == Bet.Status.LOST;
            if (showSnackbar) {
                showBetResultSnackBar(event.getBet());
                if (SessionController.getInstance().isVibrationEnabled()) {
                    switch (event.getStatus())
                    {
                        case WON:
                            VibrationManager.getInstance().won();
                            break;
                        case LOST:
                            VibrationManager.getInstance().lost();
                            break;
                        default:
                            VibrationManager.getInstance().standard();
                            break;
                    }
                }

                if (SessionController.getInstance().isSoundEnabled())
                {
                    switch (event.getStatus())
                    {
                        case WON:
                            SoundManager.getInstance().playBetWon();
                            break;
                        case LOST:
                            SoundManager.getInstance().playBetLost();
                            break;
                    }
                }
            }
        }
        @Subscribe
        public void onLoggedOut(LogOutEvent event)
        {
            SessionController.getInstance().afterLogOut(event.getLogOutType());
            finishAffinity();
            if (event.getLogOutType() != Session.LogOutType.EXIT_APPLICATION)
                navigateToLogin();
        }

        /**
         * <p>Returns whether the object is registered or not.</p>
         * @return true when registered, false otherwise.
         */
        public boolean isRegistered()
        {
            return registered;
        }

        /**
         * <p>Registers the object in the bus if it's not registered.</p>
         */
        public void register()
        {
            if (!isRegistered()) {
                DefaultApplication.getInstance().getBus().register(this);
                registered = true;
            }
        }

        /**
         * <p>Unregisters the object when it is registered.</p>
         */
        public void unregistered()
        {
            if (isRegistered())
            {
                DefaultApplication.getInstance().getBus().unregister(this);
                registered = false;
            }
        }
    }

    /**
     * <p>Shows a snackbar notifying the user about the result of a bet.</p>
     * @param bet is the bet that has been settled
     */
    private void showBetResultSnackBar(Bet bet)
    {
        String text = bet.getMarket().getMarketName() + ": ";
        int backgroundColor, textColor;
        switch (bet.getStatus())
        {
            case WON:
                text += getString(R.string.label_bet_outcome_won);
                backgroundColor = ContextCompat.getColor(this, R.color.bet_snackbar_background_win);
                textColor = ContextCompat.getColor(this, R.color.bet_snackbar_text_win);
                break;
            case LOST:
                text += getString(R.string.label_bet_outcome_lost);
                backgroundColor = ContextCompat.getColor(this, R.color.bet_snackbar_background_lose);
                textColor = ContextCompat.getColor(this, R.color.bet_snackbar_text_lose);
                break;
            case DRAWN:
                text += getString(R.string.label_bet_outcome_draw);
                backgroundColor = ContextCompat.getColor(this, R.color.bet_snackbar_background_draw);
                textColor = ContextCompat.getColor(this, R.color.bet_snackbar_text_draw);
                break;
            default:
                backgroundColor = 0;
                textColor = 0;
                break;
        }
        Snackbar snackbar = Snackbar.make(findViewById(R.id.root_view), text, Snackbar.LENGTH_SHORT);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setBackgroundColor(backgroundColor);
        final TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(textColor);
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String className = getClass().getName();
        uri = getIntent().getData();
        if(uri != null) { //Deep Link invocation
            Bundle bundle = getIntent().getExtras();
            if (bundle != null)
                isIndirectLaunch = bundle.getBoolean(BundleKey.IS_INDIRECT_LAUNCH);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        DefaultApplication.getInstance().getBus().register(this);
        listener.register();
        if (!getLightStreamerManager().isBound()) {
            getLightStreamerManager().bind(); //Binding the service
            //Subscribing to balance updates.
            getLightStreamerManager().subscribeEvent(Utils.MSG_BALANCE_UPDATE, new LightStreamerManager.Listener() {
                @Override
                public void update(Bundle data) {
                    String balanceString = data.getString(Utils.LS_BALANCE_FIELD_BALANCE);
                    if (balanceString != null) {
                        float balance = Float.valueOf(balanceString);
                        performBalanceUpdate(balance);
                    }
                }
            });
            //Subscribing to user updates.
            getLightStreamerManager().subscribeEvent(Utils.MSG_UPDATE_USER_UPDATE, new LightStreamerManager.Listener() {
                @Override
                public void update(Bundle data) {
                    try {
                        String json = data.getString(Utils.LS_USERS_JSON);
                        JSONObject getStatus = new JSONObject(json);
                        String status = getStatus.getString("status");
                        performUserStatusChanged(status);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            //Subscribing to bet updates.
            getLightStreamerManager().subscribeEvent(Utils.MSG_BET_STATUS_UPDATE, new LightStreamerManager.Listener() {
                @Override
                public void update(Bundle data) {
                    String betId = data.getString(Utils.LS_BET_ID);
                    String outCome = data.getString(Utils.LS_BET_OUTCOME);
                    double settledPrice = 0d;
                    try {
                        settledPrice = Double.valueOf(data.getString(Utils.LS_BET_SETTLEMENT_PRICE));
                    }
                    catch (NullPointerException ex)
                    {
                        //Nothing.
                    }
                    Bet.Status status;
                    if (Utils.BET_OUTCOME_WIN.equalsIgnoreCase(outCome))
                        status = Bet.Status.WON;
                    else if (Utils.BET_OUTCOME_LOSS.equalsIgnoreCase(outCome))
                        status = Bet.Status.LOST;
                    else if (Utils.BET_OUTCOME_DRAW.equalsIgnoreCase(outCome))
                        status = Bet.Status.DRAWN;
                    else
                        status = Bet.Status.OPEN;
                    Bet bet = BetsController.getInstance().findById(Integer.valueOf(betId));
                    if (bet == null) {
                        bet = new Bet();
                        long marketId = Long.valueOf(data.getString(Utils.LS_BET_MARKET_PROPERTY));
                        MarketDetail market = new MarketDetail(MarketRepository.getMarketById(DefaultApplication.getInstance(), marketId));
                        bet.setMarket(market);
                    }

                    bet.setStatus(status);
                    bet.setSettled(true);
                    DefaultApplication.getInstance().getBus().post(new BetStatusUpdateEvent(bet, status, settledPrice));
                }
            });
        }
        //Check whether to show tutorials on resume. This check only takes place on an active user.
        showRelevantTutorial();
        NetworkHelper.getInstance(DefaultApplication.getInstance());
        //If the user has to verify, he is warned about it
        Bundle extras = getIntent().getExtras();
        if (!verifyAccountWarningShown && extras != null && extras.containsKey(ASK_USER_TO_VERIFY) && extras.getBoolean(ASK_USER_TO_VERIFY))
        {
            View rootView = findViewById(R.id.root_view);
            Snackbar.make(rootView, R.string.validation_required_warning, Snackbar.LENGTH_SHORT).show();
            verifyAccountWarningShown = true;
        }
    }

    /**
     * <p>Show the relevant tutorial according to the user data.</p>
     */
    private void showRelevantTutorial() {
        SessionController.getInstance().showRelevantTutorial();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getLightStreamerManager().isBound())
        {
            getLightStreamerManager().unSubscribeEvent(Utils.MSG_UPDATE_USER_UPDATE);
            getLightStreamerManager().unSubscribeEvent(Utils.MSG_BALANCE_UPDATE);
            getLightStreamerManager().unSubscribeEvent(Utils.MSG_BET_STATUS_UPDATE);
            getLightStreamerManager().unBind();
        }
        listener.unregistered();
        DefaultApplication.getInstance().getBus().unregister(this);
        NetworkHelper.unregisterReceiver();
    }

    private LightStreamerManager getLightStreamerManager()
    {
        if (mLightStreamerManager == null)
            mLightStreamerManager = new LightStreamerManager(this, "BaseActivity");
        return mLightStreamerManager;
    }

    /**
     * <p>This method is triggered when a show tutorial event is triggered.</p>
     * @param event is the triggered event.
     */
    public void onShowTutorial(ShowTutorialEvent event)
    {
        if (event.getTutorial() != TutorialDialogFragment.MODE_DISPLAY_BETTING_TUTORIAL)
            showTutorial(event.getTutorial());
    }

    /**
     * <p>Performs a balance update.</p>
     */
    public void performBalanceUpdate(float balance)
    {
        SessionController.getInstance().setBalance(balance); //The balance is updated.
        showRelevantTutorial();
        DefaultApplication.getInstance().getBus().post(new BalanceUpdatedEvent());
    }

    /**
     * <p>Performs a status update.</p>
     * @param status new status for the user.
     */
    public void performUserStatusChanged(String status)
    {
        SessionController.getInstance().setUserStatus(status);
        showRelevantTutorial();
        DefaultApplication.getInstance().getBus().post(new UserStatusUpdateEvent(status));
    }

    /**
     * <p>Displays a tutorial.</p>
     * @param tutorial is the tutorial to display.
     */
    private void showTutorial(int tutorial)
    {
        boolean mShowFundingDialog = (tutorial == TutorialDialogFragment.MODE_DISPLAY_FUNDING_TUTORIAL && !StringUtils.isUrl(getResources().getString(R.string.funding_tutorial_url)));
        TutorialDialogFragment tutorialDialogFragment = new TutorialDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SwipeImagesFragment.KEY_DISPLAY_MODE, tutorial);
        tutorialDialogFragment.setArguments(bundle);
        if (mShowFundingDialog)
            tutorialDialogFragment.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    showFundMessage();
                }
            });
        tutorialDialogFragment.show(getSupportFragmentManager(), "TUTORIAL_DIALOG");
    }

    private void showFundMessage() {

        AlertDialog.Builder alert = DialogUtils.getAlertDialogBuilder(this);
        alert.setTitle(getResources().getString(R.string.label_user_option));
        alert.setMessage(getResources().getString(R.string.msg_place_fund_in_account));

        alert.setPositiveButton(R.string.label_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Do nothing.
            }
        });
        alert.setNegativeButton(R.string.label_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Launcher.navigateToFundsBrowserSession(BaseActivity.this);
            }
        });
        alert.setInverseBackgroundForced(true);
        alert.show();

    }

    //Activity launchers

    /**
     * <p>Starts the account creation process.</p>
     * @param email is the email that the user typed.
     */
    public void navigateToOpenAccount(String email)
    {
        Bundle bundle = new Bundle();
        if(email.toString().isEmpty()){
            bundle.putString(BundleKey.EMAIL, email.toString());
        }
        Intent launchIntent = new Intent(this, RegistrationStepOneActivity.class);
        launchIntent.putExtras(bundle);
        startActivity(launchIntent);
    }

    /**
     * <p>Starts the account creation process.</p>
     */
    public void navigateToOpenAccount()
    {
        navigateToOpenAccount("");
    }

    /**
     * <p>Starts the log in process.</p>
     */
    public void navigateToLogin()
    {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * <p>Start the depositing process.</p>
     */
    public void navigateToDeposit()
    {
        try {
            SessionController sessionController = SessionController.getInstance();

            String userID = sessionController.getUserId() + "";
            String token = URLEncoder.encode(sessionController.getUserToken(), "utf-8");
            BrandSettings brandSettings = sessionController.getBrandSettings();
            StringBuilder depositUrl = new StringBuilder(brandSettings.getDepositPage());

            StringUtils.replaceAll(depositUrl, "{0}", token);
            StringUtils.replaceAll(depositUrl, "{1}", userID);
            StringUtils.replaceAll(depositUrl, "{2}", Integer.toString(BuildConfig.VERSION_CODE));
            StringUtils.replaceAll(depositUrl, "{3}", sessionController.getPromoCode());

            String getFundsURL = (EndPointConfiguration.getApiHost() + depositUrl);

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(BundleKey.WEBVIEW_URL, getFundsURL);
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Shows dashboard.</p>
     */
    public void navigateToDashboard()
    {
        Intent launchIntent = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(launchIntent);
    }

    /**
     * <p>Shows anonymous dashboard.</p>
     */
    public void navigateToAnonymousDashboard()
    {
        Intent anonymousDashBoardIntent = new Intent(getApplicationContext(), AnonymousDashboardActivity.class);
        startActivity(anonymousDashBoardIntent);
    }
}
