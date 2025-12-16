package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;

/**
 * Contains useful methods for {@link Vector} instances
 */
@UtilityClass
public class VectorUtil {
    /**
     * Rotates the vector around the x axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return the same vector
     */

    public static Vector rotateAroundX(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double y = angleCos * vector.getY() - angleSin * vector.getZ();
        double z = angleSin * vector.getY() + angleCos * vector.getZ();
        return vector.setY(y).setZ(z);
    }

    /**
     * Rotates the vector around the y axis.
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return the same vector
     */

    public static Vector rotateAroundY(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * vector.getX() + angleSin * vector.getZ();
        double z = -angleSin * vector.getX() + angleCos * vector.getZ();
        return vector.setX(x).setZ(z);
    }

    /**
     * Rotates the vector around the z axis
     * <p>
     * This piece of math is based on the standard rotation matrix for vectors
     * in three dimensional space. This matrix can be found here:
     * <a href="https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">Rotation
     * Matrix</a>.
     *
     * @param angle the angle to rotate the vector about. This angle is passed
     *              in radians
     * @return the same vector
     */

    public static Vector rotateAroundZ(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * vector.getX() - angleSin * vector.getY();
        double y = angleSin * vector.getX() + angleCos * vector.getY();
        return vector.setX(x).setY(y);
    }
}
