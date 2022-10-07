package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TreeResetConfirmMessage  extends  BasicMessage{

    private static final long serialVersionUID = -3891383848924403186L;

    public TreeResetConfirmMessage(ServentInfo receiver) {
        super(MessageType.TREE_RESET_CONFIRM, AppConfig.myServentInfo, receiver);
    }
}
