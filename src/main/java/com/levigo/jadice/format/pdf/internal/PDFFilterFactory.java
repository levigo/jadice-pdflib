package com.levigo.jadice.format.pdf.internal;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.internal.codec.ASCII85InputStream;
import com.levigo.jadice.document.internal.codec.ASCIIHexInputStream;
import com.levigo.jadice.document.internal.codec.LZWInputStream;
import com.levigo.jadice.document.internal.codec.PNGPredictorInputStream;
import com.levigo.jadice.document.internal.codec.PackbitsInputStream;
import com.levigo.jadice.document.internal.codec.ZInflaterInputStream;
import com.levigo.jadice.document.internal.codec.tiff.TIFFPredictorInputStream;
import com.levigo.jadice.document.io.ByteArraySeekableInputStream;
import com.levigo.jadice.document.io.ConcatenatedInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.crypt.NoSecurityHandler;
import com.levigo.jadice.format.pdf.internal.crypt.SecurityHandler;
import com.levigo.jadice.format.pdf.internal.crypt.StreamDecryptor;
import com.levigo.jadice.format.pdf.internal.msg.GlobalMessages;
import com.levigo.jadice.format.pdf.internal.msg.PDFSecurityMessages;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSCommonDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNullObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;

public final class PDFFilterFactory {
  private static final FilterEntry[] EMPTY_FILTER_ARRAY = new FilterEntry[0];

  private static final Map<String, Filter> FILTER_MAPPING;
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFFilterFactory.class);

  public enum Filter { //
    ASCII85(new ASCII85FilterStreamFactory(), "ASCII85Decode", "A85"), //
    ASCIIHEX(new ASCIIHexFilterStreamFactory(), "ASCIIHexDecode", "AHx"), //
    FLATE(new FlateFilterStreamFactory(), "FlateDecode", "Fl"), //
    LZW(new LZWFilterStreamFactory(), "LZWDecode", "LZW"), //
    RUNLENGTH(new RLEFilterStreamFactory(), "RunLengthDecode", "RL"), //
    CCITTFAX("CCITTFaxDecode", "CCF"), //
    CRYPT(new CryptFilterStreamFactory(), "Crypt"), //
    DCT("DCTDecode", "DCT"), //
    JBIG2("JBIG2Decode"), //
    JPX("JPXDecode", "JPX"), //
    PREDICTOR_PNG(new PNGPredictorFilterStreamFactory()), //
    PREDICTOR_TIFF(new TIFFPredictorFilterStreamFactory());

    private final FilterStreamFactory filterStreamFactory;

    private final String[] aliases;

    Filter(FilterStreamFactory fsf, String... aliases) {
      filterStreamFactory = fsf;
      if (aliases != null)
        this.aliases = aliases;
      else
        this.aliases = new String[0];
    }

    Filter(String... aliases) {
      this(null, aliases);
    }

    public String[] getAliases() {
      return aliases;
    }

    public FilterStreamFactory getFilterStreamFactory() {
      return filterStreamFactory;
    }

    public boolean isStreamDecompressionPossible() {
      return filterStreamFactory != null;
    }
  }

  protected interface FilterStreamFactory {
    SeekableInputStream create(SeekableInputStream sis, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler);
  }

  public static final class FilterEntry {
    private final Filter filter;

    private final DSDictionary streamDictionary;
    private final DSDictionary decodeParms;

    public FilterEntry(Filter filter, DSDictionary streamDictionary, DSDictionary decodeParms) {
      this.filter = filter;
      this.streamDictionary = streamDictionary;
      this.decodeParms = decodeParms;
    }

    public DSDictionary getDecodeParms() {
      return decodeParms;
    }

    public Filter getFilter() {
      return filter;
    }

    public DSDictionary getStreamDictionary() {
      return streamDictionary;
    }
  }

  private static final class ASCII85FilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      return new ASCII85InputStream(stream);
    }
  }

  private static final class ASCIIHexFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      return new ASCIIHexInputStream(stream);
    }
  }

  private static final class CryptFilterStreamFactory implements FilterStreamFactory {
    @Override
    public SeekableInputStream create(final SeekableInputStream sis, FilterEntry filterEntry,
        ReferenceResolver resolver, SecurityHandler securityHandler) {

      try {
        StreamDecryptor streamDecryptor = null;
        DSNameObject cryptFilterName = null;

        if (filterEntry.decodeParms == null) {
          // Try to get the default crypt filter
          streamDecryptor = securityHandler.getStreamDecryptor();
        } else {
          final DSNameObject type = resolver.resolveName(filterEntry.decodeParms, "Type");

          if (type != null && !type.matches("CryptFilterDecodeParms"))
            LOGGER.warn(PDFSecurityMessages.CRYPT_FILTER_DECODE_PARMS_TYPE_MISMATCH);

          cryptFilterName = resolver.resolveName(filterEntry.decodeParms, "Name");
          streamDecryptor = securityHandler.getStreamDecryptor(cryptFilterName);
        }

        // Try to get the default crypt filter
        if (streamDecryptor == null)
          streamDecryptor = securityHandler.getStreamDecryptor();

        if (streamDecryptor != null) {
          final DSDictionary streamDictionary = filterEntry.getStreamDictionary();
          return streamDecryptor.getDecryptedStream(streamDictionary, sis);
        }

      } catch (PDFSecurityException e) {
        LOGGER.error(PDFSecurityMessages.DECRYPTION_OF_STREAM_FAILED, e);
      }

      return sis;
    }
  }

  private static final class FlateFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      return new ZInflaterInputStream(stream);
    }
  }

  private static final class LZWFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      final DSDictionary decodeParms = filterEntry.getDecodeParms();
      final int earlyChange = null != decodeParms ? resolver.resolveInt(decodeParms, "EarlyChange", 1) : 1;
      final LZWInputStream s = new LZWInputStream(stream);
      if (earlyChange == 0) // early change is default for the LZWInputStream
        s.setUseEarlyChange(false);
      return s;
    }
  }

  private static final class PNGPredictorFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      final DSDictionary decodeParms = filterEntry.getDecodeParms();
      final int colors = resolver.resolveInt(decodeParms, "Colors", 1);
      final int columns = resolver.resolveInt(decodeParms, "Columns", 1);
      final int bpc = resolver.resolveInt(decodeParms, "BitsPerComponent", "BPC", 8);
      return new PNGPredictorInputStream(stream, colors, columns, bpc);
    }
  }

  private static final class RLEFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      return new PackbitsInputStream(stream, Integer.MAX_VALUE, false);
    }
  }

  private static final class TIFFPredictorFilterStreamFactory implements FilterStreamFactory {
    public SeekableInputStream create(SeekableInputStream stream, FilterEntry filterEntry, ReferenceResolver resolver,
        SecurityHandler securityHandler) {
      final DSDictionary decodeParms = filterEntry.getDecodeParms();
      final int bpc = resolver.resolveInt(decodeParms, "BitsPerComponent", "BPC", 8);
      final int colors = resolver.resolveInt(decodeParms, "Colors", 1);
      final int columns = resolver.resolveInt(decodeParms, "Columns", 1);

      final int bytesPerLine = ((colors * columns * bpc) / 8);

      final int width = resolver.resolveInt(filterEntry.getStreamDictionary(), "Width", "W", -1);
      final int height = resolver.resolveInt(filterEntry.getStreamDictionary(), "Height", "H", -1);

      if (width < 0 || height < 0) {
        LOGGER.error(GlobalMessages.PREDICTION_MISSING_IMAGE_SIZE);
        return stream;
      }
      return new TIFFPredictorInputStream(stream, 2, colors, bytesPerLine, new Dimension(width, height));
    }
  }

  public static final class FilteringResult {
    private final FilterEntry[] pendingFilters;
    private final FilterEntry[] processedFilters;
    private final SeekableInputStream stream;

    public FilteringResult(SeekableInputStream stream, FilterEntry[] pendingFilters, FilterEntry[] processedFilters) {
      this.stream = stream;
      this.pendingFilters = pendingFilters;
      this.processedFilters = processedFilters;
    }

    public FilterEntry[] getPendingFilters() {
      return pendingFilters;
    }

    public FilterEntry[] getProcessedFilters() {
      return processedFilters;
    }

    public SeekableInputStream getStream() {
      return stream;
    }
  }

  static {
    final Map<String, Filter> tmp = new HashMap<>(20);

    for (final Filter f : Filter.values()) {
      for (final String alias : f.getAliases()) {
        tmp.put(alias, f);
      }
    }

    FILTER_MAPPING = Collections.unmodifiableMap(tmp);
  }

  public static final boolean isCCITTFaxDecode(DSNameObject name) {
    return FILTER_MAPPING.get(name.getName()) == Filter.CCITTFAX;
  }

  public static final boolean isDCTDecode(DSNameObject name) {
    return FILTER_MAPPING.get(name.getName()) == Filter.DCT;
  }

  public static final boolean isJBIG2Decode(DSNameObject name) {
    return FILTER_MAPPING.get(name.getName()) == Filter.JBIG2;
  }

  public static final boolean isLZWDecode(DSNameObject name) {
    return FILTER_MAPPING.get(name.getName()) == Filter.LZW;
  }

  public static final boolean isRunLengthDecode(DSNameObject name) {
    return FILTER_MAPPING.get(name.getName()) == Filter.RUNLENGTH;
  }

  private final ReferenceResolver resolver;
  private SecurityHandler securityHandler;

  public PDFFilterFactory(ReferenceResolver resolver) {
    this(resolver, new NoSecurityHandler());
  }

  public PDFFilterFactory(ReferenceResolver resolver, SecurityHandler securityHandler) {
    this.resolver = resolver;
    this.securityHandler = securityHandler;
  }

  public List<FilterEntry> buildFilterChain(DSDictionary streamDictionary) {
    DSObject filter = null;
    DSObject params = null;
    // if no dictionary is given we have to return the pure stream
    if (streamDictionary != null) {
      filter = resolver.resolveObject(streamDictionary, "Filter", "F");
      params = resolver.resolveObject(streamDictionary, "DecodeParms", "DP");
    }

    final List<FilterEntry> filterChain = new ArrayList<>(5);

    if (filter == null || filter instanceof DSNullObject) {
      // no filters were specified. check the security handler.
      filterChain.add(buildFilterEntry(new DSNameObject("Crypt"), streamDictionary, null));
    } else if (filter.isNameObject()) {
      // there is just a single filter that has to be applied
      final DSNameObject filterName = (DSNameObject) filter;

      DSDictionary singleParam = null;

      if (params != null) {
        if (params instanceof DSDictionary) {
          singleParam = (DSDictionary) params;
        } else if (params instanceof DSArray && ((DSArray) params).size() == 1) {
          singleParam = resolver.resolveDictionary(((DSArray) params).get(0));
        }
      } else {
        if (filterName.matches("Crypt")) {
          singleParam = createCryptFilterDecodeParms();
        }
      }

      FilterEntry filterEntry = buildFilterEntry(filterName, streamDictionary, singleParam);

      // Check if we have to prepend a crypt filter. If the filter chain does not specify any crypt
      // filter, we have to prepend a crypt filter entry to ensure that the filter chain asks the
      // security handler.
      if (filterEntry.filter != Filter.CRYPT)
        filterChain.add(buildFilterEntry(new DSNameObject("Crypt"), streamDictionary, null));

      filterChain.add(filterEntry);

      filterEntry = buildPredictionEntry(filterEntry.getFilter(), streamDictionary, singleParam);

      if (filterEntry != null)
        filterChain.add(filterEntry);

    } else if (filter.isArray()) {
      final DSArray filters = (DSArray) filter;
      DSArray filterParameter = null;

      if (params != null) {
        if (params instanceof DSArray) {
          filterParameter = (DSArray) params;
        } else {
          filterParameter = new DSArray();

          // if only a single DecodeParms dictionary has been specified, we will
          // provide it to all filters
          for (int i = 0; i < filters.size() - 1; i++) {
            filterParameter.add(params);
          }
        }
      }

      if (filterParameter != null && filterParameter.size() != filters.size()) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(GlobalMessages.INSUFFICIENT_PARAMETER_COUNT);
        }
        filterParameter = null;
      }

      for (int i = 0; i < filters.size(); i++) {

        final DSNameObject filterName = (DSNameObject) filters.get(i);

        DSDictionary currentParams = null;

        if (filterParameter != null) {
          currentParams = resolver.resolveDictionary(filterParameter.get(i));
        } else {
          if (filterName.matches("Crypt")) {
            currentParams = createCryptFilterDecodeParms();
          }
        }

        FilterEntry filterEntry = buildFilterEntry(filterName, streamDictionary, currentParams);

        // Check if we have to prepend a crypt filter. If the filter chain does not specify any
        // crypt filter, we have to prepend a crypt filter entry to ensure the filter chain asks the
        // security handler.
        if (i == 0 && filterEntry.filter != Filter.CRYPT)
          filterChain.add(buildFilterEntry(new DSNameObject("Crypt"), streamDictionary, null));

        filterChain.add(filterEntry);

        filterEntry = buildPredictionEntry(filterEntry.getFilter(), streamDictionary, currentParams);
        if (filterEntry != null)
          filterChain.add(filterEntry);
      }

    } else {
      throw new IllegalArgumentException("the passed DSObject is neither a DSNameObject nor a DSArray");
    }
    return filterChain;
  }

  private DSDictionary createCryptFilterDecodeParms() {
    final DSDictionary singleParam = new DSCommonDictionary();
    singleParam.addNamedEntry(new DSNameObject("Type"), new DSNameObject("CryptFilterDecodeParms"));
    singleParam.addNamedEntry(new DSNameObject("Name"), new DSNameObject("Identity"));
    return singleParam;
  }

  protected FilterEntry buildFilterEntry(DSNameObject filter, DSDictionary streamDictionary,
      DSDictionary decodeParams) {
    final String filterName = filter.getName();
    final Filter f = FILTER_MAPPING.get(filterName);
    if (f == null) {
      throw new RuntimeException("Illegal filter type: " + filterName);
    }
    return new FilterEntry(f, streamDictionary, decodeParams);
  }

  protected FilterEntry buildPredictionEntry(Filter filter, DSDictionary streamDictionary, DSDictionary decodeParms) {
    if (decodeParms != null && (filter == Filter.LZW || filter == Filter.FLATE)) {

      final int predictor = resolver.resolveInt(decodeParms.getNamedEntryValue("Predictor"), 1);

      switch (predictor){
        case 1: // no predictor
          break;
        case 2: // TIFF predictor 2
          return new FilterEntry(Filter.PREDICTOR_TIFF, streamDictionary, decodeParms);
        case 10: // PNG prediction (on encoding, PNG None on all rows)
        case 11: // PNG prediction (on encoding, PNG Sub on all rows)
        case 12: // PNG prediction (on encoding, PNG Up on all rows)
        case 13: // PNG prediction (on encoding, PNG Average on all rows)
        case 14: // PNG prediction (on encoding, PNG Paeth on all rows)
        case 15: // PNG prediction (on encoding, PNG optimum)
          /*
           * PDF spec says:
           *
           * For LZWDecode and FlateDecode, a Predictor value greater than or equal to 10 merely indicates
           * that a PNG predictor is in use; the specific predictor function used is explicitly encoded in the
           * incoming data. The value of Predictor supplied by the decoding filter need not match the value
           * used when the data was encoded if they are both greater than or equal to 10.
           */

          return new FilterEntry(Filter.PREDICTOR_PNG, streamDictionary, decodeParms);
        default:
          throw new RuntimeException("Predictor not supported: " + predictor);
      }

    }
    return null;
  }

  /**
   * <code>generateFilterStream(PDFNameObject, InputStream)</code> creates an applicable
   * FilterInputStream based on the passed {@link DSNameObject}
   *
   * @param stream      the stream to be filtered
   * @param filterEntry
   * @return the stream
   * @throws RuntimeException
   */
  public SeekableInputStream generateFilterStream(SeekableInputStream stream, FilterEntry filterEntry)
      throws RuntimeException {

    final Filter filter = filterEntry.getFilter();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("using " + filter);
    }

    if (!filter.isStreamDecompressionPossible()) {
      // FIXME a PDFProcessingRuntimeException would be better
      throw new RuntimeException("Filter type " + filter + " is only supported for image decompression");
    }
    // generate the filter
    return filter.getFilterStreamFactory().create(stream, filterEntry, resolver, securityHandler);
  }

  public final FilteringResult buildFilterStreamChain(DSStream stream, boolean acceptNonStreamable) {
    final SeekableInputStream sis = stream.getSourceStream();
    try {
      sis.seek(0);
    } catch (final IOException e1) {
      if (LOGGER.isErrorEnabled()) {
        // FIXME wouldn't an exception be better?
        LOGGER.error(GlobalMessages.SOURCE_STREAM_RESET, e1);
      }
    }
    return buildFilterStreamChain(sis, buildFilterChain(stream.getDictionary()), acceptNonStreamable);
  }

  public FilteringResult buildFilterStreamChain(SeekableInputStream sis, List<FilterEntry> filterChain,
      boolean acceptNonStreamable) {

    final List<FilterEntry> processed = new ArrayList<>(2);

    for (final FilterEntry e : filterChain) {
      if (e.getFilter().isStreamDecompressionPossible()) {
        sis = generateFilterStream(sis, e);
        processed.add(e);
      } else {
        if (!acceptNonStreamable) {
          // FIXME a PDFProcessingRuntimeException would be better
          throw new RuntimeException("Filter type " + e.getFilter() + "is only supported for image decompression");
        }
        break; // for
      }
    }

    // check if we have pending steps
    FilterEntry[] pendingFilters;
    if (processed.size() < filterChain.size()) {
      pendingFilters = new FilterEntry[filterChain.size() - processed.size()];
      for (int i = 0; i < pendingFilters.length; i++) {
        pendingFilters[i] = filterChain.get(i + processed.size());
      }
    } else {
      // no pending filters
      pendingFilters = EMPTY_FILTER_ARRAY;
    }
    FilterEntry[] processedFilters;
    if (processed.size() > 0) {
      processedFilters = processed.toArray(new FilterEntry[processed.size()]);
    } else {
      processedFilters = EMPTY_FILTER_ARRAY;
    }
    return new FilteringResult(sis, pendingFilters, processedFilters);
  }

  public final SeekableInputStream getInputStreamFromPDFStream(DSStream stream) {
    return buildFilterStreamChain(stream, false).getStream();
  }

  public final SeekableInputStream getInputStreamFromPDFStreams(DSStream[] streams) {
    // FIXME is this really a good idea?
    if (streams == null)
      return new ByteArraySeekableInputStream(new byte[0]);

    if (streams.length > 1) {
      final ConcatenatedInputStream concatStream = new ConcatenatedInputStream();

      for (final DSStream stream : streams) {
        concatStream.appendInputStream(getInputStreamFromPDFStream(stream));
      }

      return concatStream;
    }

    return getInputStreamFromPDFStream(streams[0]);
  }
}
