package uk.oczadly.karl.nanopowbench;

/**
 * @author Karl Oczadly
 */
public class Device {
    
    private final int platformId, deviceId;
    private final long maxLocalWorkSize;
    private final String platformName, deviceName;
    
    public Device(int platformId, int deviceId, long maxLocalWorkSize, String platformName, String deviceName) {
        this.platformId = platformId;
        this.deviceId = deviceId;
        this.maxLocalWorkSize = maxLocalWorkSize;
        this.platformName = platformName;
        this.deviceName = deviceName;
    }
    
    
    public int getPlatformId() {
        return platformId;
    }
    
    public int getDeviceId() {
        return deviceId;
    }
    
    public long getMaxLocalWorkSize() {
        return maxLocalWorkSize;
    }
    
    public String getPlatformName() {
        return platformName;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
}
