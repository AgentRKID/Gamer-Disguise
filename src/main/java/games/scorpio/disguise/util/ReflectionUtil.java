package games.scorpio.disguise.util;

import games.scorpio.disguise.GamerDisguise;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class ReflectionUtil {

    private static Field MODIFIER_FIELDS;

    public static Field setModifiers(Field key, int... modifiers) {
        try {
            key.setAccessible(true);

            for (int modifier : modifiers) {
                MODIFIER_FIELDS.set(key, key.getModifiers() & ~modifier);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return key;
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, params);
            declaredMethod.setAccessible(true);
            return declaredMethod;
        } catch (Exception e) {
            return null;
        }
    }

    public static Method getMethodSuppressed(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method method = clazz.getMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }


    public static Class<?> getClassSuppressed(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            return null;
        }
    }


    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T get(Field field, Object object) {
        try {
            return (T) field.get(object);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void set(Field field, Object clazz, Object value) {
        try {
            field.set(clazz, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <T> T newInstance(Class<?> clazz, Object... params) {
        try {
            return (T) clazz.getConstructor(ListUtil.toArray(ListUtil.toClasses(params))).newInstance(params);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T newInstance(Constructor<?> constructor, Object... params) {
        try {
            return (T) constructor.newInstance(params);
        } catch (Exception ex) {
            GamerDisguise.getInstance().getLogger().severe("Failed to create new instance using constructor and params.");
            ex.printStackTrace();
            return null;
        }
    }

    public static <T> T cast(Object object) {
        return (T) object;
    }

    static  {
        try {
            MODIFIER_FIELDS = Field.class.getDeclaredField("modifiers");
            MODIFIER_FIELDS.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
