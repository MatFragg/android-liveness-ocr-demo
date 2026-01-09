package com.matfragg.rekognition_demo.presentation.document_ocr.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.BitmapFactory
import android.util.Base64
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DniResultScreen(
    dniData: com.matfragg.rekognition_demo.domain.document_ocr.model.DniData,
    onNavigateBack: () -> Unit,
    onSave: (com.matfragg.rekognition_demo.domain.document_ocr.model.DniData) -> Unit = {},
    viewModel: DniResultViewModel = hiltViewModel()
) {
    LaunchedEffect(dniData) {
        viewModel.setDniData(dniData)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos del DNI") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onSave(dniData) }) {
                        Icon(Icons.Default.Save, "Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de la persona
            dniData.fotoPersonaBase64?.let { photoBase64 ->
                PhotoCard(photoBase64)
            }

            // Datos personales
            PersonalDataCard(dniData)

            // Datos del documento
            DocumentDataCard(dniData)

            // Vista previa de imágenes
            ImagesPreviewCard(dniData)

            // Botones de acción
            ActionButtons(
                onSave = { onSave(dniData) },
                onShare = { /* Implementar */ },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun PhotoCard(photoBase64: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Fotografía",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(12.dp))

            val bitmap = remember(photoBase64) {
                val bytes = Base64.decode(photoBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto del DNI",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun PersonalDataCard(dniData: com.matfragg.rekognition_demo.domain.document_ocr.model.DniData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Datos Personales",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            HorizontalDivider()

            DataField("DNI", dniData.numeroDni, Icons.Default.Badge)
            DataField("Apellidos", dniData.apellidos, Icons.Default.Person)
            DataField("Nombres", dniData.nombres, Icons.Default.Person)
            DataField("Fecha de Nacimiento", dniData.fechaNacimiento, Icons.Default.Cake)
            DataField("Sexo", dniData.sexo, Icons.Default.Wc)
            DataField("Nacionalidad", dniData.nacionalidad, Icons.Default.Flag)
        }
    }
}

@Composable
fun DocumentDataCard(dniData: com.matfragg.rekognition_demo.domain.document_ocr.model.DniData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Datos del Documento",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            HorizontalDivider()

            DataField("Fecha de Emisión", dniData.fechaEmision, Icons.Default.CalendarToday)
            DataField("Fecha de Vencimiento", dniData.fechaVencimiento, Icons.Default.Event)

            // Indicador de estado
            val isExpired = dniData.isExpired()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isExpired) Icons.Default.Warning else Icons.Default.CheckCircle,
                    null,
                    tint = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isExpired) "Documento vencido" else "Documento vigente",
                    color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun ImagesPreviewCard(dniData: com.matfragg.rekognition_demo.domain.document_ocr.model.DniData) {
    var showFullImage by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Imágenes Capturadas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Frontal
                dniData.frontImageBase64?.let { frontBase64 ->
                    ImageThumbnail(
                        imageBase64 = frontBase64,
                        label = "Frontal",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedImage = frontBase64
                            showFullImage = true
                        }
                    )
                }

                // Trasera
                dniData.backImageBase64?.let { backBase64 ->
                    ImageThumbnail(
                        imageBase64 = backBase64,
                        label = "Trasera",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedImage = backBase64
                            showFullImage = true
                        }
                    )
                }
            }
        }
    }

    // Dialog para mostrar imagen completa
    if (showFullImage && selectedImage != null) {
        AlertDialog(
            onDismissRequest = { showFullImage = false },
            confirmButton = {
                TextButton(onClick = { showFullImage = false }) {
                    Text("Cerrar")
                }
            },
            text = {
                val bitmap = remember(selectedImage) {
                    val bytes = Base64.decode(selectedImage, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen completa",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }
}

@Composable
fun ImageThumbnail(
    imageBase64: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val bitmap = remember(imageBase64) {
                val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun DataField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value.ifBlank { "No disponible" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun ActionButtons(
    onSave: () -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(Modifier.width(8.dp))
            Text("Guardar Datos")
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, null)
            Spacer(Modifier.width(8.dp))
            Text("Compartir")
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escanear otro DNI")
        }
    }
}