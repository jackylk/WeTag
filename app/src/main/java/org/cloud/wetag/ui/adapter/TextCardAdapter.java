package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.design.chip.Chip;
import android.view.View;
import android.widget.TextView;

import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;

import java.util.List;

public class TextCardAdapter extends DataObjectCardAdapter {

  public TextCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                         String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
  }

  @Override
  void drawDataObject(Context context, CardItemViewHolder holder, DataObject dataObject,
                      int position) {
    List<String> labels = dataObject.getLabels();
    for (Chip chip : holder.chips) {
      if (labels.contains(chip.getText().toString())) {
        chip.setChecked(true);
      } else {
        chip.setChecked(false);
      }
    }
    ((TextView) holder.dataObjectView).setText(dataObject.getSource());
    if (objectSelection.isSelectEnabled()) {
      holder.checkView.setVisibility(View.VISIBLE);
      if (objectSelection.get().contains(dataObject)) {
        holder.checkView.setChecked(true);
      } else {
        holder.checkView.setChecked(false);
      }
    } else {
      holder.checkView.setVisibility(View.GONE);
    }
  }

}
