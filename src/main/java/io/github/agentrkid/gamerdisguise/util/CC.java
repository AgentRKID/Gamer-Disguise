package io.github.agentrkid.gamerdisguise.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CC {
    public static String[] translate(String... strings) {
        List<String> stringList = new ArrayList<>(Arrays.asList(strings));
        String[] str = new String[stringList.size()];
        for (int i = 0; i < stringList.size(); i++) {
            str[i] = translate(stringList.get(i));
        }
        return str;
    }

    public static String translate(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
