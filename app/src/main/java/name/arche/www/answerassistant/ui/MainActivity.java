package name.arche.www.answerassistant.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import name.arche.www.answerassistant.event.ScreenShotFinishEvent;
import name.arche.www.answerassistant.event.ScreenShotStartEvent;
import name.arche.www.answerassistant.services.AssistantFloatWindow;
import name.arche.www.answerassistant.util.Shotter;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PROJECTION = 1000;
    private static final int REQUEST_DRAW_OVERLAY = 1001;

    private Context mContext;

    @Subscribe
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EventBus.getDefault().register(this);
        startAssistantFloatWindow();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void startAssistantFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_DRAW_OVERLAY);
            } else {
                startService(new Intent(MainActivity.this, AssistantFloatWindow.class));
            }
        } else
            startService(new Intent(MainActivity.this, AssistantFloatWindow.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void screenShot(ScreenShotStartEvent event) {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivityForResult(
                    ((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION
            );

        } else {
            Toast.makeText(mContext, "版本过低,无法截屏", Toast.LENGTH_SHORT).show();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == -1 && data != null) {
                    Shotter shotter = new Shotter(MainActivity.this, data);
                    shotter.startScreenShot(new Shotter.OnShotListener() {
                        @Override
                        public void onFinish(Bitmap bitmap) {
                            if (bitmap == null)
                                return;

                            EventBus.getDefault().post(new ScreenShotFinishEvent(bitmap));
                        }
                    });
                }
                break;
            case REQUEST_DRAW_OVERLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        startService(new Intent(MainActivity.this, AssistantFloatWindow.class));
                    }
                }
                break;

        }
    }
}
