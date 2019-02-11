package org.cloud.wetag.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.cloud.wetag.R;
import org.cloud.wetag.entity.DataSet;
import org.cloud.wetag.entity.DataSetCollection;
import org.cloud.wetag.ui.adapter.ImageAdapter;
import org.cloud.wetag.utils.CaptureStrategy;
import org.cloud.wetag.utils.MediaStoreCompat;

import java.io.FileNotFoundException;

public class LabelActivity extends BaseActivity {

  private MediaStoreCompat mediaStoreCompat;
  private ImageView imageView;
  private static final int REQUEST_CODE_CAPTURE = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_label);

    int index = getIntent().getIntExtra("dataset_index", -1);
    DataSet dataSet = DataSetCollection.getDataSetList().get(index);
    mediaStoreCompat = new MediaStoreCompat(this);
    mediaStoreCompat.setCaptureStrategy(
        new CaptureStrategy(false, "org.cloud.wetag.fileprovider", dataSet.getName()));
    imageView = findViewById(R.id.captured_image);

    RecyclerView recyclerView = findViewById(R.id.images_rv);
    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
    recyclerView.setLayoutManager(layoutManager);
    ImageAdapter adapter = new ImageAdapter();
    recyclerView.setAdapter(adapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_labeling, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.item_camera:
        if (mediaStoreCompat != null) {
          mediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE);
        }
        break;
      default:
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) {
      return;
    }
    if (requestCode == REQUEST_CODE_CAPTURE) {
      Uri imageUri = mediaStoreCompat.getCurrentPhotoUri();
      String path = mediaStoreCompat.getCurrentPhotoPath();

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        LabelActivity.this.revokeUriPermission(imageUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }
      try {
        Bitmap bitmap = BitmapFactory.decodeStream(
            getContentResolver().openInputStream(imageUri));
        imageView.setImageBitmap(bitmap);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
