package c.mars.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

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
                bleCallbacks.deviceFound(device);
            }
        }
    };

    private BluetoothCallbacks bleCallbacks;

    public BluetoothLEConnector(Context context, BluetoothCallbacks bleCallbacks) {
        this.context = context;
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
                    scanning = false;
                    adapter.stopLeScan(callback);
                    bleCallbacks.stop();
                }
            }, 10000); // 10 sec
            scanning = true;
            adapter.startLeScan(callback);
            adapter.startDiscovery();
            bleCallbacks.start();
        } else {
            scanning = false;
            adapter.stopLeScan(callback);
            bleCallbacks.stop();
        }
    }
}
