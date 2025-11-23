package cn.starlight.fabricproxy.mixin; // Pas aan naar jouw package structuur

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameProfile.class)
public interface GameProfileAccessor {
    @Accessor("properties")
    @Mutable // DIT IS DE SLEUTEL: Dit vertelt Mixin om de 'final' weg te halen
    void setProperties(PropertyMap properties);
}