package com.flocktory.pushdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Key;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.flocktory.pushdemo.MainActivity.log;

class FlocktoryApiClient {

    private static final String FL_PROFILE_STRING_KEY = "fl-profile";
    private static final String FL_WEB_SESSION_STRING_KEY = "fl-web-session";
    private static final String FL_SITE_SESSION_STRING_KEY = "fl-site-session";
    private static final String SHARED_PREFS_FILE_KEY = "fl-shared-prefs";

    private static final String FL_WEB_SESSION_COOKIE_NAME = "__flocktory-web_session2";
    private static final String FL_SITE_SESSION_FIELD_NAME = "site-session-id";

    private static final String FL_SITE_ID = ""; // fixme
    private static final String GCM_SENDER_ID = ""; // fixme
    private static final String CLIENT_API_TOKEN = ""; // для тестовой отправки fixme

    private static String pushSubscribedAddr = "https://api.flocktory.com/u_flockman/attach-push-to-session.js";
    private static String sendTestPushAddr = "https://client.flocktory.com/v2/push/send-one/";
    private static String setupApiAddr = "https://api.flocktory.com/u_shaman/setup-api.js";
    private static String gcmTokenPrefix = "https://android.googleapis.com/gcm/send/";

    private Context context;

    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static HttpRequestFactory REQUEST_FACTORY = HTTP_TRANSPORT.createRequestFactory();

    FlocktoryApiClient(Context c) {
        this.context  = c;
    }

    private class PushSubscribedUrl extends GenericUrl {
        @Key
        String body;
        @Key
        public String callback = "flock_jsonp";

        PushSubscribedUrl() {
            super(pushSubscribedAddr);
        }
    }

    private class SendTestPushUrl extends GenericUrl {
        @Key
        String token;

        SendTestPushUrl() {
            super(sendTestPushAddr);
        }
    }

    private class SetupApiUrl extends GenericUrl {
        @Key
        public String body;
        @Key
        public String callback = "flock_jsonp";

        public SetupApiUrl() {
            super(setupApiAddr);
        }
    }

    private String pushSubscribe(String token, String siteId, String gcmSenderId) throws JSONException, IOException {

        log("starting push subscription...");

        PushSubscribedUrl url = new PushSubscribedUrl();

        JSONObject bodyJson = new JSONObject()
                .put("from-mobile-app", true)
                .put("platform", "firebase")
                .put("os", "android")
                .put("site-id", FL_SITE_ID)
                .put("token", gcmTokenPrefix + token)
                .put("provider-meta", new JSONObject().put("gcm-sender-id", gcmSenderId));
        // !!
        insertSiteSession(bodyJson);

        url.body = bodyJson.toString();

        HttpRequest req = REQUEST_FACTORY.buildGetRequest(url);
        // !!
        insertCookie(req);
        HttpResponse resp = req.execute();

        String respString = resp.parseAsString();
        log("status code:" + resp.getStatusCode());

        if (!respString.contains("message")) { // fixme
            log("regtoken response " + respString);

            // пример ожидаемого ответа
            // flock_jsonp_3({"id":"5de015dc-560e-483d-91f1-1ef676a10869"})

            return respString.split(":\"")[1].split("\"")[0]; // fixme maybe
        } else {
            log("FL REQUEST FAILED WITH RESPONSE: " + respString);
            return null;
        }
    }

    private void executeInitRequest() throws JSONException, IOException {
        SetupApiUrl url = new SetupApiUrl();
        JSONObject body = new JSONObject()
                .put("siteId", FL_SITE_ID);
        url.body = body.toString();
        HttpRequest req = REQUEST_FACTORY.buildGetRequest(url);

        HttpResponse resp = req.execute();
        retrieveCookie(resp);
        retrieveSiteSession(resp);
    }

    public void sendTestPush (String profileId,
                              String landingUrl,
                              String iconUrl,
                              String subject,
                              String body)
            throws JSONException, IOException {
        SendTestPushUrl url  = new SendTestPushUrl();
        url.token = CLIENT_API_TOKEN;

        String apiPushProfileString = "p" + profileId;

        JSONObject bodyJSON = new JSONObject()
                .put("site_id", FL_SITE_ID)
                .put("profile_id", apiPushProfileString)
                .put("landing_url", landingUrl)
                .put("icon_url", iconUrl)
                .put("subject", subject)
                .put("body", body)
                //.put("rich_image_url", richImage)

                // optional custom data
                .put("extra_data", new JSONObject().put("k1", "v1"));

        String content = bodyJSON.toString();

        HttpRequest req = REQUEST_FACTORY.buildPostRequest(url,new ByteArrayContent("application/json", content.getBytes()));
        HttpResponse resp = req.execute();
        //String respString = resp.parseAsString();
        //log("Status Code:" + resp.getStatusCode());
        //log("Response: " + respString);
    }

    public void regToken (String token) {
        try {
            log("Registering a token: " + token);
            String profile = pushSubscribe(token, FL_SITE_ID, GCM_SENDER_ID);
            if (profile != null) {
                savePushProfile(profile);
                log("... SUCCESS!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log("FAILED FL SUBSCRIPTION: " + e.getMessage());
        }
    }

    public Bitmap getPicture(String urlStr) {
        try {
            GenericUrl url = new GenericUrl(urlStr);
            HttpRequest req = REQUEST_FACTORY.buildGetRequest(url);
            insertCookie(req);

            HttpResponse resp = req.execute();

            return BitmapFactory.decodeStream(resp.getContent());

        } catch (Exception e) {
            log("error while getting a picture");
            // вот так вот
            return null;
        }

    }

    // этот метод нужен на случай, если лендинг производится не по landing url, а прямиком в приложение
    // принимает landingUrl
    // трекинг клика
    public void trackLanding(String urlStr) {
        try {
            GenericUrl url = new GenericUrl(urlStr);
            HttpRequest req = REQUEST_FACTORY.buildGetRequest(url);
            insertCookie(req);

            req.execute();
        } catch (Exception ignored) {}
    }

    // работа с кукой и сайт сессией в реквестах и json body
    private void insertCookie (HttpRequest req) {
        String savedWebSession  = getWebSessionId();

        if (savedWebSession != null) {
            log("existing web session: " + savedWebSession);
            HttpHeaders h = new HttpHeaders();
            h.setCookie(FL_WEB_SESSION_COOKIE_NAME + "=" + savedWebSession);
            req.setHeaders(h);
        } else {
            log("no existing flocktory web session found");
        }

    }

    private void retrieveCookie (HttpResponse resp) {
        String setCookieString =
                (String) resp
                        .getHeaders()
                        .getFirstHeaderStringValue("set-cookie");

        if (setCookieString != null) {
            String receivedWebSession = setCookieString.split("=")[1];
            log("received flocktory web session " + receivedWebSession);
            saveWebSessionId(receivedWebSession);
        } else {
            log("no setcookie header received");
        }

    }

    private void retrieveSiteSession (HttpResponse resp) throws JSONException, IOException {
        /*
         *  ожидается ответ вида
         *  something({... , "site-session-id":"..." , ...})
         */
        String respString = resp.parseAsString();
        // Log.d("FL INIT",respString);
        Pattern p = Pattern.compile("^[^\\(]*\\((.*)\\)$");
        Matcher m = p.matcher(respString);
        m.find();
        JSONObject respJson = new JSONObject(m.group(1));
        String siteSession = (String) respJson.get(FL_SITE_SESSION_FIELD_NAME);
        if (siteSession != null) {
            saveKeyValue(FL_SITE_SESSION_STRING_KEY, siteSession);
            log("received site session: " + siteSession);
        }
    }

    private void insertSiteSession (JSONObject j) {
        String siteSession = getSiteSessionId();
        if (siteSession == null) {
            log("could not insert site session id since it is null");
        } else {
            try {
                j.put(FL_SITE_SESSION_FIELD_NAME, siteSession);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // работа с shared preferences, сохранение и получение данных
    private void saveKeyValue (String k, String s) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPref.edit();
        e.putString(k, s);
        e.apply();
    }

    private String getKeyValue (String k) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFS_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPref.getString(k, null);
    }

    private void savePushProfile (String s) {
        saveKeyValue(FL_PROFILE_STRING_KEY, s);
    }

    public String getPushProfile() {
        return getKeyValue(FL_PROFILE_STRING_KEY);
    }

    public void saveWebSessionId(String s) {
        saveKeyValue(FL_WEB_SESSION_STRING_KEY, s);
    }

    public String getWebSessionId() {
        return getKeyValue(FL_WEB_SESSION_STRING_KEY);
    }

    public String getSiteSessionId() {
        String v = getKeyValue(FL_SITE_SESSION_STRING_KEY);
        if (v == null) {
            log("no session id found, preforming an init request");
            try {
                executeInitRequest();
            } catch (Exception e) {
                log("exception while performing init request with message: " + e.getMessage());
            }
            v = getKeyValue(FL_SITE_SESSION_STRING_KEY);
        } else {
            log("existing site session: " +  v);
        }
        return v;
    }

    public String getSavedSiteSessionId() {
        return getKeyValue(FL_SITE_SESSION_STRING_KEY);
    }

}
