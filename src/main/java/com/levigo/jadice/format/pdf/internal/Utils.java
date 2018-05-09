package com.levigo.jadice.format.pdf.internal;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSBoolean;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSHexString;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNumber;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSRectangle;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;
import com.levigo.jadice.format.pdf.internal.objects.DSString;

public class Utils {
  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  public static final DSRectangle transformedBounds(AffineTransform tx, DSRectangle source) {
    final Point2D transformedUpperLeft = tx.transform(source.getUpperLeft(), null);
    final Point2D transformedUpperRight = tx.transform(source.getUpperRight(), null);
    final Point2D transformedLowerLeft = tx.transform(source.getLowerLeft(), null);
    final Point2D transformedLowerRight = tx.transform(source.getLowerRight(), null);

    return new DSRectangle(//
        // lower left first
        new Point2D.Double( //
            min(//
                transformedUpperLeft.getX(), //
                transformedUpperRight.getX(), //
                transformedLowerLeft.getX(), //
                transformedLowerRight.getX() //
            ),//
            min(//
                transformedUpperLeft.getY(), //
                transformedUpperRight.getY(), //
                transformedLowerLeft.getY(), //
                transformedLowerRight.getY() //
            )//
        ), //
        // upper right
        new Point2D.Double( //
            max(//
                transformedUpperLeft.getX(), //
                transformedUpperRight.getX(), //
                transformedLowerLeft.getX(), //
                transformedLowerRight.getX() //
            ),//
            max(//
                transformedUpperLeft.getY(), //
                transformedUpperRight.getY(), //
                transformedLowerLeft.getY(), //
                transformedLowerRight.getY() //
            )//
        ) //
    );
  }

  private static double min(double... args) {
    if (args == null || args.length == 0)
      throw new IllegalArgumentException("input required");
    double min = Double.MAX_VALUE;
    for (final double d : args) {
      if (d < min)
        min = d;
    }
    return min;
  }

  private static double max(double... args) {
    if (args == null || args.length == 0)
      throw new IllegalArgumentException("input required");
    double max = Double.MIN_VALUE;
    for (final double d : args) {
      if (d > max)
        max = d;
    }
    return max;
  }

  public static final AffineTransform affineTransformFromArray(DSArray array) {

    /*
     * to avoid NPE
     */
    if (array == null) {
      return null;
    }

    int arrayPos = 0;
    final double[] values = new double[6];
    for (final DSObject obj : array) {
      if (obj.isNumber()) {
        values[arrayPos++] = ((DSNumber) obj).getDouble();
      } else if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("non number value in array");
      }
    }

    if (arrayPos < 6) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("not enough transformation values. filling " + (6 - arrayPos) + " values with 0");
      }
      for (int i = arrayPos; i < values.length; i++) {
        values[i] = 0.0;
      }
    }
    return new AffineTransform(values);

  }

  /**
   * checks if the passed PDFObject is an array. The following might happen:
   *
   * <ul>
   * <li>if <code>obj</code> is <code>null</code>, <code>null</code> will be returned</li>
   * <li>if obj is of type <code>{@link DSArray}</code> it will be casted and returned as an
   * <code>{@link DSArray}</code></li>
   * <li>if none of the above applies, <code>null</code> will be returned</li>
   * </ul>
   *
   * @param obj
   * @return
   */
  public final static DSArray getArray(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isArray()) {
      return (DSArray) obj;
    }

    return null;
  }

  public final static DSBoolean getBoolean(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof DSBoolean) {
      return (DSBoolean) obj;
    }
    return null;
  }

  public final static DSDictionary getDictionary(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isDictionary()) {
      return (DSDictionary) obj;
    }

    return null;
  }

  public final static DSHexString getHexString(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof DSHexString) {
      return (DSHexString) obj;
    }

    return null;
  }

  public final static DSNameObject getNameObject(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isNameObject()) {
      return (DSNameObject) obj;
    }

    return null;
  }

  public final static DSNumber getNumber(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isNumber()) {
      return (DSNumber) obj;
    }

    return null;
  }

  /**
   * checks if the passed PDFObject is an array. The following might happen:
   *
   * <ul>
   * <li>if <code>obj</code> is <code>null</code>, <code>null</code> will be returned</li>
   * <li>if obj is of type <code>{@link DSArray}</code> it will be casted and returned as an
   * <code>{@link DSArray}</code></li>
   * <li>if it is any other type of <code>PDFObject</code> a new Array will be created an the obj
   * will be placed into it.</li>
   * </ul>
   *
   * @param obj
   * @return
   */
  public final static DSArray getOrCreateArray(DSObject obj) {
    if (obj == null) {
      return null;
    }

    final DSArray check = getArray(obj);

    if (check != null) {
      return check;
    }

    // it is any other type of PDFObject. We'll create an array
    final DSArray array = new DSArray();
    array.add(obj);
    return array;
  }

  public final static DSRectangle getRectangle(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isArray()) {
      return new DSRectangle((DSArray) obj);
    }

    return null;
  }

  public final static DSStream getStream(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof DSStream) {
      return (DSStream) obj;
    }

    return null;
  }

  public final static DSString getString(DSObject obj) {
    if (obj == null) {
      return null;
    }

    if (obj.isString()) {
      return (DSString) obj;
    }

    return null;
  }

  /**
   * Name object to enum mapping utility.
   *
   * @param <T>
   * @param nameObject
   * @param values
   * @param defaultValue
   * @return
   */
  public static <T extends Enum<T>> T mapEnum(DSNameObject nameObject, T[] values, T defaultValue) {
    T result = null;
    if (nameObject != null) {
      final String name = nameObject.getName();
      for (final T t : values) {
        if (t.name().equals(name)) {
          result = t;
          break;
        }
      }
    }

    if (result == null) {
      result = defaultValue;
    }

    return result;
  }

  private Utils() {
    // disallow instance creation
  }
}
