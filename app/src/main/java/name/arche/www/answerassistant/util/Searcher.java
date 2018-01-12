package name.arche.www.answerassistant.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

/**
 * Created by arche on 2018/1/12.
 */

public class Searcher implements Callable {

    private final String question;

    public Searcher(String question) {
        this.question = question;
    }

    Long search(String question) throws IOException {
        String path = "http://www.baidu.com/s?tn=ichuner&lm=-1&word=" +
                URLEncoder.encode(question, "gb2312") + "&rn=1";
        boolean findIt = false;
        String line = null;
        while (!findIt) {
            URL url = new URL(path);
            BufferedReader breaded = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            while ((line = breaded.readLine()) != null) {
                if (line.contains("百度为您找到相关结果约")) {
                    findIt = true;
                    int start = line.indexOf("百度为您找到相关结果约") + 11;

                    line = line.substring(start);
                    int end = line.indexOf("个");
                    line = line.substring(0, end);
                    break;
                }

            }
        }
        line = line.replace(",", "");
        return Long.valueOf(line);
    }


    @Override
    public Long call() throws Exception {
        return search(question);
    }
}
