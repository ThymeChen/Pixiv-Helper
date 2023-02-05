package cn.thymechen.xiaoming.plugin.utils;

import okhttp3.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Http 工具，用于快速发送 GET/POST 请求
 *
 * @author ThymeChen
 */
@SuppressWarnings("unused")
public class HttpUtil {

    private static final String NULL = "null";

    /**
     * 发送 GET 请求
     *
     * @param url 请求地址
     * @return 字符串
     * @throws IOException
     */
    public static String get(String url) throws IOException {
        return get(url, new HashMap<>(), new HashMap<>());
    }

    /**
     * 发送 GET 请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 字符串
     * @throws IOException
     */
    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, new HashMap<>(), params);
    }

    /**
     * 发送 GET 请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 字符串
     * @throws IOException
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        StringBuilder result = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(getAsStream(url, headers, params)));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public static InputStream getAsStream(String url) throws IOException {
        return getAsStream(url, new HashMap<>(), new HashMap<>());
    }

    public static InputStream getAsStream(String url, Map<String, String> params) throws IOException {
        return getAsStream(url, new HashMap<>(), params);
    }

    public static InputStream getAsStream(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        InputStream result;

        if (params.size() > 0 && params.keySet().stream().noneMatch(String::isBlank)) {
            StringBuilder sb = new StringBuilder(url);
            sb.append("?");
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
            url = sb.deleteCharAt(sb.length() - 1).toString();
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();
        Call call = client.newCall(request);

        Response response = call.execute();
        result = new ByteArrayInputStream(response.body().bytes());
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

        if (params.size() > 0 && params.keySet().stream().noneMatch(String::isEmpty)) {
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
        }

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
