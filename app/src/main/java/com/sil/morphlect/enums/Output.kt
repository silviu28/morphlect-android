package com.sil.morphlect.enums

enum class Output {
    Sharpness,
    Brightness,
    Contrast,
    Hue,
    Bitrate,
    QualityRating;

    fun toFilter(): Filter? {
        return when (this) {
            Sharpness -> Filter.Sharpness
            Brightness -> Filter.Brightness
            Contrast -> Filter.Contrast
            Hue -> Filter.Hue
            Bitrate -> Filter.Blur
            QualityRating -> null
        }
    }
}