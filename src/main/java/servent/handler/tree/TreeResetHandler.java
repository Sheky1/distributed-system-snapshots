package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.tree.TreeResetConfirmMessage;
import servent.message.tree.TreeResetMessage;
import servent.message.util.MessageUtil;

public class TreeResetHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeResetHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_RESET) {
            TreeResetMessage treeResetMessage = (TreeResetMessage) clientMessage;
            for(Integer initiator: treeResetMessage.getRegions()) {
                AppConfig.initiatorVersions.put(initiator, AppConfig.initiatorVersions.get(initiator) + 1);
            }
            for(Integer child: AppConfig.TREE_STATE.getChildren()) {
                Message regionTransactionMessage = new TreeResetMessage(AppConfig.getInfoById(child), treeResetMessage.getRegions());
                MessageUtil.sendMessage(regionTransactionMessage);
            }
            AppConfig.TREE_STATE.reset();
            AppConfig.blanks.clear();
            AppConfig.regions.clear();
            AppConfig.collectedChildren = 0;
            AppConfig.treeResetConfirmed = 0;
        }
    }
}
