package com.matfragg.rekognition_demo.data.document_ocr.parser

import com.matfragg.rekognition_demo.domain.document_ocr.model.DniData
import javax.inject.Inject

class DniParserService @Inject constructor(
    private val fieldExtractor: DniFieldExtractor
) {

    fun parseDniData(frontText: String, backText: String): DniData {
        val combinedText = "$frontText\n$backText"

        // Extraer número de DNI
        val numeroDni = fieldExtractor.extractDniNumber(combinedText) ?: ""

        // Extraer nombres y apellidos
        val namesMap = fieldExtractor.extractNames(frontText)
        val apellidos = namesMap["APELLIDOS"] ?: ""
        val nombres = namesMap["NOMBRES"] ?: ""

        // Extraer fechas
        val dates = fieldExtractor.extractDates(combinedText)
        val fechaNacimiento = dates.getOrNull(0) ?: ""
        val fechaEmision = dates.getOrNull(1) ?: ""
        val fechaVencimiento = dates.getOrNull(2) ?: ""

        // Extraer otros campos
        val sexo = fieldExtractor.extractSex(frontText) ?: ""
        val nacionalidad = fieldExtractor.extractNationality(frontText) ?: "PERUANA"

        return DniData(
            numeroDni = numeroDni,
            apellidos = apellidos,
            nombres = nombres,
            fechaNacimiento = fechaNacimiento,
            sexo = sexo,
            nacionalidad = nacionalidad,
            fechaEmision = fechaEmision,
            fechaVencimiento = fechaVencimiento
        )
    }
}