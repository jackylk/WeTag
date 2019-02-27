package org.cloud.wetag.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cloud.wetag.R;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.DataSetCollection;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.adapter.DataObjectCardAdapter;
import org.cloud.wetag.ui.adapter.ImageCardAdapter;
import org.cloud.wetag.ui.adapter.Seq2SeqCardAdapter;
import org.cloud.wetag.ui.adapter.TextCardAdapter;

public class PageFragment extends Fragment {

  public final static int ALL_UNLABELED = 0;
  public final static int ALL_LABELED = 1;
  public final static int SINGLE_LABELED = 2;

  private DataObjectCardAdapter adapter;
  private ObjectSelection objectSelection;
  private DataObjectCardAdapter.OnDataObjectStateChangedListener listener;
  private RecyclerView recyclerView;
  private DataSet dataSet;

  public static PageFragment newInstance(DataSet dataSet, int pageType, String filterLabel) {
    PageFragment fragment = new PageFragment();
    Bundle args = new Bundle();
    args.putString("dataset_name", dataSet.getName());
    args.putInt("page_type", pageType);
    args.putString("filter_label", filterLabel);
    fragment.setArguments(args);
    fragment.dataSet = dataSet;
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    String datasetName = getArguments().getString("dataset_name");
    int pageType = getArguments().getInt("page_type");
    String labelName = getArguments().getString("filter_label");
    DataSet dataSet = DataSetCollection.getDataSet(datasetName);
    adapter = dataSet.createAdapter(pageType, labelName, objectSelection);
    adapter.registerOnCheckChangedListener(listener);
    super.onCreate(savedInstanceState);
  }

  public void registerOnCheckChangedListener(DataObjectCardAdapter.OnDataObjectStateChangedListener listener) {
    this.listener = listener;
  }

  public DataObjectCardAdapter getAdapter() {
    return adapter;
  }

  public void setObjectSelection(ObjectSelection objectSelection) {
    this.objectSelection = objectSelection;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_labeling_page, container, false);
    recyclerView = view.findViewById(R.id.recycler_view);
    if (dataSet.isImageDataSet()) {
      recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
    } else {
      recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),
          LinearLayoutManager.VERTICAL, false));
    }
    recyclerView.setAdapter(adapter);
    return view;
  }

  /**
   * back is pressed by user in parent activity
   * @return true if this class consumed the event
   */
  public boolean onBackPressed() {
    if (adapter != null) {
      return adapter.onBackPressed();
    } else {
      return false;
    }
  }

}
