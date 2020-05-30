package org.comroid.uniform.adapter.properties;

import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniArrayNode;
import org.comroid.uniform.node.UniNode;
import org.comroid.uniform.node.UniObjectNode;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class JavaPropertiesSerializationAdapter extends SerializationAdapter<Properties, Properties, Properties> {
    protected JavaPropertiesSerializationAdapter() {
        super("text/x-java-propertie", Properties.class, Properties.class);
    }

    @Override
    public UniNode parse(@Nullable String data) {
        return createUniObjectNode(ofString(data));
    }

    @Override
    public UniObjectNode createUniObjectNode(Properties node) {
        return new UniObjectNode(this, new PropertiesAdapter(node));
    }

    @Override
    public UniArrayNode createUniArrayNode(Properties node) {
        throw new UnsupportedOperationException("Cannot create ArrayNode for Properties");
    }

    private Properties ofString(String data) {
        try {
            final Properties prop = new Properties();
            prop.load(new StringReader(data));

            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
