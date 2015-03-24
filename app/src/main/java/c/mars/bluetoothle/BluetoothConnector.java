package c.mars.bluetoothle;

/**
 * Created by Constantine Mars on 3/24/15.
 */
public interface BluetoothConnector {
    public void scan(boolean enable);
    public boolean isScanning();
}
