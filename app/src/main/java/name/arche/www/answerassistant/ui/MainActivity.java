package name.arche.www.answerassistant.ui;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import name.arche.www.answerassistant.R;
import name.arche.www.answerassistant.event.ScreenShotFinishEvent;
import name.arche.www.answerassistant.event.ScreenShotStartEvent;
import name.arche.www.answerassistant.services.AssistantFloatWindow;
import name.arche.www.answerassistant.util.Shotter;
import name.arche.www.answerassistant.util.TessAPIClient;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PROJECTION = 1000;
    private static final int REQUEST_DRAW_OVERLAY = 1001;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1002;


    private Context mContext;

    @Subscribe
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        EventBus.getDefault().register(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //第一请求权限被取消显示的判断，一般可以不写
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            new InitTask(mContext).execute();
        }

    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private ProgressDialog mProgressDialog;

        public InitTask(Context context) {
            mContext = context;
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("初始化中");
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            TessAPIClient.getInstanse().init(mContext, TessAPIClient.LANGUAGE.CHINESE);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            startAssistantFloatWindow();
            screenShot(null);
        }
    }

    @Override
    public void onDestroy() {
        Log.e("zzf", "onDestroy");
        stopService(new Intent(mContext, AssistantFloatWindow.class));
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void startAssistantFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mContext)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_DRAW_OVERLAY);
            } else {
                startService(new Intent(mContext, AssistantFloatWindow.class));
            }
        } else
            startService(new Intent(mContext, AssistantFloatWindow.class));
    }

    Shotter mShotter = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void screenShot(ScreenShotStartEvent event) {
        if (mShotter != null) {
            mShotter.startScreenShot(new Shotter.OnShotListener() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    if (bitmap == null)
                        return;

                    EventBus.getDefault().post(new ScreenShotFinishEvent(bitmap));
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                startActivityForResult(
                        ((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION
                );

            } else {
                Toast.makeText(mContext, "版本过低,无法截屏", Toast.LENGTH_SHORT).show();
            }
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == -1 && data != null) {
                    mShotter = new Shotter(mContext, data);
                    mShotter.startScreenShot(new Shotter.OnShotListener() {
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
                        startService(new Intent(mContext, AssistantFloatWindow.class));
                    }
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new InitTask(mContext).execute();
        }

    }
}
