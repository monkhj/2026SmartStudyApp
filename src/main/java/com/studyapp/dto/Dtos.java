package com.studyapp.dto;

import java.util.List;

record SubjectStatsResult(
    List<String> labels,
    List<Double> values
) {}

record BarChartResult(
    List<String>  labels,
    List<Integer> values
) {}
