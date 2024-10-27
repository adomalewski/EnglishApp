package com.adsolutions.englishapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonCheck;
    Button buttonNext;
    Button buttonAnswer;
    TextView textViewResult;
    TextView textViewQuestion;
    EditText editTextAnswer;
    Spinner dropdownQuestionLang;

    Boolean answerResult = null;

    AppStorageManager appStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCheck = findViewById(R.id.buttonCheck);
        buttonNext = findViewById(R.id.buttonNext);
        buttonAnswer = findViewById(R.id.buttonAnswer);
        textViewResult = findViewById(R.id.textViewResult);
        textViewQuestion = findViewById(R.id.textViewQuestion);
        editTextAnswer = findViewById(R.id.editTextAnswer);
        dropdownQuestionLang = findViewById(R.id.dropdownQuestionLang);

        init();
        eventsHandlers();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            if (!isElementClicked(ev, editTextAnswer)) {
                hideKeyboard();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        AlarmReceiver.cancelAlarm();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        AlarmReceiver.cancelAlarm();
        super.onRestart();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onPause() {
        if (!this.isFinishing()) {
            AlarmReceiver.setAlarm(this);
            QuestionManager.storeQuestionsData();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        AlarmReceiver.setAlarm(this);
        QuestionManager.storeQuestionsData();
        super.onDestroy();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
        clearPlainUI();
        QuestionManager.retrieveNextQuestion();
        textViewQuestion.setText(QuestionManager.getQuestionText());
    }

    private void clearPlainUI() {
        answerResult = null;
        editTextAnswer.setText("");
        textViewResult.setText("");
        buttonCheck.setEnabled(false);
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

    private void init() {
        AlarmReceiver.cancelAlarm();
        appStorageManager = AppStorageManager.getInstance();
        appStorageManager.initializeStorage(this);
        if (!QuestionManager.isDictionaryFilled()) {
            QuestionManager.storeSettingsIfUnexist();
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
        setQuestionLanguageDropdown(QuestionManager.getQuestionLanguage());
    }

    private void eventsHandlers() {
        buttonCheck.setOnClickListener(view -> {
            answerResult = QuestionManager.checkResult(editTextAnswer.getText().toString());

            if (answerResult) {
                textViewResult.setText("Correct!");
                textViewResult.setTextColor(Color.GREEN);

            } else {
                textViewResult.setText("Wrong!");
                textViewResult.setTextColor(Color.RED);
            }

            hideKeyboard();
        });

        buttonNext.setOnClickListener(view -> {
            setNewQuestion();
        });

        buttonAnswer.setOnClickListener(view -> {
            textViewResult.setText("");
            textViewResult.setText(QuestionManager.getFormattedAnswer());
            textViewResult.setTextColor(Color.BLUE);
        });

        editTextAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    buttonCheck.setEnabled(true);
                } else {
                    buttonCheck.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dropdownQuestionLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                boolean realChange = !Objects.equals(QuestionManager.getQuestionLanguage(), dropdownQuestionLang.getItemAtPosition(position).toString());
                if (realChange) {
                    QuestionManager.setQuestionLanguage(dropdownQuestionLang.getItemAtPosition(position).toString());
                    setNewQuestion();
                    AppStorageManager.getInstance().storeQuestionLanguage(QuestionManager.getQuestionLanguage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void getDataFromAPIandSetQuestionFromStorage() {
        textViewQuestion.setText(QuestionManager.getQuestionText());
        ExternalDataManager.getDataFromAPI(this, () -> null);
    }

    private void getDataFromAPIandSetNewQuestion() {
        ExternalDataManager.getDataFromAPI(this, () -> {
            setNewQuestion();
            return null;
        });
    }
}