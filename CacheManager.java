package com.gametech.logic.model.cache;

import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.gametech.logic.model.network.responses.common.DurationDetail;
import com.gametech.logic.model.network.responses.login.LoginResponse;
import com.gametech.logic.model.network.responses.login.anonymous.AnonymousLoginResponse;
import com.gametech.logic.model.network.responses.openbets.OpenBetDetails;
import com.gametech.logic.utils.Utils;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.List;

/**
 * CacheManager singleton class to store objects if any
 */
public class CacheManager {

    private static String TAG = "GT.CacheManager";

    private static Cache mCacheImpl;

    /**
     * Private constructor to stop instantiating the object
     */
    private CacheManager(){

    }

    /**
     * Static method to get the instance of cache impl
     * @return the instance of the cache implementation
     */
    @Deprecated //Cache should be accessed via SessionController
    public synchronized static Cache getInstance(){

        if(mCacheImpl == null){
            mCacheImpl = new Cache();
        }

        return mCacheImpl;
    }


    @Deprecated //Cache should be accessed via SessionController
    public static CacheManager.ICache getCache(Bundle savedInstanceState){
        if(savedInstanceState == null) {
            Crashlytics.log(Log.DEBUG, TAG, "getNenxCache savedInstanceState " + savedInstanceState);
            mCacheImpl = CacheManager.getInstance();
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "getNenxCache Unwrap bundle" + savedInstanceState);
            mCacheImpl = Parcels.unwrap(savedInstanceState.getParcelable(Utils.CACHE_KEY));
        }

        return mCacheImpl;
    }

    /*
    This will test the cache
     */
    @Deprecated
    public static boolean isValidCache(CacheManager.ICache cache){

        if(cache==null){
            Crashlytics.log(Log.ERROR, TAG, "isValidCache is null return false");
            return false;
        }

        if(!cache.isAnonymousUser()) {
            if (cache.getUserId() == null || cache.getUserId().equals("-1")) {
                Crashlytics.log(Log.ERROR, TAG, "isValidCache REQUEST_HEADER_USER_ID null return false");
                return false;
            }
            String userToken = cache.getUserToken();
            if (userToken == null || userToken.equals("")) {
                Crashlytics.log(Log.ERROR, TAG, "isValidCache REQUEST_HEADER_USER_TOKEN null return false");
                return false;
            }
            Crashlytics.log(Log.ERROR, TAG, "isValidCache return true");

        }
        return true;
    }

    /*
    This will test the cache
     */
    @Deprecated
    public static boolean isValidPreLoginDetails(CacheManager.ICache cache){

        if(cache==null){
            Crashlytics.log(Log.ERROR, TAG, "isValidPreLoginDetails is null return false");
            return false;
        }
        if (cache.getConfigMap() == null) {
            Crashlytics.log(Log.ERROR, TAG, "isValidPreLoginDetails getConfigMap null return false");
            return false;
        }
        return true;
    }


    public static void updateCache(ICache cache){
        mCacheImpl = (Cache)cache;
    }


    /**
     * NenxCache interface for methods exposed
     */
    public interface ICache{

        String getUserId();

        void setUserId(String userId);

        String getUserToken();

        void setUserToken(String userToken);

        String getUserStatus();

        void setUserStatus(String userStatus);

        String getUserFirstName();

        void setUserFirstName(String userFirstName);

        String getUserSurName();

        void setUserSurName(String userSurName);

        String getUserEmailAddress();

        void setUserEmailAddress(String userEmailAddress);

        boolean isPinCodeEnabled();

        void setPinCodeEnabled(boolean pinCodeEnabled);

        int getTimeLimit();

        void setTimeLimit(int timeLimit);

        int getLoggedInTimeToday();

        void setLoggedInTimeToday(int loggedInTimeToday);

        void setAnonymousLoginDetails(AnonymousLoginResponse response);

        void setLoginDetails(LoginResponse response);

        void resetLoginDetails();

        DurationDetail[] getDurations();

        void setDurations(DurationDetail[] durations);

        long getServerTime();

        void setServerTime(long serverTime);

        void resetCache();

        void setAccountBalance(Float accountBalance);

        Float getAccountBalance();

        void setMarketPrice(String marketId, String marketPrice);

        float getMarketPrice(long marketId);

        Boolean isCountryApproved();

        void setCountryApproved(boolean mCountryApproved);

        void setOpenBetsCount(int openBetsCount);

        int getOpenBetsCount();

        long getTimeOffset();

        void setUserAgent(float userAgent);

        float getUserAgent();

        void setConfigMap(ConfigMap configMap);

        ConfigMap getConfigMap();

        /**
         * <p>Determines whether the user has seen the tutorial which is presented after upgrading
         * the account.</p>
         * @return true when the user has seen the tutorial or false otherwise.
         */
        boolean hasUserSeenUpgradedAccountTutorial();

        /**
         * <p>Sets the property that determine whether the user has seen the tutorial that
         * is presented after upgrading the account.</p>
         * @param flag true indicates the user has seen the tutorial.
         */
        void setUserHasSeenUpgradedAccountTutorial(boolean flag);

        void setOpenbetsCache(List<OpenBetDetails> openBetDetails);

        HashMap<Long, List<OpenBetDetails>> getOpenbetsCache();

        void updateOpenbetsCacheForSettledItems(String marketId, String settledBetId);

        boolean isUpgradeRequired();

        boolean isUpgradeRecommended();

        void setUpgradeRequired(boolean type);

        void setUpgradeRecommended(boolean type);

        boolean isAnonymousUser();

        void setAnonymousUser(boolean isAnonymousUser);

        void setAttributionTrackerName(String attributionTrackerName);

        String getAttributionTrackerName();

        String getAttributionAdGroup();

        void setAttributionAdGroup(String attributionAdGroup);

        String getAttributionCampaign() ;

        void setAttributionCampaign(String attributionCampaign);

        String getAttributionClickLabel() ;

        void setAttributionClickLabel(String attributionClickLabel);

        String getAttributionCreative() ;

        void setAttributionCreative(String attributionCreative) ;

        String getAttributionTrackerToken() ;

        void setAttributionTrackerToken(String attributionTrackerToken) ;

        String getAttributionNetwork();

        void setAttributionNetwork(String attributionNetwork);

        void setPromotionsUrl(String url);

        String getPromotionsUrl();

        String getGoogleId();

        void setGoogleId(String id);

        String getLanguage();

        void setLanguage(String language);

        String getRegion();

        void setRegion(String region);

        void setCurrencyIso(String currency);

        String getCurrencyIso();

        /**
         * <p>Estates if the user has to confirm his new deposit limits.</p>
         * @return true if the user has to confirm them, false otherwise.
         */
        boolean isConfirmationNeeded();

        /**
         * <p>Sets if the user has to confirm the deposit limits or not.</p>
         * @param confirmationNeeded true if the user has to confirm the limits, false otherwise.
         */
        void setConfirmationNeeded(boolean confirmationNeeded);

        /**
         * <p>Returns the time for the reality check.</p>
         * @return an integer representing the time for the reality check or 0 when disabled.
         */
        int getRealityCheckTime();

        /**
         * <p>Sets the time for the reality check.</p>
         * @param time is the time for the reality check or 0 to disable it.
         */
        void setRealityCheckTime(int time);

        /**
         * <p>Determines if the user has seen the tutorial that is presented when the user first
         * creates an account.</p>
         * @return true if the user has seen the tutorial or false otherwise.
         */
        boolean hasUserSeenBettingTutorial();

        /**
         * <p>Sets the property that indicates if the user has seen the tutorial which is
         * presented after creating the account.</p>
         * @param flag true if the user has seen the tutorial.
         */
        void setUserHasSeenBettingTutorial(boolean flag);

        /**
         * <p>Return whether the user has placed a quick bet or not.</p>
         * @return true when the user has placed a quick bet.
         */
        boolean hasUserPlacedQuickBet();

        /**
         * <p>Sets the flag that determines whether the user has placed a quick bet or not.</p>
         * @param flag true when the user has placed a quick bet.
         */
        void setUserHasPlacedQuickBet(boolean flag);

        /**
         * Set user country iso this is provided to us by the country response
         */
        void setCountryIso(String country);
        /**
         * Get user country iso this is provided to us by the country response
         */
        String getCountryIso();

        /**
         * <p>This method returns whether the user has a balance coming from the server or not.</p>
         * @return true when the user has a balance, false otherwise.
         */
        boolean isThereBalance();

        /**
         * <p>This method sets the flag for the user having a balance from the server.</p>
         * @param thereIsBalance true when the user has a balance from the server, false otherwise.
         */
        void setThereIsBalance(boolean thereIsBalance);

        /**
         * <p>This method returns whether the user has received the bets from the server or not.</p>
         * @return true when the bets have been received by the server, false otherwise.
         */
        boolean areThereBets();

        /**
         * <p>This method sets whether the betting information has been received from the server.</p>
         * @param thereAreBets true when the bets have been received from the server, false otherwise.
         */
        void setThereAreBets(boolean thereAreBets);

        /**
         * <p>Returns whether the user has enabled vibration or not.</p>
         * @return true when vibration is enabled or false otherwise.
         */
        boolean isVibrationEnabled();

        /**
         * <p>Sets vibration as enabled or disabled.</p>
         * @param enabled true when enabled, false otherwise.
         */
        void setVibrationEnabled(boolean enabled);

        /**
         * <p>Returns whether the user has sound enabled or not.</p>
         * @return true when enabled, false otherwise.
         */
        boolean isSoundEnabled();

        /**
         * <p>Set the sound as enabled or disabled.</p>
         * @param enabled true when enabled, false otherwise.
         */
        void setSoundEnabled(boolean enabled);

        /**
         * <p>Returns the number of minutes before the user is forced to verify. -1 means that
         * the feature is disabled or the user has already verified the account.</p>
         * @return the number of minutes before the user is forced to validate his account or -1 when not required.
         */
        int getMinutesBeforeVerificationRequired();

        /**
         * <p>Sets the number of minutes that the user is able to bet or deposit for </p>
         * @param minutesBeforeVerificationRequired is the number of minutes or -1 to disable it.
         */
        void setMinutesBeforeVerificationRequired(int minutesBeforeVerificationRequired);
    }


}
