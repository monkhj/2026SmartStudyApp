package com.studyapp.dto;

import java.util.List;

public record StatisticsResult(
    List<String>  subjects,
    List<Double>  radarData,
    List<String>  barLabels,
    List<Integer> weekData,
    List<Integer> monthData,
    List<String>  trendLabels,
    List<Integer> trendAll
) {}
