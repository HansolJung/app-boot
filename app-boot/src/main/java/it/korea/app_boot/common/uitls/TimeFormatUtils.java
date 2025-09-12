package it.korea.app_boot.common.uitls;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class TimeFormatUtils {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 형변환. 현재 시간 -> String
     * @return
     */
    public static String getDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 형변환. time -> String
     * @param time 시간
     * @return
     */
    public static String getDateTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }
}
