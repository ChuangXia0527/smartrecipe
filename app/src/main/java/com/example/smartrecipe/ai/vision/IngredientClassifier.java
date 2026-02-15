package com.example.smartrecipe.ai.vision;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngredientClassifier {

    private final Interpreter tflite;
    private final List<String> labels;

    // Teachable Machine 常见输入尺寸：224
    private static final int IMG_SIZE = 224;

    // 常见归一化到 [-1, 1]
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    public IngredientClassifier(Context context) throws IOException {
        tflite = new Interpreter(loadModelFile(context, "model.tflite"));
        labels = loadLabels(context, "labels.txt");
    }

    public Result classify(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);

        ByteBuffer input = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3);
        input.order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMG_SIZE * IMG_SIZE];
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

        int idx = 0;
        for (int i = 0; i < IMG_SIZE * IMG_SIZE; i++) {
            int p = pixels[idx++];

            float r = ((p >> 16) & 0xFF);
            float g = ((p >> 8) & 0xFF);
            float b = (p & 0xFF);

            input.putFloat((r - IMAGE_MEAN) / IMAGE_STD);
            input.putFloat((g - IMAGE_MEAN) / IMAGE_STD);
            input.putFloat((b - IMAGE_MEAN) / IMAGE_STD);
        }

        float[][] output = new float[1][labels.size()];
        tflite.run(input, output);

        int bestIdx = 0;
        float bestScore = output[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > bestScore) {
                bestScore = output[0][i];
                bestIdx = i;
            }
        }

        return new Result(labels.get(bestIdx), bestScore);
    }

    public void close() {
        tflite.close();
    }

    public static class Result {
        public final String label; // 中文食材名（来自 labels.txt）
        public final float score;  // 0~1
        public Result(String label, float score) {
            this.label = label;
            this.score = score;
        }
        public String toReadable() {
            return String.format(Locale.ROOT, "%s (%.2f)", label, score);
        }
    }

    private static MappedByteBuffer loadModelFile(Context context, String assetName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assetName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private static List<String> loadLabels(Context context, String assetName) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(assetName)));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) list.add(line);
        }
        br.close();
        return list;
    }
}
