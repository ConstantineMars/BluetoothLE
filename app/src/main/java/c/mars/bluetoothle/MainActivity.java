package c.mars.bluetoothle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import timber.log.Timber;


public class MainActivity extends ActionBarActivity {

    private BluetoothConnector connector;
    private BluetoothCallbacks callbacks = new BluetoothCallbacks() {
        @Override
        public void start() {
            addLine("scanning...");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkBox.setEnabled(false);
                    scan.setText("stop");
                }
            });
        }

        @Override
        public void stop() {
            addLine("scan stopped");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkBox.setEnabled(true);
                    scan.setText("scan");
                }
            });
        }

        @Override
        public void deviceFound(BluetoothDevice device) {
            addLine("device: name:"+device.getName()+", addr:"+device.getAddress()+" uuids:"+device.getUuids()+", type:"+device.getType()+", btclass:"+device.getBluetoothClass()+", state="+device.getBondState());
        }
    };
    private BluetoothLEConnector.BLECallbacks bleCallbacks = new BluetoothLEConnector.BLECallbacks() {
        @Override
        public void connectingToGatt(String address) {
            addLine("connecting to gatt:" + address);
        }

        @Override
        public void connectedToGatt(String deviceName, int status, int newState) {
            addLine("gatt connected:"+deviceName+"["+status+":"+newState+"]");
        }

        @Override
        public void discoveringServices() {
            addLine("discovering services...");
        }

        @Override
        public void services(List<BluetoothGattService> services) {
            addLine("\n");
            for (BluetoothGattService service:services) {
                addLine("service available: "+service.getUuid()+": type:"+service.getType()+", chars.size:"+service.getCharacteristics().size());
            }
        }

        @Override
        public void log(String msg) {
            addLine(msg);
        }
    };

    private void addLine(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.append("\n"+str);
            }
        });
    }

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

    @InjectView(R.id.address)
    EditText address;

    @OnClick(R.id.connect)
    void connect(){
        if (connector instanceof BluetoothLEConnector) {
            String addr = address.getText().toString();
            Timber.d("connect to:"+addr);
            ((BluetoothLEConnector)connector).connect(addr);
        }
    }

    @OnCheckedChanged(R.id.checkbox)
    void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        createConnector(isChecked);
    }

    private void createConnector(boolean ble) {
        if (ble) {
            connector = new BluetoothLEConnector(this, callbacks, bleCallbacks);
        } else {
            connector = new BluetoothClassicConnector(this, callbacks);
        }
    }

    private static final String PREF_ADDR = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        createConnector(checkBox.isChecked());

        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().putString(PREF_ADDR, address.getText().toString()).commit();
            }
        });
        address.setText(PreferenceManager.getDefaultSharedPreferences(getApplication()).getString(PREF_ADDR, ""));
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
