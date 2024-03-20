package com.abrebo.mlmodelwithandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.abrebo.mlmodelwithandroidapp.databinding.ActivityMainBinding;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    String url="https://student-android-app-pred-810c0241ee1c.herokuapp.com/tah";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(view -> {
            StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject=new JSONObject(response);
                        String prediction = jsonObject.getString("prediction");
                        binding.textViewTahmin.setText(prediction);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String ,String> getParams(){
                    Map<String,String> params=new HashMap<String,String>();
                    params.put("cgpa",binding.editTextCgpa.getText().toString());
                    params.put("iq",binding.editTextIq.getText().toString());
                    params.put("profile_score",binding.editTextProfileScore.getText().toString());
                    return params;
                }
            };
            RequestQueue queue= Volley.newRequestQueue(MainActivity.this);
            queue.add(stringRequest);
        });





    }
}