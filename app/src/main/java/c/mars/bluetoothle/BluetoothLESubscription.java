package c.mars.bluetoothle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by Constantine Mars on 3/25/15.
 */
public class BluetoothLESubscription {
    public static final String UUID_SERVICE_GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARACTERISTIC_DEVICE_NAME = "00002A00-0000-1000-8000-00805f9b34fb";

    public static void requestDeviceName(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(UUID.fromString(UUID_SERVICE_GENERIC_ACCESS));
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(UUID_CHARACTERISTIC_DEVICE_NAME));
            gatt.readCharacteristic(characteristic);
        }
    }

    public static String readDeviceName(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(UUID_CHARACTERISTIC_DEVICE_NAME))){
            byte[] data = characteristic.getValue();
            if (data!=null && data.length>0){
                StringBuilder sb = new StringBuilder(data.length);
                for (byte b:data){
                    sb.append(String.format("%02X ", b));
                }
                sb.append(" -> ");
                for (byte b:data) {
                    sb.append((char)b);
                }
                return sb.toString();
            }
        }
        return null;
    }
}
