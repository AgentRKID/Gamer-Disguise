package io.github.agentrkid.gamerdisguise.manager;

import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DisguiseData {
    private final GameProfile originalGameProfile;
    private final String originalPlayerListName;
    private final String originalPlayerDisplayName;
}
