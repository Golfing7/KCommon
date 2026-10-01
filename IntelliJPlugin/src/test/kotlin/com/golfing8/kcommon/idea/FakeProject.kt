package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import java.lang.reflect.Proxy

/**
 * A [Project] that throws if any method is actually called - just enough to satisfy APIs that
 * require a `Project` instance in their signature (e.g. [SchemaExportInterner]'s constructor)
 * without needing the full IntelliJ platform test-fixtures framework, for tests that never touch
 * PSI (i.e. never call [SchemaExportInterner.resolveEnumValues]).
 */
val FakeProject: Project = Proxy.newProxyInstance(
    Project::class.java.classLoader,
    arrayOf(Project::class.java)
) { _, method, _ ->
    throw UnsupportedOperationException("FakeProject.${method.name}() was called - this test wasn't expected to touch PSI")
} as Project
