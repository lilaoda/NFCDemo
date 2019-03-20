package com.test.myapplication;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by Lilaoda on 2018/1/16.
 * Email:749948218@qq.com
 */

public class WriteNfcActivity extends NfcActivity {


    TextView textView;
    private String cardid = "0001";
    private String cardDept = "6666";
    private String mText = "";
    //941f8de2d61f67fc28d143c4755648eb
    private String mPackageName = "com.android.mms";//短信;
    private EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.edit_context);
        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mText = editText.getText().toString().trim();
                logPid();
            }


        });
//        findViewById(R.id.btn_reverse).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mText = cardid + "|" + cardDept + "|" + Md5Utils.getMD5(cardid + cardDept);
//                editText.setText(mText);
//            }
//        });
    }

    private void logPid() {
        for (int i = 0; i < 100; i++) {
            SystemClock.sleep(500);
          //  if (TextUtils.isEmpty(getPDAId())) {
                Log.e(TAG, "logPid: "+getPDAId()+"___"+i );
         //   }
        }
    }
    @Override
    public void onNewIntent(Intent intent) {
        if (mText == null)
            return;
        //获取Tag对象
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        writeNFCTag(detectedTag,"黎贺宇liheyu13922239152");
        mText = mText+"|"+Md5Utils.getMD5(bytesToHexString(detectedTag.getId())+mText);
        NdefMessage ndefMessage = new NdefMessage(
                new NdefRecord[]{createTextRecord(mText)});
        writeNFCTag(detectedTag, ndefMessage);
//        boolean result = writeTag(ndefMessage, detectedTag);
//        if (result) {
//            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
//        }
    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[3];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            buffer[2] = ':';
            stringBuilder.append(buffer);
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 创建NDEF文本数据
     *
     * @param text
     * @return
     */
    public NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }

    /**
     * 写数据
     *
     * @param ndefMessage 创建好的NDEF文本数据
     * @param tag         标签
     * @return
     */
    public static boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(ndefMessage);
            return true;
        } catch (Exception e) {
        }
        return false;
    }


    // 21版本之后可以，NdefRecord.createTextRecord最低要求21
    //  NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createTextRecord(null,text)});
    public void writeNFCTag(Tag tag, NdefMessage ndefMessage) {
        if (tag == null) {
            return;
        }
        //转换成字节获得大小
        int size = ndefMessage.toByteArray().length;
        try {
            //2.判断NFC标签的数据类型（通过Ndef.get方法）
            Ndef ndef = Ndef.get(tag);
            //判断是否为NDEF标签
            if (ndef != null) {
                ndef.connect();
                //判断是否支持可写
                if (!ndef.isWritable()) {
                    return;
                }
                //判断标签的容量是否够用
                if (ndef.getMaxSize() < size) {
                    return;
                }
                //3.写入数据
                ndef.writeNdefMessage(ndefMessage);
                Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                textView.setText(mText);
            } else { //当我们买回来的NFC标签是没有格式化的，或者没有分区的执行此步
                //Ndef格式类
                NdefFormatable format = NdefFormatable.get(tag);
                //判断是否获得了NdefFormatable对象，有一些标签是只读的或者不允许格式化的
                if (format != null) {
                    //连接
                    format.connect();
                    //格式化并将信息写入标签
                    format.format(ndefMessage);
                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                    textView.setText(mText);
                    Log.e(TAG, "writeNFCTag: 写入成功");
                } else {
                    Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "writeNFCTag: 写入失败");
                }
            }
        } catch (Exception e) {
        }
    }

    private String getPDAId() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }

}
