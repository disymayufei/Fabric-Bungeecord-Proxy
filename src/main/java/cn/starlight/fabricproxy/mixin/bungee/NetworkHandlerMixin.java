package cn.starlight.fabricproxy.mixin.bungee;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ServerPlayNetworkHandler.class)
public class NetworkHandlerMixin {
    
    @Inject(method = "isInProperOrder", at = @At("HEAD"), cancellable = true)
    private void isInProperOrder(Instant timestamp, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
