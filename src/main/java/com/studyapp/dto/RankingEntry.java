package com.studyapp.dto;

import lombok.*;

// percentage는 계산 후 setter로 주입하므로 record 대신 Lombok 사용
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingEntry {
    private String  userId;
    private String  username;
    private String  major;
    private int     totalMinutes;
    private int     percentage;  // 1위 대비 비율 (0~100), 계산 후 set
    private boolean isMe;
}
