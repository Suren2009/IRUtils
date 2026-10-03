package com.example.inflector;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inflector.util.EnglishInflector;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText queryInput;
    private TextView tokensText;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryInput = findViewById(R.id.queryInput);
        tokensText = findViewById(R.id.tokensText);
        resultText = findViewById(R.id.resultText);
        Button convertButton = findViewById(R.id.convertButton);

        convertButton.setOnClickListener(v -> showResults());
        queryInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                showResults();
                return true;
            }
            return false;
        });
        queryInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                showResults();
            }
        });
    }

    private void showResults() {
        String query = queryInput.getText() == null ? "" : queryInput.getText().toString();
        List<EnglishInflector.WordForms> formsList = EnglishInflector.getFormsForText(query);
        if (formsList.isEmpty()) {
            tokensText.setText("");
            resultText.setText(R.string.empty_result);
            return;
        }

        Set<String> tokens = EnglishInflector.getTokens(query);
        tokensText.setText(getString(R.string.tokens_label, TextUtils.join(", ", tokens)));

        StringBuilder sb = new StringBuilder();
        for (EnglishInflector.WordForms forms : formsList) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(getString(R.string.row_format, forms.original, forms.singular, forms.plural,
                    forms.uncountable ? getString(R.string.uncountable_suffix) : ""));
        }
        resultText.setText(sb.toString());
    }
}
