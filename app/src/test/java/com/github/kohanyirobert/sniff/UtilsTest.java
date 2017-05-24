package com.github.kohanyirobert.sniff;

import org.junit.Test;

import static com.github.kohanyirobert.sniff.Utils.isApiKeyValid;
import static com.github.kohanyirobert.sniff.Utils.isApiUrlValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    private static final String VALID_API_URL = "https://4pk2ep3glo.execute-api.us-east-1.amazonaws.com/test";
    private static final String VALID_API_KEY = "VzXyJ3VjMazRYhuCZGFTFhmxhEqNWQAxr9babARi";

    private static final String INVALID_API_URL = "https://1234.execute-api.us-dog-2.amazonaws.biz/test";
    private static final String INVALID_API_KEY = "Cr9sxU1";

    @Test
    public void testApiUrlValid_whenApiUrlValid_shouldReturnTrue() {
        assertTrue(isApiUrlValid(VALID_API_URL));
    }

    @Test
    public void testApiUrlValid_whenInvalidApiUrl_shouldReturnFalse() {
        assertFalse(isApiUrlValid(INVALID_API_URL));
    }

    @Test
    public void testApiKeyValid_whenValidApiKey_shouldReturnTrue() {
        assertTrue(isApiKeyValid(VALID_API_KEY));
    }

    @Test
    public void testApiKeyValid_whenInvalidApiKey_shouldReturnFalse() {
        assertFalse(isApiKeyValid(INVALID_API_KEY));
    }
}
