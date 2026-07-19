package dev.pulsereport.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.pulsereport.core.ui.theme.PulseTheme

/** One destination in the [PulseNavigationBar]. */
data class PulseNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

/**
 * The app's bottom navigation: a selected item shows its filled icon inside
 * an accent-tinted pill, unselected items show muted outline icons.
 */
@Composable
fun PulseNavigationBar(
    items: List<PulseNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PulseTheme.colors

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        HorizontalDivider(thickness = 1.dp, color = colors.navDivider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 6.dp),
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val tint = if (selected) colors.accent else colors.textMuted

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClickLabel = item.label,
                        ) { onItemSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier
                            .background(
                                color = if (selected) {
                                    colors.accent.copy(alpha = 0.16f)
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                },
                                shape = CircleShape,
                            )
                            .padding(horizontal = 18.dp, vertical = 4.dp)
                            .size(22.dp),
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
