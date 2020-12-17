# OkBleScanner
一个用于扫描BLE设备的安卓框架(A library for scanning ble device on android)

### 扫描参数(ScanConfig)
```
scanPeriod：扫描周期持续时长,默认30秒
scanPeriodCount : 扫描周期次数，默认2次
scanDelay：延迟多长时间进行第一次扫描，默认2秒
scanInterval：两次扫描周次之间的时间间隔，默认5秒

    static ScanConfig getDefault(){
        return new Builder()
                .scanPeriodCount(2)
                .scanDelay(2_000L)
                .scanInterval(5_000L)
                .scanPeriod(30_000L)
                .build();
    }

```

### 开始扫描
```
    //带扫描参数
    public boolean startScan(final Context ctx, final ScanConfig config)
    
    //使用默认扫描参数
    public boolean startScan(final Context ctx);
```

### 停止扫描
```
    OkBleScanner.getDefault().stopScan();
```

### 扫码回调（BleScanListener）
```
        final OkBleScanner scanner = OkBleScanner.getDefault();
        final ArrayList<BleScanResult> list = new ArrayList<BleScanResult>();
        scanner.addScanListener(new BleScanListener() {//增加扫描回调对象
            @Override
            public void onScanStart(OkBleScanner scanner) {//扫描开始
                Log.d(TAG, "onScanStart");
            }

            @Override
            public void onScanIdle(OkBleScanner scanner) {//扫描暂停
                Log.d(TAG, "onScanIdle");
            }

            @Override
            public void onScanning(OkBleScanner scanner) {//正在扫描
                Log.d(TAG, "onScanning");
            }

            @Override
            public void onScanResult(OkBleScanner scanner, BleScanResult result) {//扫描结果回调
                Log.d(TAG, "onScanResult:" + result);
                list.add(result);
            }

            @Override
            public void onScanComplete(OkBleScanner scanner, int code) { //扫描结束，code表示扫描因何而结束
                Log.d(TAG, "onScanComplete code:" + code);
                scanner.removeScanListener(this);//删除扫描回调对象
            }
        });
    
```


