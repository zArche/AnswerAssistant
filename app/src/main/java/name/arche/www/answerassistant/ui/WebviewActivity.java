package name.arche.www.answerassistant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import name.arche.www.answerassistant.R;

/**
 * Created by arche on 2018/1/12.
 */

public class WebviewActivity extends AppCompatActivity {

    private static final String HOST_NAME = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=";

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.wv_search);

        Intent intent = getIntent();
        if (intent != null) {
            String question = intent.getStringExtra("question");
            if (TextUtils.isEmpty(question)){
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
        }
    }


}
