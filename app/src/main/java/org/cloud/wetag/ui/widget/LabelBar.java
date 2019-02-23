package org.cloud.wetag.ui.widget;

import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.utils.ColorUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LabelBar {

  private ChipGroup chipGroup;
  private Map<String, Chip> chipMap;
  private View parentView;
  private DataSet dataSet;

  public LabelBar(View parentView, DataSet dataSet, View.OnClickListener confirmListener) {
    this.parentView = parentView;
    this.dataSet = dataSet;
    initLabelBar(confirmListener);
  }

  private void initLabelBar(View.OnClickListener confirmListener) {
    parentView.findViewById(R.id.label_confirm).setOnClickListener(confirmListener);
    chipGroup = parentView.findViewById(R.id.label_chipgroup);
    chipMap = new HashMap<>();
    for (String label : dataSet.getLabels()) {
      Chip chip = new Chip(chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(false);
      chip.setClickable(true);
      chip.setCheckable(true);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
  }

  public void setEnabled(boolean enabled) {
    int textColor = enabled ? ContextCompat.getColor(MyApplication.getContext(), R.color.white) :
        ContextCompat.getColor(MyApplication.getContext(), R.color.gray);
    Button confirmButton = parentView.findViewById(R.id.label_confirm);
    confirmButton.setEnabled(enabled);
    confirmButton.setTextColor(textColor);
    for (Chip chip : chipMap.values()) {
      if (enabled) {
        chip.setChipBackgroundColorResource(
            ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), chip.getText().toString()));
        chip.setTextColor(textColor);
      } else {
        chip.setChipBackgroundColorResource(R.color.dracula_primary_dark);
        chip.setTextColor(textColor);
      }
      chip.setEnabled(enabled);
    }
  }

  public void setChecked(boolean checked) {
    parentView.findViewById(R.id.label_confirm).setEnabled(checked);
    for (Chip chip : chipMap.values()) {
      chip.setChecked(checked);
    }
  }

  public void setVisible(boolean visible) {
    if (visible) {
      parentView.setVisibility(View.VISIBLE);
    } else {
      parentView.setVisibility(View.GONE);
    }
  }

  public Map<String, Chip> getChipMap() {
    return chipMap;
  }

  public void clear() {
    chipGroup.clearCheck();
  }

  public List<String> getLabelSelection() {
    List<String> labelSelection = new LinkedList<>();
    for (Map.Entry<String, Chip> chipEntry : chipMap.entrySet()) {
      if (chipEntry.getValue().isChecked()) {
        labelSelection.add(chipEntry.getKey());
      }
    }
    return labelSelection;
  }
}
