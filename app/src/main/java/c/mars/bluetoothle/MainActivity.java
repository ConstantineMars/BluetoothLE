package c.mars.bluetoothle;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    private BluetoothConnector connector;
    private BluetoothCallbacks callbacks = new BluetoothCallbacks() {
        @Override
        public void start() {
            checkBox.setEnabled(false);
            text.append("\nscanning...");
            scan.setText("stop");
        }

        @Override
        public void stop() {
            checkBox.setEnabled(true);
            text.append("\nscan stopped");
            scan.setText("scan");
        }

        @Override
        public void deviceFound(BluetoothDevice device) {
            text.append("\ndevice: name:"+device.getName()+", addr:"+device.getAddress()+" uuids:"+device.getUuids()+", type:"+device.getType()+", btclass:"+device.getBluetoothClass()+", state="+device.getBondState());
        }
    };

    @InjectView(R.id.text)
    TextView text;
    @InjectView(R.id.scan)
    Button scan;
    @InjectView(R.id.checkbox)
    CheckBox checkBox;

    private boolean enable = false;

    @OnClick(R.id.scan)
    void btnClick() {
        connector.scan(!connector.isScanning());
    }

    @OnCheckedChanged(R.id.checkbox)
    void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        createConnector(isChecked);
    }

    private void createConnector(boolean ble) {
        if (ble) {
            connector = new BluetoothLEConnector(this, callbacks);
        } else {
            connector = new BluetoothClassicConnector(this, callbacks);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        createConnector(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
