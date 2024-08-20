package com.example.currencyconverter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private EditText amountField;
    private Spinner fromCurrencyBox;
    private Spinner toCurrencyBox;
    private TextView resultLabel;
    private Button convertButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        amountField = findViewById(R.id.amountField);
        fromCurrencyBox = findViewById(R.id.fromCurrencyBox);
        toCurrencyBox = findViewById(R.id.toCurrencyBox);
        resultLabel = findViewById(R.id.resultLabel);
        convertButton = findViewById(R.id.convertButton);

        // Set up the currency adapter
        String[] currencies = getResources().getStringArray(R.array.currency_array);
        CurrencyAdapter adapter = new CurrencyAdapter(this, currencies);
        fromCurrencyBox.setAdapter(adapter);
        toCurrencyBox.setAdapter(adapter);

        fromCurrencyBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update any required UI based on selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        toCurrencyBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update any required UI based on selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });
    }

    private void performConversion() {
        String amountString = amountField.getText().toString();
        if (amountString.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String fromCurrency = (String) fromCurrencyBox.getSelectedItem();
        String toCurrency = (String) toCurrencyBox.getSelectedItem();

        // Hide the keyboard
        hideKeyboard();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Double> future = executor.submit(() -> {
            try {
                return convert(amount, fromCurrency, toCurrency);
            } catch (IOException e) {
                Log.e("CurrencyConverter", "Conversion error", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error in conversion", Toast.LENGTH_LONG).show());
                return null;
            }
        });

        executor.execute(() -> {
            try {
                Double result = future.get();
                runOnUiThread(() -> {
                    if (result != null) {
                        resultLabel.setText(String.format("%.2f %s = %.2f %s", amount, fromCurrency, result, toCurrency));
                        resultLabel.setVisibility(View.VISIBLE);
                    } else {
                        resultLabel.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e("CurrencyConverter", "Error getting result", e);
            } finally {
                executor.shutdown();
            }
        });
    }

    private double convert(double amount, String fromCurrency, String toCurrency) throws IOException {
        String apiKey = "4aa8de0236afc0dbdbda5197";
        String urlString = String.format("https://v6.exchangerate-api.com/v6/%s/latest/%s", apiKey, fromCurrency);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            Log.d("CurrencyConverter", "API Response: " + response.toString());

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject rates = jsonResponse.getJSONObject("conversion_rates");

            double conversionRate = rates.getDouble(toCurrency);
            Log.d("CurrencyConverter", "Conversion Rate: " + conversionRate);

            return amount * conversionRate;
        } catch (JSONException e) {
            Log.e("CurrencyConverter", "JSON parsing error", e);
            throw new IOException("Failed to parse JSON response", e);
        } finally {
            connection.disconnect();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = this.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
