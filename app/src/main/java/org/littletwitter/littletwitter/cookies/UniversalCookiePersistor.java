package org.littletwitter.littletwitter.cookies;

import android.content.Context;
import android.content.SharedPreferences;

import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;
import com.franmontiel.persistentcookiejar.persistence.SerializableCookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

public class UniversalCookiePersistor implements CookiePersistor {

    private final SharedPreferences sharedPreferences;

    public UniversalCookiePersistor(Context context, String sharedPrefsName) {
        this.sharedPreferences = context.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
    }

    @Override
    public List<Cookie> loadAll() {
        List<Cookie> cookies = new ArrayList<>(sharedPreferences.getAll().size());

        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            String serializedCookie = (String) entry.getValue();
            Cookie cookie = new SerializableCookie().decode(serializedCookie);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    @Override
    public void saveAll(Collection<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.putString(cookieKey(cookie), new SerializableCookie().encode(cookie));
        }
        editor.apply();
    }

    @Override
    public void removeAll(Collection<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.remove(cookieKey(cookie));
        }
        editor.apply();
    }

    // As our app accesses only one domain that too in http only
    // the cookie name itself would suffice as a cookie key
    // for saving from response and loading to request
    private static String cookieKey(Cookie cookie) {
        return cookie.name();
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
