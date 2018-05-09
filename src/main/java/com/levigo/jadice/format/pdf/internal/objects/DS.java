package com.levigo.jadice.format.pdf.internal.objects;

import static org.jadice.util.base.Strings.utf8String;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jadice.util.base.Strings;

import com.levigo.jadice.format.pdf.internal.ByteArrayStreamFactory;
import com.levigo.jadice.format.pdf.internal.ReferenceResolver;

/**
 * Static helper methods which allow for easy creation of DS objects. This is mainly syntactical
 * sugar to keep code readable.
 */
public class DS {

  public static DSArray arrayEmpty() {
    return new DSArray(Collections.<DSObject>emptyList());
  }

  public static DSArray array(final List<DSObject> dsList) {
    return new DSArray(dsList);
  }

  public static DSArray array(DSObject... dsObjects) {
    return new DSArray(dsObjects);
  }

  public static DSObject arrayOpt(List<DSObject> dsList) {
    if (dsList == null) {
      throw new IllegalArgumentException("list must not be null");
    }

    if (dsList.size() == 0) {
      throw new IllegalArgumentException("list must not be empty");
    }

    if (dsList.size() == 1) {
      return dsList.get(0);
    } else {
      return array(dsList);
    }
  }

  /**
   * Creates a {@link DSArray} from a list of numbers, converting each number to a {@link DSNumber}.
   *
   * @param list the {@link Number}s from which {@link DSNumber} instances are created and put into
   *             the resulting {@link DSArray}
   * @return a {@link DSArray} consisting of {@link DSNumber}s holding the numeric values supplied
   * as a method argument
   */
  public static DSArray arrayOfNum(final List<? extends Number> list) {
    final ArrayList<DSObject> dsList = new ArrayList<>(list.size());
    for (final Number current : list) {
      dsList.add(num(current));
    }
    return array(dsList);
  }

  public static DSArray arrayOfNum(Double... doubles) {
    final List<Double> doubleList = Arrays.asList(doubles);
    return arrayOfNum(doubleList);
  }

  public static DSArray arrayOfInt(Integer... integers) {
    final List<Integer> integerList = Arrays.asList(integers);
    return arrayOfNum(integerList);
  }

  /**
   * Creates a new empty {@link DSDictionary}
   *
   * @return - the {@link DSDictionary}
   */
  public static DSDictionary dict() {
    return new DSCommonDictionary();
  }

  /**
   * Creates a new empty {@link DSDictionary} with the given initial capacity.
   *
   * @param initialCapacity - the initial capacity
   * @return the {@link DSDictionary}
   */
  public static DSDictionary dict(int initialCapacity) {
    return new DSCommonDictionary(initialCapacity);
  }

  /**
   * Constructs a {@link DSNameObject} from the given byte array's contents. According to PDF
   * 32000-1:2008, 7.3.5 "Name Objects", UTF-8 encoding <em>should</em> be used when a Name Object
   * is converted to a String representation.
   *
   * @param utf8bytes the bytes which uniquely define the {@link DSNameObject} to be constructed
   * @return a {@link DSNameObject} constructed from the given byte array, interpreting its contents
   * as UTF-8 values.
   */
  public static DSNameObject name(byte[] utf8bytes) {
    return name(utf8String(utf8bytes));
  }

  public static DSNameObject name(String name) {
    return new DSNameObject(name);
  }

  public static DSInteger num(int value) {
    return num((long) value);
  }

  public static DSInteger num(long value) {
    return new DSInteger(value);
  }

  public static DSReal num(float value) {
    return num((double) value);
  }

  public static DSReal num(double value) {
    return new DSReal(value);
  }

  public static DSNumber num(Number value) {
    if (value instanceof Float || value instanceof Double) {
      return new DSReal(value.doubleValue());
    } else if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
      return new DSInteger(value.longValue());
    }

    return null;
  }

  /**
   * Creates a new instance of {@link DSBoolean}. In cases where the {@link DSBoolean} doesn't need
   * an object-/generation-number (e.g. when no {@link ReferenceResolver} is involved), the
   * constants {@link DSBoolean#TRUE} and {@link DSBoolean#FALSE} can be used instead.
   *
   * @param value
   * @return a newly constructed instance
   */
  public static DSBoolean bool(boolean value) {
    return new DSBoolean(value);
  }

  public static DSHexString hex(byte[] rawData) {
    return new DSHexString(rawData);
  }

  public static DSLiteralString literal(byte[] bytes) {
    return new DSLiteralString(bytes);
  }

  public static DSLiteralString literalText(String input) {
    // for now, we always create UTF-16 Strings. Support for PDFDocEncoding might be added later.
    final byte[] rawData = Strings.utf16Bytes(input);
    return new DSLiteralString(rawData);
  }

  public static DSInteger integer(int value) {
    return integer((long) value);
  }

  public static DSInteger integer(long value) {
    return new DSInteger(value);
  }

  public static DSInteger integer(Integer value) {
    return integer(value.longValue());
  }

  /**
   * Creates a new {@link DSReference} with a default generation number of '0'.
   *
   * @param objectNumber the PDF object number of the indirect object to be referenced
   * @return a new instance of {@link DSReference} which references the given object number at a
   * generation number of '0'
   */
  public static DSReference ref(long objectNumber) {
    return ref(objectNumber, 0);
  }

  public static DSReference ref(long objectNumber, int generationNumber) {
    return new DSReference(objectNumber, generationNumber);
  }

  public static DSObject nullObj() {
    return DSNullObject.INSTANCE;
  }

  /**
   * Create a {@link DSStream} using the given data. A stream dictionary will be created
   * automatically. It will include the required <code>/Length</code> entry set to the data array's
   * length.
   *
   * @param streamData the actual stream payload, assumed to be uncompressed. Must not be null!
   * @return a {@link DSStream} offering the given payload
   */
  public static DSStream stream(byte[] streamData) {
    return stream(null, streamData);
  }

  /**
   * Creates a {@link DSStream} which associates the given stream dictionary with the given data.
   * The data is assumed to be uncompressed. The data array's length is added as
   * <code>/Length</code> entry to the stream dictionary.
   *
   * @param streamDict a pre-configured stream dictionary, or <code>null</code>. In case of a
   *                   <code>null</code> parameter, a default, empty stream dictionary will be created. In
   *                   any case, the required <code>/Length</code> entry is added to the dictionary.
   * @param streamData the actual stream payload, assumed to be uncompressed. Must not be null!
   * @return a {@link DSStream} created from the given parameters
   */
  public static DSStream stream(DSDictionary streamDict, byte[] streamData) {
    Objects.requireNonNull(streamData, "streamData");
    if (streamDict == null) {
      streamDict = new DSCommonDictionary();
    }
    streamDict.addNamedEntry(name("Length"), integer(streamData.length));
    final IStreamFactory streamFactory = new ByteArrayStreamFactory(streamData);
    return new DSStream(streamDict, streamFactory);
  }

  /**
   * Creates a {@link DSRectangle} based on the given corner points.
   *
   * @param llx lower left X position
   * @param lly lower left Y position
   * @param urx upper right X position
   * @param ury upper right Y position
   * @return the {@link DSRectangle}
   */
  public static DSRectangle rect(double llx, double lly, double urx, double ury) {
    final Point2D.Double ll = new Point2D.Double(llx, lly);
    final Point2D.Double ur = new Point2D.Double(urx, ury);

    return new DSRectangle(ll, ur);
  }


  private DS() {
    // no instantiation -- only static methods
  }
}
