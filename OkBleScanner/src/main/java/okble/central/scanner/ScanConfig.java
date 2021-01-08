package okble.central.scanner;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;

import java.util.List;

public final class ScanConfig {

    private final long scanDelay;
    private final long scanInterval;
    private final int scanPeriodCount;
    private final long scanPeriod;
    private List<ScanFilter> mScanFilters;
    private ScanSettings mScanSettings;

    public ScanConfig(final Builder builder){
        this.scanDelay = builder.scanDelay;
        this.scanInterval = builder.scanInterval;
        this.scanPeriod = builder.scanPeriod;
        this.scanPeriodCount = builder.scanPeriodCount;
        this.mScanSettings = builder.mScanSettings;
        this.mScanFilters = builder.mScanFilters;
    }


    public long scanDelay() {
        return scanDelay;
    }

    public long scanInterval() {
        return scanInterval;
    }

    public int scanPeriodCount() {
        return scanPeriodCount;
    }

    public long scanPeriod() {
        return scanPeriod;
    }

    public List<ScanFilter> scanFilters() {
        return mScanFilters;
    }

    public ScanSettings scanSettings() {
        return mScanSettings;
    }


    static ScanConfig getDefault(){
        return new Builder()
                .scanPeriodCount(2)
                .scanDelay(2_000L)
                .scanInterval(5_000L)
                .scanPeriod(30_000L)
                .build();
    }

    public final static class Builder{
        private long scanDelay;
        private long scanInterval;
        private int scanPeriodCount;
        private long scanPeriod;

        private List<ScanFilter> mScanFilters;
        private ScanSettings mScanSettings;
        public Builder(){
        }

        public Builder scanDelay(long val){
            this.scanDelay = val;
            return this;
        }
        public Builder scanInterval(long val){
            this.scanInterval = val;
            return this;
        }
        public Builder scanPeriodCount(int val){
            this.scanPeriodCount = val;
            return this;
        }
        public Builder scanPeriod(long val){
            this.scanPeriod = val;
            return this;
        }

        public Builder scanSettings(ScanSettings settings){
            this.mScanSettings = settings;
            return this;
        }

        public Builder scanFilters(List<ScanFilter> filters){
            this.mScanFilters = filters;
            return this;
        }


        public ScanConfig build(){
            return new ScanConfig(this);
        }
    }

}
