package okble.central.scanner;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

public final class BleScanResult {

    private final int rssi;
    private final BluetoothDevice device;
    private final byte[] scanRecordData;


    public static BleScanResult from(final ScanResult result){
        final BluetoothDevice device = result.getDevice();
        final int rssi = result.getRssi();
        byte[] scanRecordData = null;
        if(result.getScanRecord() != null){
            scanRecordData = result.getScanRecord().getBytes();
        }
        final BleScanResult val = new BleScanResult(device, rssi, scanRecordData);
        return val;
    }

    public BleScanResult(BluetoothDevice device, int rssi, byte[] scanRecordData){
        this.rssi = rssi;
        this.device = device;
        this.scanRecordData =scanRecordData;
    }

    public int rssi() {
        return rssi;
    }

    public BluetoothDevice device() {
        return device;
    }

    public byte[] scanRecordData() {
        return scanRecordData;
    }

    @Override
    public boolean equals(Object obj) {
        if((this == obj)){
            return true;
        }
        if(obj != null && obj instanceof BleScanResult){
            final BleScanResult val = (BleScanResult)obj;
            return this.device.equals(val.device);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.device.hashCode();
    }


    @Override
    public String toString() {
        return "BleScanResult{" +
                "rssi=" + rssi +
                ", device=" + device +
                '}';
    }
}
