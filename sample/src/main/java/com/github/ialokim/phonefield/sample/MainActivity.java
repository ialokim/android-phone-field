package com.github.ialokim.phonefield.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ialokim.phonefield.PhoneEditText;
import com.github.ialokim.phonefield.PhoneInputLayout;

/**
 * MainActivity for the sample app featuring the usage of android-phone-field.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PhoneInputLayout phoneInputLayout =
                (PhoneInputLayout) findViewById(R.id.phone_input_layout);
        final PhoneEditText phoneEditText = (PhoneEditText) findViewById(R.id.edit_text);

        CustomPhoneInputLayout customPhoneInputLayout = new CustomPhoneInputLayout(this, "EG");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        lp.setMargins(0, margin, 0, margin);
        customPhoneInputLayout.setLayoutParams(lp);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        viewGroup.addView(customPhoneInputLayout, 4);


        final Button buttonInputLayout = (Button) findViewById(R.id.submit_button_input_layout);
        final Button buttonEditText = (Button) findViewById(R.id.submit_button_edit_text);

        assert phoneInputLayout != null;
        assert phoneEditText != null;
        assert buttonInputLayout != null;
        assert buttonEditText != null;

        buttonInputLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneInputLayout.isValid()) {
                    phoneInputLayout.setError(null);
                    Toast.makeText(MainActivity.this, R.string.valid_phone_number, Toast.LENGTH_LONG).show();

                } else {
                    phoneInputLayout.setError(getString(R.string.invalid_phone_number));
                }
            }
        });

        buttonEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneEditText.isValid()) {
                    phoneEditText.setError(null);
                    Toast.makeText(MainActivity.this, R.string.valid_phone_number, Toast.LENGTH_LONG).show();

                } else {
                    phoneEditText.setError(getString(R.string.invalid_phone_number));
                }
            }
        });

        phoneEditText.setPhoneNumber("+4917558585858");
        phoneInputLayout.setPhoneNumber("Dies ist ein Test mit 017558585858");
    }
}
