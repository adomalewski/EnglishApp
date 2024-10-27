package com.adsolutions.englishapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class NotificationScheduleManager {

    private static final Map<String, List<String>> scheduleMap = new HashMap<>();

    public static Map<String, List<String>> getScheduleMap() {
        return scheduleMap;
    }

    private static int alarmTimeoutHr = 2;

    public static long getAlarmTriggerTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        calendar.add(Calendar.HOUR_OF_DAY, alarmTimeoutHr);
        Date candidateTime = calendar.getTime();
        String candidateWeekday = getWeekday(candidateTime);
        int candidateHour = getHour(candidateTime);

        Date finalTime = null;
        List<String> periods;

        if (scheduleMap.get(candidateWeekday) != null) {
            periods = Objects.requireNonNull(scheduleMap.get(candidateWeekday));
            for (String period : periods) {
                int[] fromTo = parsePeriodBoundaries(period);
                boolean result = candidateHour >= fromTo[0] && candidateHour < fromTo[1];
                if (!result) {
                    if (candidateHour < fromTo[0]) {
                        finalTime = calculateDateWithExactHour(calendar, candidateTime, fromTo[0]);
                        break;
                    }
                } else {
                    finalTime = candidateTime;
                    break;
                }
            }
        }

        if (finalTime == null) {
            for (int i = 0; i < 7; ++i) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date calculatedTime = calendar.getTime();
                candidateWeekday = getWeekday(calculatedTime);
                if (scheduleMap.get(candidateWeekday) == null) {
                    continue;
                }
                periods = Objects.requireNonNull(scheduleMap.get(candidateWeekday));
                if (!periods.isEmpty()) {
                    candidateHour = parsePeriodBoundaries(periods.get(0))[0];
                    finalTime = calculateDateWithExactHour(calendar, calculatedTime, candidateHour);
                    break;
                }
            }
        }
        return finalTime.getTime();
    }

    private static int[] parsePeriodBoundaries(String period) {
        return Arrays.stream(period.split("-")).flatMapToInt(num -> IntStream.of(Integer.parseInt(num))).toArray();
    }

    private static Date calculateDateWithExactHour(Calendar calendar, Date day, int newHour) {
        calendar.setTime(day);
        calendar.set(Calendar.HOUR_OF_DAY, newHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static String getWeekday(Date date) {
        DateFormat formatterDay = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        return formatterDay.format(date).toLowerCase();
    }

    private static Integer getHour(Date date) {
        DateFormat formatterHr = new SimpleDateFormat("HH", Locale.ENGLISH);
        return Integer.parseInt(formatterHr.format(date));
    }
}