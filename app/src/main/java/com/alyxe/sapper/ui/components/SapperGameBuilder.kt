package com.alyxe.sapper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alyxe.sapper.BuildConfig
import com.alyxe.sapper.R


@Composable
fun Sapper(
    modifier: Modifier = Modifier,
    viewModel: SapperGameViewModel = viewModel(),
    fields: @Composable (SapperField) -> Unit = SapperDefaultFields
) {
    val grid by viewModel.grid.collectAsState()
    val gameState by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .then(modifier)
            ) {
                for (x in 0 until viewModel.config.width) {
                    Row(modifier = Modifier.padding(horizontal = 2.dp)) {
                        for (y in 0 until viewModel.config.height) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            ) {
                                val field = grid[x to y]
                                if (field != null) {
                                    Button(
                                        onClick = { viewModel.onFieldSelected(x, y) },
                                        contentPadding = PaddingValues(2.dp),
                                        colors = ButtonDefaults.textButtonColors(),
                                        elevation = ButtonDefaults.elevation(
                                            defaultElevation = 1.dp,
                                            pressedElevation = 2.dp,
                                            disabledElevation = 0.dp,
                                        ),
                                        enabled = field is SapperField.Closed,
                                    ) {
                                        fields(field)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (gameState != SapperGameViewModel.State.Playing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (gameState == SapperGameViewModel.State.GameOver) {
                        Text(
                            text = "Game Over".toUpperCase(Locale.current),
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                        )
                    } else if (gameState == SapperGameViewModel.State.Win) {
                        Text(
                            text = "WIN".toUpperCase(Locale.current),
                            color = Color.Yellow,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    IconButton(onClick = viewModel::onRestart) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_refresh_24),
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }
    }
}

sealed class SapperField {
    object Bomb : SapperField()
    object DeactivatedBomb : SapperField()
    class Closed(val next: SapperField) : SapperField()
    class Open(val countBombs: Int) : SapperField()
}

val SapperDefaultFields: @Composable (SapperField) -> Unit = { fieldType ->
    when (fieldType) {
        SapperField.Bomb -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_bomb),
                contentDescription = "Bomb",
                modifier = Modifier.size(40.dp),
            )
        }
        is SapperField.Closed -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f), MaterialTheme.shapes.small)
        ) {
            if (fieldType.next is SapperField.Bomb && BuildConfig.DEBUG) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bomb),
                    contentDescription = "Bomb",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Black.copy(alpha = 0.05f),
                )
            }
        }
        is SapperField.Open -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                )
        ) {
            if (fieldType.countBombs >= 1) {
                Text(
                    text = fieldType.countBombs.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        is SapperField.DeactivatedBomb -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_flag),
                contentDescription = "Bomb",
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
