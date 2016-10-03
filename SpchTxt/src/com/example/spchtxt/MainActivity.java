package com.example.spchtxt;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.content.pm.ResolveInfo;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements OnInitListener {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private static final int REQ_TTS_STATUS_CHECK = 0;  
//    private static final String STR_SENTENCE_STOP = "It is stopped!";  //Keyword: stop
//    private static final String STR_SENTENCE_FOLLOW_TOO_CLOOSE = "Please do not follow too close!";  //Keyword: follow too close
//    private static final String STR_SENTENCE_CHANGE_LANE_LEFT = "The car on the right lane is change lane to your lane!"; //Keyword: Change left
//    private static final String STR_SENTENCE_QUESTION = "The car on your right is asking: ";  //Keyword: Question
//    private static final String STR_SENTENCE_ANSWER = "Someone answers: "; //keyword:  ANSWER

    private static final String[] mstrArrKeyword={
    	/*  0  */	  "stop",
    	/*  1  */     "question",
    	/*  2  */     "reply",
    	/*  3  */     "follow",
    	/*  4  */     "close",
    	/*  5  */     "change",
    	/*  6  */     "left",
    	/*  7  */     "right"
    };
    
    private static final String[] mstrSentence = {
    	/*  0  */	  "It is stopped!",
    	/*  1  */     "Someone is asking: ",
    	/*  2  */     "Someone answers: ",
    	/*  3  */     "Please do not follow too close!",
    	/*  4  */     "The car on the right lane is changing lane to your lane!",
    	/*  5  */     "The car on the left lane is changing lane to your lane!"
    };
    
    
    private TextToSpeech mTts;  
	
	private Button m_btnTextToSpeech;
	private TextView m_tvShowInfoST;
	private TextView m_tvShowTimeST;
	private EditText m_etTxtContent;
	
	private Button m_btnSpeechToText;
	private TextView m_tvShowTimeTS;
	private MainActivity m_actHome = this;
	private long lInputBeginTS = 0;
	private long lInputEndTS = 0;
	private long lInputBeginST = 0;
	private long lInputEndST = 0;
	private String m_sSTFullPathFile = "";
	private FileWriter m_fwST = null;
	private String m_sTSFullPathFile = "";
	private FileWriter m_fwTS = null;
	private String mTS_Content = "";
	
	private SpeechRecognizer sr;  

	class splistener implements RecognitionListener             
    {  
             public void onReadyForSpeech(Bundle params)  
             {  
                       
             }  
             
             public void onBeginningOfSpeech()  
             {  
            	m_tvShowTimeST.setText("Speech Begin"); 
             	

             }  
             
             public void onRmsChanged(float rmsdB)  
             {  
                       
             }  
             
             public void onBufferReceived(byte[] buffer)  
             {  
                      
             }  
             
             public void onEndOfSpeech()  
             {  
            	 m_tvShowTimeST.setText("Speech End");
            	 lInputBeginST = System.currentTimeMillis();
             }  
             
             public void onError(int error)  
             {  
                       
            	 m_tvShowTimeST.setText("error " + error);  
             } 
             
             public void onResults(Bundle results)    
             { 
             	int nSentenceIdx = -1;
            	String strShow = "";
            	long lTimeElapsed;
            	
            	lInputEndST = System.currentTimeMillis();
            	lTimeElapsed = lInputEndST-lInputBeginST;
            	m_tvShowTimeST.setText("Text->Speech Time: " + Long.toString(lTimeElapsed) + " ms");  

            	
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);  
                String listString = data.get(0);

                nSentenceIdx = searchKeyword(listString);

                saveST_Time(lTimeElapsed, listString);
                  
                if (nSentenceIdx == -1) {
                  m_etTxtContent.setText(listString);
                } else if (nSentenceIdx == 0) {  //Stop
                 	 //m_etTxtContent.setText(mstrSentence[nSentenceIdx]);            	
                } else {           	
                	if (nSentenceIdx == 1) {  //Question
                  		strShow = mstrSentence[nSentenceIdx] + listString.substring(mstrArrKeyword[1].length());
                  	} else if (nSentenceIdx == 2) {  //Answer
                  		strShow = mstrSentence[nSentenceIdx] + listString.substring(mstrArrKeyword[2].length());            		
                  	} else {
                  		strShow = mstrSentence[nSentenceIdx];
                  	}
                  	
                  	m_etTxtContent.setText(strShow);

                }
             }  

             public void onPartialResults(Bundle partialResults)  
             {  
             }  

             public void onEvent(int eventType, Bundle params)  
             {  

             }  
    }  

	void saveST_Time(long lElapsedTime, String strContent)
	{
    	String sRecordLine = "";
		
		try {
			m_fwST = new FileWriter(m_sSTFullPathFile, true);  //append
			sRecordLine = Long.toString(lElapsedTime)  + "," + strContent + System.getProperty("line.separator");
			m_fwST.write(sRecordLine);
			m_fwST.close();
			m_fwST = null;
		} catch (IOException e) {
			m_tvShowInfoST.setText("Failed to record data on SD Card!");
			m_fwST = null;
			return;
		}
		
	}
	
	void saveTS_Time(long lElapsedTime, String strContent)
	{
    	String sRecordLine = "";
		
   		try {
			m_fwTS = new FileWriter(m_sTSFullPathFile, true);  //append
			sRecordLine = Long.toString(lElapsedTime)  + "," +  strContent + System.getProperty("line.separator");
			m_fwTS.write(sRecordLine);
			m_fwTS.close();
			m_fwTS = null;
		} catch (IOException e) {
			m_tvShowInfoST.setText("Failed to record data on SD Card!");
			m_fwTS = null;
			return;
		}
		
	}
	

	UtteranceProgressListener uttListener = new UtteranceProgressListener() {

	    @Override
	    public void onStart(String utteranceId) {
	        // TODO Auto-generated method stub	    	
	    	runOnUiThread(new Runnable() {

                @Override
                public void run() {
                //UI changes
                	long lTimeElapsed;
                	
                	lInputEndTS = System.currentTimeMillis();
                	lTimeElapsed = lInputEndTS-lInputBeginTS;
                	m_tvShowTimeTS.setText("Text->Speech Time: " + Long.toString(lTimeElapsed) + " ms");
                	
                	saveTS_Time(lTimeElapsed, mTS_Content);
                }
            });	    	
	    	
	    }

	    @Override
	    public void onError(String utteranceId) {
	        // TODO Auto-generated method stub

	    }

	    @Override
	    public void onDone(String utteranceId) {
	        // TODO Auto-generated method stub
	        //start MediaPlayer
	    	//Toast.makeText(m_actHome, "Finished Speak", Toast.LENGTH_LONG).show(); 
	    	//m_tvShowTimeTS.setText("Finished speak!");

	    }
	};	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
 		String sDataDir;
 		File flDataFolder;
    	    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        m_tvShowInfoST = (TextView)findViewById(R.id.ShowInfoST);  //Show Message to user
        m_tvShowInfoST.setText("Speech<-->Text Conversion");
        m_btnSpeechToText = (Button)findViewById(R.id.SpeechToText);
        m_btnSpeechToText.setText("SpeechToText");
        m_tvShowTimeST = (TextView)findViewById(R.id.ShowTimeST);  //Show Message to user
        m_tvShowTimeST.setText("Speech->Text Time:");
//        m_lvShowResult = (ListView) findViewById(R.id.ShowResult); 
//        m_lvShowResult = (TextView) findViewById(R.id.ShowResult); 
 
        m_etTxtContent = (EditText) findViewById(R.id.TxtContent); 
        
        m_btnTextToSpeech = (Button)findViewById(R.id.TextToSpeech);
        m_btnTextToSpeech.setText("TextToSpeech");
        m_tvShowTimeTS = (TextView)findViewById(R.id.ShowTimeTS);  //Show Message to user
        m_tvShowTimeTS.setText("Text->Speech Time:");
        
        sr = SpeechRecognizer.createSpeechRecognizer(this);        //Initialize the speech recognizer
        sr.setRecognitionListener(new splistener()); 
        
      	m_btnSpeechToText.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	//startVoiceRecognitionActivity(); 
            	sr.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));  
            }
        });  
       
        
     // Check to see if a recognition activity is present
   /*   	
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
        	m_btnSpeechToText.setOnClickListener(new View.OnClickListener() {  
                @Override  
                public void onClick(View v) {  
                	startVoiceRecognitionActivity(); 
                }
            });  
        } else {
        	m_btnSpeechToText.setEnabled(false);
        	m_btnSpeechToText.setText("Recognizer not present");
        }  
  */
        
        Intent checkIntent = new Intent();  
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);  
        m_btnTextToSpeech.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	//mTts.setSpeechRate(0.2f);
            	mTts.setOnUtteranceProgressListener(uttListener);
            	HashMap<String, String> myParams = new HashMap<String, String>();
            	myParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");
            	lInputBeginTS = System.currentTimeMillis();
                mTS_Content = m_etTxtContent.getText().toString();
                if (mTS_Content.length() > 0) {
                	mTts.speak(mTS_Content, TextToSpeech.QUEUE_ADD, myParams); 
                }

            }  
        });  
        
 
 
		/* Create /sdcard/ActivityData/ folder */
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			sDataDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.activitydata_folder);
			flDataFolder = new File(sDataDir);
			//Check whether /mnt/sdcard/ActivityData/ exists
			if (!flDataFolder.exists()) {
				//Does not exist, create it
				if (flDataFolder.mkdir()) {
					     						
				} else {
					//Failed to create
					m_tvShowInfoST.setText("Failed to record data on SD Card!");
					return;
				}
				
			} else {
			} 
		} else {        				
			//NO SD Card
			m_tvShowInfoST.setText("No SD Card!");
			return;
		}
       
        
		m_sSTFullPathFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.activitydata_folder) + File.separator + "ST_Time.csv";
		m_sTSFullPathFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.activitydata_folder) + File.separator + "TS_Time.csv";
		
    }
    
    

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "'change left/right', 'follow close', 'question/reply', 'stop'");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

 
    
    
    private boolean searchWord(String orgStr, String myword) 
    {
    	boolean blnExist = false;
    	int nIdx,nStart, nEnd, nLen;
        char chStart = ' ';
        char chEnd = ' ';

        nIdx = orgStr.indexOf(myword);
        nStart = 0;
        nEnd = 0;
        nLen = orgStr.length();
        
        if (nIdx != -1) {
        	if (nIdx == 0) {
        		nStart = 0;
        	} else {
        		nStart = nIdx - 1;
        		chStart = orgStr.charAt(nStart);
        	}
        	
        	if (nIdx + myword.length() == nLen) {
        		nEnd = nIdx + myword.length();
        	} else {
        		nEnd = nIdx + myword.length();
        		chEnd = orgStr.charAt(nEnd);
        	}
        	
        	if (chStart == ' ' && chEnd == ' ') {
        		blnExist = true;
        	}
        }
        
        return blnExist;
        
    }
    
    
    private int searchKeyword(String orgStr)
    {
    	int nSentenceIdx = -1;

    	if (searchWord(orgStr, mstrArrKeyword[0]) == true) { //Stop
    		nSentenceIdx = 0;
    	} else if (searchWord(orgStr, mstrArrKeyword[3]) == true && searchWord(orgStr, mstrArrKeyword[4]) == true) {  //Follow too close
    		nSentenceIdx = 3;
    	} else if (searchWord(orgStr, mstrArrKeyword[5]) == true && searchWord(orgStr, mstrArrKeyword[6]) == true) {  //Change lane left
    		nSentenceIdx = 4;
    	} else if (searchWord(orgStr, mstrArrKeyword[5]) == true && searchWord(orgStr, mstrArrKeyword[7]) == true) {  //Change lane right
    		nSentenceIdx = 5;
    	} else if (searchWord(orgStr, mstrArrKeyword[1]) == true) {   //Question
    		if (orgStr.indexOf(mstrArrKeyword[1]) == 0) {
    			nSentenceIdx = 1;   			
    		}
    	} else if (searchWord(orgStr, mstrArrKeyword[2]) == true) {   //Answer
    		if (orgStr.indexOf(mstrArrKeyword[2]) == 0) {
    			nSentenceIdx = 2;   			
    		}
    	}
    	
        return nSentenceIdx;
    }
    
    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	int nSentenceIdx = -1;
    	String strShow = "";

    	
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
            String listString = matches.get(0);

            nSentenceIdx = searchKeyword(listString);
            
            if (nSentenceIdx == -1) {
            	m_etTxtContent.setText(listString);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

            	startVoiceRecognitionActivity();
            } else if (nSentenceIdx == 0) {  //Stop
           	 	//m_etTxtContent.setText(mstrSentence[nSentenceIdx]);            	
            } else {           	
            	if (nSentenceIdx == 1) {  //Question
            		strShow = mstrSentence[nSentenceIdx] + listString.substring(mstrArrKeyword[1].length());
            	} else if (nSentenceIdx == 2) {  //Answer
            		strShow = mstrSentence[nSentenceIdx] + listString.substring(mstrArrKeyword[2].length());            		
            	} else {
            		strShow = mstrSentence[nSentenceIdx];
            	}
            	
            	m_etTxtContent.setText(strShow);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

            	startVoiceRecognitionActivity();
            }
            
        } else if (requestCode == REQ_TTS_STATUS_CHECK){  
            switch(resultCode){  
	            case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:  
	                mTts = new TextToSpeech(this, this);  
	                break;  
	            case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
	                Intent dataIntent = new Intent();  
	                dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);  
	                startActivity(dataIntent);  
	                break; 
	            default:break;  
            }
        }  
        
        super.onActivityResult(requestCode, resultCode, data);
        
    }
    
 
    
    @Override  
    public void onInit(int status) {  
        if(status == TextToSpeech.SUCCESS){  
            int result = mTts.setLanguage(Locale.US);  
            mTts.setOnUtteranceProgressListener(uttListener);

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){   
                Toast.makeText(this, "Lang not supported", Toast.LENGTH_LONG).show();  
                m_btnTextToSpeech.setEnabled(false);  
            }else{  
                //mTts.speak(m_etTxtContent.getText().toString(), TextToSpeech.QUEUE_ADD, null);  
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
        if(mTts != null) mTts.stop();  
    }  
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
