package com.levigo.jadice.format.pdf.internal;

import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSBoolean;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNullObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNumber;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSRectangle;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;
import com.levigo.jadice.format.pdf.internal.objects.DSString;

public interface ReferenceResolver {

  DSObject resolve(DSObject ref);


  default DSArray resolveArray(DSDictionary dict, DSNameObject name) {
    return Utils.getArray(resolveObject(dict, name));
  }

  default DSArray resolveArray(DSDictionary dict, String name) {
    return Utils.getArray(resolveObject(dict, name));
  }

  default DSArray resolveArray(DSObject obj) {
    return Utils.getArray(resolve(obj));
  }

  default DSBoolean resolveBool(DSDictionary dict, String name) {
    return Utils.getBoolean(resolveObject(dict, name));

  }

  default boolean resolveBool(DSDictionary ref, String name, boolean defaultValue) {
    return resolveBool(resolveObject(ref, name), defaultValue);
  }

  default boolean resolveBool(DSObject ref, boolean defaultValue) {
    DSObject obj;
    DSBoolean bool;
    if ((obj = resolve(ref)) == null || (bool = Utils.getBoolean(obj)) == null) {
      return defaultValue;
    }
    return bool.getValue();
  }

  default DSDictionary resolveDictionary(DSDictionary dict, DSNameObject name) {
    return Utils.getDictionary(resolveObject(dict, name));
  }

  default DSDictionary resolveDictionary(DSDictionary dict, String name) {
    return Utils.getDictionary(resolveObject(dict, name));
  }

  default DSDictionary resolveDictionary(DSObject obj) {
    return Utils.getDictionary(resolve(obj));
  }

  default double resolveDouble(DSDictionary dict, String name, double defaultValue) {
    final DSNumber num = resolveNumber(dict, name);
    if (num == null) {
      return defaultValue;
    }
    return num.getDouble();
  }

  default double resolveDouble(DSObject ref, double defaultValue) {
    DSObject obj;
    DSNumber num;
    if ((obj = resolve(ref)) == null || (num = Utils.getNumber(obj)) == null) {
      return defaultValue;
    }
    return num.getDouble();
  }

  default float resolveFloat(DSDictionary dict, String name, float defaultValue) {
    final DSNumber num = resolveNumber(dict, name);
    if (num == null) {
      return defaultValue;
    }
    return num.getFloat();
  }

  default float resolveFloat(DSObject ref, float defaultValue) {
    DSObject obj;
    DSNumber num;
    if ((obj = resolve(ref)) == null || (num = Utils.getNumber(obj)) == null) {
      return defaultValue;
    }
    return num.getFloat();
  }

  default int resolveInt(DSDictionary ref, String name, int defaultValue) {
    final DSNumber num = resolveNumber(ref, name);
    if (num == null) {
      return defaultValue;
    }
    return num.getInteger();
  }

  default int resolveInt(DSDictionary dict, String name, String alternativeName, int defVal) {
    DSObject o;

    DSNumber num;
    if ((o = resolveObject(dict, name)) != null && (num = Utils.getNumber(o)) != null) {
      return num.getInteger();
    }
    if ((o = resolveObject(dict, alternativeName)) != null && (num = Utils.getNumber(o)) != null) {
      return num.getInteger();
    }
    return defVal;
  }

  default int resolveInt(DSObject ref, int defaultValue) {
    DSObject obj;
    DSNumber num;
    if ((obj = resolve(ref)) == null || (num = Utils.getNumber(obj)) == null) {
      return defaultValue;
    }
    return num.getInteger();
  }

  default long resolveLong(DSObject ref, long defaultValue) {
    DSObject obj;
    DSNumber num;
    if ((obj = resolve(ref)) == null || (num = Utils.getNumber(obj)) == null) {
      return defaultValue;
    }
    return num.getLong();
  }

  default DSNameObject resolveName(DSDictionary dict, DSNameObject name) {
    return Utils.getNameObject(resolveObject(dict, name));
  }

  default DSNameObject resolveName(DSDictionary dict, String name) {
    return Utils.getNameObject(resolveObject(dict, name));
  }

  default DSNumber resolveNumber(DSDictionary dict, DSNameObject name) {
    return Utils.getNumber(resolveObject(dict, name));
  }

  default DSNumber resolveNumber(DSDictionary dict, String name) {
    return Utils.getNumber(resolveObject(dict, name));
  }

  default DSNumber resolveNumber(DSObject obj) {
    return Utils.getNumber(resolve(obj));
  }

  default DSObject resolveObject(DSDictionary dict, DSNameObject name) {

    if (dict == null || name == null) {
      throw new IllegalArgumentException("Arguments for this method can't be null");
    }

    final DSObject obj = dict.getNamedEntryValue(name);

    if (obj == null || obj instanceof DSNullObject) {
      return null;
    }

    // resolve the reference and return the object found
    return resolve(obj);
  }

  default DSObject resolveObject(DSDictionary dict, String name) {
    if (dict == null || name == null) {
      throw new IllegalArgumentException("Arguments for this method must not be null");
    }

    final DSObject obj = dict.getNamedEntryValue(name);

    if (obj == null || obj instanceof DSNullObject) {
      return null;
    }

    // resolve the reference and return the object found
    return resolve(obj);
  }

  default DSObject resolveObject(DSDictionary dict, String name, String alternativeName) {
    DSObject o;
    o = resolveObject(dict, name);
    if (o == null) {
      o = resolveObject(dict, alternativeName);
    }
    return o;
  }

  default DSRectangle resolveRectangle(DSDictionary dict, DSNameObject name) {
    return Utils.getRectangle(resolveObject(dict, name));
  }

  default DSRectangle resolveRectangle(DSDictionary dict, String name) {
    return Utils.getRectangle(resolveObject(dict, name));
  }

  default DSStream resolveStream(DSDictionary dict, DSNameObject name) {
    return Utils.getStream(resolveObject(dict, name));
  }

  default DSStream resolveStream(DSDictionary dict, String name) {
    return Utils.getStream(resolveObject(dict, name));
  }

  default DSStream resolveStream(DSObject obj) {
    return Utils.getStream(resolve(obj));
  }

  default DSString resolveString(DSDictionary dict, DSNameObject name) {
    return Utils.getString(resolveObject(dict, name));
  }

  default DSString resolveString(DSDictionary dict, String name) {
    return Utils.getString(resolveObject(dict, name));
  }

}
