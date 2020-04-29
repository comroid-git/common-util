package org.comroid.uniform.adapter.yml.spigot;

import java.util.List;
import java.util.Map;

import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SpigotYMLAdapter extends SerializationAdapter<ConfigurationSection, ConfigurationSection,
        ConfigurationSection> {
    protected SpigotYMLAdapter() {
        super("application/x-yaml", ConfigurationSection.class, ConfigurationSection.class);
    }

    @Override
    public UniNode parse(String data) {
        return adaptSection(makeSection(data));
    }

    private ConfigurationSection makeSection(String data) {
        final YamlConfiguration configuration = new YamlConfiguration();

        try {
            configuration.loadFromString(data);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Invalid YML Data: " + data, e);
        }

        return configuration;
    }

    @Override
    public UniObjectNode createUniObjectNode(ConfigurationSection node) {
        return null;
    }

    @Override
    public UniArrayNode createUniArrayNode(ConfigurationSection node) {
        return null;
    }

    private static final class NodeAdapter implements UniNode.Adapter<ConfigurationSection>, Map<String, Object>, List<Object> {

    }
}
