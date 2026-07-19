package dev.pulsereport.feature.dashboard

import java.time.Duration
import java.time.LocalDate

/** Sample data matching the design mockup; used for previews and tests. */
val MockDashboardState = DashboardUiState(
    selectedDate = LocalDate.now(),
    syncedSourceCount = 4,
    steps = StepsData(
        todaySteps = 8_432,
        goalSteps = 10_000,
        distanceKm = 6.1,
        weekSteps = listOf(5_500, 8_000, 4_000, 6_500, 9_000, 5_000, 8_432),
    ),
    sleep = SleepData(
        duration = Duration.ofHours(7).plusMinutes(39),
        score = 83,
        scoreLabel = "Good",
        stageWeights = listOf(2f, 5f, 2f),
    ),
    heart = HeartData(
        restingBpm = 58,
        series = listOf(12f, 14f, 8f, 24f, 16f, 26f, 14f, 18f),
    ),
    activeCalories = ActiveCaloriesData(
        burnedKcal = 612,
        goalKcal = 700,
    ),
    water = WaterData(
        consumedLitres = 1.6,
        goalLitres = 2.5,
        totalSlots = 5,
    ),
    weight = WeightData(
        currentKg = 78.4,
        weekDeltaKg = -0.6,
        series = listOf(32f, 26f, 34f, 22f, 24f, 14f, 16f),
    ),
)
