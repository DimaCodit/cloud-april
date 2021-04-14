package messages;

import java.io.Serializable;

public class RequestMessage implements Serializable, Message {

    private Action action;
    private String param;
    private String param2;

    public RequestMessage(Action action, String param) {
        this.action = action;
        this.param = param;
    }

    public RequestMessage(Action action, String param, String param2) {
        this.action = action;
        this.param = param;
        this.param2 = param2;
    }

    public Action getAction() {
        return action;
    }

    public String getParam() {
        return param;
    }

    public String getParam2() {
        return param2;
    }

}

