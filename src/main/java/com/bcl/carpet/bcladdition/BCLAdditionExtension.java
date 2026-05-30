package com.bcl.carpet.bcladdition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class BCLAdditionExtension implements CarpetExtension {

    private static final BCLAdditionExtension INSTANCE = new BCLAdditionExtension();

    private BCLAdditionExtension() {}

    public static BCLAdditionExtension getInstance() {
        return INSTANCE;
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(BCLAdditionSettings.class);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        InputStream langFile = getClass().getClassLoader()
                .getResourceAsStream("assets/carpet-bcl-addition/lang/%s.json".formatted(lang));
        if (langFile == null) {
            return Collections.emptyMap();
        }
        try (InputStreamReader reader = new InputStreamReader(langFile, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setLenient().create();
            return gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
