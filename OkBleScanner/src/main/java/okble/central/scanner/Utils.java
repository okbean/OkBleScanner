package okble.central.scanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

final class Utils {

    public static boolean isBluetoothEnabled(){
        final boolean val = BluetoothAdapter.getDefaultAdapter().isEnabled();
        return val;
    }


    public static boolean hasPermissions(final Context ctx, final String... perms){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String perm : perms) {
            if (checkSelfPermission(ctx, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private static int checkSelfPermission(final Context ctx, final String permission) {
        return ctx.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }


    public static boolean isLocationSettingsEnabled(final Context ctx){
        try {
            final int mode = Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return (mode == Settings.Secure.LOCATION_MODE_SENSORS_ONLY) ||
                    (mode == Settings.Secure.LOCATION_MODE_BATTERY_SAVING) ||
                    (mode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    private Utils(){}

}
