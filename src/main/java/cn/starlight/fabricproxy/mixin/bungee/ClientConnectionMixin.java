package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements BungeeClientConnection {
    private UUID spoofedUUID;
    private Property[] spoofedProfile;

    @Override
    public UUID getSpoofedUUID() {
        return this.spoofedUUID;
    }

    @Override
    public void setSpoofedUUID(UUID uuid) {
        this.spoofedUUID = uuid;
    }

    @Override
    public Property[] getSpoofedProfile() {
        return this.spoofedProfile;
    }

    @Override
    public void setSpoofedProfile(Property[] profile) {
        this.spoofedProfile = profile;
    }
}
