package cn.starlight.fabricproxy.mixin;

import cn.starlight.fabricproxy.FabricProxy;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UserCache.class)
public class UserCacheMixin {
    @Shadow private static boolean useRemote;

    @Redirect(method = "getOfflinePlayerProfile", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/UserCache;shouldUseRemote()Z"))
    private static boolean findProfileByName() {
        if (FabricProxy.config.getBungeeCord()) {
            return true;
        }
        return useRemote;
    }
}
