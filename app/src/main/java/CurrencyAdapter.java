package com.example.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrencyAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] currencies;

    public CurrencyAdapter(Context context, String[] currencies) {
        super(context, R.layout.spinner_item, currencies);
        this.context = context;
        this.currencies = currencies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        TextView currencyTextView = convertView.findViewById(R.id.currencyTextView);
        ImageView flagImageView = convertView.findViewById(R.id.flagImageView);

        String currency = currencies[position];
        currencyTextView.setText(currency);

        int flagResourceId = context.getResources().getIdentifier(currency.toLowerCase(), "drawable", context.getPackageName());
        if (flagResourceId != 0) {
            flagImageView.setImageResource(flagResourceId);
        } else {
            flagImageView.setImageResource(R.drawable.flag_placeholder); // Ensure this placeholder exists
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
