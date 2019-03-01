package org.cloud.wetag.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.cloud.wetag.R;

public class BaseActivity extends AppCompatActivity {

  public Context mContext;
  public Toolbar mToolbarTb;
  public DrawerLayout mDrawerLayout;

  @Override
  public void setContentView(@LayoutRes int layoutResID) {
    super.setContentView(layoutResID);

    mContext = this;
    mDrawerLayout = findViewById(R.id.drawer_layout);
    mToolbarTb = findViewById(R.id.toolbar);
    if (mToolbarTb != null) {
      setSupportActionBar(mToolbarTb);
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (this instanceof MainActivity) {
          mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
          finish();
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onTitleChanged(CharSequence title, int color) {
    super.onTitleChanged(title, color);
    if (mToolbarTb != null) {
      mToolbarTb.setTitle(title);
    }
  }

}
