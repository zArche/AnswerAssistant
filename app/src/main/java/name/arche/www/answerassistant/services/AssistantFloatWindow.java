package name.arche.www.answerassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import name.arche.www.answerassistant.R;
import name.arche.www.answerassistant.bean.Question;
import name.arche.www.answerassistant.event.CloseWebViewEvent;
import name.arche.www.answerassistant.event.ScreenShotFinishEvent;
import name.arche.www.answerassistant.event.ScreenShotStartEvent;
import name.arche.www.answerassistant.util.FileUtil;
import name.arche.www.answerassistant.util.TessAPIClient;


/**
 * Created by arche on 2018/1/11.
 */

public class AssistantFloatWindow extends Service {


    private static final String TAG = "AssistantFloatWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mFloatView;
    private TextView mAssistant;
    private Context mContext;

    @Subscribe
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        EventBus.getDefault().register(this);
        initViews();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initViews() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int w = display.getWidth();
        int h = display.getHeight();

        mFloatView = LayoutInflater.from(getApplication()).inflate(R.layout.float_window_assistant, null);

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        mLayoutParams.x = 45;
        mLayoutParams.y = h / 3 + 30;
        mLayoutParams.width = 216;
        mLayoutParams.height = 216;

        mAssistant = mFloatView.findViewById(R.id.tv_assistant);
        mAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ScreenShotStartEvent());
            }
        });
        mWindowManager.addView(mFloatView, mLayoutParams);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScreenShotFinished(ScreenShotFinishEvent event) {
        Bitmap bitmap = FileUtil.getCropBitmap(event.getBitmap());
        String content = TessAPIClient.getInstanse().recognize(bitmap);

        Question question = Question.parseFromStr(content);

        Intent intent = new Intent(mContext, WebViewWindow.class);
        intent.putExtra("question", question.getQuestion());
        startService(intent);

        Log.e(TAG, "question:" + question);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closeWebView(CloseWebViewEvent event) {
        Intent intent = new Intent(mContext, WebViewWindow.class);
        stopService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        mWindowManager.removeView(mFloatView);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
