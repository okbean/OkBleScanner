package okble.central.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.Build;

final class DefaultScanExecutor extends ScanExecutor{

    @Override
    public void executeStartScan(final ScanCallback callback) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            final BluetoothLeScanner scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            scanner.startScan(callback);
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
