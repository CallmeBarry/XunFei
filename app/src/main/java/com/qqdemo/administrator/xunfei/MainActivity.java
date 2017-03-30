package com.qqdemo.administrator.xunfei;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AAAAAAAAAAAAAAAAAAAAAAAAAA";
    @InjectView(R.id.edtxt)
    EditText mEdtxt;
    @InjectView(R.id.btn1)
    Button mBtn1;
    @InjectView(R.id.btn2)
    Button mBtn2;
    @InjectView(R.id.txt)
    TextView mTxt;
    @InjectView(R.id.progressBar2)
    ProgressBar mProgressBar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58dcba5b");

        ButterKnife.inject(this);
    }

    @OnClick({R.id.btn1, R.id.btn2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                action1(this);
                break;
            case R.id.btn2:
                action2();
                break;
        }
    }

    private void action1(final Context context) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(context, null);
//2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
//保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
//如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
//3.开始合成
        mTts.startSpeaking(mEdtxt.getText().toString(), new SynthesizerListener() {
            //播放进度回调
            //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
            @Override
            public void onSpeakBegin() {
                Toast.makeText(context, "开始阅读", Toast.LENGTH_SHORT).show();
            }

            //缓冲进度回调
            //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {
                Log.i(TAG, "onBufferProgress: AAAAAA" + i);
            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSpeakProgress(final int i, int i1, int i2) {
                Log.i(TAG, "onSpeakProgress:AAA i:" + i + "  2:" + i1 + " i2:" + i2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar2.setProgress(i);
                    }
                });

            }

            //会话结束回调接口，没有错误时，error为null
            @Override
            public void onCompleted(SpeechError speechError) {
                Toast.makeText(context, "阅读结束", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    private void action2() {
        initSpeech(this);
    }


    /**
     * 初始化语音识别
     */
    public void initSpeech(final Context context) {
        //1.创建RecognizerDialog对象

//第二个参数是个listener
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        //2.设置accent、language等参数
//设置语音识别为中文
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

//方言为普通话
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //3.设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {

//islast做了判断去掉了最后的句号
                if (!isLast) {
                    //解析语音
                    final String result = parseVoice(recognizerResult.getResultString());
                    mTxt.setText(result);
                    //  Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 解析语音json
     */
    public String parseVoice(String resultString) {
        Gson gson = new Gson();
        Voice voiceBean = gson.fromJson(resultString, Voice.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<Voice.WSBean> ws = voiceBean.ws;
        for (Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    /**
     * 语音对象封装
     */
    public class Voice {

        public ArrayList<WSBean> ws;

        public class WSBean {
            public ArrayList<CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }
}
