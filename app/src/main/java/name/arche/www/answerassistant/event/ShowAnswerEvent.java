package name.arche.www.answerassistant.event;

/**
 * Created by arche on 2018/1/12.
 */

public class ShowAnswerEvent {
    private String mAnswer;

    public ShowAnswerEvent(String answer) {
        mAnswer = answer;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public void setAnswer(String answer) {
        mAnswer = answer;
    }
}
