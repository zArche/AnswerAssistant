package name.arche.www.answerassistant.event;

import android.graphics.Bitmap;

/**
 * Created by arche on 2018/1/11.
 */

public class ScreenShotFinishEvent {
    private Bitmap mBitmap;

    public ScreenShotFinishEvent(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
