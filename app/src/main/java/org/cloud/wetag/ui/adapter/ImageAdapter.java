package org.cloud.wetag.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.cloud.wetag.entity.Image;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

  private List<Image> imageList;

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    return null;

  }

  @Override
  public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }

  static class ImageViewHolder extends RecyclerView.ViewHolder {


    public ImageViewHolder(@NonNull View itemView) {
      super(itemView);
      CardView card = (CardView) itemView;
    }
  }
}
