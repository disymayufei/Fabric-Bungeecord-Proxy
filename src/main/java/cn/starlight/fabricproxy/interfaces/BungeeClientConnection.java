package cn.starlight.fabricproxy.interfaces;

import com.mojang.authlib.properties.Property;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.UUID;

public interface BungeeClientConnection {
    UUID getSpoofedUUID();
    void setSpoofedUUID(UUID uuid);

    Collection<Property> getSpoofedProperties();
    // Aangepast van setSpoofedProfile(Property[]) naar setSpoofedProperties(Collection<Property>)
    void setSpoofedProperties(Collection<Property> properties);

    SocketAddress getSpoofedAddress();
    void setSpoofedAddress(SocketAddress address);
}