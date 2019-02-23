package org.cloud.wetag.utils;

import org.cloud.wetag.R;

import java.util.List;

public class ColorUtils {

  public static int getLabelBackgroundColor(List<String> labelDefinition, String inputLabel) {
    int index = labelDefinition.indexOf(inputLabel);
    if (index == 0) {
      return R.color.darkgreen;
    } else if (index == 1) {
      return R.color.darkred;
    } else if (index == 2) {
      return R.color.darkgray;
    } else if (index == 3) {
      return R.color.blue_dark;
    } else {
      return R.color.gray;
    }
  }
}
