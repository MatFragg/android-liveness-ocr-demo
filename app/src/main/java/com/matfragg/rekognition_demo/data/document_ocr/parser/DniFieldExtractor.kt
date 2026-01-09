package com.matfragg.rekognition_demo.data.document_ocr.parser

import java.util.regex.Pattern

class DniFieldExtractor {

    companion object {
        // Regex patterns para campos del DNI peruano
        private val DNI_NUMBER_PATTERN = Pattern.compile("\\b\\d{8}\\b")
        private val DATE_PATTERN = Pattern.compile("\\b\\d{2}[/-]\\d{2}[/-]\\d{4}\\b")
        private val NATIONALITY_PATTERN = Pattern.compile("(?i)(PERUANO|PERUANA|PER)")
        private val SEX_PATTERN = Pattern.compile("(?i)\\b(M|F|MASCULINO|FEMENINO)\\b")
    }

    fun extractDniNumber(text: String): String? {
        val matcher = DNI_NUMBER_PATTERN.matcher(text)
        val candidates = mutableListOf<String>()

        while (matcher.find()) {
            candidates.add(matcher.group())
        }

        // Retornar el primer DNI encontrado
        return candidates.firstOrNull()
    }

    fun extractDates(text: String): List<String> {
        val matcher = DATE_PATTERN.matcher(text)
        val dates = mutableListOf<String>()

        while (matcher.find()) {
            dates.add(matcher.group())
        }

        return dates
    }

    fun extractNationality(text: String): String? {
        val matcher = NATIONALITY_PATTERN.matcher(text)
        return if (matcher.find()) {
            "PERUANA"
        } else null
    }

    fun extractSex(text: String): String? {
        val matcher = SEX_PATTERN.matcher(text)
        return if (matcher.find()) {
            val match = matcher.group().uppercase()
            when {
                match.startsWith("M") -> "MASCULINO"
                match.startsWith("F") -> "FEMENINO"
                else -> match
            }
        } else null
    }

    fun extractNames(text: String, keywords: List<String> = listOf("NOMBRES", "APELLIDOS")): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val lines = text.split("\n")

        for (i in lines.indices) {
            val line = lines[i].trim()

            keywords.forEach { keyword ->
                if (line.contains(keyword, ignoreCase = true)) {
                    // Buscar el valor en la misma línea o la siguiente
                    val value = if (line.length > keyword.length + 2) {
                        line.substringAfter(keyword).trim().trimStart(':').trim()
                    } else if (i + 1 < lines.size) {
                        lines[i + 1].trim()
                    } else {
                        ""
                    }

                    if (value.isNotBlank()) {
                        result[keyword] = value
                    }
                }
            }
        }

        return result
    }
}