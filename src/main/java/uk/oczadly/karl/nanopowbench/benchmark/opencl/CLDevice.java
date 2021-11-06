package uk.oczadly.karl.nanopowbench.benchmark.opencl;

public class CLDevice {

    private final int platformId, deviceId;
    private String platformName, deviceName;

    public CLDevice(int platformId, int deviceId) {
        this.platformId = platformId;
        this.deviceId = deviceId;
    }


    public int getPlatformId() {
        return platformId;
    }

    public int getID() {
        return deviceId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
