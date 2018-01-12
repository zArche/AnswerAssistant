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
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import name.arche.www.answerassistant.R;
import name.arche.www.answerassistant.bean.Question;
import name.arche.www.answerassistant.event.CloseWebViewEvent;
import name.arche.www.answerassistant.event.ShowAnswerEvent;
import name.arche.www.answerassistant.util.Searcher;


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

    private Question mQuestion;
    private TextView mAnswer;

    @Subscribe
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }


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

        mAnswer = mFloatView.findViewById(R.id.tv_answer);

        mFloatView.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CloseWebViewEvent());
            }
        });

        mWebView = mFloatView.findViewById(R.id.wv_search);

        mQuestion = (Question) intent.getSerializableExtra("question");
        String keyWord = mQuestion.getQuestion();

        String url = "www.arche.name";

        boolean isKeywordEmpty = TextUtils.isEmpty(keyWord);

        if (!isKeywordEmpty) {
            try {
                url = HOST_NAME + URLEncoder.encode(keyWord, "gb2312") + "&rn=20";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            new AnalyzeAnswersThread().start();
        }

        Log.e(TAG,"url:" + url);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient());

        mWindowManager.addView(mFloatView, mLayoutParams);

    }

    private class AnalyzeAnswersThread extends Thread {
        @Override
        public void run() {
//                String[] ss = {"老年痴呆症","癫痫症","小儿麻痹症"};
//                mQuestion = new Question("阿尔茨海默症又被称为什么?",ss);

            String question = mQuestion.getQuestion();
            String[] answers = mQuestion.getAnswers();

            if (answers.length < 1) {
                Log.e(TAG, "检测不到答案");
                return;
            }

            //搜索
            long countQuestion = 1;
            int numOfAnswer = answers.length > 3 ? 4 : answers.length;
            long[] countQA = new long[numOfAnswer];
            long[] countAnswer = new long[numOfAnswer];

            int maxIndex = 0;

            Searcher[] searchQA = new Searcher[numOfAnswer];
            Searcher[] searchAnswers = new Searcher[numOfAnswer];
            FutureTask[] futureQA = new FutureTask[numOfAnswer];
            FutureTask[] futureAnswers = new FutureTask[numOfAnswer];
            FutureTask futureQuestion = new FutureTask<Long>(new Searcher(question));
            new Thread(futureQuestion).start();
            for (int i = 0; i < numOfAnswer; i++) {
                searchQA[i] = new Searcher(question + " " + answers[i]);
                searchAnswers[i] = new Searcher(answers[i]);

                futureQA[i] = new FutureTask<Long>(searchQA[i]);
                futureAnswers[i] = new FutureTask<Long>(searchAnswers[i]);
                new Thread(futureQA[i]).start();
                new Thread(futureAnswers[i]).start();
            }
            try {
                while (!futureQuestion.isDone()) {
                }
                countQuestion = (Long) futureQuestion.get();
                for (int i = 0; i < numOfAnswer; i++) {
                    while (true) {
                        if (futureAnswers[i].isDone() && futureQA[i].isDone()) {
                            break;
                        }
                    }
                    countQA[i] = (Long) futureQA[i].get();
                    countAnswer[i] = (Long) futureAnswers[i].get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            float[] ans = new float[numOfAnswer];
            for (int i = 0; i < numOfAnswer; i++) {
                ans[i] = (float) countQA[i] / (float) (countQuestion * countAnswer[i]);
                maxIndex = (ans[i] > ans[maxIndex]) ? i : maxIndex;
            }
            //根据pmi值进行打印搜索结果
            int[] rank = rank(ans);

            Log.e(TAG, "answer:" + answers[maxIndex]);
            EventBus.getDefault().post(new ShowAnswerEvent(answers[maxIndex]));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showAnswer(ShowAnswerEvent event) {
        if (mAnswer != null)
            mAnswer.setText("推荐答案:" + event.getAnswer());
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


    private static int[] rank(float[] floats) {
        int[] rank = new int[floats.length];
        float[] f = Arrays.copyOf(floats, floats.length);
        Arrays.sort(f);
        for (int i = 0; i < floats.length; i++) {
            for (int j = 0; j < floats.length; j++) {
                if (f[i] == floats[j]) {
                    rank[i] = j;
                }
            }
        }
        return rank;
    }
}
