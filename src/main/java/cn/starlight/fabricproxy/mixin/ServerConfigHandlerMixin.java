package cn.starlight.fabricproxy.mixin;

import cn.starlight.fabricproxy.FabricProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConfigHandler.class)
public class ServerConfigHandlerMixin {
    @Redirect(method = "lookupProfile", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"))
    private static boolean lookupProfile(MinecraftServer minecraftServer) {
        if (FabricProxy.config.getBungeeCord()) {
            return true;
        }

        return minecraftServer.isOnlineMode();
    }
}
