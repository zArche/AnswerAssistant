package name.arche.www.answerassistant.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by arche on 2018/1/11.
 */

public class TessAPIClient {

    public enum LANGUAGE {
        ENGLISH("eng"),
        CHINESE("chi_sim");

        private String language;

        LANGUAGE(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }
    }

    private static TessAPIClient sClient;
    private TessBaseAPI mTessBaseAPI;

    public static TessAPIClient getInstanse() {
        if (sClient == null) {
            synchronized (TessAPIClient.class) {
                if (sClient == null)
                    sClient = new TessAPIClient();
            }
        }

        return sClient;
    }

    private TessAPIClient() {
    }

    public void init(Context context, LANGUAGE language) {

        FileUtil.copyFileIfNeed(context, FileUtil.CHI_TESS_DATA);
        FileUtil.copyFileIfNeed(context, FileUtil.ENG_TESS_DATA);

        if (mTessBaseAPI == null)
            mTessBaseAPI = new TessBaseAPI();

        mTessBaseAPI.init(FileUtil.SDCARD_PATH, language.getLanguage());
    }

    public String recognize(Bitmap bitmap) {
        if (mTessBaseAPI == null)
            return "";

        mTessBaseAPI.setImage(bitmap);
        return mTessBaseAPI.getUTF8Text();
    }
}
