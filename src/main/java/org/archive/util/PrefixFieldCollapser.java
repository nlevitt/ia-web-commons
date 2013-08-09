package org.archive.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.format.cdx.FieldSplitLine;
import org.archive.format.cdx.FieldSplitFormat;

/**
 * Dedupes a FieldSplitLine by a specific field, or part of a specific field eg:
 * 
 * <field> = if <field> matches previous match, then its a dupe <field>:<n> = if
 * first <n> character of <field> match, then its a dupe
 * 
 * 
 * @author ilya
 * 
 */
public class PrefixFieldCollapser {

  final static String FIELD_SEP_CHAR = ":";

  final protected FieldSplitFormat names;
  final protected List<DupeMatch> dupeMatchers;

  class DupeMatch {
    final int fieldIndex;
    final int substrLength;

    String prevValue;

    DupeMatch(String str) {
      try {
        str = URLDecoder.decode(str, "UTF-8");
      } catch (UnsupportedEncodingException e) {

      }

      int sepIndex = str.indexOf(FIELD_SEP_CHAR);

      String field;

      // Match entire field
      if (sepIndex < 0) {
        field = str;
        substrLength = -1;
      } else {
        field = str.substring(0, sepIndex);
        substrLength = NumberUtils.toInt(str.substring(sepIndex + 1));
      }

      // First try parsing as int
      int index = NumberUtils.toInt(field, -1);

      // Then try names if available
      if ((index < 0) && (names != null)) {
        index = names.getFieldIndex(field);
      }

      fieldIndex = index;
    }

    boolean isUnique(FieldSplitLine line) {
      String currValue = line.getField(fieldIndex);

      if ((substrLength > 0) && (substrLength <= currValue.length())) {
        currValue = currValue.substring(0, substrLength);
      }

      boolean unique = (prevValue == null) || !currValue.equals(prevValue);

      prevValue = currValue;

      return unique;
    }
  }

  public PrefixFieldCollapser(String[] fields, FieldSplitFormat names) {
    this.names = names;
    
    this.dupeMatchers = new ArrayList<DupeMatch>(fields.length);

    for (String field : fields) {
      if (!field.isEmpty()) {
        dupeMatchers.add(new DupeMatch(field));
      }
    }
  }

  public boolean isUnique(FieldSplitLine line) {
    for (DupeMatch duper : dupeMatchers) {
      if (!duper.isUnique(line)) {
        return false;
      }
    }

    return true;
  }
}
