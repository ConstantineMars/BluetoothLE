package c.mars.bluetoothle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;

/**
 * Created by Constantine Mars on 3/24/15.
 */
public class BluetoothClassicConnector implements BluetoothConnector {
    private Context context;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothCallbacks callbacks;
    private boolean scanning = false;

    public BluetoothClassicConnector(Context context, BluetoothCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;

        manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, intentFilter);
    }

    public void scan(boolean enable) {
        if (enable) {
            scanning = true;
            callbacks.start();
            adapter.startDiscovery();
        } else {
            scanning = false;
            callbacks.stop();
            adapter.cancelDiscovery();
        }
    }

    @Override
    public boolean isScanning() {
        return scanning;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!devices.contains(device)) {
                    callbacks.deviceFound(device);
                    devices.add(device);
                }
            }
        }
    };
}
