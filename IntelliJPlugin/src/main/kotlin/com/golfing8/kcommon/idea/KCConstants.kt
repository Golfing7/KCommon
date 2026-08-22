package com.golfing8.kcommon.idea

/**
 * Fully-qualified names of the KCommon types this plugin reasons about.
 * Kept centralized since the plugin never depends on KCommon's jar directly —
 * it only ever sees these types through PSI (source or compiled) in whatever
 * project it's installed into.
 */
object KCConstants {
    const val MODULE_INFO = "com.golfing8.kcommon.module.ModuleInfo"
    const val CONF = "com.golfing8.kcommon.config.generator.Conf"
    const val CONFIG_CLASS_SOURCE = "com.golfing8.kcommon.config.generator.ConfigClassSource"
    const val CA_SERIALIZABLE = "com.golfing8.kcommon.config.adapter.CASerializable"
    const val CA_SERIALIZABLE_OPTIONS = "com.golfing8.kcommon.config.adapter.CASerializable.Options"
    const val DYNAMIC_ENUM = "com.golfing8.kcommon.struct.DynamicEnum"
    const val RANGE_MAP = "com.golfing8.kcommon.struct.map.RangeMap"
    const val OPTIONAL = "java.util.Optional"
    const val MENU_CONTAINER = "com.golfing8.kcommon.menu.MenuContainer"
    const val MENU_CONTAINER_INFO = "com.golfing8.kcommon.menu.MenuContainerInfo"

    /** Bucket name used for the module's main config.yml (i.e. @Conf.config() left unset). */
    const val MAIN_CONFIG_BUCKET = "config"
    const val DEFAULT_CONFIG_BUCKET = "@default"

    /** Mirrors CAReflective.KEY_FIELD_NAME - the reserved field excluded from serialization. */
    const val KEY_FIELD_NAME = "_key"
}
