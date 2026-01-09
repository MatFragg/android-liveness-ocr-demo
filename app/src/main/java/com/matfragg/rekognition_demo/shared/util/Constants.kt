package com.matfragg.rekognition_demo.shared.util

object Constants {
    // AWS LAMBDA URL
    //const val LAMBDA_COMPARE_URL = "https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/"
    //const val LAMBDA_DETECT_URL = "https://aylvctzkitqtanklvszguhkll40fmjqb.lambda-url.us-east-2.on.aws/"
    const val LAMBDA_DETECT_URL = "http://localhost:8081/liveness/"
    const val LAMBDA_COMPARE_URL = "http://localhost:8081/api/face-comparison/"
    const val DNI_OCR_URL= "http://localhost:8081/"
    const val AWS_REGION = "us-east-1"

    const val CONNECT_TIMEOUT = 15000L
    const val READ_TIMEOUT = 20000L
    const val GOOGLE_CLOUD_PROJECT_ID = "your-project-id"
    const val GOOGLE_CLOUD_CREDENTIALS_FILE = "google_credentials.json"
    const val MAX_IMAGE_SIZE_MB = 10
    const val IMAGE_MAX_WIDTH = 640
    const val IMAGE_QUALITY = 40
}