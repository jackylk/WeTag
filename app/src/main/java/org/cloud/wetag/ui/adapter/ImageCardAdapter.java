package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.Image;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ImageViewHolder> {

  private DataSet dataSet;
  private Context context;

  public ImageCardAdapter(DataSet dataSet) {
    this.dataSet = dataSet;
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(R.layout.image_item, viewGroup, false);
    return new ImageCardAdapter.ImageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    Image image = dataSet.getImage(position);
    Glide.with(context).load(image.getUri()).into(holder.imageView);
    for (String label : image.getLabels()) {
      Chip chip = new Chip(holder.chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(false);
      holder.chipGroup.addView(chip);
    }
  }

  @Override
  public int getItemCount() {
    return dataSet.getImageCount();
  }

  static class ImageViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    ChipGroup chipGroup;

    public ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      CardView card = (CardView) itemView;
      imageView = card.findViewById(R.id.image_iv);
      chipGroup = card.findViewById(R.id.image_chipgroup);
    }
  }
}
