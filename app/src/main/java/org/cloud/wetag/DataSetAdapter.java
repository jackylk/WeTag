package org.cloud.wetag;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.cloud.wetag.dataset.DataSet;
import org.cloud.wetag.dataset.WorkSpace;

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
  public void onBindViewHolder(@NonNull DataSetViewHolder viewHolder, final int position) {
    DataSet dataSet = dataSetList.get(position);
    viewHolder.dataSetName.setText(dataSet.getName() + "数据集");
    viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        WorkSpace.removeDataSet(position);
        notifyItemRemoved(position);
      }
    });
    List<String> imageLocations = dataSet.getImages();
    if (imageLocations.isEmpty()) {
      // if dataset is empty, load default picture
      Glide.with(context).load(R.drawable.no_image).into(viewHolder.dataSetImage);
    } else {
      Glide.with(context).load(dataSet.getImages().get(0)).into(viewHolder.dataSetImage);
    }
  }

  @Override
  public int getItemCount() {
    return dataSetList.size();
  }

  static class DataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    Button deleteButton;
    ImageView dataSetImage;

    public DataSetViewHolder(@NonNull View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.dataset_name);
      dataSetImage = itemView.findViewById(R.id.dataset_image);
      deleteButton = itemView.findViewById(R.id.dataset_delete);
    }
  }
}
