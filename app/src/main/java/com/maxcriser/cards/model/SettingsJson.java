package com.maxcriser.cards.model;

import com.maxcriser.cards.constant.ListConstants;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsJson {

    private String bodyShare = ListConstants.EMPTY_STRING;
    private String appVersion = ListConstants.EMPTY_STRING;
    private String googlePlayUrl = ListConstants.EMPTY_STRING;
    private String message = ListConstants.EMPTY_STRING;
    private String vkUrlGroup = ListConstants.EMPTY_STRING;
    private String facebookUrlGroup = ListConstants.EMPTY_STRING;
    private String googleUrlGroup = ListConstants.EMPTY_STRING;
    private boolean flagVersion;
    private boolean flagMessage;
    private boolean flagMessageUrl;
    private String titleMessage;
    private String urlMessage;

    public void setFlagMessageUrl(boolean pFlagMessageUrl) {
        flagMessageUrl = pFlagMessageUrl;
    }

    public boolean isFlagMessageUrl() {
        return flagMessageUrl;
    }

    public void setVkUrlGroup(final String pVkUrlGroup) {
        vkUrlGroup = pVkUrlGroup;
    }

    public void setFacebookUrlGroup(final String pFacebookUrlGroup) {
        facebookUrlGroup = pFacebookUrlGroup;
    }

    public void setGoogleUrlGroup(final String pGoogleUrlGroup) {
        googleUrlGroup = pGoogleUrlGroup;
    }

    public String getVkUrlGroup() {
        return vkUrlGroup;
    }

    public String getFacebookUrlGroup() {
        return facebookUrlGroup;
    }

    public String getGoogleUrlGroup() {
        return googleUrlGroup;
    }

    public void setFlagMessage(final boolean pFlagMessage) {
        flagMessage = pFlagMessage;
    }

    public boolean isFlagMessage() {
        return flagMessage;
    }

    public String getTitleMessage() {
        return titleMessage;
    }

    public void setTitleMessage(final String pTitleMessage) {
        titleMessage = pTitleMessage;
    }

    public void setUrlMessage(String pUrlMessage) {
        urlMessage = pUrlMessage;
    }

    public String getUrlMessage() {
        return urlMessage;
    }

    public SettingsJson(final String json) {
        final JSONObject dataJsonObj;
        try {
            dataJsonObj = new JSONObject(json);

            final String body_share = "body_share";
            final String google_play_url = "google_play_url";
            final String appVersion = "app_version";
            final String flag_message = "flag_version";
            final String vkGroup = "group_vk_url";
            final String facebookGroup = "group_facebook_url";
            final String googlePlusGroup = "group_google_plus_url";
            final String message = "message";
            final String flagMessage = "flag_message";
            final String titleMessage = "message_title";
            final String urlMessage = "message_url";
            final String urlMessageFlag = "message_url_flag";

            setUrlMessage(dataJsonObj.getString(urlMessage));
            setFlagMessageUrl(dataJsonObj.getBoolean(urlMessageFlag));
            setMessage(dataJsonObj.getString(message));
            setTitleMessage(dataJsonObj.getString(titleMessage));
            setFlagMessage(dataJsonObj.getBoolean(flagMessage));
            setBodyShare(dataJsonObj.getString(body_share));
            setGooglePlayUrl(dataJsonObj.getString(google_play_url));
            setFlagVersion(dataJsonObj.getBoolean(flag_message));
            setAppVersion(dataJsonObj.getString(appVersion));
            setVkUrlGroup(dataJsonObj.getString(vkGroup));
            setVkUrlGroup(dataJsonObj.getString(vkGroup));
            setGoogleUrlGroup(dataJsonObj.getString(googlePlusGroup));
            setFacebookUrlGroup(dataJsonObj.getString(facebookGroup));

        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBodyShare() {
        return bodyShare;
    }

    public String getGooglePlayUrl() {
        return googlePlayUrl;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFlagVersion() {
        return flagVersion;
    }

    private void setAppVersion(final String pAppVersion) {
        appVersion = pAppVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    private void setBodyShare(final String pBodyShare) {
        bodyShare = pBodyShare;
    }

    private void setGooglePlayUrl(final String pGooglePlayUrl) {
        googlePlayUrl = pGooglePlayUrl;
    }

    public void setMessage(final String pMessage) {
        message = pMessage;
    }

    private void setFlagVersion(final boolean pFlagVersion) {
        flagVersion = pFlagVersion;
    }
}
