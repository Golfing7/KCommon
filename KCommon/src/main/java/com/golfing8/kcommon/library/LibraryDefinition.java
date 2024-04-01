package com.golfing8.kcommon.library;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains definitions about libraries.
 */
@Data
public class LibraryDefinition {
    private final String groupID;
    private final String artifact;
    private final String version;
    private final String repo;

    public LibraryDefinition(String groupID, String artifact, String version, String repo) {
        this.groupID = groupID.replace(",", ".");
        this.artifact = artifact;
        this.version = version;
        this.repo = repo;
    }

    public LibraryDefinition(String groupID, String artifact, String version) {
        this(groupID, artifact, version, "https://repo1.maven.org/maven2");
    }

    public String getFormattedName() {
        return groupID + ":" + artifact + ":" + version;
    }
}
