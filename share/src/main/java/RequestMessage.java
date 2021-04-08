import java.io.Serializable;

public class RequestMessage implements Serializable, Message {

    private Action action;
    private String param;

    public RequestMessage(Action action, String param) {
        this.action = action;
        this.param = param;
    }

    public Action getAction() {
        return action;
    }

    public String getParam() {
        return param;
    }
}

enum Action {
    CLOSE_FILE,
    GET_STORAGE_FILE
}
