package vbot.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
//import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity
{
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mBluetoothName;
    private ArrayList<Drawable> mBluetoothImage;
    private ArrayList<BluetoothDevice> mBluetoothDevice;;
    private Set<BluetoothDevice> pairedDevices;
    private ListView mListView;
    private Button scanButton;
    private ProgressBar scanProgress;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button bluetoothButton = (Button) findViewById(R.id.bluetooth);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openBluetoothDialog();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart(){
        super.onStart();
        checkBluetooth();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void checkBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    public void openBluetoothDialog() {
        DialogFragment newFragment = new bluetoothDialogFragment();
        newFragment.show(getFragmentManager(), "bluetooth");
    }

    public void pairedDevicesList() {
        mBluetoothDevice = new ArrayList<BluetoothDevice>();
        mBluetoothName = new ArrayList<String>();
        mBluetoothImage = new ArrayList<Drawable>();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mBluetoothDevice.add(device);
                mBluetoothName.add(device.getName());
                mBluetoothImage.add(getDrawableByMajorClass(device.getBluetoothClass().getMajorDeviceClass()));
            }
            CustomAdapter adapter = new CustomAdapter(this, mBluetoothDevice, mBluetoothName, mBluetoothImage);
            mListView.setAdapter(adapter);
            //mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                //@Override
                //public void onItemClick(AdapterView av, View v, int position, long id) {
                    ////mBluetoothAdapter.cancelDiscovery();
                    //BluetoothDevice device = (BluetoothDevice) mListView.getItemAtPosition(position);
                //}
            //});
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanProgress.setVisibility(View.VISIBLE);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanProgress.setVisibility(View.GONE);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
        }
    };

    public class bluetoothDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstance) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.bluetooth_dialog, null);

            mListView = (ListView) view.findViewById(R.id.pairedlist);
            scanButton = (Button) view.findViewById(R.id.scan);
            scanProgress = (ProgressBar) view.findViewById(R.id.progressbar);
            scanButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mBluetoothAdapter.startDiscovery();
                }
            });

            builder.setView(view);
            pairedDevicesList();
            return builder.create();
        }
    }

    public class CustomAdapter extends ArrayAdapter<BluetoothDevice> {
        private ArrayList<String> mNames;
        private ArrayList<Drawable> mImages;

        public CustomAdapter(Context context, ArrayList<BluetoothDevice> bluetooth, ArrayList<String> names, ArrayList<Drawable> images) {
            super(context, R.layout.bluetooth_adapter, bluetooth);
            mNames = names;
            mImages = images;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View layoutview = inflater.inflate(R.layout.bluetooth_adapter, parent, false);
            TextView mTextView = (TextView) layoutview.findViewById(R.id.bname);
            ImageView mImageView = (ImageView) layoutview.findViewById(R.id.bimage);
            mTextView.setText(mNames.get(position));
            mImageView.setImageDrawable(mImages.get(position));
            return layoutview;
        }
    }

    private Drawable getDrawableByMajorClass(int major) {
        Drawable drawable = null;
            switch (major) {
                case BluetoothClass.Device.Major.COMPUTER:
                    drawable = getResources().getDrawable(R.drawable.computer_icon);
                    break;
                case BluetoothClass.Device.Major.PHONE:
                    drawable = getResources().getDrawable(R.drawable.phone_icon);
                    break;
                default:
                    drawable = getResources().getDrawable(R.drawable.device_icon);
                    break;
            }
        return drawable;
    }
}