package name.arche.www.answerassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import name.arche.www.answerassistant.R;
import name.arche.www.answerassistant.event.ScreenShotFinishEvent;
import name.arche.www.answerassistant.event.ScreenShotStartEvent;


/**
 * Created by arche on 2017/11/22.
 */

public class AssistantFloatWindow extends Service {


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
        mFloatView = LayoutInflater.from(getApplication()).inflate(R.layout.float_window_assistant, null);

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        mLayoutParams.x = 45;
        mLayoutParams.y = 300;
        mLayoutParams.width = 216;
        mLayoutParams.height = 216;

        mAssistant = mFloatView.findViewById(R.id.tv_assistant);
        mAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ScreenShotStartEvent());
            }
        });
        mFloatView.setVisibility(View.VISIBLE);
        mWindowManager.addView(mFloatView, mLayoutParams);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScreenShotFinished(ScreenShotFinishEvent event){
        Bitmap bitmap = event.getBitmap();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
