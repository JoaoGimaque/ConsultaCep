package com.example.cep.activities;

import static com.example.cep.utils.utils.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.cep.R;
import com.example.cep.api.RESTService;
import com.example.cep.databinding.ActivityMainBinding;
import com.example.cep.helper.Permissoes;
import com.example.cep.model.CEP;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding mBinding;
    private Retrofit retrofitCEP;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());
        mBinding.progressBarCEP.setVisibility(View.GONE);

        retroHTTP();

        Permissoes.validarPermissoes(permissoes,this,1);

        mBinding.btnConsultarCEP.setOnClickListener(this);
    }

    private void retroHTTP(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request req = chain.request()
                        .newBuilder()
                        .build();
                return chain.proceed(req);
            }
        });

        retrofitCEP = new Retrofit.Builder()
                .baseUrl(URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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
                    if (cep.getLogradouro() != null) {
                        mBinding.txtLogradouro.setText("Logadora: \n" + cep.getLogradouro());
                        if (cep.getComplemento().isEmpty()) {
                            mBinding.txtComplemento.setText("Não tem complemento");
                        } else {
                            mBinding.txtComplemento.setText("Complemento: \n" + cep.getComplemento());
                        }
                        mBinding.txtBairro.setText("Bairro: \n" + cep.getBairro());
                        mBinding.txtUF.setText("UF: " + cep.getUf());
                        mBinding.txtLocalidade.setText("Localidade: \n" + cep.getLocalidade());
                        Toast.makeText(getApplicationContext(), "CEP consultado com sucesso", Toast.LENGTH_LONG).show();

                        mBinding.progressBarCEP.setVisibility(View.GONE);
                    } else {
                        mBinding.txtCEP.setError("Digite um CEP valido");
                        mBinding.progressBarCEP.setVisibility(View.GONE);
                    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults){
            if (permissaoResultado== PackageManager.PERMISSION_DENIED){

                alertaValidacaoPermissao();

            }
        }
    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}