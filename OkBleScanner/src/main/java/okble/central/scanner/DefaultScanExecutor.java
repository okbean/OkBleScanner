package okble.central.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import java.util.List;

final class DefaultScanExecutor extends ScanExecutor{

    @Override
    public void executeStartScan(List<ScanFilter> filters, ScanSettings settings, final ScanCallback callback) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            final BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanner.startScan(filters,
                    (settings == null ? new ScanSettings.Builder().build() : settings),
                    callback);
        }
    }

    @Override
    public void executeStopScan(final ScanCallback callback) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            final BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanner.stopScan(callback);
        }
    }
}
