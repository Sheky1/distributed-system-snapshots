package app.snapshot_bitcake;

import java.util.Map;

/**
 * This class is used if the user hasn't specified a snapshot type in config.
 * 
 * @author bmilojkovic
 *
 */
public class NullSnapshotCollector implements SnapshotCollector {

	@Override
	public void run() {}

	@Override
	public void stop() {}

	@Override
	public BitcakeManager getBitcakeManager() {
		return null;
	}

	@Override
	public void addLYSnapshotInfo(int id, LYSnapshotResult lySnapshotResult) {}

	@Override
	public boolean addRegionTransactionValue(Map<Integer, Map<Integer, LYSnapshotResult>> regionTransactionValue) {

		return false;
	}

	@Override
	public void startCollecting() {}

	@Override
	public Map<Integer, Map<Integer, LYSnapshotResult>> getCollectedRegionTransactionValues() {
		return null;
	}

	@Override
	public void addResults(RegionInfo regionInfo) {

	}

	@Override
	public Map<Integer, LYSnapshotResult> getCollectedLYValues() {
		return null;
	}

	@Override
	public void addAllLYSnapshotInfo(Map<Integer, LYSnapshotResult> newCollectedLYValues) {

	}

}
