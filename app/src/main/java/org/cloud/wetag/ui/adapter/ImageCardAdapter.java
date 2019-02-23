package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.utils.ColorUtils;

import java.util.List;

public class ImageCardAdapter extends DataObjectCardAdapter
    implements View.OnLongClickListener {

  private boolean inMultiSelectMode;

  public ImageCardAdapter(DataSet dataSet, ObjectSelection objectSelection, int type,
                          String filterLabel) {
    super(dataSet, objectSelection, type, filterLabel);
    inMultiSelectMode = false;
  }

  @Override
  void onBindDataObject(Context context, CardItemViewHolder holder, DataObject dataObject,
                        int position) {
    ImageView imageView = (ImageView) holder.dataObjectView;
    Glide.with(context).load(dataObject.getUri()).into(imageView);
    imageView.setOnLongClickListener(this);
    imageView.setTag(R.id.dataobject_view, position);
    if (inMultiSelectMode) {
      imageView.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
      holder.checkView.setVisibility(View.VISIBLE);
      if (objectSelection.exist(dataObject)) {
        imageView.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        holder.checkView.setEnabled(true);
        holder.checkView.setChecked(true);
      } else {
        holder.checkView.setEnabled(false);
        holder.checkView.setChecked(false);
      }
    } else {
      imageView.clearColorFilter();
      holder.checkView.setVisibility(View.GONE);
    }

    holder.chipGroup.removeAllViews();
    List<String> labels = dataObject.getOrLoadLabels();
    for (String label : labels) {
      Chip chip = new Chip(holder.chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(true);
      chip.setClickable(false);
      chip.setTextAppearance(R.style.TextAppearance_MaterialComponents_Body1);
      chip.setTextColor(ContextCompat.getColor(context, R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      holder.chipGroup.addView(chip);
      holder.chips.add(chip);
    }
  }

  @Override
  public boolean onLongClick(View v) {
    inMultiSelectMode = true;
    onDataObjectClicked((Integer) v.getTag(R.id.dataobject_view));
    notifyDataSetChanged();
    return true;
  }

  @Override
  void onDataObjectClicked(int position) {
    if (inMultiSelectMode) {
      onDataObjectCheckClicked(position);
    } else {
      super.onDataObjectClicked(position);
    }
  }

  /**
   * back is pressed by user in parent activity
   * @return true if this class consumed the event
   */
  @Override
  public boolean onBackPressed() {
    if (inMultiSelectMode) {
      inMultiSelectMode = false;
      objectSelection.clear();
      notifyDataSetChanged();
      return true;
    }
    return false;
  }
}
