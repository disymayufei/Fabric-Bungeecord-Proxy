package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.FabricProxy;
import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
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

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Unique
    private boolean bypassProxyBungee = false;
    @Shadow
    @Final
    ClientConnection connection;
    @Shadow
    private GameProfile profile;
    @Shadow
    @Final
    MinecraftServer server;

    @Inject(method = "startVerify", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;profile:Lcom/mojang/authlib/GameProfile;", shift = At.Shift.AFTER))
    private void initUuid(CallbackInfo ci) {
        if (FabricProxy.config.getBungeeCord()) {

            if (((BungeeClientConnection) connection).getSpoofedUUID() == null) {
                bypassProxyBungee = true;
                return;
            }

            if(FabricProxy.config.getAlwaysOfficialUUID()) {
                Optional.ofNullable(this.server.getUserCache()).ifPresent(
                        userCache -> userCache.findByName(this.profile.getName()).ifPresentOrElse(
                                gameProfile -> this.profile = new GameProfile(gameProfile.getId(), this.profile.getName()),
                                () -> this.profile = new GameProfile(((BungeeClientConnection) connection).getSpoofedUUID(), this.profile.getName())
                        )
                );

                Optional<GameProfile> optional = this.server.getUserCache().findByName(this.profile.getName());
                optional.ifPresentOrElse(gameProfile -> {
                    this.profile = new GameProfile(gameProfile.getId(), this.profile.getName());
                }, () -> {
                    this.profile = new GameProfile(((BungeeClientConnection) connection).getSpoofedUUID(), this.profile.getName());
                });
            }
            else {
                this.profile = new GameProfile(((BungeeClientConnection) connection).getSpoofedUUID(), this.profile.getName());
            }

            if (((BungeeClientConnection) connection).getSpoofedProfile() != null) {
                for (Property property : ((BungeeClientConnection) connection).getSpoofedProfile()) {
                    this.profile.getProperties().put(property.name(), property);
                }
            }
        }
    }

    // 这部分代码似乎在1.19.4+已经不再需要了，mojang去掉了对公钥验证的相关逻辑，但以防万一，先留着
    /*
    @Redirect(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;getVerifiedPublicKey(Lnet/minecraft/network/encryption/PlayerPublicKey$PublicKeyData;Ljava/util/UUID;Lnet/minecraft/network/encryption/SignatureVerifier;Z)Lnet/minecraft/network/encryption/PlayerPublicKey;"))
    public PlayerPublicKey getVerifiedPublicKey(PlayerPublicKey.PublicKeyData publicKeyData, UUID playerUuid, SignatureVerifier servicesSignatureVerifier, boolean shouldThrowOnMissingKey){
        return null;
    }
     */

    @Redirect(method = "onHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private boolean skipKeyPacket(MinecraftServer minecraftServer) {
        return (bypassProxyBungee || !FabricProxy.config.getBungeeCord()) && minecraftServer.isOnlineMode();
    }
}
