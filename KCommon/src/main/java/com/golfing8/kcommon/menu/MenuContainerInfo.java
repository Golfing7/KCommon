package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.generator.Conf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional metadata for a {@link MenuContainer} implementation, describing where its backing
 * {@link MenuBuilder} config lives. This has no effect on behavior at runtime - a container still
 * loads its config however {@link MenuContainer#loadMenu()} is written to - it exists purely so
 * that the KCommon IntelliJ plugin can map the {@link MenuBuilder} config shape (title, size,
 * special-slots, etc.) onto the right place in a module's config schema.
 * <p>
 * Only meaningful when placed on a class extending {@link MenuContainer}.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface MenuContainerInfo {
    /**
     * The config path (a YAML key, or dot-separated nested keys) where this menu's data lives
     * within its config file.
     *
     * @return the path.
     */
    String path();

    /**
     * The config to map to - same semantics as {@link Conf#config()}.
     *
     * @return the config.
     */
    String config() default Conf.DEFAULT_CONF;

    /**
     * The kind of menu this container represents.
     *
     * @return the type.
     */
    Type type() default Type.NORMAL;

    /**
     * The kind of {@link MenuContainer} a config maps to.
     */
    enum Type {
        /**
         * A plain {@link MenuBuilder}-shaped menu.
         */
        NORMAL,
        /**
         * A paged menu (see {@link PagedMenuContainer}) - adds a top-level {@code max-page} key,
         * and recognizes {@code previous-page}/{@code next-page} special-slot entries with an
         * optional {@code always-show} flag.
         */
        PAGED,
    }
}
