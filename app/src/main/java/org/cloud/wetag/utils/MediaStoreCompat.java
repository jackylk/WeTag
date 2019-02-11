package org.cloud.wetag.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaStoreCompat {

  private final WeakReference<Activity> context;
  private final WeakReference<Fragment> fragment;
  private CaptureStrategy captureStrategy;
  private Uri currentPhotoUri;
  private String currentPhotoPath;
  private String imageFileName;

  public MediaStoreCompat(Activity activity) {
    context = new WeakReference<>(activity);
    fragment = null;
  }

  public MediaStoreCompat(Activity activity, Fragment fragment) {
    context = new WeakReference<>(activity);
    this.fragment = new WeakReference<>(fragment);
  }

  /**
   * Checks whether the device has a camera feature or not.
   *
   * @param context a context to check for camera feature.
   * @return true if the device has a camera feature. false otherwise.
   */
  public static boolean hasCameraFeature(Context context) {
    PackageManager pm = context.getApplicationContext().getPackageManager();
    return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

  public void setCaptureStrategy(CaptureStrategy strategy) {
    captureStrategy = strategy;
  }

  public void dispatchCaptureIntent(Context context, int requestCode) {
    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
      File photoFile = null;
      try {
        photoFile = createImageFile();
      } catch (IOException e) {
        e.printStackTrace();
      }

      if (photoFile != null) {
        currentPhotoPath = photoFile.getAbsolutePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          currentPhotoUri = FileProvider.getUriForFile(this.context.get(),
              captureStrategy.authority, photoFile);
        } else {
          currentPhotoUri = Uri.fromFile(photoFile);
        }
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          List<ResolveInfo> resInfoList = context.getPackageManager()
              .queryIntentActivities(captureIntent, PackageManager.MATCH_DEFAULT_ONLY);
          for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, currentPhotoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
          }
        }
        if (fragment != null) {
          fragment.get().startActivityForResult(captureIntent, requestCode);
        } else {
          this.context.get().startActivityForResult(captureIntent, requestCode);
        }
      }
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp =
        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    imageFileName = String.format("JPEG_%s.jpg", timeStamp);
    File storageDir;
    if (captureStrategy.isPublic) {
      storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
      if (!storageDir.exists()) storageDir.mkdirs();
    } else {
      storageDir = context.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
    if (captureStrategy.directory != null) {
      storageDir = new File(storageDir, captureStrategy.directory);
      if (!storageDir.exists()) {
        if (!storageDir.mkdirs()) {
          Log.e(this.getClass().getName(), "can not mkdir");
        }
      }
    }

    // Avoid joining path components manually
    File tempFile = new File(storageDir, imageFileName);
    tempFile.createNewFile();

    // Handle the situation that user's external storage is not ready
    if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
      return null;
    }

    return tempFile;
  }

  public Uri getCurrentPhotoUri() {
    return currentPhotoUri;
  }

  public String getImageFileName() {
    return imageFileName;
  }

  public String getCurrentPhotoPath() {
    return currentPhotoPath;
  }
}
