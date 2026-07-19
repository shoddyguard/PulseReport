package dev.pulsereport.core.model

/** Fixed daily targets, matching the values the design mockup shipped with. Shared by the dashboard tiles and the My Health detail screens so both agree on what "100%" means. */
const val GOAL_STEPS = 10_000
const val GOAL_ACTIVE_KCAL = 700
const val GOAL_HYDRATION_LITRES = 2.5
const val HYDRATION_SLOTS = 5

/** Average stride length used to turn a step count into a rough walking distance. */
const val STRIDE_METERS = 0.72

/** Days of trailing history the week-style tiles/charts cover. */
const val TRAILING_WEEK_DAYS = 7
