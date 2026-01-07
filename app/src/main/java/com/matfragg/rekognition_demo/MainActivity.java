package com.matfragg.rekognition_demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private TextView tvStatus;
    private File photo1, photo2;
    private boolean isFirstPhoto = true;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private boolean isComparisonMode = true;
    private String currentSessionId;
    private ProcessCameraProvider cameraProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);

        // LOG DE SEGURIDAD
        if (tvStatus == null) {
            Log.e("CRITICAL_ERROR", "No se encontró tvStatus en el layout!");
        } else {
            Log.d("DEBUG", "tvStatus inicializado correctamente");
        }

        Button btnLiveness = findViewById(R.id.btnLiveness);
        previewView = findViewById(R.id.previewView);
        Button btnCapture = findViewById(R.id.btnCapture);
        Button btnSwitch = findViewById(R.id.btnSwitchCamera);

        btnLiveness.setOnClickListener(v -> startLivenessCheck());
        SwitchMaterial swMode = findViewById(R.id.swMode);

        swMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isComparisonMode = isChecked;
            isFirstPhoto = true;
            photo1 = null;
            photo2 = null;

            if (isComparisonMode) {
                swMode.setText("Modo Comparación (2 Fotos)");
                tvStatus.setText("Listo para comparar rostros");
            } else {
                swMode.setText("Modo Detección (1 Foto)");
                tvStatus.setText("Listo para analizar rostro");
            }
            tvStatus.setTextColor(Color.parseColor("#BB86FC"));
        });

        btnCapture.setOnClickListener(v -> takePhoto());

        btnSwitch.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK)
                    ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;
            startCamera();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        startScanAnimation();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Log.e("CAMERA", "Error al iniciar cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), "photo_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults result) {
                        if (isComparisonMode) {
                            if (isFirstPhoto) {
                                photo1 = photoFile;
                                isFirstPhoto = false;
                                tvStatus.setText("Foto 1 capturada. Capture la Foto 2.");
                                tvStatus.setTextColor(Color.YELLOW);
                            } else {
                                photo2 = photoFile;
                                isFirstPhoto = true;
                                compareFacesBackend(photo1, photo2);
                            }
                        } else {
                            detectFaceBackend(photoFile);
                        }
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) { Log.e("CAM", "Error", e); }
                }
        );
    }

    private void detectFaceBackend(File file) {
        final TextView statusView = this.tvStatus;

        runOnUiThread(() -> {
            if (statusView != null) {
                statusView.setText("Analizando rostro en AWS...");
            }
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                String base64 = Base64.encodeToString(optimizedFileToBytes(file), Base64.NO_WRAP);
                String jsonPayload = "{\"imageBase64\":\"" + base64 + "\"}";

                URL url = new URL("https://aylvctzkitqtanklvszguhkll40fmjqb.lambda-url.us-east-2.on.aws/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.getOutputStream().write(jsonPayload.getBytes());

                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    final String finalResponse = s.hasNext() ? s.next() : "";
                    runOnUiThread(() -> {
                        if (statusView != null) {
                            statusView.setText("Análisis finalizado");
                            showResultDialog(finalResponse);
                        }
                    });
                } else {
                    final int code = conn.getResponseCode();
                    runOnUiThread(() -> {
                        if (statusView != null) statusView.setText("Error AWS: " + code);
                    });
                }
            } catch (Exception e) {
                Log.e("AWS", "Error", e);
                runOnUiThread(() -> tvStatus.setText("Error de conexión"));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void compareFacesBackend(File f1, File f2) {
        runOnUiThread(() -> tvStatus.setText("Procesando comparación en AWS..."));

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                String b1 = Base64.encodeToString(optimizedFileToBytes(f1), Base64.NO_WRAP);
                String b2 = Base64.encodeToString(optimizedFileToBytes(f2), Base64.NO_WRAP);
                String jsonPayload = "{\"sourceImage\":\"" + b1 + "\", \"targetImage\":\"" + b2 + "\"}";

                URL url = new URL("https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.getOutputStream().write(jsonPayload.getBytes());

                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    final String finalResponse = s.hasNext() ? s.next() : "";
                    runOnUiThread(() -> {
                        tvStatus.setText("Comparación finalizada");
                        showResultDialog(finalResponse);
                    });
                } else {
                    final int code = conn.getResponseCode();
                    runOnUiThread(() -> tvStatus.setText("Error AWS: " + code));
                }
            } catch (Exception e) {
                Log.e("AWS", "Error", e);
                runOnUiThread(() -> tvStatus.setText("Error de conexión"));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void showResultDialog(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            StringBuilder msg = new StringBuilder();

            if (json.has("isLive")) {
                boolean isLive = json.getBoolean("isLive");
                msg.append("🛡️ RESULTADO VIDA: ").append(isLive ? "REAL ✅" : "FALSO/SPOOF ❌").append("\n");
                msg.append("🎯 CONFIANZA: ").append(String.format("%.2f", json.getDouble("confidence"))).append("%\n");
                msg.append("📜 ESTADO: ").append(json.getString("status"));
            }
            else if (json.has("mode") && json.getString("mode").equals("compare")) {
                msg.append("📊 SIMILITUD: ").append(json.getString("similarity")).append("\n");
                msg.append("✨ CALIDAD: ").append(json.getString("quality")).append("\n");
                msg.append("🛡️ ESTADO: ").append(json.getString("status"));
            }
            else if (json.has("mode") && json.getString("mode").equals("detect")) {
                msg.append("🎯 SCORE: ").append(json.getString("confidence")).append("%\n");
                msg.append("✨ CALIDAD: ").append(json.getString("quality")).append("\n");
                msg.append("🧬 VIDA: ").append(json.getString("alive")).append("\n");
                if(json.has("emotions")) msg.append("🎭 EMOCIÓN: ").append(json.getString("emotions")).append("\n");
            }
            else {
                msg.append("Resultado: ").append(jsonString);
            }

            new AlertDialog.Builder(this)
                    .setTitle("Resultado AWS")
                    .setMessage(msg.toString())
                    .setPositiveButton("ACEPTAR", (dialog, which) -> {
                        tvStatus.setText("Listo para nueva captura");
                        tvStatus.setTextColor(Color.WHITE);
                        startCamera();
                    })
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            Log.e("UI", "Error JSON", e);
            Toast.makeText(this, "Error procesando datos: " + jsonString, Toast.LENGTH_LONG).show();
        }
    }

    private byte[] optimizedFileToBytes(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        int width = 640;
        int height = (int) (bitmap.getHeight() * (640.0 / bitmap.getWidth()));
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
        return bos.toByteArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startScanAnimation() {
        View scanLine = findViewById(R.id.scanLine);
        if (scanLine == null) return;
        scanLine.animate()
                .translationY(300)
                .setDuration(2000)
                .withEndAction(() -> {
                    scanLine.setTranslationY(-300);
                    startScanAnimation();
                }).start();
    }

    private void startLivenessCheck() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        // Esperamos 300ms para asegurar que el hardware esté libre
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            showCameraSelectionDialog();
        }, 300);
    }
    private void getLivenessResultBackend(String sessionId) {
        runOnUiThread(() -> tvStatus.setText("Verificando resultado de Liveness..."));

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("mode", "get_liveness_results");
                jsonRequest.put("sessionId", sessionId);

                URL url = new URL("https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                conn.getOutputStream().write(jsonRequest.toString().getBytes());

                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";

                    runOnUiThread(() -> {
                        tvStatus.setText("Verificación Completada");
                        showResultDialog(response);
                    });
                } else {
                    final int code = conn.getResponseCode();
                    runOnUiThread(() -> {
                        tvStatus.setText("Error al obtener resultados: " + code);
                        Toast.makeText(MainActivity.this, "Error AWS: " + code, Toast.LENGTH_LONG).show();
                        startCamera();
                    });
                }
            } catch (Exception e) {
                Log.e("LIVENESS", "Error", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void showCameraSelectionDialog() {
        String[] options = {"Cámara Frontal (Recomendada)", "Cámara Trasera (No recomendada)"};

        createLivenessSession("back");
    }

    private void createLivenessSession(String cameraPreference) {
        runOnUiThread(() -> tvStatus.setText("Iniciando sesión segura..."));

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("mode", "create_liveness_session");

                URL url = new URL("https://oj6hcp7azmemswthc5qicp4eom0hbgkl.lambda-url.us-east-2.on.aws/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.getOutputStream().write(jsonRequest.toString().getBytes());

                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "{}";

                    JSONObject jsonResponse = new JSONObject(response);
                    String sessionId = jsonResponse.getString("sessionId");

                    currentSessionId = sessionId;

                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, FaceLivenessActivity.class);
                        intent.putExtra("SESSION_ID", sessionId);
                        intent.putExtra("REGION", "us-east-1");
                        intent.putExtra("CAMERA_PREFERENCE", cameraPreference);
                        startActivityForResult(intent, 999);
                    });
                } else {
                    int codigoError = conn.getResponseCode();

                    InputStream errorStream = conn.getErrorStream();
                    Scanner s = new Scanner(errorStream).useDelimiter("\\A");
                    String errorBody = s.hasNext() ? s.next() : "";

                    Log.e("LIVENESS_ERROR", "Código: " + codigoError + " Cuerpo: " + errorBody);

                    runOnUiThread(() -> {
                        tvStatus.setText("Error Servidor: " + codigoError);
                        startCamera();
                    });
                }
            } catch (Exception e) {
                Log.e("LIVENESS", "Error iniciando sesión", e);
                runOnUiThread(() -> {
                    tvStatus.setText("Error de conexión");
                    startCamera();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (resultCode == RESULT_OK) {
                tvStatus.setText("Video validado. Consultando resultado...");
                getLivenessResultBackend(currentSessionId);
            } else {
                tvStatus.setText("Prueba de vida cancelada.");
                startCamera();
            }
        }
    }
}