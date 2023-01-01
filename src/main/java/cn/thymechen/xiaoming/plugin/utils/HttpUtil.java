package cn.thymechen.xiaoming.plugin.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class HttpUtil {
    public static String get(String url) throws IOException {
        return get(url, new HashMap<>(), new HashMap<>());
    }

    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, new HashMap<>(), params);
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        String result;

        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
//        url = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
        url = sb.toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        result = response.body().string();
        response.close();

        return result;
    }

    public static String post(String url) throws IOException {
        return post(url, new HashMap<>(), new HashMap<>());
    }

    public static String post(String url, Map<String, String> params) throws IOException {
        return post(url, new HashMap<>(), params);
    }

    public static String post(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        String result;

        FormBody.Builder builder = new FormBody.Builder();

        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }

//        url = URLEncoder.encode(url, StandardCharsets.UTF_8);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(builder.build())
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        result = response.body().string();
        response.close();

        return result;
    }
}
