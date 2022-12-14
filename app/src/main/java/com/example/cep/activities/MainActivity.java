package com.example.cep.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cep.R;
import com.example.cep.api.RESTService;
import com.example.cep.databinding.ActivityMainBinding;
import com.example.cep.model.CEP;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private final String URL = "https://viacep.com.br/ws/";
    private ActivityMainBinding mBinding;
    private Retrofit retrofitCEP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        mBinding.progressBarCEP.setVisibility(View.GONE);

        retrofitCEP = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mBinding.btnConsultarCEP.setOnClickListener(this);
    }

    private Boolean validarCampos() {

        Boolean status = true;
        String cep = mBinding.txtCEP.getText().toString().trim();

        if (cep.isEmpty()) {
            mBinding.txtCEP.setError("Digite um CEP válido.");
            status = false;
        }

        if (cep.length() != 8) {
            mBinding.txtCEP.setError("O CEP deve possuir 8 dígitos");
            status = false;
        }
        return status;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConsultarCEP:
                if (validarCampos()) {
                    esconderTeclado();
                    consultarCEP();
                }
                break;
        }
    }

    private void esconderTeclado() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void consultarCEP() {
        String sCep = mBinding.txtCEP.getText().toString().trim();

        sCep = sCep.replaceAll("[.-]+", "");

        RESTService restService = retrofitCEP.create(RESTService.class);

        Call<CEP> call = restService.consultarCEP(sCep);

        mBinding.progressBarCEP.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<CEP>() {
            @Override
            public void onResponse(Call<CEP> call, Response<CEP> response) {
                if (response.isSuccessful()) {
                    CEP cep = response.body();
                    mBinding.txtLogradouro.setText("Logadora: \n" + cep.getLogradouro());
                    if (cep.getComplemento().isEmpty()) {
                        mBinding.txtComplemento.setText("Não tem complemento");
                    }else{
                        mBinding.txtComplemento.setText("Complemento: \n" + cep.getComplemento());
                    }
                    mBinding.txtBairro.setText("Bairro: \n" + cep.getBairro());
                    mBinding.txtUF.setText("UF: " + cep.getUf());
                    mBinding.txtLocalidade.setText("Localidade: \n" + cep.getLocalidade());
                    Toast.makeText(getApplicationContext(), "CEP consultado com sucesso", Toast.LENGTH_LONG).show();

                    mBinding.progressBarCEP.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<CEP> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Ocorreu um erro ao tentar consultar o CEP. Erro: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();

                mBinding.progressBarCEP.setVisibility(View.GONE);
            }
        });
    }
}