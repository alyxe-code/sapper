package com.alyxe.sapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alyxe.sapper.ui.components.GridBuilder
import com.alyxe.sapper.ui.components.Sapper
import com.alyxe.sapper.ui.components.SapperDefaultFields
import com.alyxe.sapper.ui.components.SapperGameViewModel
import com.alyxe.sapper.ui.theme.SapperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SapperTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {

                    val viewModel: SapperGameViewModel = viewModel()

                    var size by remember { mutableStateOf(5) }
                    var bombCount by remember { mutableStateOf(5) }

                    val gameState by viewModel.state.collectAsState()
                    if (gameState == SapperGameViewModel.State.Init) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(text = "Size = $size")
                            Slider(
                                value = size / 10f,
                                onValueChange = { size = (it * 10f).toInt() },
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(text = "Bombs count = $bombCount")
                            Slider(
                                value = bombCount / 10f,
                                onValueChange = {
                                    bombCount = (it * 10f).toInt()
                                },
                            )

                            Spacer(Modifier.height(40.dp))

                            Button(onClick = {
                                viewModel.onLevelSelected(
                                    GridBuilder.Config(
                                        width = size,
                                        height = size,
                                        bombCount = minOf(bombCount, size * size - 1),
                                    )
                                )
                            }) {
                                Text(text = "Play")
                            }
                        }
                    } else {
                        Sapper(
                            viewModel = viewModel,
                            fields = SapperDefaultFields,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SapperTheme {
        Greeting("Android")
    }
}