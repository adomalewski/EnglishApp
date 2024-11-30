package com.adsolutions.englishapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adsolutions.englishapp.enums.SettingsTab;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SettingsPageActivity extends AppCompatActivity {

    Button buttonSave;
    Button buttonCancel;
    Button buttonResetStorage;
    LinearLayout topicTab;
    LinearLayout storageTab;
    LinearLayout dataSourceTab;
    LinearLayout monitoringTab;
    LinearLayout tabContentLayout;
    TabLayout tabSettings;
    TextView textViewSpreadsheetId;
    TextView textViewApiKey;
    TextView textViewQuestionPosition;
    TextView textViewCurrentRoundWordsList;
    TextView textViewPreviousRoundWordsList;
    TableLayout settingsQuestionNumberTable;
    CustomAutoCompleteTextView topicDropdown;
    CustomAutoCompleteTextView lastQuestionsDropdown;

    LayoutInflater inflator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page_activity);

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
        super.onRestart();
    }

    @Override
    public void onPause() {
        if (!this.isFinishing()) {
            QuestionManager.storeQuestionsData();

            if (AppStateManager.isNotPreOpenAnotherActivity()) {
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
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        tabContentLayout = findViewById(R.id.tabContent);
        topicTab = (LinearLayout) View.inflate(this, R.layout.view_topic_tab, null);
        dataSourceTab = (LinearLayout) View.inflate(this, R.layout.view_data_source_tab, null);
        storageTab = (LinearLayout) View.inflate(this, R.layout.view_storage_tab, null);
        monitoringTab = (LinearLayout) View.inflate(this, R.layout.view_monitoring_tab, null);
        tabSettings = findViewById(R.id.tabSettings);
        topicDropdown = topicTab.findViewById(R.id.topicDropdown);
        textViewSpreadsheetId = dataSourceTab.findViewById(R.id.editSpreadsheetId);
        textViewApiKey = dataSourceTab.findViewById(R.id.editApiKey);
        lastQuestionsDropdown = monitoringTab.findViewById(R.id.lastQuestionsDropdown);
        textViewQuestionPosition = monitoringTab.findViewById(R.id.textViewQuestionPosition);
        textViewCurrentRoundWordsList = monitoringTab.findViewById(R.id.textViewCurrentRoundWordsList);
        textViewPreviousRoundWordsList = monitoringTab.findViewById(R.id.textViewPreviousRoundWordsList);
        settingsQuestionNumberTable = monitoringTab.findViewById(R.id.settingsQuestionNumberTable);
        buttonResetStorage = storageTab.findViewById(R.id.buttonResetStorage);

        inflator = SettingsPageActivity.this.getLayoutInflater();
    }

    private static final String[] COUNTRIES = new String[]{
            "Botswana", "France", "Italy", "Germany", "Spain", "Belarus", "Brazil", "Poland", "Germany", "USA", "UK", "Greece", "Slovakia"
    };

    private void init() {
        tabContentLayout.addView(topicTab);
        SimpleContainsAutocompleteAdapter<String> adapter = new SimpleContainsAutocompleteAdapter<>(
                this, new ArrayList<>(Arrays.asList(COUNTRIES)));
        topicDropdown.setAdapter(adapter);
        topicDropdown.setThreshold(0);
    }

    private void eventsHandlers() {
        buttonSave.setOnClickListener(view -> {
            String newApiKey = textViewApiKey.getText().toString();
            String newSpreadsheetId = textViewSpreadsheetId.getText().toString();
            String currentApiKey = ExternalDataManager.getAPIkey();
            String currentSpreadsheetId = ExternalDataManager.getSpreadsheetId();
            ExternalDataManager.setAPIkey(newApiKey);
            ExternalDataManager.setSpreadsheetId(newSpreadsheetId);
            ExternalDataManager.storeAllData();

            if (!newApiKey.equals(currentApiKey) || !newSpreadsheetId.equals(currentSpreadsheetId)) {
                QuestionManager.resetDictionaryMaps();
                QuestionManager.storeSettingsAfterChangedDataSourceIds();
                QuestionManager.resetCurrentRoundQuestionListIndex();
                QuestionManager.clearHistory();
                AppStateManager.setAfterChangedDataSourceIds();
            }

            switchToMainActivity();
        });

        buttonCancel.setOnClickListener(view -> {
            switchToMainActivity();
        });

        buttonResetStorage.setOnClickListener(view -> {
            QuestionManager.resetDictionaryMaps();
            AppStorageManager.deleteSharedPreferences(this, AppStorageManager.preferencesFilename);
            QuestionManager.storeSettingsAfterReset();
            QuestionManager.setInitialStatesAfterReset();
            QuestionManager.clearHistory();
            AppStateManager.setAfterResetStorage();
        });

        topicDropdown.setOnClickListener(view -> {
            topicDropdown.showDropDown();
        });

        lastQuestionsDropdown.setOnClickListener(view -> {
            lastQuestionsDropdown.showDropDown();
        });

        tabSettings.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (SettingsTab.from(tab.getPosition())) {
                    case TOPIC:
                        tabContentLayout.removeAllViews();
                        tabContentLayout.addView(topicTab);
                        break;
                    case DATA_SOURCE:
                        String apiKey = ExternalDataManager.getAPIkey();
                        String spreadsheetId = ExternalDataManager.getSpreadsheetId();
                        textViewSpreadsheetId.setText(spreadsheetId);
                        textViewApiKey.setText(apiKey);

                        tabContentLayout.removeAllViews();
                        tabContentLayout.addView(dataSourceTab);
                        break;
                    case STORAGE:
                        tabContentLayout.removeAllViews();
                        tabContentLayout.addView(storageTab);
                        break;
                    case MONITORING:
                        settingsQuestionNumberTable.removeAllViews();
                        QuestionMonitoring.getCurrentMonitoringTableData().forEach((key, value) -> {
                            Map.Entry<String, Integer> rowDetails =
                                    value.entrySet().stream().findFirst().orElseThrow();
                            addNewRow(
                                    String.valueOf(key),
                                    rowDetails.getKey(),
                                    String.valueOf(rowDetails.getValue()));
                        });
                        textViewQuestionPosition.setText(QuestionMonitoring.getDecoratedCurrentRoundQuestionListIndex());
                        textViewCurrentRoundWordsList.setText(QuestionMonitoring.getDecoratedCurrentRoundQuestionList());
                        textViewPreviousRoundWordsList.setText(QuestionMonitoring.getDecoratedPreviousRoundQuestionList());

                        SimpleContainsAutocompleteAdapter<String> adapter = new SimpleContainsAutocompleteAdapter<>(
                                getApplicationContext(), QuestionMonitoring.getDecoratedAllPreviousQuestionsList());
                        lastQuestionsDropdown.setAdapter(adapter);
                        lastQuestionsDropdown.setThreshold(0);

                        tabContentLayout.removeAllViews();
                        tabContentLayout.addView(monitoringTab);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void switchToMainActivity() {
        AppStateManager.setPreOpenAnotherActivity();
        finish();
    }

    private void addNewRow(String index, String question, String number) {
        TableRow rowView = new TableRow(settingsQuestionNumberTable.getContext());
        inflator.inflate(R.layout.settings_question_number_tab_row_content, rowView);
        ((TextView) rowView.getVirtualChildAt(0)).setText(index);
        ((TextView) rowView.getVirtualChildAt(1)).setText(question);
        ((TextView) rowView.getVirtualChildAt(2)).setText(number);

        settingsQuestionNumberTable.addView(rowView);
    }
}