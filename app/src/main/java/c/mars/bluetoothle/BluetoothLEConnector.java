package c.mars.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

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
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Timber.d("connection["+gatt.getDevice().getName()+"] state:"+newState+", status:"+status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleCallbacks.connectedToGatt(gatt.getDevice().getName(), status, newState);

                bleCallbacks.discoveringServices();
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            bleCallbacks.services(services);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    };

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
    }

}
