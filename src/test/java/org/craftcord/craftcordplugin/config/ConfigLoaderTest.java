package org.craftcord.craftcordplugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigLoaderTest {

    @Test
    void resolvesLocalHostWhenBindModeLocalAndHostBlank() {
        FileConfiguration fileConfiguration = baseConfig();
        when(fileConfiguration.getString("bindMode", "local")).thenReturn("local");
        when(fileConfiguration.getString("host", "")).thenReturn("");

        CraftCordConfig config = new ConfigLoader().load(fileConfiguration);

        assertEquals("127.0.0.1", config.host());
    }

    @Test
    void resolvesGlobalHostWhenBindModeGlobalAndHostBlank() {
        FileConfiguration fileConfiguration = baseConfig();
        when(fileConfiguration.getString("bindMode", "local")).thenReturn("global");
        when(fileConfiguration.getString("host", "")).thenReturn("");

        CraftCordConfig config = new ConfigLoader().load(fileConfiguration);

        assertEquals("0.0.0.0", config.host());
    }

    @Test
    void keepsExplicitHostOverride() {
        FileConfiguration fileConfiguration = baseConfig();
        when(fileConfiguration.getString("bindMode", "local")).thenReturn("local");
        when(fileConfiguration.getString("host", "")).thenReturn("192.168.1.22");

        CraftCordConfig config = new ConfigLoader().load(fileConfiguration);

        assertEquals("192.168.1.22", config.host());
    }

    @Test
    void rejectsInvalidBindMode() {
        FileConfiguration fileConfiguration = baseConfig();
        when(fileConfiguration.getString("bindMode", "local")).thenReturn("internet");
        when(fileConfiguration.getString("host", "")).thenReturn("");

        assertThrows(IllegalArgumentException.class, () -> new ConfigLoader().load(fileConfiguration));
    }

    private FileConfiguration baseConfig() {
        FileConfiguration fileConfiguration = mock(FileConfiguration.class);
        when(fileConfiguration.getInt("port", 8080)).thenReturn(8080);
        when(fileConfiguration.getString("websocketPath", "/ws")).thenReturn("/ws");
        when(fileConfiguration.getString("httpBasePath", "/api/v1")).thenReturn("/api/v1");
        when(fileConfiguration.getString("apiToken", "change-me")).thenReturn("secret");
        when(fileConfiguration.getBoolean("enableHttp", true)).thenReturn(true);
        when(fileConfiguration.getBoolean("enableWebSocket", true)).thenReturn(true);
        when(fileConfiguration.getBoolean("logRequests", false)).thenReturn(false);
        when(fileConfiguration.getBoolean("logEvents", false)).thenReturn(false);
        return fileConfiguration;
    }
}

