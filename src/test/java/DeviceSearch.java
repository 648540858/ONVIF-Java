import be.teletask.onvif.DiscoveryManager;
import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.*;
import be.teletask.onvif.models.*;
import be.teletask.onvif.responses.OnvifResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceSearch {

    Map<String, Device> deviceMap = new HashMap<>();

    @Before
    public void getDevice() {
        DiscoveryManager manager = new DiscoveryManager();
        manager.setDiscoveryTimeout(2000);

        manager.discover(new DiscoveryListener() {
            @Override
            public void onDiscoveryStarted() {
                System.out.println("Discovery started");
            }

            @Override
            public void onDevicesFound(List<Device> devices) {
                if (devices == null || devices.size() == 0) return;
                for (Device device : devices){
                    deviceMap.put(device.getHostName(),  device);
                }
            }

            @Override
            public void onDiscoveryFinished() {
                for (Device device : deviceMap.values()) {
                    System.out.println(device.getHostName());
                }
            }
        });

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDeviceInfo(){
        OnvifManager onvifManager = new OnvifManager();
        onvifManager.setOnvifResponseListener(new OnvifResponseListener(){

            @Override
            public void onResponse(OnvifDevice onvifDevice, OnvifResponse response) {
                System.out.println("[RESPONSE] " + onvifDevice.getHostName()
                        + "======" + response.getErrorCode()
                        + "======" + response.getErrorMessage());
            }

            @Override
            public void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage) {
                System.out.println("[ERROR] " + onvifDevice.getHostName() + "======" + errorCode + "=======" + errorMessage);
            }
        });

        for (Device device : deviceMap.values()) {
            String username = null;
            String password = null;
            if (device.getHostName().contains("192.168.1.252")) {
                username = "admin";
                password = "admin123";
            }else if  (device.getHostName().contains("192.168.1.251")) {

            }
            if (username == null || password == null) continue;
            OnvifDevice onvifDevice = new OnvifDevice(device.getHostName(), username, password);

            onvifManager.getServices(onvifDevice, new OnvifServicesListener() {
                @Override
                public void onServicesReceived(OnvifDevice onvifDevice, OnvifServices services) {
                    if (services.getProfilesPath().equals("/onvif/Media")) {
                        onvifDevice.setPath(services);
                        onvifManager.getMediaProfiles(onvifDevice, new OnvifMediaProfilesListener() {
                            @Override
                            public void onMediaProfilesReceived(OnvifDevice device,
                                                                List<OnvifMediaProfile> mediaProfiles) {
                                for (OnvifMediaProfile mediaProfile : mediaProfiles) {
                                    System.out.println(mediaProfile.getName());
                                    System.out.println(mediaProfile.getToken());
                                    if (mediaProfile.getName().equals("mainStream")){
                                        onvifManager.getMediaStreamURI(device, mediaProfile, new OnvifMediaStreamURIListener() {
                                            @Override
                                            public void onMediaStreamURIReceived(OnvifDevice device,
                                                                                 OnvifMediaProfile profile, String uri) {
                                                System.out.printf(device + "=====" + profile.getName() + "=====" + uri);
                                            }
                                        });
                                    }
                                }

                            }
                        });




                    }
//                    System.out.println("[RECEIVED] " + onvifDevice.getHostName() + "=======" + services.getServicesPath());
//                    System.out.println("[RECEIVED] " + onvifDevice.getHostName() + "=======" + services.getDeviceInformationPath());
//                    System.out.println("[RECEIVED] " + onvifDevice.getHostName() + "=======" + services.getProfilesPath());
//                    System.out.println("[RECEIVED] " + onvifDevice.getHostName() + "=======" + services.getStreamURIPath());
                }
            });
//            onvifManager.getDeviceInformation(onvifDevice, new OnvifDeviceInformationListener() {
//                @Override
//                public void onDeviceInformationReceived(OnvifDevice device,
//                                                        OnvifDeviceInformation deviceInformation) {
//                    System.out.println("[DeviceInformationReceived] " + device.getHostName() + "======" + deviceInformation.getSerialNumber());
//                    System.out.println("[DeviceInformationReceived] " + device.getHostName() + "======" + deviceInformation.getManufacturer());
//                    System.out.println("[DeviceInformationReceived] " + device.getHostName() + "======" + deviceInformation.getHardwareId());
//                    System.out.println("[DeviceInformationReceived] " + device.getHostName() + "======" + deviceInformation.getFirmwareVersion());
//                    System.out.println("[DeviceInformationReceived] " + device.getHostName() + "======" + deviceInformation.getModel());
//                }
//            });
        }

        try {
            Thread.sleep(5500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
