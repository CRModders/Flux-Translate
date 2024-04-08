package dev.crmodders.flux_translate.menus;

import dev.crmodders.flux.FluxSettings;
import dev.crmodders.flux.api.gui.SteppedIntSliderElement;
import dev.crmodders.flux.api.gui.SwitchGameStateButtonElement;
import dev.crmodders.flux.localization.TranslationKey;
import dev.crmodders.flux.menus.LanguagePickerMenu;
import dev.crmodders.flux.menus.LayoutMenu;
import finalforeach.cosmicreach.gamestates.GameState;

public class TranslationsOptionMenu extends LayoutMenu {

    public static final TranslationKey TEXT_MSAA = new TranslationKey("fluxapi:flux_options.msaa");
    public static final TranslationKey TEXT_LANGUAGES = new TranslationKey("fluxapi:flux_options.languages");
    private static final int[] MSAA_STEPS = new int[]{0, 2, 4, 8, 16};

    public TranslationsOptionMenu(GameState previousState) {
        super(previousState);
        SteppedIntSliderElement msaa = new SteppedIntSliderElement(MSAA_STEPS, FluxSettings.AntiAliasing);
        msaa.translation = TEXT_MSAA;
        msaa.updateText();
        this.addFluxElement(msaa);
        SwitchGameStateButtonElement language = new SwitchGameStateButtonElement(() -> {
            return new LanguagePickerMenu(GameState.currentGameState);
        });
        language.translation = TEXT_LANGUAGES;
        language.updateText();
        this.addFluxElement(language);
        this.setLayoutEnabled(false);
        this.addDoneButton();
    }
}
