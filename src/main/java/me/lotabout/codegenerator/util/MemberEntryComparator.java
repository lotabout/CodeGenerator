package me.lotabout.codegenerator.util;

import java.util.Comparator;

public class MemberEntryComparator implements Comparator<MemberEntry<?>> {
  private final int sort;

  public MemberEntryComparator(final int sort) {
    this.sort = sort;
  }

  public int compare(final MemberEntry<?> e1, final MemberEntry<?> e2) {
    if (this.sort == 0) {
      return 0;
    } else {
      final String name1 = getElementNameNoLeadingUnderscore(e1);
      final String name2 = getElementNameNoLeadingUnderscore(e2);
      int res = name1.compareToIgnoreCase(name2);
      if (this.sort == 2) {
        res = -1 * res;
      }

      return res;
    }
  }

  private static String getElementNameNoLeadingUnderscore(final MemberEntry<?> e) {
    final String name = e.getName();
    return name.startsWith("_") ? name.substring(1) : name;
  }
}
