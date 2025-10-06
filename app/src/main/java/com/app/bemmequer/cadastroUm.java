package com.app.bemmequer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class cadastroUm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_cadastro_um);   //Essa parte abaixo do código precisa ter o nome da minha tela atual?
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cadastroUm), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Variável Local
        Button btn_proximo = findViewById(R.id.btn_proximo);
        btn_proximo.setOnClickListener(view -> startActivity(new Intent(this, cadastroDois.class)));
    }
}