package com.rupeecounter.app.ui.counter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rupeecounter.app.data.model.Denomination
import com.rupeecounter.app.data.model.DenominationItem
import com.rupeecounter.app.data.model.DenominationType

@Composable
fun DenominationRow(
    item: DenominationItem,
    onCountStringChanged: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNote = item.denomination.type == DenominationType.NOTE
    val badgeColor = if (isNote) {
        getNoteBadgeColor(item.denomination)
    } else {
        getCoinBadgeColor(item.denomination)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .semantics {
                contentDescription = "${item.denomination.displayLabel}: ${item.count} pieces, subtotal ${item.formattedSubtotal}"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.count > 0L) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.count > 0L) 1.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Denomination Badge & Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.widthIn(min = 85.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = item.denomination.displayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Decrement Button (-)
            IconButton(
                onClick = onDecrement,
                enabled = item.count > 0L,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrement ${item.denomination.displayLabel}",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Numeric Input Field
            OutlinedTextField(
                value = if (item.count == 0L) "" else item.count.toString(),
                onValueChange = onCountStringChanged,
                placeholder = {
                    Text(
                        text = "0",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .width(82.dp)
                    .padding(horizontal = 4.dp)
            )

            // Increment Button (+)
            IconButton(
                onClick = onIncrement,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment ${item.denomination.displayLabel}",
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Subtotal Display
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 90.dp)
            ) {
                Text(
                    text = item.formattedSubtotal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.count > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                Text(
                    text = if (item.count > 0L) "${item.count} pcs" else "0 pcs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Distinct badge colors matching official RBI banknote tones
private fun getNoteBadgeColor(denomination: Denomination): Color {
    return when (denomination) {
        Denomination.NOTE_500 -> Color(0xFF5D6D7E) // Stone Grey
        Denomination.NOTE_200 -> Color(0xFFD35400) // Bright Orange
        Denomination.NOTE_100 -> Color(0xFF5B2C6F) // Lavender / Purple
        Denomination.NOTE_50 -> Color(0xFF16A085)  // Fluorescent Blue / Teal
        Denomination.NOTE_20 -> Color(0xFFB7950B)  // Greenish Yellow
        Denomination.NOTE_10 -> Color(0xFF6E2C00)  // Chocolate Brown
        else -> Color(0xFF006D44)
    }
}

private fun getCoinBadgeColor(denomination: Denomination): Color {
    return when (denomination) {
        Denomination.COIN_20 -> Color(0xFF8D6E63) // Bimetallic bronze
        Denomination.COIN_10 -> Color(0xFFA1887F) // Bimetallic outer/inner
        Denomination.COIN_5 -> Color(0xFF78909C)  // Nickel brass / steel
        Denomination.COIN_2 -> Color(0xFF90A4AE)  // Stainless steel
        Denomination.COIN_1 -> Color(0xFFB0BEC5)  // Stainless steel
        else -> Color(0xFF455A64)
    }
}
