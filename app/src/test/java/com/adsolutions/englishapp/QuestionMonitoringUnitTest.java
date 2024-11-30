package com.adsolutions.englishapp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestionMonitoringUnitTest {

    @Mock
    MockedStatic<QuestionManager> qmMock;

    @Captor
    ArgumentCaptor<Integer> indexArgCaptor;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void checkGetCurrentMonitoringTableData() {
        List<Integer> currentQuestionsList = List.of(3, 0, 1, 2);
        List<String> currentQuestionTextList = List.of("three", "zero", "one", "two");
        List<Integer> previousQuestionsList = List.of(1, 3, 2, 0);
        int currentQuestionListIndex = 1;

        qmMock.when(QuestionManager::getCurrentRoundQuestionsList)
                .thenReturn(currentQuestionsList);
        qmMock.when(QuestionManager::getPreviousRoundQuestionsList)
                .thenReturn(previousQuestionsList);
        qmMock.when(QuestionManager::getCurrentRoundQuestionListIndex)
                .thenReturn(currentQuestionListIndex);
        qmMock.when(() -> QuestionManager.getQuestionText(indexArgCaptor.capture())).thenAnswer(
                AdditionalAnswers.returnsElementsOf(currentQuestionTextList));

        Map<Integer, Map<String, Integer>> monitoringTable = QuestionMonitoring.getCurrentMonitoringTableData();

        qmMock.verify(QuestionManager::getCurrentRoundQuestionsList, atLeast(1));
        qmMock.verify(QuestionManager::getPreviousRoundQuestionsList, atLeast(1));
        qmMock.verify(QuestionManager::getCurrentRoundQuestionListIndex, atLeast(1));
        qmMock.verify(() -> QuestionManager.getQuestionText(anyInt()), atLeast(1));
        qmMock.verifyNoMoreInteractions();
        assertEquals(currentQuestionsList, indexArgCaptor.getAllValues());

        Map<Integer, Map<String, Integer>> expectedTable = new LinkedHashMap<>() {{
            put(1, new LinkedHashMap<>() {{
                put("one", 5);
            }});
            put(2, new LinkedHashMap<>() {{
                put("two", 3);
            }});
            put(3, new LinkedHashMap<>() {{
                put("three", 1);
            }});
            put(0, new LinkedHashMap<>() {{
                put("zero", 0);
            }});
        }};

        assertEquals(expectedTable, monitoringTable);
        Assert.assertEquals(new ArrayList<>(expectedTable.entrySet()),
                new ArrayList<>(monitoringTable.entrySet()));
    }
}