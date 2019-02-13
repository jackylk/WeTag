package org.cloud.wetag.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.cloud.wetag.model.DataSet;
import org.cloud.wetag.model.ImageSelection;
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
                                   ImageSelection imageSelection) {
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
      fragment.setImageSelection(imageSelection);
      fragment.setParentAdapter(this);
    }
  }

  public void refreshFragment(int position) {
    if (position > 0 && position < fragments.size()) {
      ImageCardAdapter adapter = fragments.get(position).getAdapter();
      adapter.refreshImages();
    }
  }

  public void refreshAllFragments() {
    for (int i = 0; i < fragments.size(); i++) {
      refreshFragment(i);
    }
  }

  public void setEnableLabelBar(boolean enabled) {
    for (PageFragment fragment : fragments) {
      fragment.setEnableLabelBar(enabled);
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
    if (position == ALL) {
      return "所有";
    } else if (position == ALL_UNLABELED) {
      return "未标注";
    } else if (position == ALL_LABELED) {
      return "已标注";
    } else {
      return dataSet.getLabels().get(position - 3);
    }
  }
}
