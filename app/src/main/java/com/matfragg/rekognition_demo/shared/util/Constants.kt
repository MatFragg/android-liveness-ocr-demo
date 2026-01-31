package com.matfragg.rekognition_demo.shared.util

object Constants {
    // AWS LAMBDA URL
    //const val LAMBDA_COMPARE_URL = "https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/"
    //const val LAMBDA_DETECT_URL = "https://aylvctzkitqtanklvszguhkll40fmjqb.lambda-url.us-east-2.on.aws/"
    const val LAMBDA_DETECT_URL = "https://springboot-liveness-ocr-demo.onrender.com/liveness/"
    const val LAMBDA_COMPARE_URL = "http://localhost:8081/api/face-comparison/"
    const val DNI_OCR_URL = "https://springboot-liveness-ocr-demo.onrender.com/"
    const val AWS_REGION = "us-east-1"
    const val ACJ_API = "https://api.acjdigital.com/"
    const val ACJ_API_CLIENT_ID = "7dG8kB4cU2eP9wX3"
    const val ACJ_API_CLIENT_SECRET= "R5mK1oJ8vL3nQ9bA6tV2zS0yH4xE7qU5"
    const val CONNECT_TIMEOUT = 200000L
    const val READ_TIMEOUT = 200000L
    const val GOOGLE_CLOUD_PROJECT_ID = "your-project-id"
    const val GOOGLE_CLOUD_CREDENTIALS_FILE = "google_credentials.json"
    const val MAX_IMAGE_SIZE_MB = 10
    const val IMAGE_MAX_WIDTH = 640
    const val IMAGE_QUALITY = 40

    const val DNI_ASPECT_RATIO = 1.586f // ISO/IEC 7810 ID-1
    const val HORIZONTAL_VIEW_PORTION = 0.75f // 75% del alto
    const val VERTICAL_VIEW_PORTION = 0.90f
}