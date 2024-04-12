package dev.crmodders.flux_translate;

import com.badlogic.gdx.Gdx;
import dev.crmodders.flux.FluxSettings;
import dev.crmodders.flux.api.events.GameEvents;
import dev.crmodders.flux.localization.LanguageFile;
import dev.crmodders.flux.localization.TranslationApi;
import dev.crmodders.flux.logging.LogWrapper;
import finalforeach.cosmicreach.io.SaveLocation;
import net.fabricmc.api.ModInitializer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FluxTranslations implements ModInitializer {

    private static String TAG = "\u001B[35;1m{Flux-Translate}\u001B[0m\u001B[37m:";
    public static File LangDir = new File(SaveLocation.getSaveFolderLocation() + "/langs");
    @Override
    public void onInitialize() {
        LogWrapper.info("Flux Translate Loaded");

        if (!LangDir.exists()) LangDir.mkdirs();

        GameEvents.ON_GAME_INITIALIZED.register(() -> {
            for (File file : Objects.requireNonNull(LangDir.listFiles())) {
                try {
                    TranslationApi.registerLanguageFile(
                            LanguageFile.loadLanguageFile(Gdx.files.absolute(file.getAbsolutePath()))
                    );
                } catch (IOException e) {
                    LogWrapper.info("%s: Could Not Access Language File \"%s\"".formatted(TAG, file.getAbsolutePath()));
                }
            }

            TranslationApi.setLanguage(FluxSettings.LanguageSetting.getValue());
        });
    }
}
