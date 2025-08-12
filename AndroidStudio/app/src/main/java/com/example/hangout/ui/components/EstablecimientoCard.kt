package com.example.hangout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.hangout.models.Establecimiento

@Composable
fun EstablecimientoCard(establecimiento: Establecimiento) {
    val imageUrl = "http://10.0.2.2:5000${establecimiento.imagen_url}"

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(260.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = establecimiento.nombre_establecimiento,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Yellow, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("0.0", color = Color.Black, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = establecimiento.nombre_establecimiento,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = establecimiento.ambiente.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "${establecimiento.reviews.size} valoraciones",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
