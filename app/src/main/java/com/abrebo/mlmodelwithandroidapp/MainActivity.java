package com.abrebo.mlmodelwithandroidapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.abrebo.mlmodelwithandroidapp.databinding.ActivityMainBinding;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Interpreter tflite;
    String modelFile = "converted_model.tflite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TensorFlow Lite modelini yükleme
        try {
            tflite = new Interpreter(loadModelFile(this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tahmin butonuna tıklama olayı
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kullanıcı girdilerini al
                float cgpa = Float.parseFloat(binding.editTextCgpa.getText().toString());
                float iq = Float.parseFloat(binding.editTextIq.getText().toString());
                float profile_score = Float.parseFloat(binding.editTextProfileScore.getText().toString());

                // Giriş verisini modele uygun formata getir
                float[][] input = {{cgpa, iq, profile_score}}; // Örnek giriş verisi, modelinize göre değişebilir

                // Modeli çalıştır ve sonucu al
                float[][] output = runModel(input);

                // Sonucu kullanma
                float result = output[0][0]; // Örnek çıktıyı alın, modelinize göre çıktıyı işleyebilirsiniz

                // Sonucu gösterme
                // Sonucu yuvarlayarak gösterme
                float roundedResult = Math.round(result);
                String resultMessage = "Tahmin sonucu: " + roundedResult;
                Toast.makeText(MainActivity.this, resultMessage, Toast.LENGTH_SHORT).show();
                binding.textViewTahmin.setText(resultMessage);
            }
        });
    }

    // Modeli yükleme
    private MappedByteBuffer loadModelFile(Activity activity, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Modeli çalıştırma
    private float[][] runModel(float[][] input) {
        // Giriş verisini modelinize uygun hale getirme
        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{input.length, input[0].length}, DataType.FLOAT32);

        // Dönüşümü gerçekleştirme
        float[] flatInput = new float[input.length * input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                flatInput[i * input[i].length + j] = input[i][j];
            }
        }

        // Tek boyutlu diziyi TensorBuffer'a yükleme
        inputBuffer.loadArray(flatInput);

        // Modeli çalıştırma
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{input.length, /* çıktı boyutu */}, DataType.FLOAT32);
        tflite.run(inputBuffer.getBuffer(), outputBuffer.getBuffer());

        // Çıktıyı alarak işleme
        float[][] output = new float[][]{outputBuffer.getFloatArray()};
        return output;
    }
}
