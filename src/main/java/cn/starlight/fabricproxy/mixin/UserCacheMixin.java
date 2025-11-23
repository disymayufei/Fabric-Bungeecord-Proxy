// java
package cn.starlight.fabricproxy.mixin;

import cn.starlight.fabricproxy.FabricProxy;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

@Mixin(targets = "net.minecraft.util.UserCache")
public class UserCacheMixin {

    @Inject(method = "getOfflinePlayerProfile", at = @At("HEAD"))
    private void onGetOfflinePlayerProfile(String name, CallbackInfoReturnable<Optional<GameProfile>> cir) {
        if (!FabricProxy.config.getBungeeCord()) return;

        Object self = this;
        Class<?> cls = self.getClass();

        // 1) Try to call shouldUseRemote() reflectively on the instance (preferred).
        try {
            Method m = cls.getMethod("shouldUseRemote");
            m.setAccessible(true);
            Object res = m.invoke(self);
            if (res instanceof Boolean && !((Boolean) res)) {
                // force true by invoking a setter if available (rare) or proceed to next step
            }
            // If method exists we can try to replace by setting related field(s) below as well.
        } catch (Exception ignored) {}

        // 2) Try to set common boolean fields if present (useRemote or obf names).
        String[] candidates = new String[] {
                "useRemote", "shouldUseRemote", "field_23223", "field_26851", "field_33120" // add candidates you discover
        };
        for (String candidate : candidates) {
            try {
                Field f = findFieldRecursively(cls, candidate);
                if (f == null) continue;
                f.setAccessible(true);
                Class<?> t = f.getType();
                if (t == boolean.class) {
                    f.setBoolean(self, true);
                    break;
                } else if (t == Boolean.class) {
                    f.set(self, Boolean.TRUE);
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        // 3) As a last resort, try to call a setter method named setUseRemote(boolean) or similar.
        try {
            Method setter = cls.getMethod("setUseRemote", boolean.class);
            setter.setAccessible(true);
            setter.invoke(self, true);
        } catch (Exception ignored) {
        }
    }

    private Field findFieldRecursively(Class<?> cls, String name) {
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            try {
                return cur.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cur = cur.getSuperclass();
            }
        }
        return null;
    }
}
