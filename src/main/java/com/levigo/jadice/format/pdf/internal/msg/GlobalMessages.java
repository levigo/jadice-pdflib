package com.levigo.jadice.format.pdf.internal.msg;

public interface GlobalMessages {


  String UNSUPPORTED_FUNCTION_TYPE = "unsupported function (Type: {0})";


  String FUNCTION_STREAM_READ_FAILURE = "unable to load function stream";


  String FUNCTION_SAMPLE_READ_FAILURE = "unable to read sample";


  String EXT_GSPACE_LOADING_FAILED_USING_DEFAULT = "unable to load external ColorSpace. Using default ColorSpace: {0}";


  String IMAGE_LOADING_FAILED = "unable to load image.";


  String INLINE_IMAGE_INFO_BROKEN = "information of inline image is broken.";


  String IMAGE_DATA_READ_FAILURE = "unable to read inline image data.";


  String REQUESTED_XOBJECT_NOT_FOUND = "requested XObject has not been found: {0}";


  String MISSING_XOBJECT_SUBTYPE = "XObject subtype not available";


  String UNKNOWN_TYPE_OF_XOBJECT = "unknown type of XObject: {0}";


  String FORM_DICT_REF_ENTRY_NOT_SUPPORTED = "Encountered Ref Dictionary in Form Dictionary. Ref entries are not supported.";


  String UNSUPPORTED_COLORSPACE = "Unsupported colorspace {0}.";


  String USING_DEFAULT_COLORSPACE = "Using default colorspace {0}.";


  String INSUFFICIENT_VALUES_FOR_SEPARATION_CS = "unable to create Separation colorspace because of insufficient values";


  String CS_MISSING_SEPARATION_BASE = "missing Base ColorSpace for Separation. Using Default ColorSpace";


  String CS_MISSING_TINT_TRANSFORM = "unable to create colorspace due to missing tint transformation";


  String CS_SUBSTITUTION_FAILED = "unable to use a substituted color space for the ICC Profile. Using default: {0}";


  String CS_MISSING_INDEXED_BASE = "unable to get base colorspace for indexed ColorSpace. Using default: {0}";


  String CS_INDEXED_MISSING_HIGHVAL = "no highval given for indexed color space. Using default colorSpace: {0}";


  String CS_INDEXED_LUT_MISSING = "the lookup table of an indexed color space is empty. Using default ColorSpace instead: {0}";


  String CS_INDEXED_CV_MISSING = "color values for indexed colorspace is missing";


  String CS_INDEXED_INVALID = "index color space is defined wrong. Using default colorspace. Image may be displayed broken";


  String CS_TINT_TX_WRONG_VALUE_COUNT = "Unbalanced number of colourant names and tint transform input value count";


  String INVALID_COLOR_CALIBRATION_MATRIX = "invalid color calibration matrix";


  String ENCRYPTED_DOCUMENTS_NOT_SUPPORTED = "Encrypted PDF documents in this configuration are not supported";


  String DOCUMENT_HAS_NO_PAGES = "No pages have been found in the document.";


  String IOEXCEPTION_OBJECT_RESOLVING = "received IOException while trying read PDF structure";


  String PREDICTION_MISSING_IMAGE_SIZE = "image sizes not given but needed for prediction. Predition won't be applied. Image produced may be unexpected";


  String INSUFFICIENT_PARAMETER_COUNT = "insufficient count of filter parameters, ignoring parameters";


  String SOURCE_STREAM_RESET = "unable to reset resource Stream.";


  String DETERMINE_LENGTH_FAILED = "Unable to determine the REQUIRED document length.";


  String PDFLexer_131 = "unable to read region completely. region output may be corrupt";


  String SEEK_IN_DOCUMENT_FAILED = "unable to reset location for reading. file handling may break completely";


  String PAGE_CONTENTS_INITIALIZATION_FAILURE = "Unable to initialize PDF page contents";


  String REQUESTED_FONT_NOT_DECLARED = "requested font has not been defined in the document: {0}";


  String MISSING_EXTERNAL_GRAPHICS_STATES = "no external graphics states available in this document. Result may contain invalid contents";


  String UNSUPPORTED_FIELD = "Unsupported form field of type {0}";


  String DOCUMENT_STREAM_ACCESS_FAILED = "Failed to access the document stream data";


  String FONT_MISSING_SUBTYPE = "Subtype entry in font dictionary is missing";


  String FONT_ILLEGAL_SUBTYPE = "Missing type identifier for the requested font. PDF/A requires an identifier.";


  String NOT_A_FONT_DICTIONARY = "The given dictionary is not a font dictionary";


  String UNSUPPORTED_COLORSPACE_SHADING_PATTERN = "Unsupported Color Space: Shading Pattern";


  String INLINE_IMAGE_INFLATE_FAILED = "Flate data decompressoin of inline image failed.";


  String UNSUPPORTED_COMPRESSION = "Unsupported compression type {0}.";


  String FUNCTION_TYPE4_ERROR = "illegal type4 function definition";


  String FUNCTION_TYPE4_INSUFFICIENT_RETURN = "type4 function returned not enough values";


  String FUNCTION_TYPE4_ILLEGAL_RETURN = "type4 function returned non number result";


  String FUNCTION_TYPE4_TOOMANY_RETURN = "type4 function retunred too many results";


  String FUNCTION_TYPE2_MISSING_N = "missing interpolation exponent in type 2 function.";


  String FUNCTION_TYPE2_UNBALANCED_C0_C1 = "unbalanced C0 and C1 arrays in type 2 function";


  String FUNCTION_TYPE2_ILLEGAL_C_VAL = "illegal value in C* array of a type 2 function";


  String FUNCTION_MISSING_FT = "missing function type";


  String CS_ILLEGAL_DEF = "illegal color space definition";


  String CS_DEVN_INSUFFICIENT_VALS = "insufficient values for DeviceN colorspace definition";


  String CS_DEVN_ILLEGAL_DEF = "illegal DeviceN color space definition";


  String CS_MISSING_ALT_CS = "missing alternate color space declaration";


  String MISSING_EXTERNAL_GRAPHICS_STATE = "Missing referenced external graphics state {0}";


  String INVALID_DESTINATION = "An invalid destination specification was encountered: {0}";


  String UNSUPPORTED_DESTINATION_TARGET_TYPE = "Unsupported destination target type: {0}";


  String UNSUPPORTED_ACTION = "The action {0} is currently unsupported";

  String ILLEGAL_DASH_PATTERN_SPECIFICATION = "An invalid dash-pattern specification was encountered";


  String UNSUPPORTED_CONTENT_XFA = "This document contains XFA form data which represents the actual document content. XFA forms are not supported.";


  String COLOR_OPERATIONS_NOT_ALLOWED = "Color operations not allowed";


  String METADATA_READ_ERROR = "Error reading metadata";


  String MISSING_XOBJECT_RESOURCES = "Missing resources with XObjects.";


  String MISSING_XOBJECT_DATA = "Missing data for XObject {0}.";


  String FAILED_TO_CREATE_ATTACHMENT = "Failed to create attachment {0}.";


}
