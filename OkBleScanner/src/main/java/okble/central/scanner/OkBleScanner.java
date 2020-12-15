package okble.central.scanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static okble.central.scanner.BleScanListener.BLUETOOTH_NOT_ENABLED;
import static okble.central.scanner.BleScanListener.LOCATION_SETTINGS_NOT_ENABLED;
import static okble.central.scanner.BleScanListener.NO_PERMISSION_TO_ACCESS_LOCATION;
import static okble.central.scanner.BleScanListener.SCAN_COMPLETE;
import static okble.central.scanner.BleScanListener.USER_STOP_SCAN;

public final class OkBleScanner {

    private final Handler mWorkerHandler;
    private final Handler mMainHandler;
    private OkBleScanner(){
        final HandlerThread thread = new HandlerThread("OkBleScanner");
        thread.start();
        this.mWorkerHandler = new Handler(thread.getLooper());
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static OkBleScanner getDefault(){
        return Holder.VAL;
    }

    public void stopScan(){
        synchronized (this){
            if(mScanWorker != null){
                mScanWorker.setStop();
                mScanWorker = null;
            }
        }
    }

    public boolean startScan(final Context ctx, final ScanConfig config){
        return startScan(ctx, config, null);
    }

    public boolean startScan(final Context ctx){
        return startScan(ctx, null, null);
    }

    private volatile ScanWorker mScanWorker = null;
    protected boolean startScan(final Context ctx, final ScanConfig config0, final ScanExecutor executor0){
        synchronized (this){
            if(isWorking()){
                return false;
            }
            if(ctx == null){
                throw new IllegalArgumentException("arg ctx of Context can not be null!");
            }
            if(!mRegisterBluetoothStateReceiver){
                mRegisterBluetoothStateReceiver = true;
                registerBluetoothStateReceiver(ctx);
            }
            final ScanConfig config = config0 == null ? ScanConfig.getDefault() : config0;
            checkScanConfig(config);
            final ScanExecutor executor = executor0 == null ? new DefaultScanExecutor() : executor0;
            final ScanWorker worker = new ScanWorker(ctx.getApplicationContext(), config, executor);
            mScanWorker = worker;
            mWorkerHandler.post(worker);
        }
        return true;
    }

    public boolean isWorking(){
        return mScanWorker != null;
    }

    public boolean isScanning(){
        return mScanWorker != null &&
                mScanWorker.state == ScanState.SCANNING;
    }

    public boolean isScanIdle(){
        return mScanWorker != null &&
                mScanWorker.state == ScanState.IDLE;
    }


    public BleScanResult scanForResult(final Context ctx, final String address, final long timeout, final TimeUnit timeUnit){
        final BleScanResult val = scanForResult(ctx, new AddressFilter(address), timeout, timeUnit);
        return val;
    }

    public BleScanResult scanForResult(final Context ctx, final BleScanResultFilter filter, final long timeout, final TimeUnit timeUnit){
        checkMainThread("can not be called in main thread!");
        final BleScanResult[] results = new BleScanResult[1];
        final CountDownLatch lock = new CountDownLatch(1);
        final BleScanListener listener = new BleScanListener(){

            @Override
            public void onScanStart(OkBleScanner scanner) {
            }

            @Override
            public void onScanIdle(OkBleScanner scanner) {
            }
            @Override
            public void onScanning(OkBleScanner scanner) {
            }

            @Override
            public void onScanResult(OkBleScanner scanner, BleScanResult result) {
                if(filter != null && filter.accept(result)){
                    results[0] = result;
                    lock.countDown();
                }
            }

            @Override
            public void onScanComplete(OkBleScanner scanner, int code) {
                lock.countDown();
            }
        };
        addScanListener(listener);
        startScan(ctx);
        try {
            lock.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        removeScanListener(listener);
        return results[0];
    }


    private final static void checkMainThread(final String errorMsg){
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException(errorMsg);
        }
    }

    private static void checkScanConfig(final ScanConfig config){
        if(config.scanInterval() < 2_000L){
            throw new IllegalArgumentException("scan interval should be more than 2000 mills!");
        }

        if(config.scanPeriodCount() < 1){
            throw new IllegalArgumentException("scan period count should be more than zero!");
        }

        if(config.scanPeriod() < 5_000L){
            throw new IllegalArgumentException("scan period should be more than 5000 mills!");
        }
    }


    private boolean mRegisterBluetoothStateReceiver = false;

    private enum ScanState {
        NONE, START, IDLE, SCANNING, COMPLETE;
    }

    private final class ScanWorker implements Runnable{
        final ScanConfig mConfig;
        final ScanExecutor mExecutor;
        final Context mContext;
        public ScanWorker(final Context context, final ScanConfig config0, final ScanExecutor executor0){
            this.mContext = context;
            this.mConfig = config0;
            this.mExecutor = executor0;
        }

        private ScanState state = ScanState.NONE;

        @Override
        public void run() {
            state = ScanState.START;
            fireScanStart();

            if(mStop){
                state = ScanState.COMPLETE;
                fireScanComplete(this, USER_STOP_SCAN, null);
                return;
            }

            if(!checkScanAble()){
                return;
            }


            final int count = mConfig.scanPeriodCount();
            if(count < 1){
                state = ScanState.COMPLETE;
                fireScanComplete(this,SCAN_COMPLETE, null);
                return;
            }

            for(int i=0; i<count; i++){
                final long delay = (i==0) ? mConfig.scanDelay() : mConfig.scanInterval();
                state = ScanState.IDLE;

                if(delay > 0){
                    fireScanIdle();
                    await(delay);

                    if(mStop){
                        state = ScanState.COMPLETE;
                        fireScanComplete(this,USER_STOP_SCAN, null);
                        return;
                    }

                    if(!checkScanAble()){
                        return;
                    }
                }


                state = ScanState.SCANNING;
                fireScanning();
                startScan();

                final long period = mConfig.scanPeriod();
                await(period);

                stopScan();

                if(mErrorCode != 0){
                    state = ScanState.COMPLETE;
                    fireScanComplete(this,mErrorCode, null);
                    break;
                }else if(mStop){
                    state = ScanState.COMPLETE;
                    fireScanComplete(this,USER_STOP_SCAN, null);
                    break;

                }else if(i == (count - 1)){
                    state = ScanState.COMPLETE;
                    fireScanComplete(this,SCAN_COMPLETE, null);
                    break;
                }else {
                    if(!checkScanAble()){
                        return;
                    }
                }
            }
        }


        private boolean checkScanAble(){
            if(!Utils.isBluetoothEnabled()){
                state = ScanState.COMPLETE;
                fireScanComplete(this,BLUETOOTH_NOT_ENABLED, null);
                return false;

            }else if(!Utils.hasPermissions(mContext, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !Utils.hasPermissions(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)){
                state = ScanState.COMPLETE;
                fireScanComplete(this,NO_PERMISSION_TO_ACCESS_LOCATION, null);
                return false;

            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Utils.isLocationSettingsEnabled(mContext)){
                state = ScanState.COMPLETE;
                fireScanComplete(this,LOCATION_SETTINGS_NOT_ENABLED, null);
                return false;
            }

            return true;
        }

        private void await(final long duration){
            if(duration <= 0){
                return;
            }
            boolean interrupted = false;
            synchronized (this){
                try {
                    this.wait(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    interrupted = true;
                }
            }
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }

        private void stopScan(){
            try{
                mExecutor.executeStopScan(mScanCallback);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private void startScan(){
            try{
                mExecutor.executeStartScan(mScanCallback);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private boolean mStop = false;
        public void setStop(){
            synchronized (this){
                mStop = true;
                this.notify();
            }
        }

        private int mErrorCode = 0;
        public void setScanFailed(int errorCode){
            synchronized (this){
                mErrorCode = errorCode;
                this.notify();
            }
        }
    }


    private final ScanCallback0 mScanCallback = new ScanCallback0(){

        @Override
        public void onScanResult0(int callbackType, BleScanResult result) {
            if(result == null){
                return;
            }
            if(mScanWorker == null || mScanWorker.state != ScanState.SCANNING){
                return;
            }
            fireScanResult(result);
        }

        @Override
        public void onScanFailed0(int errorCode) {
            if(mScanWorker != null){
                mScanWorker.setScanFailed(errorCode);
            }
        }
    };



    private void fireScanStart(){
        final ArrayList<BleScanListener> list = new ArrayList<BleScanListener>(mBleScanListeners);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for(BleScanListener  l : list){
                    l.onScanStart(OkBleScanner.this);
                }
            }
        });
    }

    private void fireScanIdle(){
        final ArrayList<BleScanListener> list = new ArrayList<BleScanListener>(mBleScanListeners);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for(BleScanListener  l : list){
                    l.onScanIdle(OkBleScanner.this);
                }
            }
        });
    }

    private void fireScanning(){
        final ArrayList<BleScanListener> list = new ArrayList<BleScanListener>(mBleScanListeners);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for(BleScanListener  l : list){
                    l.onScanning(OkBleScanner.this);
                }
            }
        });
    }

    private void fireScanResult(final BleScanResult val){
        final ArrayList<BleScanListener> list = new ArrayList<BleScanListener>(mBleScanListeners);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for(BleScanListener  l : list){
                    l.onScanResult(OkBleScanner.this,val);
                }
            }
        });
    }

    private void fireScanComplete(final ScanWorker scanWorker, final int code, final Exception ex){
        if(scanWorker == mScanWorker){
            mScanWorker = null;
        }
        final ArrayList<BleScanListener> list = new ArrayList<BleScanListener>(mBleScanListeners);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for(BleScanListener  l : list){
                    l.onScanComplete(OkBleScanner.this, code);
                }
            }
        });
    }

    private final ArrayList<BleScanListener> mBleScanListeners = new ArrayList<BleScanListener>(2);
    public void addScanListener(BleScanListener listener){
        if(listener == null || mBleScanListeners.contains(listener)){
            return;
        }
        mBleScanListeners.add(listener);
    }
    public void removeScanListener(BleScanListener listener){
        if(listener == null || !mBleScanListeners.contains(listener)){
            return;
        }
        mBleScanListeners.remove(listener);
    }
    public void clearScanListeners(){
        mBleScanListeners.clear();
    }


    private final static class Holder{
       private final static OkBleScanner VAL = new OkBleScanner();
    }


    private void registerBluetoothStateReceiver(final Context ctx){
        try{
            ctx.getApplicationContext().registerReceiver(mBluetoothStateReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void unregisterBluetoothStateReceiver(final Context ctx){
        ctx.getApplicationContext().unregisterReceiver(mBluetoothStateReceiver);
    }
    private final BroadcastReceiver mBluetoothStateReceiver = new BluetoothStateReceiver();
    private final class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);
            if(state == BluetoothAdapter.STATE_OFF ||
                    state == BluetoothAdapter.STATE_TURNING_OFF){
                if(mScanWorker != null){
                    mScanWorker.setScanFailed(BLUETOOTH_NOT_ENABLED);
                }
            }
        }
    }

}