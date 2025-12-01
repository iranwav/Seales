package com.example.seales

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TecladoScreen()
            }
        }
    }
}

@Composable
fun TecladoScreen() {
    val context = LocalContext.current
    var textoEscrito by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (textoEscrito.isEmpty()) "TEXTO..." else textoEscrito,
                color = if (textoEscrito.isEmpty()) Color.Gray else Color.White,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        VisualizadorSenas(texto = textoEscrito)

        Spacer(modifier = Modifier.weight(1f))

        TecladoPersonalizado(
            onLetraClick = { letra -> textoEscrito += letra },
            onBorrarClick = {
                if (textoEscrito.isNotEmpty()) textoEscrito = textoEscrito.dropLast(1)
            },
            onCompartirClick = {
                if (textoEscrito.isNotEmpty()) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textoEscrito)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Compartir frase")
                    context.startActivity(shareIntent)
                }
            }
        )
    }
}

@Composable
fun VisualizadorSenas(texto: String) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        if (texto.isEmpty()) {
            Text(
                "SEÑAS...",
                color = Color.DarkGray,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                items(texto.toList()) { char ->
                    val letraString = char.toString().uppercase()
                    if (letraString == " ") {
                        Spacer(modifier = Modifier.width(30.dp))
                    } else {
                        val nombreImagen = if (letraString == "Ñ") "lsm_nn" else "lsm_${letraString.lowercase()}"
                        val resourceId = context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)
                        if (resourceId != 0) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = "Seña $letraString",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TecladoPersonalizado(
    onLetraClick: (String) -> Unit,
    onBorrarClick: () -> Unit,
    onCompartirClick: () -> Unit
) {
    val fila1 = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    val fila2 = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "Ñ")
    val fila3 = listOf("Z", "X", "C", "V", "B", "N", "M")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilaDeTeclas(fila1, onLetraClick)
        FilaDeTeclas(fila2, onLetraClick)
        FilaDeTeclas(fila3, onLetraClick)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BotonControl("BORRAR", Color(0xFFB71C1C), Modifier.weight(1f), onBorrarClick)
            BotonControl("ESPACIO", Color(0xFF424242), Modifier.weight(2f)) { onLetraClick(" ") }
            BotonControl("ENVIAR", Color(0xFF2E7D32), Modifier.weight(1f), onCompartirClick)
        }
    }
}

@Composable
fun FilaDeTeclas(letras: List<String>, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        letras.forEach { letra ->
            TeclaLetraIndividual(letra, onClick, Modifier.weight(1f))
        }
    }
}

@Composable
fun TeclaLetraIndividual(
    letra: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nombreImagen = if (letra == "Ñ") "lsm_nn" else "lsm_${letra.lowercase()}"
    val resourceId = remember(letra) {
        context.resources.getIdentifier(nombreImagen, "drawable", context.packageName)
    }

    Button(
        onClick = { onClick(letra) },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E3E)),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .padding(4.dp)
            .size(65.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Seña $letra",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = letra,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
fun BotonControl(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(65.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}