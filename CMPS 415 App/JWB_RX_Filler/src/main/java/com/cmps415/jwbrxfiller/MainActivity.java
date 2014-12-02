package com.cmps415.jwbrxfiller;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText Wnum = (EditText) findViewById(R.id.txtWnum);
        final String storedWnum = Wnum.getText().toString();
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);

        Wnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                btnLogin.setEnabled(charSequence.length() == 7);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PatientReportActivity.class);
                intent.putExtra("Wnum",storedWnum);
                startActivity(intent);
                finish();
            }

        });
    }

}
