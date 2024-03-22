package com.example.translator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.translation.Translator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translateIV;

    String[] fromlanguage = {"From", "English", "Africans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Welsh", "Hindi", "Urdu"};
    String[] tolanguage = {"To", "English", "Africans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Welsh", "Hindi", "Urdu"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode, toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idTospinner);
        sourceText = findViewById(R.id.EditSource);
        micIV = findViewById(R.id.iIVdMic);
        translateBtn = findViewById(R.id.idBtnTranslation);
        translateIV = findViewById(R.id.idTranslatedTV);


        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromlanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromlanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(tolanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, tolanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateIV.setVisibility(View.VISIBLE);
                translateIV.setText("");
                if (sourceText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter any text.",Toast.LENGTH_LONG);
                }
                else if (fromLanguageCode == 0){
                    Toast.makeText(MainActivity.this,"Please select source language.",Toast.LENGTH_LONG);
                }
                else if (toLanguageCode == 0){
                    Toast.makeText(MainActivity.this,"Please select translation language.",Toast.LENGTH_LONG);
                }
                else {
                   translateText(fromLanguageCode,toLanguageCode,sourceText.getText().toString());
                }

            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something to translate");

                try{
                    startActivityForResult(intent,REQUEST_PERMISSION_CODE);
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source){
        translateIV.setText("Downloading model, please wait....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translateIV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translateIV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Failed to translate!!Try again.",Toast.LENGTH_SHORT);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to download model. Check your internet connection.",Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_PERMISSION_CODE){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceText.setText(result.get(0));
        }
    }


    private int getLanguageCode(String language){

            int languageCode = 0;
            switch (language){
                case "English":
                    languageCode = FirebaseTranslateLanguage.EN;
                    break;
                case "Africans":
                    languageCode = FirebaseTranslateLanguage.AF;
                    break;
                case "Arabic":
                    languageCode = FirebaseTranslateLanguage.AR;
                    break;
                case "Belarusian":
                    languageCode = FirebaseTranslateLanguage.BE;
                    break;
                case "Bulgarian":
                    languageCode = FirebaseTranslateLanguage.BG;
                    break;
                case "Bengali":
                    languageCode = FirebaseTranslateLanguage.BN;
                    break;
                case "Welsh":
                    languageCode = FirebaseTranslateLanguage.CY;
                    break;
                case "Hindi":
                    languageCode = FirebaseTranslateLanguage.HI;
                    break;
                case "Urdu":
                    languageCode = FirebaseTranslateLanguage.UR;
                    break;

                default: languageCode = 0;

            }
            return languageCode;

        }

}