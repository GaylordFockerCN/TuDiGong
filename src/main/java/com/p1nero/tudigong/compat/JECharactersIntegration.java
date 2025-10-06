package com.p1nero.tudigong.compat;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public class JECharactersIntegration {
    private static final Logger LOGGER = LogManager.getLogger();

    private static Method matchesMethod = null;
    private static boolean jecharactersLoaded = false;
    private static boolean initialized = false;

    private static void initialize() {
        if (initialized) {
            return;
        }
        jecharactersLoaded = ModList.get().isLoaded("jecharacters");
        if (jecharactersLoaded) {
            try {
                Class<?> matchClass = Class.forName("me.towdium.jecharacters.utils.Match");
                matchesMethod = matchClass.getMethod("matches", String.class, String.class);
            } catch (Exception e) {
                LOGGER.warn("Failed to reflectively access JECharacters", e);
                jecharactersLoaded = false;
            }
        }
        initialized = true;
    }

    public static boolean match(String text, String query) {
        initialize();
        if (jecharactersLoaded && matchesMethod != null) {
            try {
                return (boolean) matchesMethod.invoke(null, text, query);
            } catch (Exception e) {
                LOGGER.error("Failed to invoke JECharacters match method", e);
                return text.toLowerCase().contains(query.toLowerCase());
            }
        }
        return text.toLowerCase().contains(query.toLowerCase());
    }

    public static <T> boolean matches(T ingredient, IIngredientHelper<T> ingredientHelper, String query) {
        String name = ingredientHelper.getDisplayName(ingredient);
        return match(name, query);
    }

    public static boolean isLoaded() {
        initialize();
        return jecharactersLoaded;
    }
}
