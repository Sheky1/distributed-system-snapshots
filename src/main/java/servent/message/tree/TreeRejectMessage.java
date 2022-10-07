package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TreeRejectMessage extends BasicMessage {

    private static final long serialVersionUID = 4632948952510475549L;

    public TreeRejectMessage(ServentInfo receiver) {
        super(MessageType.TREE_REJECT, AppConfig.myServentInfo, receiver);
    }
}
