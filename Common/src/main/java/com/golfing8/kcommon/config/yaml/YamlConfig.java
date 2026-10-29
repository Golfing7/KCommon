package com.golfing8.kcommon.config.yaml;

import com.golfing8.kcommon.config.KFileConfig;
import com.golfing8.kcommon.config.commented.ConfigTransformer;
import com.golfing8.kcommon.config.exc.ConfigException;
import com.golfing8.kcommon.module.AbstractModule;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A YAML config file that keeps its comments, for platforms without Bukkit's config API.
 * <p>
 * Comments are read and written the same way as Bukkit's {@code Configuration}, through {@link ConfigTransformer}.
 * </p>
 */
public class YamlConfig extends YamlSection implements KFileConfig {
    private final @Nullable Path configPath;
    private final @Nullable AbstractModule<?> module;
    /**
     * The comments of each mapped value
     */
    @Getter
    private final Map<String, String[]> comments = new HashMap<>();
    /**
     * The inline comments trailing each mapped value
     */
    @Getter
    private final Map<String, String> inlineComments = new HashMap<>();
    /**
     * The source config this config falls back on for missing values, usually the copy in the plugin jar.
     */
    @Getter
    @Setter
    private @Nullable YamlConfig source;
    /**
     * Tracks if this config has been modified since it was last saved.
     */
    @Getter
    @Setter
    private boolean modified;

    /**
     * Creates a config that isn't backed by a file, such as the defaults read from a jar.
     */
    public YamlConfig() {
        this(null, null);
    }

    public YamlConfig(@Nullable Path configPath) {
        this(configPath, null);
    }

    public YamlConfig(@Nullable Path configPath, @Nullable AbstractModule<?> module) {
        super(null, configPath == null ? "" : configPath.getFileName().toString());
        this.configPath = configPath;
        this.module = module;
    }

    @Override
    public @Nullable AbstractModule<?> getModule() {
        return module;
    }

    @Override
    public String getFileNameNoExtension() {
        return getName().replace(".yml", "");
    }

    private static Yaml yaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setWidth(Integer.MAX_VALUE);
        dumperOptions.setAllowUnicode(true);
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
        Representer representer = new Representer(dumperOptions);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }

    /**
     * Loads this config from its file.
     */
    public void load() {
        if (configPath == null)
            throw new IllegalStateException("This config isn't backed by a file!");

        try {
            loadFromString(new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8));
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to load config file at location %s!", this.configPath), exc);
        }
    }

    /**
     * Loads this config from a string.
     *
     * @param data the YAML.
     */
    public void loadFromString(String data) {
        values.clear();
        comments.clear();
        inlineComments.clear();

        Object loaded = yaml().load(data);
        if (loaded instanceof Map) {
            loadFrom((Map<?, ?>) loaded);
        } else if (loaded != null) {
            throw new ConfigException(this, "The top level of a config must be a map!");
        }

        ConfigTransformer transformer = new ConfigTransformer(data);
        for (String key : transformer) {
            comments.put(key, transformer.getJunk().stream().map(String::trim).filter(str -> str.startsWith("#")).toArray(String[]::new));
            if (transformer.getInlineComment() != null)
                inlineComments.put(key, transformer.getInlineComment());
        }
        this.modified = false;
    }

    /**
     * Writes this config, with its comments, to a string.
     *
     * @return the YAML.
     */
    public String saveToString() {
        Map<String, Object> map = toMap();
        String yamlString = map.isEmpty() ? "" : yaml().dump(map);
        ConfigTransformer transformer = new ConfigTransformer(yamlString);
        for (String key : transformer) {
            if (comments.containsKey(key))
                transformer.insertComment(comments.get(key));
            if (inlineComments.containsKey(key))
                transformer.insertInlineComment(inlineComments.get(key));
        }

        return String.join("\n", transformer.getTransformedLines());
    }

    @Override
    public void save() {
        if (configPath == null)
            throw new IllegalStateException("This config isn't backed by a file!");

        try {
            modified = false;
            Files.write(configPath, saveToString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to save config at path %s!", this.configPath), exc);
        }
    }

    @Override
    public void setComments(String path, String... comments) {
        if (comments == null || comments.length == 0) {
            this.comments.remove(path);
        } else {
            this.comments.put(path, Arrays.stream(comments).map(str -> "# " + str).toArray(String[]::new));
        }
    }

    @Override
    public void ensureExists(String path) {
        if (!this.contains(path)) {
            if (this.source == null || !this.source.contains(path))
                throw new ConfigException(this, "Config entry at path " + path + " doesn't exist!");

            this.set(path, this.source.get(path));
            this.save();
        }
    }

    @Override
    public boolean tryLoadFromSource(String path) {
        if (!this.contains(path)) {
            if (this.source == null || !this.source.contains(path))
                return false;

            this.set(path, this.source.get(path));
            if (configPath != null)
                this.save();
        }
        return true;
    }
}
