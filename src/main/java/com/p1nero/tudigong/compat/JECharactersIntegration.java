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

    public static <T> boolean matches(T ingredient, IIngredientHelper<T> ingredientHelper, String query) {
        initialize();
        String name = ingredientHelper.getDisplayName(ingredient);
        if (jecharactersLoaded && matchesMethod != null) {
            try {
                return (boolean) matchesMethod.invoke(null, name, query);
            } catch (Exception e) {
                // If reflection fails, fall back to simple contains check
                return name.toLowerCase().contains(query.toLowerCase());
            }
        }
        return name.toLowerCase().contains(query.toLowerCase());
    }

    public static boolean isLoaded() {
        initialize();
        return jecharactersLoaded;
    }
}
