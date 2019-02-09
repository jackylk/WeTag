package org.cloud.wetag;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.dataset.DataSet;

import java.util.List;

public class DataSetAdapter extends RecyclerView.Adapter<DataSetAdapter.DataSetViewHolder> {

  private Context context;
  private List<DataSet> dataSetList;

  public DataSetAdapter(List<DataSet> dataSetList) {
    this.dataSetList = dataSetList;
  }

  @NonNull
  @Override
  public DataSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(R.layout.dataset_item, viewGroup, false);
    return new DataSetViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull DataSetViewHolder viewHolder, int position) {
    DataSet dataSet = dataSetList.get(position);
    viewHolder.dataSetName.setText(dataSet.getName());

    ImageView[] imageViews = new ImageView[]{
        viewHolder.dataSetImage1, viewHolder.dataSetImage2, viewHolder.dataSetImage3};
    List<String> imageLocations = dataSet.getImages();
    for (int i = 0; i < imageViews.length; i++) {
      String imageLocation;
      try {
        imageLocation = imageLocations.get(i);
      } catch (IndexOutOfBoundsException e) {
        imageLocation = null;
      }
      if (imageLocation == null) {
        // if image location is null, load default picture
        Glide.with(context).load(R.drawable.no_image).into(imageViews[i]);
      } else {
        Glide.with(context).load(dataSet.getImages().get(0)).into(imageViews[i]);
      }
    }
  }

  @Override
  public int getItemCount() {
    return dataSetList.size();
  }

  static class DataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    ImageView dataSetImage1, dataSetImage2, dataSetImage3;

    public DataSetViewHolder(@NonNull View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.dataset_name);
      dataSetImage1 = itemView.findViewById(R.id.dataset_image1);
      dataSetImage2 = itemView.findViewById(R.id.dataset_image2);
      dataSetImage3 = itemView.findViewById(R.id.dataset_image3);
    }
  }
}
