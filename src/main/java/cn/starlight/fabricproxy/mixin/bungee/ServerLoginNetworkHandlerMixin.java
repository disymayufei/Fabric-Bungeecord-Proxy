package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.FabricProxy;
import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import cn.starlight.fabricproxy.mixin.GameProfileAccessor;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Unique
    private boolean bypassProxyBungee = false;

    @Shadow @Final
    ClientConnection connection;

    @Shadow @Final
    MinecraftServer server;

    @Shadow
    private GameProfile profile;

    @Inject(method = "startVerify", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;profile:Lcom/mojang/authlib/GameProfile;", shift = At.Shift.AFTER))
    private void initUuid(CallbackInfo ci) {
        try {
            if (!FabricProxy.config.getBungeeCord()) return;

            if (!(connection instanceof BungeeClientConnection)) {
                return;
            }

            BungeeClientConnection bcc = (BungeeClientConnection) connection;
            UUID spoofedUUID = bcc.getSpoofedUUID();
            Collection<Property> spoofedProperties = bcc.getSpoofedProperties();

            if (spoofedUUID == null) {
                return;
            }

            // Maak het nieuwe profiel
            GameProfile newProfile = new GameProfile(spoofedUUID, this.profile.name());

            // Verwerk skins/properties
            if (spoofedProperties != null && !spoofedProperties.isEmpty()) {

                // Maak de mutable map
                Multimap<String, Property> internalMap = LinkedHashMultimap.create();
                for (Property prop : spoofedProperties) {
                    internalMap.put(prop.name(), prop);
                }

                // Maak de PropertyMap
                PropertyMap newPropertyMap = new PropertyMap(internalMap);

                // Injecteer via de Accessor (met dubbele cast voor de compiler)
                ((GameProfileAccessor) (Object) newProfile).setProperties(newPropertyMap);
            }

            // Wijs het nieuwe profiel toe aan het veld
            this.profile = newProfile;

        } catch (Throwable t) {
            // Alleen errors loggen als het echt misgaat
            t.printStackTrace();
        }
    }

    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean skipKeyPacket(MinecraftServer minecraftServer) {
        return (bypassProxyBungee || !FabricProxy.config.getBungeeCord()) && minecraftServer.isOnlineMode();
    }
}