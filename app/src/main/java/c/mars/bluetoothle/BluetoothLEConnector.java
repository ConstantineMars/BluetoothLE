package c.mars.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by Constantine Mars on 3/24/15.
 */
public class BluetoothLEConnector implements BluetoothConnector {
    private Context context;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;

    public boolean isScanning() {
        return scanning;
    }

    @Override
    public void stop() {
        if (gatt == null) {
            return;
        }

        gatt.close();
        gatt = null;
    }

    boolean scanning = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private ArrayList<BluetoothDevice> list = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!list.contains(device)) {
                Timber.d("device: " + device);
                list.add(device);
                callbacks.deviceFound(device);
            }
        }
    };

    private BluetoothCallbacks callbacks;
    private BLECallbacks bleCallbacks;

    private BluetoothGatt gatt;
    private List<BluetoothGattService> services;
    private List<BluetoothGattCharacteristic> characteristics;
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Timber.d("connection[" + gatt.getDevice().getName() + "] state:" + newState + ", status:" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleCallbacks.connectedToGatt(gatt.getDevice().getName(), status, newState);

                bleCallbacks.discoveringServices();
                gatt.discoverServices();
            } else {
                bleCallbacks.log("onConnectionStateChange:["+status+","+newState+"]");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            bleCallbacks.log("onServicesDiscovered:"+status);

            bleCallbacks.log("reading device name characteristic...");
            BluetoothLESubscription.requestDeviceName(gatt);
//            services = gatt.getServices();
//            bleCallbacks.log("> [s] services:");
//            for (BluetoothGattService service:services) {
//                bleCallbacks.log("");
//                bleCallbacks.log("> [s] "+service.getUuid()+", type:"+service.getType());
//                bleCallbacks.log(">> [c] characteristics:");
//                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//                if (!characteristics.isEmpty()) {
//                    for (BluetoothGattCharacteristic characteristic : characteristics) {
//                        byte[] value = characteristic.getValue();
//                        bleCallbacks.log(">> [c] " + characteristic.getUuid() + "=" + (value != null ? value.toString() : "null"));
//
//                        if (characteristic.getUuid().equals(UUID.fromString()))
////                        gatt.readCharacteristic(characteristic);
//
//                        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                        if (!descriptors.isEmpty()) {
//                            bleCallbacks.log(">>> [d] descriptors:");
//                            for (BluetoothGattDescriptor descriptor : descriptors) {
//                                bleCallbacks.log(">>> [d] " + descriptor.getUuid() + "=" + (descriptor.getValue() != null ? descriptor.getValue().toString() : "null"));
//                            }
//                        }
//                    }
//                }
//            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String deviceName = BluetoothLESubscription.readDeviceName(characteristic);
            if (deviceName == null) {
                bleCallbacks.log("characteristic read: " + characteristic.getUuid() + "=" + (characteristic.getValue() != null ? Arrays.toString(characteristic.getValue()) : "null") + ", status=" + status);
            } else {
                bleCallbacks.log("name: "+deviceName);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            bleCallbacks.log("characteristic changed: "+characteristic.getUuid()+"="+(characteristic.getValue()!=null? Arrays.toString(characteristic.getValue()):"null"));
        }
    };

    public void subscribeForNotifications(BluetoothGattCharacteristic characteristic, String descriptorUUID) {
        gatt.setCharacteristicNotification(characteristic, true);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(descriptorUUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    public void unSubscribeForNotifications(BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, false);
    }

    public BluetoothLEConnector(Context context, BluetoothCallbacks callbacks, BLECallbacks bleCallbacks) {
        this.context = context;
        this.callbacks = callbacks;
        this.bleCallbacks = bleCallbacks;
        Timber.plant(new Timber.DebugTree());

        manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
    }

    public void scan(boolean enable) {
        Timber.d("scan:"+enable);
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scanning) {
                        scanning = false;
                        adapter.stopLeScan(callback);
                        callbacks.stop();
                    }
                }
            }, 10000); // 10 sec
            scanning = true;
            adapter.startLeScan(callback);
            adapter.startDiscovery();
            callbacks.start();
        } else {
            scanning = false;
            adapter.stopLeScan(callback);
            callbacks.stop();
        }
    }

    public void connect(String address) {
        BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null) {
            Timber.e("can't find device "+address);
            return;
        }

        bleCallbacks.connectingToGatt(address);
        gatt = device.connectGatt(context, false, gattCallback);
    }

    public interface BLECallbacks {
        void connectingToGatt(String address);
        void connectedToGatt(String deviceName, int status, int newState);
        void discoveringServices();
        void services(List<BluetoothGattService> services);
        void log(String msg);
    }

}
