package org.cloud.wetag.utils;

import org.cloud.wetag.R;

import java.util.List;

public class ColorUtils {

  public static int getLabelBackgroundColor(List<String> labelDefinition, String inputLabel) {
    int index = labelDefinition.indexOf(inputLabel);
    if (index == 0) {
      return R.color.green;
    } else if (index == 1) {
      return R.color.red;
    } else {
      return R.color.gray;
    }
  }
}
