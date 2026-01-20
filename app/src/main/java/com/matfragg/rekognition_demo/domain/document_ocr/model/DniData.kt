package com.matfragg.rekognition_demo.domain.document_ocr.model
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DniData(
    val numeroDni: String,
    val apellidos: String,
    val nombres: String,
    val fechaNacimiento: String,
    val sexo: String,
    val nacionalidad: String,
    val fechaEmision: String,
    val fechaVencimiento: String,
    val fotoPersonaBase64: String? = null,
    val frontImageBase64: String? = null,
    val backImageBase64: String? = null,
    val serialNumber: String? = null,
    val confidence: Double = 0.0
) {
    @RequiresApi(Build.VERSION_CODES.O)
    private val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")


    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedVencimiento(string: String): String {
        if (fechaVencimiento.isBlank()) return "No detectado"

        return try {
            // 1. Intentamos parsear el formato ISO (2031-06-27T05:00:00.000+00:00)
            // Usamos OffsetDateTime porque tiene información de zona (+00:00)
            val parsedDate = java.time.OffsetDateTime.parse(fechaVencimiento).toLocalDate()
            parsedDate.format(outputFormatter)
        } catch (e: Exception) {
            // 2. Si falla el ISO, intentamos el formato dd/MM/yyyy por si acaso
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val date = LocalDate.parse(fechaVencimiento, inputFormatter)
                date.format(outputFormatter)
            } catch (e2: Exception) {
                // Si todo falla, devolvemos el original para no perder data
                fechaVencimiento
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isExpired(): Boolean {
        return try {
            // Aplicamos la misma lógica: parsear como ISO y comparar
            val expiryDate = java.time.OffsetDateTime.parse(fechaVencimiento).toLocalDate()
            expiryDate.isBefore(LocalDate.now())
        } catch (e: Exception) {
            // Reintento con formato manual si el ISO falla
            try {
                val date = LocalDate.parse(fechaVencimiento, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                date.isBefore(LocalDate.now())
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun isValid(): Boolean {
        return numeroDni.isNotBlank() &&
                apellidos.isNotBlank() &&
                nombres.isNotBlank() &&
                fechaNacimiento.isNotBlank()
    }

    fun getFullName(): String = "$nombres $apellidos"
}