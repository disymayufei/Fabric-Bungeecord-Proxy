package cn.starlight.fabricproxy.mixin.bungee;

import cn.starlight.fabricproxy.interfaces.BungeeClientConnection;
import cn.starlight.fabricproxy.mixin.ClientConnectionAccessor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cn.starlight.fabricproxy.FabricProxy.config;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    private static final Gson gson = new Gson();

    @Shadow
    @Final
    private ClientConnection connection;

    @Inject(method = "login", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Z)V"))
    private void onProcessHandshakeStart(HandshakeC2SPacket packet, boolean transfer, CallbackInfo ci) {
        if (config.getBungeeCord() /* && NetworkState.LOGIN.equals(packet.getNewNetworkState()) */) {
            String[] split = packet.address().split("\00");
            if (split.length == 3 || split.length == 4) {
                ((ClientConnectionAccessor) connection).setAddress(new java.net.InetSocketAddress(split[1], ((java.net.InetSocketAddress) connection.getAddress()).getPort()));

                if (split[2].contains("-")) {
                    // regular uuid format
                    ((BungeeClientConnection) connection).setSpoofedUUID(UUID.fromString(split[2]));
                } else {
                    // add '-' to the uuid
                    ((BungeeClientConnection) connection).setSpoofedUUID(UUID.fromString(
                            split[2].substring(0, 8) + "-" +
                                    split[2].substring(8, 12) + "-" +
                                    split[2].substring(12, 16) + "-" +
                                    split[2].substring(16, 20) + "-" +
                                    split[2].substring(20) // 8-4-4-4-12
                    ));
                }

                if (split.length == 4) {
                    try {
                        // Parse JSON manually and construct Property objects (preserve signature)
                        JsonElement je = gson.fromJson(split[3], JsonElement.class);
                        if (je != null && je.isJsonArray()) {
                            JsonArray arr = je.getAsJsonArray();
                            List<Property> props = new ArrayList<>(arr.size());
                            for (JsonElement el : arr) {
                                if (!el.isJsonObject()) continue;
                                JsonObject o = el.getAsJsonObject();
                                String name = o.has("name") && !o.get("name").isJsonNull() ? o.get("name").getAsString() : null;
                                String value = o.has("value") && !o.get("value").isJsonNull() ? o.get("value").getAsString() : null;
                                String signature = o.has("signature") && !o.get("signature").isJsonNull() ? o.get("signature").getAsString() : null;
                                if (name != null && value != null) {
                                    props.add(new Property(name, value, signature));
                                }
                            }
                            // Aangepast naar de nieuwe methode naam en parameter type
                            ((BungeeClientConnection) connection).setSpoofedProperties(props);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } else {
                if (!config.getAllowBypassProxy()) {
                    Text disconnectMessage = Text.literal("If you wish to use IP forwarding, please enable it in your BungeeCord config as well!");
                    connection.send(new LoginDisconnectS2CPacket(disconnectMessage));
                    connection.disconnect(disconnectMessage);
                }
            }
        }
    }
}