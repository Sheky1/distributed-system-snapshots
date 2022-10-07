package app.snapshot_bitcake;

import app.Cancellable;

import java.util.Map;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();

	void addLYSnapshotInfo(int id, LYSnapshotResult lySnapshotResult);

	boolean addRegionTransactionValue(Map<Integer, Map<Integer, LYSnapshotResult>> regionTransactionValue);

	void startCollecting();

	Map<Integer, Map<Integer, LYSnapshotResult>> getCollectedRegionTransactionValues();

	void addResults(RegionInfo regionInfo);

	Map<Integer, LYSnapshotResult> getCollectedLYValues();

	void addAllLYSnapshotInfo(Map<Integer, LYSnapshotResult> newCollectedLYValues);
}