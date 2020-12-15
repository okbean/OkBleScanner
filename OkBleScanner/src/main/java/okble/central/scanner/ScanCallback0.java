package okble.central.scanner;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

abstract class ScanCallback0 extends ScanCallback {

    @Override
    public void onScanFailed(int errorCode) {
        onScanFailed0(errorCode);
    }


    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        final BleScanResult val = BleScanResult.from(result);
        onScanResult0(callbackType, val);
    }


    public abstract void onScanResult0(int callbackType, BleScanResult result);


    public abstract void onScanFailed0(int errorCode);
}
