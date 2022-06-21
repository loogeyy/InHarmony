//package com.example.inharmony;
//
//import android.content.Context;
//
//import com.codepath.asynchttpclient.RequestParams;
//import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
//import com.codepath.oauth.OAuthBaseClient;
//import com.github.scribejava.core.builder.api.BaseApi;
//
//public class SpotifyClient extends OAuthBaseClient {
//    //https://api.spotify.com/v1
//    public static final BaseApi REST_API_INSTANCE = TwitterApi.instance(); // Change this
//    public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
//    public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;       // Change this inside apikey.properties
//    public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET; // Change this inside apikey.properties
//
//    // Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
//    public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";
//
//    // See https://developer.chrome.com/multidevice/android/intents
//    public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";
//
//    public SpotifyClient(Context context) {
//        super(context, REST_API_INSTANCE,
//                REST_URL,
//                REST_CONSUMER_KEY,
//                REST_CONSUMER_SECRET,
//                null,  // OAuth2 scope, null for OAuth1
//                String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
//                        context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
//    }
//    // Obtain the home timeline from the Twitter API
//    public void getHomeTimeline(JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/home_timeline.json");
//        // Can specify query string params directly or through RequestParams.
//        RequestParams params = new RequestParams();
//        params.put("count", 25);
//        params.put("since_id", 1);
//        params.put("tweet_mode", "extended");
//        client.get(apiUrl, params, handler);
//    }
//
//    public void publishTweet(String tweetContext, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/update.json");
//        RequestParams params = new RequestParams();
//        params.put("tweet_mode", "extended");
//        params.put("status", tweetContext);
//        params.put("since_id", 1);
//        client.post(apiUrl, params, "", handler);
//    }
//
//    public void getHomeTimelineEndless(String max_id, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/home_timeline.json");
//        RequestParams params = new RequestParams();
//        params.put("tweet_mode", "extended");
//        params.put("count", 25);
//        params.put("max_id", max_id);
//        client.get(apiUrl, params, handler);
//    }
//
//    public void publishReply(String tweetContext, String inReplyToStatusId, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/update.json");
//        RequestParams params = new RequestParams();
//        params.put("status", tweetContext);
//        params.put("in_reply_to_status_id", inReplyToStatusId);
//        client.post(apiUrl, params, "", handler);
//    }
//
//    public void publishRetweet(String id, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/retweet/" + id + ".json");
//        RequestParams params = new RequestParams();
//        params.put("id", id);
//        client.post(apiUrl, params, "", handler);
//    }
//
//    public void unRetweet(String id, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("statuses/unretweet/" + id + ".json");
//        RequestParams params = new RequestParams();
//        params.put("id", id);
//        client.post(apiUrl, params, "", handler);
//    }
//
//    public void favorite(String id, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("favorites/create.json");
//        RequestParams params = new RequestParams();
//        params.put("id", id);
//        client.post(apiUrl, params, "", handler);
//    }
//
//    public void unfavorite(String id, JsonHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("favorites/destroy.json");
//        RequestParams params = new RequestParams();
//        params.put("id", id);
//        client.post(apiUrl, params, "", handler);
//    }
//}
