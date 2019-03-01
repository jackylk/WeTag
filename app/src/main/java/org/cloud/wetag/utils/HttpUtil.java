package org.cloud.wetag.utils;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

  private static MediaType JSON = MediaType.get("application/json; charset=utf-8");
  private static OkHttpClient client = new OkHttpClient();

  public static void postAsync(String url, String json, Callback callback) {
    RequestBody body = RequestBody.create(JSON, json);
    final Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();
    client.newCall(request).enqueue(callback);
  }

  public static Response getSync(String url, String token) throws IOException {
    final Request request = new Request.Builder()
        .url(url)
        .addHeader("X-Auth-Token", token)
        .get()
        .build();
    return client.newCall(request).execute();
  }

}
