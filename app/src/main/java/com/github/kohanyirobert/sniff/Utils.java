package com.github.kohanyirobert.sniff;

public final class Utils {

    private static final String API_URL_PATTERN = "^https://[a-z0-9]{10}\\.execute-api\\.[a-z0-9-]+\\.amazonaws\\.com/[a-z0-9]*$";
    private static final String API_KEY_PATTERN = "^[a-zA-Z0-9]{40}$";

    public static boolean isApiUrlValid(String apiUrl) {
        return apiUrl.matches(API_URL_PATTERN);
    }

    public static boolean isApiKeyValid(String apiKey) {
        return apiKey.matches(API_KEY_PATTERN);
    }
}
