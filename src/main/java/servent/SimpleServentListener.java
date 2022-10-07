package servent;

import app.AppConfig;
import app.Cancellable;
import app.snapshot_bitcake.LaiYangBitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.*;
import servent.handler.tree.*;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServentListener implements Runnable, Cancellable {

    private volatile boolean working = true;

    private final SnapshotCollector snapshotCollector;

    public SimpleServentListener(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
    }

    /*
     * Thread pool for executing the handlers. Each client will get it's own handler thread.
     */
    private final ExecutorService threadPool = Executors.newWorkStealingPool();

    private final List<Message> redMessages = new ArrayList<>();

    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
            /*
             * If there is no connection after 1s, wake up and see if we should terminate.
             */
            listenerSocket.setSoTimeout(1000);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
            System.exit(0);
        }


        while (working) {
            try {
                Message clientMessage;
                if (redMessages.size() > 0 && redMessages.get(0).getInitiatorVersions().equals(AppConfig.initiatorVersions)) {
                    clientMessage = redMessages.remove(0);
                } else {
                    /*
                     * This blocks for up to 1s, after which SocketTimeoutException is thrown.
                     */
                    Socket clientSocket = listenerSocket.accept();

                    //GOT A MESSAGE! <3
                    clientMessage = MessageUtil.readMessage(clientSocket);
                }
                synchronized (AppConfig.colorLock) {
                    AppConfig.timestampedStandardPrint("Working on the message " + clientMessage + ", currentState: " + AppConfig.initiatorVersions.toString() + ", messageState: " + clientMessage.getInitiatorVersions().toString());
                    boolean sameVersions = true;
                    for(Integer initiator: AppConfig.initiatorVersions.keySet()) {
                        if(AppConfig.initiatorVersions.get(initiator) < clientMessage.getInitiatorVersions().get(initiator)) {
                            sameVersions = false;
                            break;
                        }
                    }
                    if (!sameVersions) {
                        /*
                         * If the message is red, we are white, and the message isn't a marker,
                         * then store it. We will get the marker soon, and then we will process
                         * this message. The point is, wcae need the marker to know who to send
                         * our info to, so this is the simplest way to work around that.
                         */
//                        if (clientMessage.getMessageType() == MessageType.LY_REGION_TRANSACTION || clientMessage.getMessageType() == MessageType.LY_BLANK_STATUS) {
//                            AppConfig.timestampedStandardPrint("Got the region transaction message.");
//                        } else if (clientMessage.getMessageType() != MessageType.LY_MARKER) {
//                            AppConfig.timestampedStandardPrint("Adding message to the redMessage queue.");
//                            redMessages.add(clientMessage);
//                            continue;
//                        } else {
//                            LaiYangBitcakeManager lyFinancialManager = (LaiYangBitcakeManager) snapshotCollector.getBitcakeManager();
//                            lyFinancialManager.markerEvent(Integer.parseInt(clientMessage.getMessageText()), snapshotCollector);
//                        }
                        if (clientMessage.getMessageType() == MessageType.LY_MARKER) {
                            LaiYangBitcakeManager lyFinancialManager = (LaiYangBitcakeManager) snapshotCollector.getBitcakeManager();
                            lyFinancialManager.markerEvent(Integer.parseInt(clientMessage.getMessageText()), snapshotCollector);
                        } else if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
                            AppConfig.timestampedStandardPrint("Adding message to the redMessage queue.");
                            redMessages.add(clientMessage);
                            continue;
                        } else {
                            AppConfig.timestampedStandardPrint("Got a message that's not a transaction or a marker. Carrying on.");
                        }
                    }
                }

                MessageHandler messageHandler = new NullHandler(clientMessage);

                /*
                 * Each message type has it's own handler.
                 * If we can get away with stateless handlers, we will,
                 * because that way is much simpler and less error prone.
                 */
                switch (clientMessage.getMessageType()) {
                    case TRANSACTION:
                        messageHandler = new TransactionHandler(clientMessage, snapshotCollector.getBitcakeManager());
                        break;
                    case LY_MARKER:
                        messageHandler = new LYMarkerHandler();
                        break;
                    case LY_TELL:
                        messageHandler = new LYTellHandler(clientMessage, snapshotCollector);
                        break;
                    case TREE_QUERY:
                        messageHandler = new TreeQueryHandler(clientMessage);
                        break;
                    case TREE_ACCEPT:
                        messageHandler = new TreeAcceptHandler(clientMessage);
                        break;
                    case TREE_REJECT:
                        messageHandler = new TreeRejectHandler(clientMessage);
                        break;
                    case TREE_BORDER_REJECT:
                        messageHandler = new TreeBorderRejectHandler(clientMessage);
                        break;
                    case FOUND_REGION:
                        messageHandler = new FoundRegionHandler(clientMessage);
                        break;
                    case LY_COLLECTED_RESULTS:
                        messageHandler = new LYCollectedResultsHandler(clientMessage, snapshotCollector);
                        break;
                    case LY_REGION_TRANSACTION:
                        messageHandler = new LYRegionTransactionHandler(clientMessage, snapshotCollector);
                        break;
                    case TREE_RESET:
                        messageHandler = new TreeResetHandler(clientMessage);
                        break;
                    case TREE_RESET_CONFIRM:
                        messageHandler = new TreeResetConfirmHandler(clientMessage);
                        break;
                }

                threadPool.submit(messageHandler);
            } catch (SocketTimeoutException timeoutEx) {
                //Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

}
