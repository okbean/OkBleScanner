package okble.central.scanner;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import java.util.List;

abstract class ScanExecutor {

    public abstract void executeStartScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback callback);

    public abstract void executeStopScan(final ScanCallback callback);

}
