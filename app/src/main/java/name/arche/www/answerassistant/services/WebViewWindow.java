package name.arche.www.answerassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import name.arche.www.answerassistant.R;
import name.arche.www.answerassistant.event.CloseWebViewEvent;


/**
 * Created by arche on 2018/1/12.
 */

public class WebViewWindow extends Service {


    private static final String TAG = "WebViewWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatView;
    private WebView mWebView;
    private Context mContext;
    private static final String HOST_NAME = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        initViews(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initViews(Intent intent) {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int w = display.getWidth();
        int h = display.getHeight();

        mFloatView = LayoutInflater.from(getApplication()).inflate(R.layout.float_window_webview, null);

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
        mLayoutParams.width = w;
        mLayoutParams.height = h / 3;

        mFloatView.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CloseWebViewEvent());
            }
        });

        mWebView = mFloatView.findViewById(R.id.wv_search);

        String question = intent.getStringExtra("question");
        if (TextUtils.isEmpty(question)) {
            question = "Arche";
        }
        String url = HOST_NAME;
        try {
            url = url + URLEncoder.encode(question, "gb2312") + "&rn=20";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient());

        mWindowManager.addView(mFloatView, mLayoutParams);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        mWindowManager.removeView(mFloatView);
        super.onDestroy();
    }
}
