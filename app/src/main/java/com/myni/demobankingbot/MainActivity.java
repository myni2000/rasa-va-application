package com.myni.demobankingbot;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myni.Model.UserMessage;
import com.myni.Model.ask;
import com.myni.Network.ApiInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import darren.googlecloudtts.GoogleCloudTTS;
import darren.googlecloudtts.GoogleCloudTTSFactory;
import darren.googlecloudtts.model.VoicesList;
import darren.googlecloudtts.parameter.AudioConfig;
import darren.googlecloudtts.parameter.AudioEncoding;
import darren.googlecloudtts.parameter.VoiceSelectionParams;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    //private EditText editText;
    //private ImageView micButton;
    //todo: ArrayList holds data to be sent to the API
    String callquestion;
    ImageView imgAsk;
    TextView txtquestion, txtAnswer;
    ProgressDialog progressDialog;
    ImageView ic_hint;
    ApiInterface apiInterface;
    String question;
    ask ask;
    int n=0;
    private ApiInterface messageSender;
    GoogleCloudTTS googleCloudTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        findIds();
        addEvent();
    }

    private void addEvent() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"vi_VN");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"vi_VN");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                if (txtquestion.getVisibility() == View.INVISIBLE)
                    txtquestion.setVisibility(View.VISIBLE);
                txtquestion.setText("");
                txtquestion.setHint("Đang nghe...");
            }
            @Override
            public void onRmsChanged(float v) {

            }
            @Override
            public void onBufferReceived(byte[] bytes) {

            }
            @Override
            public void onEndOfSpeech() {

            }
            @Override
            public void onError(int i) {

            }
            @Override
            public void onResults(Bundle bundle) {
                imgAsk.setImageResource(R.drawable.mic1);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                txtquestion.setText(data.get(0));
                if (txtquestion.getText() != "") {
                    txtAnswer.setText("");
                    txtAnswer.setHint("FIBA đang trả lời...");
                    String msg = txtquestion.getText().toString();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://nckhmyni2000.herokuapp.com/webhooks/rest/")
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    if (msg.trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
                    } else {
                        //showTextView(msg, USER);
                        txtAnswer.setHint("FIBA đang trả lời...");
                        ask = new ask("User", msg.toLowerCase());
                    }
                    Toast.makeText(MainActivity.this, "" + ask.getMessage(), Toast.LENGTH_LONG).show();
                    messageSender = retrofit.create(ApiInterface.class);
                    Call<List<UserMessage>> response = messageSender.sendMessage(ask);
                    response.enqueue(new Callback<List<UserMessage>>() {
                        @Override
                        public void onResponse(Call<List<UserMessage>> call, Response<List<UserMessage>> response) {
                            if (response.body() == null || response.body().size() == 0) {
                                Toast.makeText(MainActivity.this, "Sorry I dint understand", Toast.LENGTH_LONG).show();
                            } else {
                                UserMessage botResponse = response.body().get(0);
                                txtAnswer.setText(botResponse.getText());
                                if(txtAnswer.getText()!="") {
                                    new TTS().execute();
                                    SaveDatabase();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<UserMessage>> call, Throwable t) {
                            txtAnswer.setText("Waiting for message");
                            Toast.makeText(MainActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                            if(txtAnswer.getText()!="") {
                                new TTS().execute();
                                SaveDatabase();
                            }
                        }

                    });

                }

            }
            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        progressDialog = new ProgressDialog(this);
        imgAsk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    String text= (String) txtAnswer.getText();
                    Log.i(text,text);
                    if(text.equals("Hãy hỏi mình thứ gì đó !")==false)
                    {
                        googleCloudTTS.stop();
                    }
                    imgAsk.setImageResource(R.drawable.mic2);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }

        });
        ic_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.infor_dialog);
                dialog.setCanceledOnTouchOutside(true);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }
        });
    }

    private void SaveDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Kết nối tới node có tên là contacts (node này do ta định nghĩa trong CSDL Firebase)
        DatabaseReference myRef = database.getReference("Userutterance");
        String uniqueID = UUID.randomUUID().toString();
        String UserutteranceID = "user-" + uniqueID;
        String question = txtquestion.getText().toString();
        String answer = txtAnswer.getText().toString();
        myRef.child(UserutteranceID).child("question").setValue(question);
        myRef.child(UserutteranceID).child("answer").setValue(answer);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        myRef.child(UserutteranceID).child("DateTime").setValue(dtf.format(now));
    }


    class TTS extends AsyncTask<String, Void, Void>
    {
        private Exception exception;
        @Override
        protected Void doInBackground(String... strings) {
                googleCloudTTS = GoogleCloudTTSFactory.create("AIzaSyC_XyFFv-2H9JTEuP6YFeuw4n3z0OxbFAo");
                // Load google cloud VoicesList and select the languageCode and voiceName with index (0 ~ N).
                VoicesList voicesList = googleCloudTTS.load();
               // String languageCode = voicesList.getLanguageCodes()[0];
                //String voiceName = voicesList.getVoiceNames(languageCode)[0];
                googleCloudTTS.setVoiceSelectionParams(new VoiceSelectionParams("vi-VN", "vi-VN-Standard-A"))
                        .setAudioConfig(new AudioConfig(AudioEncoding.MP3, 1, 0));
                // start speak
                googleCloudTTS.start((String) txtAnswer.getText());
            return null;
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    private void findIds() {
        txtquestion = findViewById(R.id.txtquestion);
        txtAnswer = findViewById(R.id.txtanswer);
        imgAsk = findViewById(R.id.imgAsk);
        ic_hint=findViewById(R.id.ic_hint);
    }
}





