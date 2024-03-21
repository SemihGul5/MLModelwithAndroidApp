package com.abrebo.mlmodelwithandroidapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.abrebo.mlmodelwithandroidapp.databinding.ActivityMainBinding;
import com.abrebo.mlmodelwithandroidapp.ml.ConvertedModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ConvertedModel model;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

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

    // Modeli yükleme ve çalıştırma
    private float[][] runModel(float[][] input) {
        try {
            // Modeli yükleme
            model = ConvertedModel.newInstance(context);

            // Giriş verisini TensorBuffer'a dönüştürme
            int inputSize = input.length * input[0].length;
            float[] flatInput = new float[inputSize];
            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input[i].length; j++) {
                    flatInput[i * input[i].length + j] = input[i][j];
                }
            }
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, inputSize}, DataType.FLOAT32);
            inputFeature0.loadArray(flatInput);

            // Modeli çalıştırma
            ConvertedModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Çıktıyı işleme ve geri döndürme
            float[][] output = new float[][]{outputFeature0.getFloatArray()};
            return output;

        } catch (IOException e) {
            // Model yükleme hatası durumunda uyarı gösterme
            e.printStackTrace();
            Toast.makeText(this, "Model yüklenirken bir hata oluştu", Toast.LENGTH_SHORT).show();
            return null;
        }
        finally {
            // Modeli kapatma
            if (model != null) {
                model.close();
            }
        }
    }
}