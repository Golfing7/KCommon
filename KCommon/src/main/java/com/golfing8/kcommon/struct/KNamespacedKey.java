package com.golfing8.kcommon.struct;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A copy of the bukkit NamespacedKey class.
 * <p>
 * This was created as 1.8 MC doesn't have this class.
 * </p>
 */
public class KNamespacedKey {
    private static final Pattern VALID_NAMESPACE = Pattern.compile("[a-z0-9._-]+");
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    private final String namespace;
    private final String key;

    public KNamespacedKey(String namespace, String key) {
        Preconditions.checkArgument((namespace != null && VALID_NAMESPACE.matcher(namespace).matches()), String.format("Invalid namespace. Must be [a-z0-9._-]: %s", namespace));
        Preconditions.checkArgument((key != null && VALID_KEY.matcher(key).matches()), String.format("Invalid key. Must be [a-z0-9/._-]: %s", key));
        this.namespace = namespace;
        this.key = key;
        String string = toString();
        Preconditions.checkArgument((string.length() < 256), "KNamespacedKey must be less than 256 characters", string);
    }

    public KNamespacedKey(Plugin plugin, String key) {
        Preconditions.checkArgument((plugin != null), "Plugin cannot be null");
        Preconditions.checkArgument((key != null), "Key cannot be null");
        this.namespace = plugin.getName().toLowerCase(Locale.ROOT);
        this.key = key.toLowerCase(Locale.ROOT);
        Preconditions.checkArgument(VALID_NAMESPACE.matcher(this.namespace).matches(), String.format("Invalid namespace. Must be [a-z0-9._-]: %s", this.namespace));
        Preconditions.checkArgument(VALID_KEY.matcher(this.key).matches(), String.format("Invalid key. Must be [a-z0-9/._-]: %s", this.key));
        String string = toString();
        Preconditions.checkArgument((string.length() < 256), String.format("KNamespacedKey must be less than 256 characters (%s)", string));
    }

    
    public String getNamespace() {
        return this.namespace;
    }

    
    public String getKey() {
        return this.key;
    }

    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.namespace.hashCode();
        hash = 47 * hash + this.key.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KNamespacedKey other = (KNamespacedKey)obj;
        return (this.namespace.equals(other.namespace) && this.key.equals(other.key));
    }

    public String toString() {
        return this.namespace + ":" + this.key;
    }

    @Deprecated
    public static KNamespacedKey randomKey() {
        return new KNamespacedKey("kcommon", UUID.randomUUID().toString());
    }
}
