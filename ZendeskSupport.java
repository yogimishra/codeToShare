import android.content.Context;
import android.util.Log;

import com.zendesk.sdk.feedback.impl.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.model.network.AnonymousIdentity;
import com.zendesk.sdk.model.network.Identity;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.support.SupportActivity;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.json.JSONObject;

/**
 * Created by yogeshmishra on 23/10/15.
 */
public class ZendeskSupport {

    public static final String ZENDESK_APPLICATION_ID = "f64749492e9d8f007f77e7c6d3312e7b88b5bb7211469700";

    public static final String ZENDESK_OOATH_CLIENT_ID  = "mobile_sdk_client_cd04726ead0e5bd486bb";

    private static final String TAG = "ZendeskSupport";
    private static ZendeskSupport zendeskSupport;
    private enum User {
        ANONYMOUS,
        REGISTERED
    };
    private static User mUser;

    private ZendeskSupport(Context context, CacheManager.ICache mCache) {
        zendeskSupport = this;
        mUser = mCache.isAnonymousUser() ? User.ANONYMOUS : User.REGISTERED;
        initialiseZendeskSDK(context, mCache);
    }

    public static ZendeskSupport getInstance(Context context, CacheManager.ICache mCache) {
        if(zendeskSupport == null) {
            zendeskSupport = new ZendeskSupport(context, mCache);
        }
        else {
            User user = mCache.isAnonymousUser() ? User.ANONYMOUS : User.REGISTERED;
            if(!user.equals(mUser)) {
                zendeskSupport = new ZendeskSupport(context, mCache);
            }
        }

        return zendeskSupport;
    }

    private void initialiseZendeskSDK(final Context context, final CacheManager.ICache mCache) {

        if (mCache == null) {
            Log.d(TAG, "Cache is null. Zendesk SDK not initialised");
            return;
        }

        String zendeskURL = context.getResources().getString(R.string.support_url_zendesk);
        ZendeskConfig.INSTANCE.init(context, zendeskURL, ZENDESK_APPLICATION_ID, ZENDESK_OOATH_CLIENT_ID,
                new ZendeskCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        AnonymousIdentity.Builder builder = new AnonymousIdentity.Builder();
                        if (!mCache.isAnonymousUser()) {
                            builder.withEmailIdentifier(mCache.getUserEmailAddress());
                            builder.withNameIdentifier(mCache.getUserFirstName() + " " + mCache.getUserSurName());
                        }
                        builder.withExternalIdentifier(mCache.getUserId());
                        Identity anonymousIdentity = builder.build();
                        ZendeskConfig.INSTANCE.setIdentity(anonymousIdentity);
                        //Setting Configuration for contact component
                        ZendeskConfig.INSTANCE.setContactConfiguration(new GTContactConfiguration(context));
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        // The SDK is not initialised
                        Log.d(TAG, "Zendesk SDK not initialised - " + errorResponse.toString());
                    }
                });
    }

    public void startSupportActivity(Context context) {
        //Logging MixPanal event for Help with empty parameters
        JSONObject params = new JSONObject();
        MixPanelWrapper.logEvent(context, MixPanelEvents.HELP_EVENT, params);

        SupportActivity.Builder builder = new SupportActivity.Builder();
        if(mUser.equals(User.ANONYMOUS))
            builder.showContactUsButton(false);
        else
            builder.showContactUsButton(true);

        builder.show(context);
    }

    /**
     * This class will configure the feedback dialog
     */
    class GTContactConfiguration extends BaseZendeskFeedbackConfiguration {

        public final transient Context mContext;

        public GTContactConfiguration(Context context) {
            this.mContext = context;
        }

        @Override
        public String getRequestSubject() {

            /**
             * A request will normally have a shorter subject and a longer description. Here we are
             * specifying the subject that will be on the request that is created by the feedback
             * dialog.
             */
            return mContext.getString(R.string.rate_my_app_dialog_feedback_request_subject);
        }

        @Override
        public String getAdditionalInfo() {
            return super.getAdditionalInfo();
        }
    }
}
