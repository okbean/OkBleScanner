package okble.central.scanner;

public class AddressFilter implements BleScanResultFilter{

    private final String mAddress;
    public AddressFilter(final String address){
        this.mAddress = address;
    }

    @Override
    public boolean accept(BleScanResult result) {
        if(result != null && result.device()
                .getAddress().equalsIgnoreCase(mAddress)){
            return true;
        }
        return false;
    }
}
