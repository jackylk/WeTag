package org.cloud.wetag.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;

import java.util.Date;
import java.util.List;

public class CloudDataSetCardAdapter extends
    RecyclerView.Adapter<CloudDataSetCardAdapter.CloudDataSetViewHolder> {

  private Context context;
  private List<DataSet> dataSets;

  public CloudDataSetCardAdapter(List<DataSet> dataSets) {
    this.dataSets = dataSets;
  }

  @NonNull
  @Override
  public CloudDataSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    if (context == null) {
      context = viewGroup.getContext();
    }
    View view = LayoutInflater.from(context).inflate(R.layout.cloud_dataset_item, viewGroup, false);
    return new CloudDataSetCardAdapter.CloudDataSetViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull CloudDataSetViewHolder holder, int position) {
    DataSet dataSet = dataSets.get(position);
    holder.dataSetName.setText(dataSet.getName());
    holder.dataSetDesc.setText(dataSet.getDesc());

    holder.dataSetCreateTime.setText(new Date(dataSet.getCreateTime()).toString());
  }

  @Override
  public int getItemCount() {
    return dataSets.size();
  }

  static class CloudDataSetViewHolder extends RecyclerView.ViewHolder {

    CardView cardView;
    TextView dataSetName;
    TextView dataSetCreateTime;
    TextView dataSetDesc;

    public CloudDataSetViewHolder(@NonNull final View itemView) {
      super(itemView);
      cardView = (CardView) itemView;
      dataSetName = itemView.findViewById(R.id.cloud_dataset_name);
      dataSetDesc = itemView.findViewById(R.id.cloud_dataset_desc);
      dataSetCreateTime = itemView.findViewById(R.id.cloud_dataset_create_time);
    }
  }
}
