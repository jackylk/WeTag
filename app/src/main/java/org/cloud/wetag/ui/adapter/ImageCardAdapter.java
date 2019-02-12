package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.Image;
import org.cloud.wetag.model.ImageSelection;
import org.cloud.wetag.ui.widget.CheckView;

import java.io.File;
import java.util.Set;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ImageViewHolder> {

  private DataSet dataSet;
  private Context context;
  private ImageSelection imageSelection;
  private OnCheckChangedListener listener;

  public ImageCardAdapter(DataSet dataSet, ImageSelection imageSelection) {
    this.dataSet = dataSet;
    this.imageSelection = imageSelection;
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(R.layout.image_item, viewGroup, false);
    return new ImageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
    Image image = dataSet.getImage(position);
    Glide.with(context).load(image.getUri()).into(holder.imageView);
    holder.chipGroup.removeAllViews();
    Set<String> labels = image.getOrLoadLabels();
    for (String label : labels) {
      Chip chip = new Chip(holder.chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(true);
      chip.setClickable(false);
      holder.chipGroup.addView(chip);
    }
    holder.checkView.setChecked(false);
  }

  @Override
  public int getItemCount() {
    return dataSet.getImageCount();
  }

  public void registerOnCheckChangedListener(OnCheckChangedListener listener) {
    this.listener = listener;
  }

  public interface OnCheckChangedListener {
    void onImageCheckedChanged(Image image, boolean check);
  }

  class ImageViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    ChipGroup chipGroup;
    CheckView checkView;

    public ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      CardView card = (CardView) itemView;
      imageView = card.findViewById(R.id.image_iv);
      chipGroup = card.findViewById(R.id.image_chipgroup);
      checkView = card.findViewById(R.id.check_view);
      checkView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (v instanceof CheckView) {
            int position = getAdapterPosition();
            Image image = dataSet.getImage(position);
            if (!imageSelection.exist(image)) {
              imageSelection.add(image);
              checkView.setChecked(true);
              listener.onImageCheckedChanged(image,true);
            } else {
              imageSelection.remove(image);
              checkView.setChecked(false);
              listener.onImageCheckedChanged(image,false);
            }
          } else if (v instanceof ImageView) {
            Log.e("TAG", v.toString());
          } else if (v instanceof ChipGroup) {
            Log.e("TAG", v.toString());
          }
        }
      });
    }
  }
}
