package io.github.agentrkid.gamerdisguise.util;

import java.lang.reflect.Field;

public class ModifierUtil {
    public static Field MODIFIER_FIELDS;

    public static Field changeModifiers(Field key, int... modifiers) {
        try {
            // Always make accessible
            key.setAccessible(true);

            for (int modifier : modifiers) {
                MODIFIER_FIELDS.set(key, key.getModifiers() & ~modifier);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return key;
    }

    static {
        try {
            MODIFIER_FIELDS = Field.class.getDeclaredField("modifiers");
            MODIFIER_FIELDS.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
