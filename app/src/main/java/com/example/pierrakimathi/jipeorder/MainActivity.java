package com.example.pierrakimathi.jipeorder;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import java.util.ArrayList;

public class MainActivity extends BaseSampleActivity implements TestFragment.fragListener,
        MainFrag.callListener,
        DetailFrag.delListener,
        DetailFrag.numListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    FragmentManager fm;
    Fragment fragment;
    private TextView mTextView;
    ArrayList<BaseItem> myitemlist = new ArrayList<BaseItem>();

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] filters;
    boolean writeMode;
    Tag myTag;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.main_activity);

        mAdapter = new TestTitleFragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
        mIndicator = indicator;

        fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.right_frag_container);

        if (fragment == null) {

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.right_frag_container, new MainFrag());
            ft.commit();

        }


        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //NFC
        setUpNFC();
    }

    private void setUpNFC() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (this.nfcAdapter == null) {
            Toast.makeText(this, R.string.nfc_not_available, Toast.LENGTH_LONG).show();
            // finish();
            return;
        }

        Intent ndefDiscovered = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.pendingIntent = PendingIntent.getActivity(this, 0, ndefDiscovered, 0);

        IntentFilter ndefIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndefIntentFilter.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException(e);
        }
        filters = new IntentFilter[]{ndefIntentFilter};
        if (nfcAdapter.isEnabled()) {
            Toast.makeText(this, R.string.introduction, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.nfc_not_available, Toast.LENGTH_LONG).show();
        }
    }


    MainFrag f;

    @Override
    public void onItemClick(XmlResourceParser xmlItem) {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            f = (MainFrag) fm.findFragmentById(R.id.right_frag_container);
        } else {
            fm.popBackStack();
            fm.executePendingTransactions();
        }

        if (f != null && xmlItem != null)
            f.update(xmlItem);
    }


    @Override
    public void onButtonClick(BaseItem item) {

        if (item != null)
            myitemlist.add(item);


        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.right_frag_container, new DetailFrag(myitemlist));
        ft.addToBackStack(null);
        ft.commit();
    }


    @Override
    public void onDelClick(int i) {

    }

    @Override
    public void onNumChange(int i, int p) {

        BaseItem bit = myitemlist.get(p);
        bit.num = i;
        myitemlist.set(p, bit);
    }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    protected void onResume() {
        super.onResume();
        if (this.nfcAdapter != null) {
            this.nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, new String[][]{});
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.nfcAdapter != null) {
            this.nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.i(TAG, "Discovered tag with intent: " + intent);

        Parcelable[] parcelableArrayExtra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs;
        if (parcelableArrayExtra != null) {
            msgs = new NdefMessage[parcelableArrayExtra.length];

            for (int i = 0; i < parcelableArrayExtra.length; i++) {
                msgs[i] = (NdefMessage) parcelableArrayExtra[i];
            }

            for (NdefMessage message : msgs) {
                String text = this.extractText(message);
                if (text != null) {
                    Toast.makeText(this, "TAG detected: " + text, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(this, "This tag is not a Text Tag", Toast.LENGTH_LONG).show();

        }
    }

    private String extractText(NdefMessage ndefMessage) {
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord record : records) {
            if (NdefRecord.TNF_WELL_KNOWN == record.getTnf()) {
                TextRecord textRecord = new TextRecord.Builder(record).build();
                return textRecord.getText();
            }
        }
        return null;
    }
}