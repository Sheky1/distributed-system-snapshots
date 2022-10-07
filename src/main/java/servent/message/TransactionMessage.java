package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.LYSnapshotResult;
import app.snapshot_bitcake.LaiYangBitcakeManager;
import servent.message.snapshot.LYTellMessage;

import java.util.List;
import java.util.Map;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {

	private static final long serialVersionUID = -333251402058492901L;

	private final transient BitcakeManager bitcakeManager;
	
	public TransactionMessage(ServentInfo sender, ServentInfo receiver, int amount, BitcakeManager bitcakeManager) {
		super(MessageType.TRANSACTION, sender, receiver, String.valueOf(amount));
		this.bitcakeManager = bitcakeManager;
	}

	private TransactionMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, boolean white, List<ServentInfo> routeList, String messageText,
						  int messageId, BitcakeManager bitcakeManager, Map<Integer, Integer> initiatorVersions) {
		super(messageType, sender, receiver, routeList, messageText, messageId, initiatorVersions);
		this.bitcakeManager = bitcakeManager;
	}

	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());
		
		bitcakeManager.takeSomeBitcakes(amount);
		if (bitcakeManager instanceof LaiYangBitcakeManager) {
			LaiYangBitcakeManager lyFinancialManager = (LaiYangBitcakeManager)bitcakeManager;
			lyFinancialManager.recordGiveTransaction(getReceiverInfo().getId(), amount);
		}
	}

	@Override
	public Message setInitiatorVersions() {
		return new TransactionMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId(), getBitcakeManager(), AppConfig.initiatorVersions);
	}

	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}
}

