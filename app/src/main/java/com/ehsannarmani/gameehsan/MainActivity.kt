package com.ehsannarmani.gameehsan

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ehsannarmani.gameehsan.ui.theme.GameEhsanTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameEhsanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

                    val steps = GameDirection.entries
                    val gameStep = remember {
                        mutableStateOf<GameDirection?>(null)
                    }
                    val movements = remember {
                        mutableStateListOf<Boolean>()
                    }
                    LaunchedEffect(Unit) {
                        sensorManager.registerListener(
                            object : SensorEventListener {
                                override fun onSensorChanged(event: SensorEvent?) {
                                    if (event?.values?.count() == 3) {
                                        val (x, y, z) = event.values

                                        if (gameStep.value != null) {
                                            val targetDegree = when (gameStep.value!!) {
                                                GameDirection.Right, GameDirection.Left -> x
                                                GameDirection.Top, GameDirection.Bottom -> y
                                                GameDirection.NegativeZ, GameDirection.PositiveZ -> z
                                            }

                                            val detectValue = when(gameStep.value!!){
                                                GameDirection.NegativeZ,GameDirection.PositiveZ-> 35
                                                GameDirection.Left,GameDirection.Right-> 15
                                                GameDirection.Top,GameDirection.Bottom-> 15
                                            }
                                            val shouldDetect =
                                                Math.toDegrees(targetDegree.toDouble()).absoluteValue >= detectValue
                                            println("should detect: $shouldDetect")
                                            if (shouldDetect) {
                                                var movementResult = false
                                                println("Y: $y")
                                                when (gameStep.value!!) {
                                                    GameDirection.Left -> {
                                                        movementResult = (Math.toDegrees(y.toDouble())>=  detectValue)
                                                    }

                                                    GameDirection.Right -> {
                                                        movementResult = (Math.toDegrees(y.toDouble()) <= -detectValue)
                                                    }

                                                    GameDirection.Top -> {
                                                        movementResult = (Math.toDegrees(x.toDouble()) >= detectValue)
                                                    }

                                                    GameDirection.Bottom -> {
                                                        movementResult = (Math.toDegrees(x.toDouble()) <= -detectValue)
                                                    }

                                                    GameDirection.NegativeZ -> {
                                                        movementResult = (Math.toDegrees(z.toDouble()) >= detectValue)
                                                    }

                                                    GameDirection.PositiveZ -> {
                                                        movementResult = (Math.toDegrees(z.toDouble()) <= -detectValue)
                                                    }
                                                }
                                                movements.add(movementResult)
                                                if (movementResult){
                                                    gameStep.value = steps.random()
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                                }

                            },
                            gyroscope, SensorManager.SENSOR_DELAY_NORMAL,
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (gameStep.value != null) {
                                Text(text = gameStep.value?.display() ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (gameStep.value == null){
                                Button(onClick = {
                                    gameStep.value = steps.random()
                                }) {
                                    Text(text = "Start Game")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (movements.isNotEmpty()){
                                Text(text = if (movements.last()) "Correct!" else "Wrong!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class GameDirection {
    Right, Left, Top, Bottom, PositiveZ, NegativeZ
}

fun GameDirection.display(): String {
    return when (this) {
        GameDirection.Right -> {
            "Turn Right"
        }

        GameDirection.Left -> {
            "Turn Left"
        }

        GameDirection.Bottom -> {
            "Turn Bottom"
        }

        GameDirection.Top -> {
            "Turn Top"
        }

        GameDirection.PositiveZ -> {
            "Turn In Z Angle (Right)"
        }

        GameDirection.NegativeZ -> {
            "Turn In Z Angle (Left)"
        }
    }
}