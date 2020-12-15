package okble.central.scanner;

public interface BleScanResultFilter {

    boolean accept(BleScanResult result);
}
