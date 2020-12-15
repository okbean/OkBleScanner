package okble.central.scanner;

import android.bluetooth.le.ScanCallback;

abstract class ScanExecutor {

    public abstract void executeStartScan(final ScanCallback callback);

    public abstract void executeStopScan(final ScanCallback callback);

}
