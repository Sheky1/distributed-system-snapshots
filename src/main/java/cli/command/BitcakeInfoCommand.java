package cli.command;


import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;

public class BitcakeInfoCommand implements CLICommand {

	private final SnapshotCollector collector;
	
	public BitcakeInfoCommand(SnapshotCollector collector) {
		this.collector = collector;
	}
	
	@Override
	public String commandName() {
		return "bitcake_info";
	}

	@Override
	public void execute(String args) {
		if(AppConfig.initiatorVersions.containsKey(AppConfig.myServentInfo.getId())) collector.startCollecting();
		else AppConfig.timestampedErrorPrint("I'm not an initiator. Cannot start bitcake_info command.");
	}

}
