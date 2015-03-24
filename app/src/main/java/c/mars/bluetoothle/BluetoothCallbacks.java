package c.mars.bluetoothle;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Constantine Mars on 3/24/15.
 */
public interface BluetoothCallbacks {
    public void start();
    public void stop();
    public void deviceFound(BluetoothDevice device);
}
