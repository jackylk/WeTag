package org.cloud.wetag.utils;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

  /**
   * copy all files from assets folder to descPath
   *  @param  activity  activity
   *  @param  srcPath  source folder under assets, for example /assets/aa
   *  @param  descPath destination folder, for example /data/data/package_name/files/
   */
  public static void copyAssetsDir2Phone(Activity activity, String srcPath, String descPath) {
    try {
      String[] srcFileList = activity.getAssets().list(srcPath);
      if (srcFileList.length > 0) {
        File desc = new File(descPath);
        if (!desc.exists()) {
          desc.mkdir();
        }

        // copy all files to desc folder
        for (String srcFileName : srcFileList){
          String srcFile = srcPath + File.separator + srcFileName;
          InputStream inputStream = activity.getAssets().open(srcFile);
          File file = new File(descPath + File.separator + srcFileName);
          Log.i("copyAssets2Phone","file:" + srcFile);
          if (!file.exists() || file.length() == 0) {
            FileOutputStream fos = new FileOutputStream(file);
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len=inputStream.read(buffer)) != -1){
              fos.write(buffer,0, len);
            }
            fos.flush();
            inputStream.close();
            fos.close();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
