package com.test.myapplication;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import static android.nfc.tech.MifareClassic.TYPE_CLASSIC;

/**
 * Created by Lilaoda on 2018/1/16.
 * Email:749948218@qq.com
 */

public class ReadNfcActivity extends NfcActivity {


    TextView textView;
    byte a[] = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private String mPackageName = "com.android.mms";//短信;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        textView = (TextView) findViewById(R.id.textView);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ReadNfcActivity.this, WriteNfcActivity.class));
            }
        });
        Log.e(TAG, "mPendingIntent: " + mNfcAdapter);
        Log.e(TAG, "mPendingIntent: " + mPendingIntent);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.e(TAG, "onNewIntent: " + intent.getAction() + "___" + intent.getDataString());
//        processIntent(intent);
        readNfcTag(intent);

        //1.获取Tag对象
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        //2.获取Ndef的实例
        //   Ndef ndef = Ndef.get(detectedTag);
        // mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
        Log.e(TAG, "Tag:getId " + bytesToHexString(tag.getId()));
        Log.e(TAG, "Tag:getId " + tag.getId());
        Log.e(TAG, "Tag: " + getString(tag.getTechList()));
        Log.e(TAG, "Tag: " + tag.toString());
        //   readNfcTag(intent);
//        writeNFCTag(tag,"liheyu13922239152");
//        卡片信息: 卡片类型：TYPE_CLASSIC
//        共16个扇区
//                共64个块
//        存储空间: 1024B
//        Id标签是:4F66362A
    }

    private String getString(String[] techList) {
        String s = "";
        if (techList != null) {
            for (String s1 : techList) {
                s += s1 + ",";
            }
        }
        return s;
    }


    /**
     * 读取NFC标签文本数据
     */
    private void readNfcTag(Intent intent) {
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msgs[] = null;
        int contentSize = 0;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                contentSize += msgs[i].toByteArray().length;
            }
        } else {
            Log.e(TAG, "readNfcTag: rawMsgs" + "null");
        }
        try {
            if (msgs != null) {
                String mTagText = "";
                for (int i = 0; i < msgs.length; i++) {
                    NdefRecord record = msgs[i].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagText += textRecord + "\n";
                }
//                    NdefRecord record = msgs[0].getRecords()[0];
//                    String textRecord = parseTextRecord(record);
//                    mTagText += textRecord + "\n\ntext\n" + contentSize + " bytes";
                textView.setText(mTagText);
                Log.e(TAG, "readNfcTag: " + mTagText);
            }
        } catch (Exception e) {
        }
//        }
    }

    public static String ByteArrayToHexString(byte[] bytesId) {   //Byte数组转换为16进制字符串
        // TODO 自动生成的方法存根
        int i, j, in;
        String[] hex = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String output = "";
        for (j = 0; j < bytesId.length; ++j) {
            in = bytesId[j] & 0xff;
            i = (in >> 4) & 0x0f;
            output += hex[i];
            i = in & 0x0f;
            output += hex[i];
        }
        return output;
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

    //读写MifareClassic协议的NFC卡，如公交卡等
    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] bytesId = tag.getId();// 获取id数组
        String info = ByteArrayToHexString(bytesId);
        //读取TAG
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            String metaInfo = "";
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n" + "Id标签是:" + info;
            for (int j = 0; j < sectorCount; j++) {
                boolean auth = mfc.authenticateSectorWithKeyA(j, a);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            textView.setText(metaInfo);
            Log.e(TAG, "卡片信息: " + metaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     *
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public void writeNFCTag(Tag tag, String text) {
        if (tag == null) {
            return;
        }
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord
                .createApplicationRecord(text)});
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
                } else {
                    Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        }
    }
}
