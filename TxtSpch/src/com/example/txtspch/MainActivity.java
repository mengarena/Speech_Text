package com.example.txtspch;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements OnInitListener {
//	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final int REQ_TTS_STATUS_CHECK = 0;  
    private TextToSpeech mTts;  
	
	private Button m_btnTextToSpeech;
	private TextView m_tvShowInfo;
	private TextView m_tvShowTime;
	private EditText m_etInputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_tvShowInfo = (TextView)findViewById(R.id.ShowInfo);  //Show Message to user
        m_tvShowInfo.setText("Text To Speech Conversion");
        m_btnTextToSpeech = (Button)findViewById(R.id.TextToSpeech);
        m_btnTextToSpeech.setText("TextToSpeech");
        m_tvShowTime = (TextView)findViewById(R.id.ShowTime);  //Show Message to user
        m_tvShowTime.setText("");
        m_etInputText = (EditText) findViewById(R.id.InputText); 
        
        
        Intent checkIntent = new Intent();  
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);  
        m_btnTextToSpeech.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                mTts.speak(m_etInputText.getText().toString(), TextToSpeech.QUEUE_ADD, null);  
            }  
        });  
        
    }


    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        if(requestCode == REQ_TTS_STATUS_CHECK){  
            switch(resultCode){  
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:  
                    mTts = new TextToSpeech(this, this);  
                    break;  
                case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA://需要的语音数据已损坏  
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA://缺少需要语言的语音数据  
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME://缺少需要语言的发音数据  
                    Intent dataIntent = new Intent();  
                    dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);  
                    startActivity(dataIntent);  
                    break;  
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL://检查失败   
                default:break;  
            }  
        }   
    }  
  
  
    @Override  
    public void onInit(int status) {  
        if(status == TextToSpeech.SUCCESS){  
            int result = mTts.setLanguage(Locale.US);  
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){  
              //判断语言是否可用  
                Toast.makeText(this, "Lang not supported", Toast.LENGTH_LONG).show();  
                m_btnTextToSpeech.setEnabled(false);  
            }else{  
                mTts.speak(m_etInputText.getText().toString(), TextToSpeech.QUEUE_ADD, null);  
                m_btnTextToSpeech.setEnabled(true);  
            }  
        }   
    }  
  
  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        mTts.shutdown();  
    }  
  
  
    @Override  
    protected void onPause() {  
        super.onPause();  
        if(mTts != null)mTts.stop();  
    }  
}
