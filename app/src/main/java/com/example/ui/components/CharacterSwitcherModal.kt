package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterCatalog
import com.example.data.CharacterPreset
import com.example.ui.theme.LuxNeonRed
import com.example.ui.theme.LuxPureWhite

/**
 * Character Switcher Modal overlay matching the user's HTML/CSS mockup.
 * Allows instant selection of anime/cyberpunk AI personas.
 */
@Composable
fun CharacterSwitcherModal(
    isOpen: Boolean,
    selectedCharacterId: String,
    onSelectCharacter: (CharacterPreset) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onDismiss)
                .padding(24.dp)
                .testTag("character_switcher_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {} // Prevent click-through
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Character",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LuxPureWhite
                    )

                    // Close Button (Glassmorphic circular icon button)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(onClick = onDismiss)
                            .testTag("close_character_modal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Character Cards List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CharacterCatalog.CHARACTERS, key = { it.id }) { char ->
                        val isSelected = char.id == selectedCharacterId

                        val cardBg = if (isSelected) {
                            LuxNeonRed.copy(alpha = 0.15f)
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }

                        val cardBorder = if (isSelected) {
                            LuxNeonRed
                        } else {
                            Color.White.copy(alpha = 0.10f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(cardBg)
                                .border(if (isSelected) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape(18.dp))
                                .clickable {
                                    onSelectCharacter(char)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("char_card_${char.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Character Avatar
                            AsyncImage(
                                model = char.imageUrl,
                                contentDescription = char.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, LuxNeonRed, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = char.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxPureWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = char.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
