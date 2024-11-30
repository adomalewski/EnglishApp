package com.adsolutions.englishapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.adsolutions.englishapp.enums.Language;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Intent settingsActivityIntent;

    Button buttonNext;
    Button buttonAnswer;
    Button settingsButton;
    TextView textViewResult;
    TextView textViewQuestion;
    Spinner dropdownQuestionLang;

    Boolean answerResult = null;

    AppStorageManager appStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateElements();
        init();
        eventsHandlers();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        AlarmReceiver.cancelAlarm();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        AlarmReceiver.cancelAlarm();

        if(AppStateManager.isAfterResetStorage()) {
            clearPlainUI();
            getDataFromAPIandSetNewQuestion();
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this, R.array.questionLanguages, android.R.layout.simple_spinner_dropdown_item);
            dropdownQuestionLang.setAdapter(adapter);
            setQuestionLanguageDropdown(QuestionManager.getQuestionLanguage().name());

            AppStateManager.unsetAfterResetStorage();
        }

        if(AppStateManager.isAfterChangedDataSourceIds()) {
            clearPlainUI();
            getDataFromAPIandSetNewQuestion();

            AppStateManager.unsetAfterChangedDataSourceIds();
        }

        super.onRestart();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onPause() {
        if (!this.isFinishing()) {
            QuestionManager.storeQuestionsData();

            if(AppStateManager.isNotPreOpenAnotherActivity()) {
                AlarmReceiver.setAlarm(this);
                AppStateManager.unsetPreOpenAnotherActivity();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        AlarmReceiver.setAlarm(this);
        QuestionManager.storeQuestionsData();
        super.onDestroy();
    }

    private void initiateElements() {
        buttonNext = findViewById(R.id.buttonNext);
        buttonAnswer = findViewById(R.id.buttonAnswer);
        settingsButton = findViewById(R.id.settingsButton);
        textViewResult = findViewById(R.id.textViewResult);
        textViewQuestion = findViewById(R.id.textViewQuestion);
        dropdownQuestionLang = findViewById(R.id.dropdownQuestionLang);

        settingsActivityIntent = new Intent(this, SettingsPageActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    }

    private void init() {
        AlarmReceiver.cancelAlarm();
        appStorageManager = AppStorageManager.getInstance();
        appStorageManager.initializeStorage(this);
        ExternalDataManager.readAllDataFromStorage();
        if (!QuestionManager.areDictionariesFilled()) {
            QuestionManager.storeSettingsIfMissing();
            clearPlainUI();
            if (!QuestionManager.isWordInStorage()) {
                getDataFromAPIandSetNewQuestion();
            } else {
                getDataFromAPIandSetQuestionFromStorage();
            }
        } else {
            QuestionManager.readQuestionsDataFromStorage();
            textViewQuestion.setText(QuestionManager.getQuestionText());
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.questionLanguages, android.R.layout.simple_spinner_dropdown_item);
        dropdownQuestionLang.setAdapter(adapter);
        setQuestionLanguageDropdown(QuestionManager.getQuestionLanguage().name());
    }

    private void eventsHandlers() {
        buttonNext.setOnClickListener(view -> {
            clearPlainUI();
            setNewQuestion();
        });

        buttonAnswer.setOnClickListener(view -> {
            textViewResult.setText("");
            textViewResult.setText(QuestionManager.getFormattedAnswer());
            textViewResult.setTextColor(Color.BLUE);
        });

        settingsButton.setOnClickListener(view -> {
            AppStateManager.setPreOpenAnotherActivity();
            startActivity(settingsActivityIntent);
        });

        dropdownQuestionLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                boolean realChange = !Objects.equals(QuestionManager.getQuestionLanguage().name(), dropdownQuestionLang.getItemAtPosition(position).toString());
                if (realChange) {
                    QuestionManager.setQuestionLanguage(Language.valueOf(dropdownQuestionLang.getItemAtPosition(position).toString()));
                    QuestionManager.clearHistory();
                    clearPlainUI();
                    setNewQuestion();
                    AppStorageManager.getInstance().storeQuestionLanguage(QuestionManager.getQuestionLanguage().name());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    private boolean isElementClicked(MotionEvent me, View viewEl) {
        int[] l = new int[2];
        viewEl.getLocationOnScreen(l);
        Rect rect = new Rect(l[0], l[1], l[0] + viewEl.getWidth(), l[1] + viewEl.getHeight());
        return rect.contains((int)me.getX(), (int)me.getY());
    }

    private void setNewQuestion() {
        QuestionManager.retrieveNextQuestion();
        textViewQuestion.setText(QuestionManager.getQuestionText());
        QuestionMonitoring.addCurrentTextToAllPreviousQuestionsList();
    }

    private void clearPlainUI() {
        answerResult = null;
        textViewResult.setText("");
        textViewQuestion.setText("");
    }

    private void setQuestionLanguageDropdown(String value) {
        int position = getSpinnerValueIndex(dropdownQuestionLang, value);
        dropdownQuestionLang.setSelection(position);
    }

    private int getSpinnerValueIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }

        return 0;
    }

    private void getDataFromAPIandSetQuestionFromStorage() {
        textViewQuestion.setText(QuestionManager.getQuestionText());
        ExternalDataManager.getDataFromAPI(this, () -> {
            AnswerToQuestionTransformer.buildDictionary(Language.EN, Language.PL);
            return null;
        });
    }

    private void getDataFromAPIandSetNewQuestion() {
        ExternalDataManager.getDataFromAPI(this, () -> {
            setNewQuestion();
            AnswerToQuestionTransformer.buildDictionary(Language.EN, Language.PL);
            return null;
        });
    }
}