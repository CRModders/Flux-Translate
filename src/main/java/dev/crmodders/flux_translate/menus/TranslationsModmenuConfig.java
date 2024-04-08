package dev.crmodders.flux_translate.menus;

import org.coolcosmos.modmenu.api.ConfigScreenFactory;
import org.coolcosmos.modmenu.api.ModMenuApi;

public class TranslationsModmenuConfig implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TranslationsOptionMenu::new;
    }
}