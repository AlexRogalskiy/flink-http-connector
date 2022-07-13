package com.getindata.connectors.http.internal.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.flink.util.StringUtils;

import com.getindata.connectors.http.internal.config.ConfigException;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ConfigUtils {

    private static final String PROPERTY_NAME_DELIMITER = ".";

    /**
     * Convert properties that name starts with given {@code keyPrefix} to Map.
     * Values for this property will be cast to {@code valueClazz} type.
     *
     * @param properties properties to extract keys from.
     * @param keyPrefix prefix used to match property name with.
     * @param valueClazz type to cast property values to.
     * @return Map of propertyName to propertyValue.
     */
    public static <T> Map<String, T> propertiesToMap(
            Properties properties,
            String keyPrefix,
            Class<T> valueClazz) {

        Map<String, T> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String) {
                String key = (String) entry.getKey();
                if (key.startsWith(keyPrefix)) {
                    tryAddToConfigMap(properties, valueClazz, map, key);
                }
            } else {
                throw new ConfigException(
                    entry.getKey().toString(),
                    entry.getValue(), "Key must be a string."
                );
            }
        }
        return map;
    }

    /**
     * A utility method to extract last element from property name. This method assumes property to
     * be in format as <b>{@code this.is.my.property.name}</b>, using "dot" as a delimiter. For this
     * example the returned value would be <b>{@code name}</b>.
     *
     * @param propertyKay Property name to extract the last element from.
     * @return property last element or the property name if {@code propertyKey} parameter had no
     * dot delimiter.
     * @throws ConfigException when invalid property such as null, empty, blank, ended with dot was
     *                         used.
     */
    public static String extractPropertyLastElement(String propertyKay) {
        if (StringUtils.isNullOrWhitespaceOnly(propertyKay)) {
            throw new ConfigException("Provided a property name that is null, empty or blank.");
        }

        if (!propertyKay.contains(PROPERTY_NAME_DELIMITER)) {
            return propertyKay;
        }

        int delimiterLastIndex = propertyKay.lastIndexOf(PROPERTY_NAME_DELIMITER);
        if (delimiterLastIndex == propertyKay.length() - 1) {
            throw new ConfigException(
                String.format(
                    "Invalid property - %s. Property name should not end with property delimiter.",
                    propertyKay)
            );
        }

        return propertyKay.substring(delimiterLastIndex + 1);
    }

    private static <T> void tryAddToConfigMap(
            Properties properties,
            Class<T> clazz, Map<String, T> map,
            String key) {
        try {
            map.put(key, clazz.cast(properties.get(key)));
        } catch (ClassCastException e) {
            throw new ConfigException(
                String.format("Unable to cast value for property %s to type %s", key,
                    clazz), e);
        }
    }

}
