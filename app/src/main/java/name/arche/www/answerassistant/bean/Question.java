package name.arche.www.answerassistant.bean;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by arche on 2018/1/12.
 */

public class Question {
    private String mQuestion;
    private String[] mAnswers;

    private Question(String question, String[] answers) {
        mQuestion = question;
        mAnswers = answers;
    }

    public static Question parseFromStr(String str) {
        //先去除空行
        str = str.replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").
                replaceAll("^((\r\n)|\n)", "");
        str = str.replace('.', ' ').replace(" ", "");
        //问号统一替换为英文问号防止报错
        str = str.replace("？", "?");
        int begin = (str.charAt(1) >= '0' && str.charAt(1) <= 9) ? 2 : 1;

        int index = str.indexOf('?');

        String question = str.trim().substring(begin, index > 0 ? index + 1 : 1);
        question = question.replaceAll("((\r\n)|\n)", "");
        String remain = str.substring(str.indexOf("?") + 1);
        String[] ans = remain.trim().split("\n");

        return new Question(question, ans);
    }

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestion(String question) {
        mQuestion = question;
    }

    public String[] getAnswers() {
        return mAnswers;
    }

    public void setAnswers(String[] answers) {
        mAnswers = answers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "mQuestion='" + mQuestion + '\'' +
                ", mAnswers=" + Arrays.toString(mAnswers) +
                '}';
    }
}
