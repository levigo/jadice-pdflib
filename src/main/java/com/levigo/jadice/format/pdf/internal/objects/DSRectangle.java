package com.levigo.jadice.format.pdf.internal.objects;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Represents a rectangle in the PDF sense. In contrast to Java2D {@link Rectangle2D}s, a PDF
 * rectangle is defined by its lower left and upper right corners.
 */
public class DSRectangle {

  private final Point2D.Double lowerLeft;
  private final Point2D.Double upperRight;
  private Point2D.Double lowerRight;
  private Point2D.Double upperLeft;

  /**
   * construct a Rectangle based on a array
   *
   * @param array PDFArray base for the Rectangle
   */
  public DSRectangle(DSArray array) {
    if (array == null) {
      throw new IllegalArgumentException("rectangle array must not be null");
    }
    final Iterator<DSObject> iter = array.iterator();

    lowerLeft = new Point2D.Double(nextOrDefault(iter), nextOrDefault(iter));
    upperRight = new Point2D.Double(nextOrDefault(iter), nextOrDefault(iter));

    initializeUndefinedPoints();
  }

  public DSRectangle(Rectangle2D rect) {
    if (rect.getWidth() < 0) {
      rect.setRect(rect.getX(), rect.getY(), rect.getWidth() * -1, rect.getHeight());
    }
    if (rect.getHeight() < 0) {
      rect.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() * -1);
    }

    lowerLeft = new Point2D.Double(rect.getX(), rect.getY());
    upperRight = new Point2D.Double(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());

    initializeUndefinedPoints();
  }

  public DSRectangle(Point2D.Double lowerLeft, Point2D.Double upperRight) {
    if (lowerLeft == null)
      throw new IllegalArgumentException("lowerLeft may not be null");
    if (upperRight == null)
      throw new IllegalArgumentException("upperRight may not be null");

    this.lowerLeft = lowerLeft;
    this.upperRight = upperRight;
    initializeUndefinedPoints();
  }

  /**
   * constructs a {@link Rectangle2D}. There will be a normalization in case of negative width and
   * height values.
   *
   * @return a Java2D Rectangle2D
   */
  public Rectangle2D createRectangle2D() {
    double x = getLowerLeft().x;
    double y = getLowerLeft().y;
    double w = getWidth();
    double h = getHeight();

    /*
     * as Java doesn't like any negative width- or height-values for rectangles doing some value
     * flipping
     */
    if (w < 0) {
      w *= -1;
      x -= w;
    }

    if (h < 0) {
      h *= -1;
      y -= h;
    }

    return new Rectangle2D.Double(x, y, w, h);
  }

  private double getDouble(DSObject obj) {
    if (!(obj instanceof DSNumber)) {
      // Log.error(LOG_CONTEXT,
      // "one object in the Array isn't a PDFNumber! setting to 0");
      return 0;
    }
    return ((DSNumber) obj).getDouble();
  }

  /**
   * get the height value of this rectange as a <code>double</code>
   *
   * @return the height of the rectangle
   */
  public double getHeight() {
    return upperRight.y - lowerLeft.y;
  }

  public Point2D.Double getLowerLeft() {
    return lowerLeft;
  }

  public Point2D.Double getLowerRight() {
    return lowerRight;
  }

  public Point2D.Double getUpperLeft() {
    return upperLeft;
  }

  public Point2D.Double getUpperRight() {
    return upperRight;
  }

  /**
   * get the width value of this rectange as a <code>double</code>
   *
   * @return the width of the rectangle
   */
  public double getWidth() {
    return upperRight.x - lowerLeft.x;
  }

  private void initializeUndefinedPoints() {
    lowerRight = new Point2D.Double(upperRight.x, lowerLeft.y);
    upperLeft = new Point2D.Double(lowerLeft.x, upperRight.y);
  }

  private double nextOrDefault(Iterator<DSObject> iter) {
    return iter.hasNext() ? getDouble(iter.next()) : 0;
  }

  public DSRectangle normalize() {
    double tllx;
    double tlly;
    double turx;
    double tury;

    if (getLowerLeft().x > getUpperRight().x) {
      tllx = getUpperRight().x;
      turx = getLowerLeft().x;
    } else {
      tllx = getLowerLeft().x;
      turx = getUpperRight().x;
    }

    if (getLowerLeft().y > getUpperRight().y) {
      tlly = getUpperRight().y;
      tury = getLowerLeft().y;
    } else {
      tlly = getLowerLeft().y;
      tury = getUpperRight().y;
    }

    return new DSRectangle(new Point2D.Double(tllx, tlly), new Point2D.Double(turx, tury));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[[lowerLeft: " + lowerLeft.x + "," + lowerLeft.y + "][upperRight: "
        + upperRight.x + ", " + upperRight.y + "]]";
  }
}
