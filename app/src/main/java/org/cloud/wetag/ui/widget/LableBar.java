package org.cloud.wetag.ui.widget;

import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

public class LableBar {

  private ChipGroup chipGroup;
  private Map<String, Chip> chipMap;

  private void initLabelBar(View view) {
    view.findViewById(R.id.label_confirm).setOnClickListener(this);
    chipGroup = findViewById(R.id.label_chipgroup);
    chipMap = new HashMap<>();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(false);
      chip.setClickable(true);
      chip.setCheckable(true);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      chip.setOnCheckedChangeListener(this);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
    setEnableLabelBar(false);
  }
}
