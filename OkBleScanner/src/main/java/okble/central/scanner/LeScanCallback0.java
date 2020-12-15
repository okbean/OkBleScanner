package okble.central.scanner;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

abstract class LeScanCallback0 implements BluetoothAdapter.LeScanCallback {

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
        final BleScanResult val = new BleScanResult(device, rssi, scanRecord);
        onLeScan(val);
    }

    public abstract void onLeScan(final BleScanResult result);
}