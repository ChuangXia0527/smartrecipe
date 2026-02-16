package com.example.smartrecipe.ui.recognize;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.smartrecipe.R;
import com.example.smartrecipe.ai.vision.IngredientClassifier;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RecognizeActivity extends AppCompatActivity {

    private PreviewView previewView;
    private Button btnCapture, btnPick;
    private TextView tvLabels;
    private ImageView ivPicked;

    private ImageCapture imageCapture;
    private IngredientClassifier classifier;

    // 你可以调这个阈值：越高越严格
    private static final float CONF_THRESHOLD = 0.60f;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, "没有相机权限，无法使用拍照识别", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                processGalleryUri(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        btnPick = findViewById(R.id.btnPick);
        tvLabels = findViewById(R.id.tvLabels);
        ivPicked = findViewById(R.id.ivPicked);

        // 初始化 TFLite 分类器（assets 里必须有 model.tflite / labels.txt）
        try {
            classifier = new IngredientClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "模型加载失败：请检查 assets/model.tflite 和 labels.txt", Toast.LENGTH_LONG).show();
        }

        // 相机预览
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

        btnCapture.setOnClickListener(v -> takePhotoAndClassify());
        btnPick.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "相机启动失败", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhotoAndClassify() {
        if (classifier == null) {
            Toast.makeText(this, "模型未就绪", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageCapture == null) {
            Toast.makeText(this, "相机未就绪", Toast.LENGTH_SHORT).show();
            return;
        }

        // 拍照模式隐藏相册预览
        ivPicked.setVisibility(View.GONE);

        File outputFile = new File(getCacheDir(), "capture_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(outputFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri == null) {
                            savedUri = Uri.fromFile(outputFile);
                        }

                        Toast.makeText(RecognizeActivity.this,
                                "拍照成功，正在按相册识别流程处理", Toast.LENGTH_SHORT).show();
                        processGalleryUri(savedUri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(RecognizeActivity.this, "拍照失败：" + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processGalleryUri(@NonNull Uri uri) {
        if (classifier == null) {
            Toast.makeText(this, "模型未就绪", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, s) ->
                        decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            if (bitmap == null) {
                Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show();
                return;
            }

            // 显示预览
            ivPicked.setImageBitmap(bitmap);
            ivPicked.setVisibility(View.VISIBLE);

            // TFLite 分类
            IngredientClassifier.Result r = classifier.classify(bitmap);

            tvLabels.setText("识别结果：\n" + r.toReadable());

            ArrayList<String> ingredients = new ArrayList<>();
            if (r.score >= CONF_THRESHOLD) {
                ingredients.add(r.label); // labels.txt 是中文，这里就是 “番茄”
            } else {
                Toast.makeText(this, "置信度较低，建议换图/补充训练样本", Toast.LENGTH_SHORT).show();
            }

            // 跳到确认页（即使为空也能手动添加）
            Intent it = new Intent(RecognizeActivity.this, RecognizeResultActivity.class);
            it.putStringArrayListExtra("ingredients", ingredients);
            startActivity(it);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "读取图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close();
    }
}
