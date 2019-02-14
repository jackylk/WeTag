package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.content.ContextCompat;
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
import org.cloud.wetag.utils.ColorUtils;

import java.util.LinkedList;
import java.util.List;

import static org.cloud.wetag.ui.PageFragment.ALL;
import static org.cloud.wetag.ui.PageFragment.ALL_LABELED;
import static org.cloud.wetag.ui.PageFragment.ALL_UNLABELED;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ImageViewHolder> {

  private Context context;
  private ImageSelection imageSelection;
  private OnImageCheckChangedListener listener;

  private DataSet dataSet;
  private List<Image> images;

  // type can be constant value in PageFragment
  private int type;
  private String filterLabel;

  public ImageCardAdapter(DataSet dataSet, ImageSelection imageSelection, int type,
                           String filterLabel) {
    this.imageSelection = imageSelection;
    this.dataSet = dataSet;
    this.type = type;
    this.filterLabel = filterLabel;
    refreshImages();
  }

  public void refreshImages() {
    if (type == ALL) {
      images = dataSet.getImages();
    } else if (type == ALL_UNLABELED) {
      images = new LinkedList<>();
      for (Image image : dataSet.getImages()) {
        if (image.getLabels().size() == 0) {
          images.add(image);
        }
      }
    } else if (type == ALL_LABELED) {
      images = new LinkedList<>();
      for (Image image : dataSet.getImages()) {
        if (image.getLabels().size() > 0) {
          images.add(image);
        }
      }
    } else {
      images = new LinkedList<>();
      for (Image image : dataSet.getImages()) {
        if (image.getLabels().contains(filterLabel)) {
          images.add(image);
        }
      }
    }
    notifyDataSetChanged();
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
    Image image = images.get(position);
    Glide.with(context).load(image.getUri()).into(holder.imageView);
    holder.chipGroup.removeAllViews();
    List<String> labels = image.getOrLoadLabels();
    for (String label : labels) {
      Chip chip = new Chip(holder.chipGroup.getContext());
      chip.setText(label);
      chip.setEnabled(true);
      chip.setClickable(false);
      chip.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
      chip.setTextColor(ContextCompat.getColor(context, R.color.white));
      chip.setChipBackgroundColorResource(
          ColorUtils.getLabelBackgroundColor(dataSet.getLabels(), label));
      holder.chipGroup.addView(chip);
    }
    holder.checkView.setChecked(false);
  }

  @Override
  public int getItemCount() {
    return images.size();
  }

  public void registerOnCheckChangedListener(OnImageCheckChangedListener listener) {
    this.listener = listener;
  }

  public interface OnImageCheckChangedListener {
    void onImageCheckClicked(Image image, boolean check);
    void onImageClicked(Image image);
  }

  class ImageViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    ChipGroup chipGroup;
    CheckView checkView;

    public ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      final CardView card = (CardView) itemView;
      imageView = card.findViewById(R.id.image_iv);
      imageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          listener.onImageClicked(images.get(getAdapterPosition()));
        }
      });
      chipGroup = card.findViewById(R.id.image_chipgroup);
      checkView = card.findViewById(R.id.check_view);
      checkView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (v instanceof CheckView) {
            int position = getAdapterPosition();
            Image image = images.get(position);
            if (!imageSelection.exist(image)) {
              imageSelection.add(image);
              checkView.setChecked(true);
              listener.onImageCheckClicked(image,true);
            } else {
              imageSelection.remove(image);
              checkView.setChecked(false);
              listener.onImageCheckClicked(image,false);
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
