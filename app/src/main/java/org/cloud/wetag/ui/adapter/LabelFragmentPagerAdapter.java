package org.cloud.wetag.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.cloud.wetag.model.DataObject;
import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ObjectSelection;
import org.cloud.wetag.ui.PageFragment;

import java.util.LinkedList;
import java.util.List;

import static org.cloud.wetag.ui.PageFragment.ALL;
import static org.cloud.wetag.ui.PageFragment.ALL_LABELED;
import static org.cloud.wetag.ui.PageFragment.ALL_UNLABELED;
import static org.cloud.wetag.ui.PageFragment.SINGLE_LABELED;

public class LabelFragmentPagerAdapter extends FragmentPagerAdapter {

  private DataSet dataSet;
  private List<PageFragment> fragments;

  public LabelFragmentPagerAdapter(FragmentManager fm, DataSet dataSet,
                                   ObjectSelection objectSelection,
                                   DataObjectCardAdapter.OnDataObjectCheckChangedListener listener) {
    super(fm);
    this.dataSet = dataSet;

    // tabs are: All, All unlabled, All labeled, every single label
    this.fragments = new LinkedList<>();
    this.fragments.add(PageFragment.newInstance(dataSet, ALL, null));
    this.fragments.add(PageFragment.newInstance(dataSet, ALL_UNLABELED, null));
    this.fragments.add(PageFragment.newInstance(dataSet, ALL_LABELED, null));
    for (String label : dataSet.getLabels()) {
      this.fragments.add(PageFragment.newInstance(dataSet, SINGLE_LABELED, label));
    }

    // set image selection in all fragment, so that we can get the image selection
    // in this class, for image deletion menu item to work
    for (PageFragment fragment : this.fragments) {
      fragment.setObjectSelection(objectSelection);
      fragment.registerOnCheckChangedListener(listener);
    }
  }

  public void refreshFragment(int position) {
    if (position >= 0 && position < fragments.size()) {
      DataObjectCardAdapter adapter = fragments.get(position).getAdapter();
      if (adapter != null) {
        adapter.refreshAllCards();
      }
    }
  }

  public void refreshAllFragments() {
    for (int i = 0; i < fragments.size(); i++) {
      refreshFragment(i);
    }
  }

  @Override
  public Fragment getItem(int i) {
    return fragments.get(i);
  }

  @Override
  public int getCount() {
    return fragments.size();
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    DataObjectCardAdapter adapter = fragments.get(position).getAdapter();
    int count;
    if (adapter != null) {
      count = fragments.get(position).getAdapter().getItemCount();
    } else {
      if (position > 2) {
        count = countImages(position, dataSet.getLabels().get(position - 3));
      } else {
        count = countImages(position, null);
      }
    }
    if (position == ALL) {
      return "所有(" + count + ")";
    } else if (position == ALL_UNLABELED) {
      return "未标注(" + count + ")";
    } else if (position == ALL_LABELED) {
      return "已标注(" + count + ")";
    } else {
      return dataSet.getLabels().get(position - 3) + "(" + count + ")";
    }
  }

  private int countImages(int position, String filterLabel) {
    int count = 0;
    if (position == ALL) {
      count = dataSet.getDataObjects().size();
    } else if (position == ALL_UNLABELED) {
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().size() == 0) {
          count++;
        }
      }
    } else if (position == ALL_LABELED) {
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().size() > 0) {
          count++;
        }
      }
    } else {
      for (DataObject dataObject : dataSet.getDataObjects()) {
        if (dataObject.getLabels().contains(filterLabel)) {
          count++;
        }
      }
    }
    return count;
  }
}
