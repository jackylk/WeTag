package org.cloud.wetag.ui.widget;

import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;

import org.cloud.wetag.MyApplication;
import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.utils.ColorUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LabelBar implements CompoundButton.OnCheckedChangeListener {

//  private List<String> labelSelection;
  private ChipGroup chipGroup;
  private Map<String, Chip> chipMap;
  private View parentView;
  private DataSet dataSet;

  public LabelBar(View parentView, DataSet dataSet, View.OnClickListener confirmListener) {
    this.parentView = parentView;
    this.dataSet = dataSet;
//    this.labelSelection = new LinkedList<>();
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
      chip.setTextColor(ContextCompat.getColor(MyApplication.getContext(), R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      chip.setOnCheckedChangeListener(this);
      chipGroup.addView(chip);
      chipMap.put(label, chip);
    }
  }

  public void setEnableLabelBar(boolean enabled) {
    parentView.findViewById(R.id.label_confirm).setEnabled(enabled);
    for (Chip chip : chipMap.values()) {
      chip.setEnabled(enabled);
    }
  }

  public void setCheckedLabelBar(boolean checked) {
    parentView.findViewById(R.id.label_confirm).setEnabled(checked);
    for (Chip chip : chipMap.values()) {
      chip.setChecked(checked);
    }
  }

  public Map<String, Chip> getChipMap() {
    return chipMap;
  }

  public void clear() {
    chipGroup.clearCheck();
//    labelSelection.clear();
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

  // chip clicked
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//      String label = buttonView.getText().toString();
//      if (isChecked) {
//        labelSelection.add(label);
//      } else {
//        labelSelection.remove(label);
//      }
  }
}
