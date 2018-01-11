package name.arche.www.answerassistant.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {


    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

    public static final String TESS_DATA_DIR = SDCARD_PATH + "tessdata" + File.separator;

    public static final String ENG_TESS_DATA = "eng.traineddata";
    public static final String CHI_TESS_DATA = "chi_sim.traineddata";

    public static void copyFileIfNeed(Context context, String fileName) {
        try {

            File dir = new File(TESS_DATA_DIR);
            if (!dir.exists())
                dir.mkdirs();

            File modelFile = new File(dir, fileName); // /sdcard/tessdata目录下

            InputStream is = context.getAssets().open(fileName);
            if (modelFile.length() == is.available()) {
                return;
            }
            OutputStream os = new FileOutputStream(modelFile);
            byte[] buffer = new byte[1024];
            int length = is.read(buffer);
            while (length > 0) {
                os.write(buffer, 0, length);
                length = is.read(buffer);
            }
            os.flush();
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getCropBitmap(Bitmap sourceBitmap) {
        if (sourceBitmap == null || sourceBitmap.isRecycled()) {
            return null;
        }
        Rect rect = new Rect();
        rect.left = 0 + sourceBitmap.getWidth() * 1 / 12;
        rect.top = 0 + sourceBitmap.getHeight() * 1 / 7;
        rect.right = sourceBitmap.getWidth() - sourceBitmap.getWidth() * 1 / 12;
        rect.bottom = sourceBitmap.getHeight() - sourceBitmap.getHeight() * 3 / 7;
        return Bitmap.createBitmap(sourceBitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    public static void saveBitmap(Bitmap bitmap, String filePath, int quality) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        if (bitmap == null)
            return;
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delete(String path) {
        if (TextUtils.isEmpty(path))
            return;
        File file = new File(path);
        if (!file.exists())
            return;
        delete(file);
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    public static void deleteFiles(String folderPath) {
        File dir = new File(folderPath);
        if (dir == null || !dir.exists() || !dir.isDirectory() || dir.listFiles() == null)
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete();
        }
    }

    public static List<String> getImageListName(String path) {
        List<String> imageList = new ArrayList<String>();
        File file = new File(path);
        File[] files = file.listFiles();
        for (int j = 0; j < files.length; j++) {
            if (files[j].isFile() & files[j].getName().endsWith(".jpg")) {
                imageList.add(files[j].getName());
            }
        }
        return imageList;
    }

    /**
     * 加载本地图片
     *
     * @param url the file url
     * @return Bitmap
     */
    public static Bitmap getLoacalBitmap(String url) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(url);
            bitmap = BitmapFactory.decodeStream(fis);
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public static void copy(String sourcePath, String targetDirPath, String targetFileName) {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return;
        }

        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = new File(targetDir, targetFileName);
        if (targetFile.exists())
            targetFile.delete();

        InputStream myInput;
        OutputStream myOutput;
        try {
            myInput = new FileInputStream(sourceFile);
            myOutput = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解压指定zip文件
    public static void unZip(String unZipfileName) {//unZipfileName需要解压的zip文件名
        FileOutputStream fileOut;
        File file;
        String f = unZipfileName.substring(0, unZipfileName.length() - 4);
        File ff = new File(f);
        try {
            ZipInputStream zipIn = new ZipInputStream(new
                    BufferedInputStream(new FileInputStream(unZipfileName)));
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                file = new File(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    //如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    if (!ff.exists()) {
                        ff.mkdir();
                    }
                    fileOut = new FileOutputStream(f + "/" + file.getName());
                    int readedBytes;
                    byte[] buf = new byte[1024];
                    while ((readedBytes = zipIn.read(buf)) > 0) {
                        fileOut.write(buf, 0, readedBytes);
                    }
                    fileOut.close();
                }
                zipIn.closeEntry();
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }


    public static void zip(String src, String dest) {
        //提供了一个数据项压缩成一个ZIP归档输出流
        ZipOutputStream out = null;
        try {

            File outFile = new File(dest);//源文件或者目录
            File fileOrDirectory = new File(src);//压缩文件路径
            out = new ZipOutputStream(new FileOutputStream(outFile));
            //如果此文件是一个文件，否则为false。
            if (fileOrDirectory.isFile()) {
                zipFileOrDirectory(out, fileOrDirectory, "");
            } else {
                //返回一个文件或空阵列。
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], "");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //关闭输出流
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void zipFileOrDirectory(ZipOutputStream out,
                                           File fileOrDirectory, String curPath) throws IOException {
        //从文件中读取字节的输入流
        FileInputStream in = null;
        try {
            //如果此文件是一个目录，否则返回false。
            if (!fileOrDirectory.isDirectory()) {
                // 压缩文件
                byte[] buffer = new byte[4096];
                int bytes_read;
                in = new FileInputStream(fileOrDirectory);
                //实例代表一个条目内的ZIP归档
                ZipEntry entry = new ZipEntry(curPath
                        + fileOrDirectory.getName());
                //条目的信息写入底层流
                out.putNextEntry(entry);
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                }
                out.closeEntry();
            } else {
                // 压缩目录
                File[] entries = fileOrDirectory.listFiles();
                for (int i = 0; i < entries.length; i++) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], curPath
                            + fileOrDirectory.getName() + "/");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // throw ex;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


}