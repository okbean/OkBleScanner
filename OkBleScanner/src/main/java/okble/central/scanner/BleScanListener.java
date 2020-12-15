package okble.central.scanner;

import static android.bluetooth.le.ScanCallback.SCAN_FAILED_ALREADY_STARTED;
import static android.bluetooth.le.ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;
import static android.bluetooth.le.ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED;
import static android.bluetooth.le.ScanCallback.SCAN_FAILED_INTERNAL_ERROR;

public abstract class BleScanListener {

    public final static int SCAN_COMPLETE = 0;
    public final static int USER_STOP_SCAN = -1;
    public final static int BLUETOOTH_NOT_ENABLED = -2;
    public final static int NO_PERMISSION_TO_ACCESS_LOCATION = -3;
    public final static int LOCATION_SETTINGS_NOT_ENABLED = -4;

    public final static int ALREADY_STARTED =  SCAN_FAILED_ALREADY_STARTED;
    public final static int APPLICATION_REGISTRATION_FAILED =  SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;;
    public final static int INTERNAL_ERROR =  SCAN_FAILED_INTERNAL_ERROR;
    public final static int FEATURE_UNSUPPORTED = SCAN_FAILED_FEATURE_UNSUPPORTED;
    public final static int OUT_OF_HARDWARE_RESOURCES = 5;
    public final static int SCANNING_TOO_FREQUENTLY = 6;


    public abstract void onScanStart(OkBleScanner scanner);

    public abstract void onScanIdle(OkBleScanner scanner);

    public abstract void onScanning(OkBleScanner scanner);

    public abstract void onScanResult(OkBleScanner scanner, BleScanResult result);

    public abstract void onScanComplete(OkBleScanner scanner, int code);

}
