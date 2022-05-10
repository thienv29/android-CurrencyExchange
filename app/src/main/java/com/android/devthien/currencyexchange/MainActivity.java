package com.android.devthien.currencyexchange;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.devthien.currencyexchange.apis.ApiServices;
import com.android.devthien.currencyexchange.core.Constant;
import com.android.devthien.currencyexchange.core.Loading;
import com.android.devthien.currencyexchange.core.XMLDOMParser;
import com.android.devthien.currencyexchange.databinding.ActivityMainBinding;
import com.android.devthien.currencyexchange.models.NationModel;
import com.android.devthien.currencyexchange.models.ResponseModel;
import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Loading loading;

    private List<NationModel> nationModelList;
    private List<String> nationString;
    private Spinner codeFrom;
    private Spinner codeTo;
    private EditText edtNum1;
    private EditText edtNum2;
    private Button buttonSwap;
    private ArrayAdapter arrayAdapter;
    private double valueFrom = 1, valueTo = 1;

    Gson gson = new Gson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    private void init(){
        nationModelList = new ArrayList<>();

        codeFrom = findViewById(R.id.codeFrom);
        codeTo = findViewById(R.id.codeTo);
        buttonSwap = findViewById(R.id.buttonSwap);
        edtNum1 = findViewById(R.id.edtNum1);
        edtNum2 = findViewById(R.id.edtNum2);

        edtNum1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String b1 = String.valueOf(edtNum1.getText());
                if (!b1.isEmpty()){
                    Double s = Double.parseDouble(String.valueOf(edtNum1.getText()));
                    edtNum2.setText(String.valueOf(valueTo*s));
                }else
                {
                    edtNum2.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int spinnerPositiona = codeTo.getSelectedItemPosition();
                codeTo.setSelection(codeFrom.getSelectedItemPosition());
                codeFrom.setSelection(spinnerPositiona);
                changeCodeCountry();
                edtNum1.setText("");
                edtNum2.setText("");
            }
        });
        codeFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeCodeCountry();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        codeTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeCodeCountry();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loading = new Loading(MainActivity.this);
        getApi();


    }
    private void changeCodeCountry(){
        String c1 = codeFrom.getSelectedItem().toString().split(" ")[0].toLowerCase(Locale.ROOT);
        String c2 = codeTo.getSelectedItem().toString().split(" ")[0].toLowerCase(Locale.ROOT);
        if (c1 != c2){
            new ReadRss().execute("https://"+c1+".fxexchangerate.com/"+c2+".xml");
        }
        else{
            Toast.makeText(MainActivity.this,"Vui lòng chọn khác quốc gia",Toast.LENGTH_LONG).show();
            codeFrom.setSelection(codeTo.getSelectedItemPosition() == nationModelList.size() ? codeTo.getSelectedItemPosition()-1 : codeTo.getSelectedItemPosition() + 1);
            String c1s = codeFrom.getSelectedItem().toString().split(" ")[0].toLowerCase(Locale.ROOT);
            String c2s = codeTo.getSelectedItem().toString().split(" ")[0].toLowerCase(Locale.ROOT);
            new ReadRss().execute("https://"+c1s+".fxexchangerate.com/"+c2s+".xml");
        }
    }
    private void convertNationListTofull(){
        for (NationModel s: nationModelList) {
            s.setUrlFlag(Constant.API_FLAG_URL+s.getCountryCode().toLowerCase(Locale.ROOT)+".gif");
            s.setUrlMap(Constant.API_MAP_URL+s.getCountryCode()+".png");
        }
    }
    private void getApi(){
        loading.startLoading();
        //link api: countryInfoJSON?formatted=true&lang=it&username=thienv29&style=full&fbclid=IwAR0-M-iwVsSj0OZwULO9428GiGzxgAoZGb0oyiQLiFFLpjRtoNC0LR7jloI
        ApiServices.apiServices.getListNation("true","it","thienv29","full","IwAR0-M-iwVsSj0OZwULO9428GiGzxgAoZGb0oyiQLiFFLpjRtoNC0LR7jloI").enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel nationModels = response.body();
                nationModelList = nationModels.geonames;
                convertNationListTofull();

                Log.d(TAG, "onResponse: "+gson.toJson(nationModelList));
                Toast.makeText(MainActivity.this, "call api success", Toast.LENGTH_SHORT).show();

                toListStringNation();
                arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, nationString);
                codeFrom.setAdapter(arrayAdapter);
                codeTo.setAdapter(arrayAdapter);
                codeTo.setSelection(2);
                loading.dismissDialog();

            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(MainActivity.this, "call api error", Toast.LENGTH_LONG).show();
                loading.dismissDialog();
            }
        });
    }
    private void toListStringNation(){
        nationString = new ArrayList<>();
        for (NationModel nation: nationModelList) {
            String s = nation.getCurrencyCode()+" ("+nation.getCountryName()+")";
            nationString.add(s);
        }
    }
    private class ReadRss extends AsyncTask<String,Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                InputStreamReader inputStreamReader = new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line ="";
                while ((line = bufferedReader.readLine())!= null){
                    content.append(line);
                }
                bufferedReader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            XMLDOMParser parser = new XMLDOMParser();
            Document document = parser.getDocument(s);
            NodeList nodeList = document.getElementsByTagName("item");
            String tygia = "";
            for (int i = 0; i < 1; i++) {
                Element element = (Element) nodeList.item(i);
                NodeList DescriptionNode = element.getElementsByTagName("description");
                Element DescriptionEle = (Element) DescriptionNode.item(i);
                tygia = Html.fromHtml(DescriptionEle.getFirstChild().getNodeValue().trim()).toString();
                Log.e("tygia", tygia);
            }
            String[] arr = tygia.split("\n");
            String currency = arr[0];

            String[] arrcurency = currency.split("=");
            valueFrom = Double.parseDouble(arrcurency[0].trim().split(" ")[0]);
            valueTo = Double.parseDouble(arrcurency[1].trim().split(" ")[0]);
            Log.d(TAG, "onPostExecute: "+valueFrom);
            Log.d(TAG, "onPostExecute: "+valueTo);
            loading.dismissDialog();


        }

    }

}