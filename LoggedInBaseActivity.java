import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


/**
 * <p>This class is a base activity that will ensure that the user is logged in and, if it's not,
 * it will be redirected to log in activity.</p>
 * @author Yogesh Mishra
 * @version 1.0
 */
public abstract class LoggedInBaseActivity extends BaseActionBarActivity {
    private boolean loggedIn; //In order to prevent logging out by timeout, the authenticated state of the user is stored.
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loggedIn = isUserLoggedIn();
        if (!loggedIn)
        {
            Uri uri = getIntent().getData();
            Intent intent = null;
            if(uri == null) {
                intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else {// Deep Link
                intent = new Intent(this, SplashActivity.class);
                intent.setData(uri);
            }
            finish();
            startActivity(intent);
        }
        else
            onLoggedInCreate(savedInstanceState);
    }

    @Override
    protected final void onPause() {
        super.onPause();
        if (loggedIn)
            onLoggedInPause();
    }

    @Override
    protected final void onResume() {
        super.onResume();
        if (loggedIn)
        {
            if (isConfirmationNeeded())
                showConfirmLimitsDialog();
            onLoggedInResume();
        }
    }

    @Override
    protected final void onStop() {
        super.onStop();
        if (loggedIn)
            onLoggedInStop();
    }

    @Override
    protected final void onStart() {
        super.onStart();
        if (loggedIn)
            onLoggedInStart();
    }

    /**
     * <p>This method has to be implemented instead of onCreate in order to ensure that the navigateToLogin check is not bypassed.</p>
     * @param savedInstanceState is the saved instance state received in onCreate.
     */
    protected abstract void onLoggedInCreate(Bundle savedInstanceState);

    /**
     * <p>This method has to be implemented instead of onStart in order to ensure that the navigateToLogin check is not bypassed.</p>
     */
    protected abstract void onLoggedInStart();

    /**
     * <p>This method has to be implemented instead of onResume in order to ensure that the navigateToLogin check is not bypassed.</p>
     */
    protected abstract void onLoggedInResume();

    /**
     * <p>This method has to be implemented instead of onPause in order to ensure that the navigateToLogin check is not bypassed.</p>
     */
    protected abstract void onLoggedInPause();

    /**
     * <p>This method has to be implemented instead of onStop in order to ensure that the navigateToLogin check is not bypassed.</p>
     */
    protected abstract void onLoggedInStop();

    /**
     * <p>Returns whether the user is logged in or not.</p>
     * @return true if the user is logged in, false otherwise.
     */
    private boolean isUserLoggedIn()
    {
        SessionController sessionController = SessionController.getInstance();
        return sessionController.isValidSession() && !sessionController.isAnonymousUser();
    }

    /**
     * <p>This method tells the activity if the user has to confirm the navigateToDeposit limits or not.</p>
     * @return true when the user has to confirm the navigateToDeposit limits, false otherwise.
     */
    private boolean isConfirmationNeeded()
    {
        return SessionController.getInstance().isConfirmationRequired();
    }

    /**
     * <p>Display the dialog which will allow the user to confirm the navigateToDeposit limits.</p>
     */
    private void showConfirmLimitsDialog()
    {
        /*
        Since confirmation dialog is about to be displayed, in order to prevent the dialog from being
        displayed again, confirmation needed is set to false. It would be better to handle it in the
        response but it's done like this to keep it simple.
         */
        mCache.setConfirmationNeeded(false);
        new ConfirmDepositDialogFragment().show(getSupportFragmentManager(), "CONFIRM_LIMITS_DIALOG");

    }
}
