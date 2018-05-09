package com.levigo.jadice.format.pdf.internal;

public class PDFFormatVersionInfo {

  public static final PDFFormatVersionInfo UNKNOWN_FORMAT = new PDFFormatVersionInfo(0, 0) {
    @Override
    public boolean isVersionKnown() {
      return false;
    }

    @Override
    public String toString() {
      return "UNKNOWN";
    }
  };

  private static final String DOC_VER_ARCHIVING = "PDF/A";

  private final int major;
  private final int minor;
  // true if this document is PDF/A
  private int archivingLevel = -1;

  private char archivingComplianceLevel = ' ';
  private String archivingAmendment = null;

  public PDFFormatVersionInfo(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  public int getMinorVersionNumber() {
    return minor;
  }

  public int getMajorVersionNumber() {
    return major;
  }

  public boolean isArchivingFormat() {
    // first check for PDF/A-1(a,b)
    if (archivingLevel == 1) {
      return archivingComplianceLevel == 'a' //
          || archivingComplianceLevel == 'b' //
          || archivingComplianceLevel == 'A' //
          || archivingComplianceLevel == 'B';
    } else if (archivingLevel == 2) {
      return archivingComplianceLevel == 'a' //
          || archivingComplianceLevel == 'b' //
          || archivingComplianceLevel == 'A' //
          || archivingComplianceLevel == 'B' //
          || archivingComplianceLevel == 'u' //
          || archivingComplianceLevel == 'U';
    }
    return false;
  }

  public boolean isVersionKnown() {
    return true;
  }

  void setArchivingInformation(int archivingLevel, char complianceLevel, String archivingAmendment) {
    this.archivingLevel = archivingLevel;
    archivingComplianceLevel = Character.toLowerCase(complianceLevel);
    this.archivingAmendment = archivingAmendment;
  }

  public char getArchivingComplianceLevel() {
    return archivingComplianceLevel;
  }

  public int getArchivingLevel() {
    return archivingLevel;
  }

  @Override
  public String toString() {

    final StringBuffer b = new StringBuffer();
    if (isArchivingFormat()) {
      b.append(DOC_VER_ARCHIVING);
      b.append('-');
      b.append(archivingLevel);
      b.append(archivingComplianceLevel);
      if (archivingAmendment != null)
        b.append(archivingAmendment);
      b.append(" [");
    }

    b.append("PDF-1.");
    b.append(getMinorVersionNumber());

    if (isArchivingFormat()) {
      b.append(']');
    }
    return b.toString();
  }

}
