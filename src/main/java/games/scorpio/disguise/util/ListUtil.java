package games.scorpio.disguise.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class ListUtil {

    public static <E> List<E> newArrayList() {
        return new ArrayList<>();
    }

    public static <E> List<E> newConcurrentList() {
        return new CopyOnWriteArrayList<>();
    }

    public static <E> E findFirstMatch(List<E> list, Function<E, Boolean> function) {
        for (E value : list) {
            boolean passed = function.apply(value);

            if (passed) {
                return value;
            }
        }
        return null;
    }

    public static List<Class<?>> toClasses(Object... objects) {
        List<Class<?>> list = newArrayList();

        for (Object object : objects) {
            list.add(object.getClass());
        }
        return list;
    }

    @SuppressWarnings("ALL")
    public static <T> T[] toArray(List<?> list) {
        return (T[]) list.toArray();
    }

}
