package com.github.ialokim.phonefield;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.IdRes;

/**
 * PhoneField is a custom view for phone numbers with the corresponding country flag, and it uses
 * libphonenumber to validate the phone number.
 * <p>
 * Created by Ismail on 5/6/16.
 */
public abstract class PhoneField extends LinearLayout {

    private Spinner mSpinner;

    private CountriesAdapter mAdapter;

    protected EditText mEditText;

    private Country mCountry;

    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();

    private PhoneNumberFormattingTextWatcher mPhoneNumberFormatterTextWatcher;

    private boolean mAutoFill = false;
    private boolean mAutoFormat = false;
    private int mDefaultCountryPosition = -1;

    /**
     * Instantiates a new Phone field.
     *
     * @param context the context
     */
    public PhoneField(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Phone field.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public PhoneField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Phone field.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public PhoneField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), getLayoutResId(), this);
        updateLayoutAttributes();
        prepareView();
        applyAttrs(attrs);
    }
    /**
     * Prepare view.
     */
    protected void prepareView() {
        mSpinner = (Spinner) findViewWithTag(getResources().getString(R.string.com_lamudi_phonefield_flag_spinner));
        mEditText = (EditText) findViewWithTag(getResources().getString(R.string.com_lamudi_phonefield_edittext));

        if (mSpinner == null || mEditText == null) {
            throw new IllegalStateException("Please provide a valid xml layout");
        }

        List<Country> countries = new ArrayList<>();
        for (List<Country> c : Countries.COUNTRIES.values()) {
            countries.addAll(c);
        }

        mAdapter = new CountriesAdapter(getContext(), countries);
        mAdapter.sort(new Comparator<Country>() {
            @Override
            public int compare(Country c1, Country c2) {
                return c1.getDisplayName().compareToIgnoreCase(c2.getDisplayName());
            }
        });
        mSpinner.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String rawNumber = s.toString();
                if (rawNumber.isEmpty()) {
                    selectDefaultCountry();
                } else {
                    if (rawNumber.startsWith("00")) {
                        rawNumber = rawNumber.replaceFirst("00", "+"); //todo: only valid for Europe??
                        mEditText.removeTextChangedListener(this);
                        mEditText.setText(rawNumber);
                        mEditText.addTextChangedListener(this);
                        mEditText.setSelection(1);
                    }
                    try {
                        Phonenumber.PhoneNumber number = parsePhoneNumber(rawNumber);
                        selectCountry(number.getCountryCode(), number.getNationalNumber());
                    } catch (NumberParseException ignored) {
                    }
                }
            }
        };

        mEditText.addTextChangedListener(textWatcher);

        mSpinner.setAdapter(mAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Country country = mAdapter.getItem(position);
                if (mCountry == null || mCountry.equals(country))
                    return;

                setError(null);
                selectCountry(country);
                String rawInput = getRawInput();
                if (rawInput.startsWith("+") || rawInput.length() == 0) {
                    if (mAutoFill) {
                        String dialCode = mCountry.getDialCode(true);
                        mEditText.setText(dialCode);
                        mEditText.setSelection(dialCode.length());
                    } else {
                        mEditText.removeTextChangedListener(textWatcher);
                        mEditText.setText("");
                        mEditText.addTextChangedListener(textWatcher);
                    }
                } else if (mAutoFormat) {
                    mEditText.removeTextChangedListener(textWatcher);
                    mEditText.setText(rawInput);
                    mEditText.setSelection(mEditText.getText().length());
                    mEditText.addTextChangedListener(textWatcher);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCountry = null;
            }
        });

    }

    public void applyAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PhoneField);
        @IdRes int hint = ta.getResourceId(R.styleable.PhoneField_hint, -1);
        String defaultCountry = ta.getString(R.styleable.PhoneField_defaultCountry);
        boolean autoFill = ta.getBoolean(R.styleable.PhoneField_autoFill, false);
        boolean autoFormat = ta.getBoolean(R.styleable.PhoneField_autoFormat, false);
        if (hint != -1)
            setHint(hint);
        if (defaultCountry != null)
            setDefaultCountry(defaultCountry);
        if (autoFill)
            setAutoFill(autoFill);
        if (autoFormat)
            setAutoFormat(autoFormat);
        ta.recycle();
        selectDefaultCountry();
    }

    private Phonenumber.PhoneNumber parsePhoneNumber(String number) throws NumberParseException {
        String defaultRegion = mCountry != null ? mCountry.getCode().toUpperCase() : "";
        return mPhoneUtil.parseAndKeepRawInput(number, defaultRegion);
    }

    private void selectCountry(int dialCode, long number) {
        List<Country> l = Countries.COUNTRIES.get(dialCode);
        if (l == null)
            return;
        for (Country country : l) {
            if (country.containsNumber(number)) {
                selectCountry(country);
                return;
            }
        }
    }

    private void selectCountry(Country country) {
        mCountry = country;
        if (mAutoFormat)
            mPhoneNumberFormatterTextWatcher.setCountry(mCountry.getCode());
        mSpinner.setSelection(mAdapter.getPosition(mCountry));
    }

    private void selectDefaultCountry() {
        if (mDefaultCountryPosition != -1) {
            selectCountry(mAdapter.getItem(mDefaultCountryPosition));
        }
    }

    private void hideKeyboard() {
        ((InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }


    /**
     * Gets spinner.
     *
     * @return the spinner
     */
    public Spinner getSpinner() {
        return mSpinner;
    }

    /**
     * Gets edit text.
     *
     * @return the edit text
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * Checks whether the entered phone number is valid or not.
     *
     * @return a boolean that indicates whether the number is of a valid pattern
     */
    public boolean isValid() {
        try {
            return mPhoneUtil.isValidNumber(parsePhoneNumber(getRawInput()));
        } catch (NumberParseException e) {
            return false;
        }
    }

    /**
     * Gets phone number formatted as E164, whenever possible.
     *
     * @return the phone number or {@code null} if it could not be parsed
     */
    public String getE164PhoneNumber() {
        try {
            Phonenumber.PhoneNumber number = parsePhoneNumber(getRawInput());
            return mPhoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException ignored) {
        }
        return null;
    }

    /**
     * Sets default country.
     *
     * @param countryCode the country code
     */
    public void setDefaultCountry(String countryCode) {
        for (List<Country> countries : Countries.COUNTRIES.values()) {
            for (Country country : countries) {
                if (country.getCode().equalsIgnoreCase(countryCode)) {
                    mDefaultCountryPosition = mAdapter.getPosition(country);
                }
            }
        }
    }

    /**
     * Sets phone number.
     *
     * @param rawNumber the raw number
     */
    public void setPhoneNumber(String rawNumber) {
        try {
            Phonenumber.PhoneNumber number = parsePhoneNumber(rawNumber);
            selectCountry(number.getCountryCode(), number.getNationalNumber());
            mEditText.setText(mPhoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
        } catch (NumberParseException ignored) {
        }
    }

    /**
     * Update layout attributes.
     */
    protected abstract void updateLayoutAttributes();

    /**
     * Gets layout res id.
     *
     * @return the layout res id
     */
    public abstract int getLayoutResId();

    /**
     * Sets hint.
     *
     * @param resId the res id
     */
    public abstract void setHint(int resId);

    /**
     * Sets the autofill property.
     *
     * @param autoFill whether the dialCode should be inserted automatically when changing the country
     */
    public void setAutoFill(boolean autoFill) {
        mAutoFill = autoFill;
    }

    /**
     * Sets the autoformat property.
     *
     * @param autoFormat whether the dialCode should be formatted automatically
     */
    public void setAutoFormat(boolean autoFormat) {
        mAutoFormat = autoFormat;
        if (mAutoFormat) {
            mPhoneNumberFormatterTextWatcher = new PhoneNumberFormattingTextWatcher();
            mEditText.addTextChangedListener(mPhoneNumberFormatterTextWatcher);
        }
    }

    /**
     * Gets raw input.
     *
     * @return the raw input
     */
    public String getRawInput() {
        return mAutoFormat ? mPhoneNumberFormatterTextWatcher.getRawPhoneNumber() : mEditText.getText().toString();
    }

    /**
     * Sets error.
     *
     * @param error the error
     */
    public abstract void setError(String error);

}
