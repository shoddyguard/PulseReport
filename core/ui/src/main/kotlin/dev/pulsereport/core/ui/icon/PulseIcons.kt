package dev.pulsereport.core.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook as OutlinedMenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Home as OutlinedHome
import androidx.compose.material.icons.outlined.IosShare as OutlinedIosShare
import androidx.compose.material.icons.outlined.MonitorHeart as OutlinedMonitorHeart
import androidx.compose.material.icons.outlined.Mood as OutlinedMood
import androidx.compose.material.icons.outlined.Tune as OutlinedTune
import androidx.compose.ui.graphics.vector.ImageVector

/** The app's icon set, from the standard Material icons library. */
object PulseIcons {
    val Walk: ImageVector = Icons.AutoMirrored.Filled.DirectionsWalk
    val Bedtime: ImageVector = Icons.Filled.Bedtime
    val Heart: ImageVector = Icons.Filled.MonitorHeart
    val Fire: ImageVector = Icons.Filled.LocalFireDepartment
    val WaterDrop: ImageVector = Icons.Filled.WaterDrop
    val Weight: ImageVector = Icons.Filled.MonitorWeight
    val EditNote: ImageVector = Icons.Filled.EditNote
    val CheckCircle: ImageVector = Icons.Filled.CheckCircle
    val Person: ImageVector = Icons.Filled.Person
    val ChevronRight: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val ChevronLeft: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft
    val ChevronDown: ImageVector = Icons.Filled.KeyboardArrowDown
    val DragHandle: ImageVector = Icons.Filled.DragIndicator

    val Home: ImageVector = Icons.Outlined.OutlinedHome
    val HomeFill: ImageVector = Icons.Filled.Home
    val HealthOutlined: ImageVector = Icons.Outlined.OutlinedMonitorHeart
    val Mood: ImageVector = Icons.Outlined.OutlinedMood
    val MoodFill: ImageVector = Icons.Filled.Mood
    val Book: ImageVector = Icons.AutoMirrored.Outlined.OutlinedMenuBook
    val BookFill: ImageVector = Icons.AutoMirrored.Filled.MenuBook
    val Share: ImageVector = Icons.Outlined.OutlinedIosShare
    val ShareFill: ImageVector = Icons.Filled.IosShare
    val Tune: ImageVector = Icons.Outlined.OutlinedTune
    val TuneFill: ImageVector = Icons.Filled.Tune
}
