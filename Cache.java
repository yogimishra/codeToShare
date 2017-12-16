package com.gametech.logic.model.cache;

import android.util.Log;
import android.util.SparseArray;


import com.gametech.logic.model.network.responses.common.DurationDetail;
import com.gametech.logic.model.network.responses.login.LoginResponse;
import com.gametech.logic.model.network.responses.login.LoginResult;
import com.gametech.logic.model.network.responses.login.LoginUser;
import com.gametech.logic.model.network.responses.login.anonymous.AnonymousLoginResponse;
import com.gametech.logic.model.network.responses.login.anonymous.AnonymousLoginResult;
import com.gametech.logic.model.network.responses.openbets.OpenBetDetails;
import com.gametech.logic.utils.Utils;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * NenxCacheImplementation class which stores the objects required across the app
 * @author Abhinav Holkar
 * @version 1.0
 */
@Parcel
@SuppressWarnings("Convert2Diamond")
public class Cache implements  CacheManager.ICache {

    private static String TAG = "Cache";

    public Cache(/*Required empty bean constructor*/){

    }

    public Float mAccountBalance;

    public SparseArray<Float> mMarketPrices = new SparseArray<Float>();

    public Boolean mCountryApproved = null;

    public int mOpenBetsCount;

    public long mServerTime;

    public long mOffset;

    public float mUserAgent;

    public ConfigMap configMap;

    public boolean hasUserUpgradedAccount;

    public boolean requiredUpdate = false;

    public boolean recommendUpdate = false;

    public boolean isAnonymousUser = false;

    public String mAttributionTrackerName = "";

    public String mAttributionAdGroup = "";

    public String mAttributionCampaign = "";

    public String mAttributionClickLabel = "";

    public String mAttributionCreative = "";

    public String mAttributionTrackerToken = "";

    public String mAttributionNetwork = "";

    public String promotions_url;

    public String googleAdvertisingId;

    public String mUserId;// Defualt value must be -1;

    public String mUserToken;


    public String mUserStatus;

    public String mUserFirstName;

    public String mUserSurName;

    public String mUserEmailAddress;

    public boolean mPinCodeEnabled;

    public int timeLimit;

    public int loggedInTimeToday;

    public DurationDetail[] mDurations;

    public HashMap<Long, List<OpenBetDetails>> mOpenbetsCache;

    public String language;

    public String region;

    public String currency;

    public boolean confirmationNeeded;

    public int mRealityCheckTime;

    public boolean userSawNewAccountTutorial;

    public boolean placedQuickBet;

    public String mUserCountry;

    public boolean balanceReceived;

    public boolean betsReceived;

    public boolean soundEnabled;

    public boolean vibrationEnabled;

    public int minutesBeforeVerificationRequired;

    @Override
    public boolean hasUserPlacedQuickBet() {
        return placedQuickBet;
    }

    @Override
    public void setUserHasPlacedQuickBet(boolean flag) {
        placedQuickBet = flag;
    }

    @Override
    public void setUserId(String userId) {
        mUserId = userId;
    }

    @Override
    public String getUserId() {
        return mUserId;
    }

    @Override
    public void setUserToken(String userToken) {
        mUserToken = userToken;
    }

    @Override
    public String getUserToken() {
        return mUserToken;
    }

    @Override
    public void setUserStatus(String userStatus) {
        mUserStatus = userStatus;
    }

    @Override
    public String getUserStatus() {
        return mUserStatus;
    }

    @Override
    public void setUserFirstName(String userFirstName) {
        mUserFirstName = userFirstName;
    }

    @Override
    public String getUserFirstName() {
        return mUserFirstName;
    }

    @Override
    public void setUserSurName(String userSurName) {
        mUserSurName = userSurName;
    }

    @Override
    public String getUserSurName() {
        return mUserSurName;
    }

    @Override
    public String getUserEmailAddress() {
        return mUserEmailAddress;
    }

    @Override
    public void setUserEmailAddress(String userEmailAddress) {
        this.mUserEmailAddress = userEmailAddress;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public int getLoggedInTimeToday() {
        return loggedInTimeToday;
    }

    @Override
    public void setLoggedInTimeToday(int loggedInTimeToday) {
        this.loggedInTimeToday = loggedInTimeToday;
    }

    @Override
    public boolean isPinCodeEnabled() {
        return mPinCodeEnabled;
    }

    @Override
    public void setPinCodeEnabled(boolean pinCodeEnabled) {
        this.mPinCodeEnabled = pinCodeEnabled;
    }

    @Override
    public void setAnonymousLoginDetails(AnonymousLoginResponse response) {
        AnonymousLoginResult anonymousLoginResult = response.getResult();
        setUserId(anonymousLoginResult.getAnonymousUserId());
        setUserToken(anonymousLoginResult.getUserToken());
    }

    @Override
    public void setLoginDetails(LoginResponse response) {
        LoginResult loginResult = response.getResult();
        setUserToken(loginResult.getUserToken());
        setServerTime(loginResult.getServerTime());
        setLoggedInTimeToday(loginResult.getLoggedInTimeToday());
        setCountryApproved(loginResult.isApprovedCountry());
        setDurations(loginResult.getDurations());

        LoginUser loginUser = loginResult.getUser();
        setUserId(loginUser.getId() + "");
        setUserEmailAddress(loginUser.getEmailAddress());
        setUserStatus(loginUser.getStatus());
        setUserFirstName(loginUser.getFirstName());
        setUserSurName(loginUser.getSurname());
        setTimeLimit(loginUser.getTimeLimit());
        setPinCodeEnabled(loginUser.isPinCodeEnabled());
        setUserAgent(loginUser.getUserAgent());
        setCurrencyIso(loginUser.getCurrency().getIso());

        if(Utils.isProoptionBuild()){
            setLanguage(loginUser.getLanguage());
            setRegion(loginUser.getRegion());
        }
    }

    @Override
    public void resetLoginDetails() {
        setUserId("-1");
        setUserToken("");
    }

    @Override
    public void setDurations(DurationDetail[] durations) {
        this.mDurations = durations;
    }

    @Override
    public DurationDetail[] getDurations() {
        return this.mDurations;
    }

    @Override
    public void setUserAgent(float userAgent) {
        mUserAgent = userAgent;
    }

    @Override
    public float getUserAgent() {
        return mUserAgent;
    }

    @Override
    public void setConfigMap(ConfigMap configMap) {
        this.configMap = configMap;

    }

    @Override
    public ConfigMap getConfigMap() {
        return configMap;
    }

    @Override
    public long getTimeOffset() {
        return mOffset;
    }


    @Override
    public long getServerTime() {
        return mServerTime;
    }

    @Override
    public void setServerTime(long serverTime) {
        long currentDeviceTime = new Date().getTime();
        Log.d(TAG, "currentDeviceTime :" + currentDeviceTime);
        Log.d(TAG, "serverTime :"+serverTime);
        mServerTime = serverTime;
        mOffset = (mServerTime - currentDeviceTime);

    }

    @Override
    public void setAccountBalance(Float accountBalance) {
        if(accountBalance != null){
            mAccountBalance = accountBalance;
        }
    }

    @Override
    public Float getAccountBalance() {

        if(mAccountBalance == null){
            mAccountBalance = -1f;
        }
        return mAccountBalance;
    }

    @Override
    public void setMarketPrice(String marketId, String marketPrice) {
        //key is market id, value is its price
        if(marketPrice != null){
            mMarketPrices.put(Integer.valueOf(marketId), Float.valueOf(marketPrice));
        }
    }

    @Override
    public float getMarketPrice(long marketId) {
        return mMarketPrices.get(Long.valueOf(marketId).intValue(), -1f);
    }


    /***
     *
     * @return TRUE: if country ISO is approved
     *  FALSE ELSE
     */

    public Boolean isCountryApproved() {
        return mCountryApproved;
    }

    public void setCountryApproved(boolean mCountryApproved) {
        this.mCountryApproved = mCountryApproved;
    }



    @Override
    public void setOpenBetsCount(int openBetsCount) {

        mOpenBetsCount = openBetsCount;
    }

    @Override
    public int getOpenBetsCount() {
        return mOpenBetsCount;
    }

    @Override
    public boolean hasUserSeenUpgradedAccountTutorial() {
        return hasUserUpgradedAccount;
    }

    @Override
    public void setUserHasSeenUpgradedAccountTutorial(boolean flag) {
        this.hasUserUpgradedAccount = flag;
    }

    @Override
    public void setOpenbetsCache(List<OpenBetDetails> openBetDetailsList) {


        if(openBetDetailsList != null && !openBetDetailsList.isEmpty()){

            if(mOpenbetsCache == null){
                mOpenbetsCache = new HashMap<Long, List<OpenBetDetails>>();
            }


            for(OpenBetDetails openBetDetails : openBetDetailsList){

                Long marketId = openBetDetails.getMarketId();

                if(mOpenbetsCache.containsKey(marketId)){
                    //key market id exists, check open bets against it
                    List<OpenBetDetails> currentOpenBetsList = mOpenbetsCache.get(marketId);

                    if(!currentOpenBetsList.contains(openBetDetails)){

                        //doest contain the open bet item, hence add to existing one
                        currentOpenBetsList.add(openBetDetails);
                        Log.d(TAG, "Added open bet :"+openBetDetails.getId()+", for market id:"+marketId);
                        break;//break the loop as we already added item to the market key

                    }

                }else{
                    //not found the market id, add it
                    List<OpenBetDetails> initialOpenbetsList = new ArrayList<>();
                    initialOpenbetsList.add(openBetDetails);
                    mOpenbetsCache.put(marketId, initialOpenbetsList);
                    Log.d(TAG, "First item , added open bet :"+openBetDetails.getId()+", for market id:"+marketId);
                }
            }

        }else{
            //list is null
            if(mOpenbetsCache != null){
                mOpenbetsCache.clear();
            }
        }
    }

    @Override
    public HashMap<Long, List<OpenBetDetails>> getOpenbetsCache() {
        return mOpenbetsCache;
    }

    @Override
    public void updateOpenbetsCacheForSettledItems(String marketId, String settledBetId) {

        if(mOpenbetsCache != null && marketId != null && settledBetId != null){

            Long marketID = Long.valueOf(marketId);

            if(mOpenbetsCache.containsKey(marketID)){
                //found market id
                List<OpenBetDetails> openBetDetailsList = mOpenbetsCache.get(marketID);
                boolean flag = false;
                int locationIndex = 0;
                for(int index = 0; index < openBetDetailsList.size(); index++){

                    OpenBetDetails openBetDetails = openBetDetailsList.get(index);
                    if(openBetDetails.getId() == Integer.valueOf(settledBetId)){
                        //found the settled bet
                        flag = true;
                        locationIndex = index;
                        break;
                    }
                }

                if(flag){
                    Log.d(TAG, "Removing settled bet id :"+settledBetId+" for market id :"+marketId);
                    openBetDetailsList.remove(locationIndex);
                }

                if(openBetDetailsList.size() == 0){
                    Log.d(TAG, "Removed all items for market id :"+marketId+", hence removing from cache");
                    mOpenbetsCache.remove(marketID);
                }
            }
        }
    }

    @Override
    public boolean isUpgradeRequired() {
        return requiredUpdate;
    }

    @Override
    public boolean isUpgradeRecommended() {
        return recommendUpdate;
    }

    @Override
    public void setUpgradeRequired(boolean type) {
        requiredUpdate = type;
    }

    @Override
    public void setUpgradeRecommended(boolean type) {
        recommendUpdate = type;
    }

    @Override
    public boolean isAnonymousUser() {
        return isAnonymousUser;
    }

    @Override
    public void setAnonymousUser(boolean isAnonymousUser) {
        this.isAnonymousUser = isAnonymousUser;
    }

    @Override
    public void setAttributionTrackerName(String attributionTrackerName) {
        mAttributionTrackerName =attributionTrackerName;
    }

    @Override
    public String getAttributionTrackerName() {
        return mAttributionTrackerName;
    }


    @Override
    public String getAttributionAdGroup() {
        return mAttributionAdGroup;
    }

    @Override
    public void setAttributionAdGroup(String attributionAdGroup) {
        mAttributionAdGroup = attributionAdGroup;
    }

    @Override
    public String getAttributionCampaign() {
        return mAttributionCampaign;
    }

    @Override
    public void setAttributionCampaign(String attributionCampaign) {
        mAttributionCampaign = attributionCampaign;
    }

    @Override
    public String getAttributionClickLabel() {
        return mAttributionClickLabel;
    }

    @Override
    public void setAttributionClickLabel(String attributionClickLabel) {
        mAttributionClickLabel = attributionClickLabel;
    }

    @Override
    public String getAttributionCreative() {
        return mAttributionCreative;
    }

    @Override
    public void setAttributionCreative(String attributionCreative) {
        mAttributionCreative = attributionCreative;
    }

    @Override
    public String getAttributionTrackerToken() {
        return mAttributionTrackerToken;
    }

    @Override
    public void setAttributionTrackerToken(String attributionTrackerToken) {
        mAttributionTrackerToken = attributionTrackerToken;
    }

    @Override
    public String getAttributionNetwork() {
        return mAttributionNetwork;
    }

    @Override
    public void setAttributionNetwork(String attributionNetwork) {
        mAttributionNetwork = attributionNetwork;
    }

    @Override
    public void setPromotionsUrl(String url) {
        promotions_url = url;
    }

    @Override
    public String getPromotionsUrl() {
        return promotions_url;
    }

    @Override
    public String getGoogleId() {
        return googleAdvertisingId;
    }

    @Override
    public void setGoogleId(String id) {
        googleAdvertisingId=id;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void setCurrencyIso(String currency) {
        this.currency = currency;
    }

    @Override
    public String getCurrencyIso() {
        return this.currency;
    }

    @Override
    public boolean isConfirmationNeeded() {
        return confirmationNeeded;
    }

    @Override
    public void setConfirmationNeeded(boolean confirmationNeeded) {
        this.confirmationNeeded = confirmationNeeded;
    }

    @Override
    public int getRealityCheckTime() {
        return mRealityCheckTime;
    }

    @Override
    public void setRealityCheckTime(int time) {
        mRealityCheckTime = time;
    }

    @Override
    public void setCountryIso(String country) {
        mUserCountry = country;
    }

    @Override
    public String getCountryIso() {
        return mUserCountry;
    }

    @Override
    public boolean isThereBalance() {
        return balanceReceived;
    }

    @Override
    public void setThereIsBalance(boolean thereIsBalance) {
        balanceReceived = thereIsBalance;
    }

    @Override
    public boolean areThereBets() {
        return betsReceived;
    }

    @Override
    public void setThereAreBets(boolean thereAreBets) {
        betsReceived = thereAreBets;
    }

    @Override
    public boolean hasUserSeenBettingTutorial() {
        return userSawNewAccountTutorial;
    }

    @Override
    public void setUserHasSeenBettingTutorial(boolean flag) {
        userSawNewAccountTutorial = flag;
    }

    @Override
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    @Override
    public void setVibrationEnabled(boolean enabled) {
        vibrationEnabled = enabled;
    }

    @Override
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @Override
    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Method to reset the entire the cache object
     */
    @Override
    public void resetCache(){
        //reset all the stored content here
        if(mAccountBalance!=null) {
            mAccountBalance = -1f;
        }
        if(mOpenbetsCache != null){
            mOpenbetsCache.clear();
        }
        setUserHasPlacedQuickBet(false);
        setUserHasSeenBettingTutorial(false);
        setUserHasSeenUpgradedAccountTutorial(false);
        setThereAreBets(false);
        setThereIsBalance(false);
        setUserId("-1");
    }

    @Override
    public int getMinutesBeforeVerificationRequired() {
        return minutesBeforeVerificationRequired;
    }

    @Override
    public void setMinutesBeforeVerificationRequired(int minutesBeforeVerificationRequired) {
        this.minutesBeforeVerificationRequired = minutesBeforeVerificationRequired;
    }
}
