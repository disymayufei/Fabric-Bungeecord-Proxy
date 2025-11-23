package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.UUID;
import com.mojang.authlib.properties.Property;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements BungeeClientConnection {

    @Unique
    private UUID spoofedUUID;

    @Unique
    private Collection<Property> spoofedProperties; // Type is nu Collection<Property>

    @Unique
    private SocketAddress spoofedAddress;

    @Override
    public UUID getSpoofedUUID() {
        return this.spoofedUUID;
    }

    @Override
    public Collection<Property> getSpoofedProperties() {
        return this.spoofedProperties;
    }

    @Override
    public SocketAddress getSpoofedAddress() {
        return this.spoofedAddress;
    }

    @Override
    public void setSpoofedUUID(UUID uuid) {
        this.spoofedUUID = uuid;
    }

    @Override
    public void setSpoofedProperties(Collection<Property> properties) { // Handtekening is nu Collection<Property>
        this.spoofedProperties = properties;
    }

    @Override
    public void setSpoofedAddress(SocketAddress address) {
        this.spoofedAddress = address;
    }

    @Inject(method = "channelActive", at = @At("TAIL"))
    private void onChannelActive(CallbackInfo ci) {
        System.out.println("[FabricProxy-SKIN] ClientConnection active. Has spoofed UUID? " + (spoofedUUID != null));
    }
}