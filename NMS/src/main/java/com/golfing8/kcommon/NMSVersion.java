package com.golfing8.kcommon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A wrapper for the server version
 */
@Getter
public class NMSVersion implements Comparable<NMSVersion> {
    public static final NMSVersion UNKNOWN = new NMSVersion(-1, -1),
            v1_7 = new NMSVersion(7, -1),
            v1_8 = new NMSVersion(8, -1),
            v1_9 = new NMSVersion(9, -1),
            v1_10 = new NMSVersion(10, -1),
            v1_11 = new NMSVersion(11, -1),
            v1_12 = new NMSVersion(12, -1),
            v1_13 = new NMSVersion(13, -1),
            v1_14 = new NMSVersion(14, -1),
            v1_15 = new NMSVersion(15, -1),
            v1_16 = new NMSVersion(16, -1),
            v1_17 = new NMSVersion(17, -1),
            v1_18 = new NMSVersion(18, -1),
            v1_19 = new NMSVersion(19, -1),
            v1_20 = new NMSVersion(20, -1),
            v1_21 = new NMSVersion(21, -1);

    private final int major;
    /**
     * Negative values mean that the minor version is insignificant
     */
    private final int minor;

    public NMSVersion(int value, int minor) {
        this.major = value;
        this.minor = minor;
    }

    /**
     * Checks if the major version is equal
     *
     * @param version the other version
     * @return true if equal
     */
    public boolean majorEquals(NMSVersion version) {
        if (version == null)
            return false;

        return this.major == version.major;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NMSVersion) {
            return major == ((NMSVersion) obj).major;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    /**
     * Checks if this version is as new or newer than the other version
     *
     * @param version the version
     * @return true if as new or newer
     */
    public boolean isAtOrAfter(NMSVersion version) {
        if (this.major == version.major) {
            if (this.minor == -1 || version.minor == -1)
                return true;

            return this.minor >= version.minor;
        }

        return this.major >= version.major;
    }

    /**
     * Checks if this version is as old or older than the other version
     *
     * @param version the version
     * @return true if as old or older
     */
    public boolean isAtOrBefore(NMSVersion version) {
        if (this.major == version.major) {
            if (this.minor == -1 || version.minor == -1)
                return true;

            return this.minor <= version.minor;
        }
        return this.major <= version.major;
    }

    @Override
    public String toString() {
        if (minor > 0) {
            return "1." + major + "." + minor;
        }
        return "1." + major;
    }

    @Override
    public int compareTo(@NotNull NMSVersion o) {
        return major == o.major ? minor - o.minor : major - o.major;
    }

    /**
     * Loads the version of the server
     *
     * @return the version
     */
    public static NMSVersion loadVersion() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String version = bukkitVersion.substring(0, bukkitVersion.indexOf("-"));
        String[] split = version.split("\\.");
        int major = Integer.parseInt(String.valueOf(split[1]));
        int minor = split.length > 2 ? Integer.parseInt(String.valueOf(split[2])) : 0;

        try {
            return new NMSVersion(major, minor);
        } catch (IllegalArgumentException exc) {
            return UNKNOWN;
        }
    }
}
