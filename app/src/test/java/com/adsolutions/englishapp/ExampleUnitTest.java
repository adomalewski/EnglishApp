package com.adsolutions.englishapp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

public class ExampleUnitTest {

    @Mock
    MockedStatic<QuestionManager> qmMock;

    @Captor
    ArgumentCaptor<Integer> indexArgCaptor;


    @Before
    public void before() {
        // needed to initialize captors etc.
        MockitoAnnotations.openMocks(this);
    }

    @Ignore
    @Test
    public void exampleTest() {
        List<Integer> currentQuestionsList = List.of(3, 0, 1, 2);
        List<String> currentQuestionTextList = List.of("three", "zero", "one", "two");
        List<Integer> previousQuestionsList = List.of(1, 3, 2, 0);
        int currentQuestionListIndex = 1;

//        MockedStatic<QuestionManager> qmMock = Mockito.mockStatic(QuestionManager.class);
//        ArgumentCaptor<Integer> indexArgCaptor = ArgumentCaptor.forClass(Integer.class);
        qmMock.when(QuestionManager::getCurrentRoundQuestionsList)
                .thenReturn(currentQuestionsList);
        qmMock.when(QuestionManager::getPreviousRoundQuestionsList)
                .thenReturn(previousQuestionsList);
        qmMock.when(QuestionManager::getCurrentRoundQuestionListIndex)
                .thenReturn(currentQuestionListIndex);
//        qmMock.when(() -> QuestionManager.getQuestionText(anyInt())).thenAnswer(
//                AdditionalAnswers.returnsElementsOf(currentQuestionTextList));
        qmMock.when(() -> QuestionManager.getQuestionText(indexArgCaptor.capture())).thenAnswer(
                AdditionalAnswers.returnsElementsOf(currentQuestionTextList));

        Map<Integer, Map<String, Integer>> monitoringTable = QuestionMonitoring.getCurrentMonitoringTableData();

        qmMock.verify(QuestionManager::getCurrentRoundQuestionsList, atLeast(1));
        qmMock.verify(QuestionManager::getPreviousRoundQuestionsList, atLeast(1));
        qmMock.verify(QuestionManager::getCurrentRoundQuestionListIndex, atLeast(1));
        qmMock.verify(() -> QuestionManager.getQuestionText(anyInt()), atLeast(1));
        qmMock.verifyNoMoreInteractions();

        assertEquals(currentQuestionsList, indexArgCaptor.getAllValues());
    }
}
