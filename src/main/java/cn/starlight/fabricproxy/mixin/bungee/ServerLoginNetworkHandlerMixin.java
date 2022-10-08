package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.FabricProxy;
import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    private boolean bypassProxyBungee = false;
    @Shadow
    @Final
    public ClientConnection connection;
    @Shadow
    GameProfile profile;
    @Shadow
    @Final
    MinecraftServer server;

    @Inject(method = "onHello", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;profile:Lcom/mojang/authlib/GameProfile;", shift = At.Shift.AFTER))
    private void initUuid(CallbackInfo ci) {
        if (FabricProxy.config.getBungeeCord()) {


            if (((BungeeClientConnection) connection).getSpoofedUUID() == null) {
                bypassProxyBungee = true;
                return;
            }

            if(FabricProxy.config.getAlwaysOfficialUUID()){
                Optional<GameProfile> optional = this.server.getUserCache().findByName(this.profile.getName());
                optional.ifPresent(gameProfile -> {
                    this.profile = new GameProfile(gameProfile.getId(), this.profile.getName());
                });
            }
            else {
                this.profile = new GameProfile(((BungeeClientConnection) connection).getSpoofedUUID(), this.profile.getName());
            }

            if (((BungeeClientConnection) connection).getSpoofedProfile() != null) {
                for (Property property : ((BungeeClientConnection) connection).getSpoofedProfile()) {
                    this.profile.getProperties().put(property.getName(), property);
                }
            }
        }
    }

    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean skipKeyPacket(MinecraftServer minecraftServer) {
        return (bypassProxyBungee || !FabricProxy.config.getBungeeCord()) && minecraftServer.isOnlineMode();
    }
}
