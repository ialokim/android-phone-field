android-phone-field
===================

![](https://img.shields.io/github/tag/ialokim/android-phone-field.svg?style=flat-square)
![](https://img.shields.io/github/license/ialokim/android-phone-field.svg?style=flat-square)

A small UI library that allows you to create phone fields with corresponding country flags which format and validate the phone number using [libphonenumber](https://github.com/googlei18n/libphonenumber) from google.

![Sample App](raw/phone-field.gif "Sample App")

The library provides two different fields:

 * `PhoneEditText` : includes an `EditText` alongside the flags spinner
 * `PhoneInputLayout` : includes a `TextInputLayout` from the androidx.* (before known as support) library alongside the flags spinner
 
## Features
 
 * Displays the correct country flag if the user enters a valid international phone number, with complete support even for the complex [NANP](https://en.wikipedia.org/wiki/North_American_Numbering_Plan) area codes which all start with `+1`
 * Allows the user to choose the country manually and only enter a national phone number
 * Allows you to choose a default country, which the field will change to automatically when the field is cleared
 * Formats the phone number according to the currently chosen country with whitespaces, dashes and parentheses
 * Validates the phone number, allows to set a custom error
 * Returns the valid phone number including the country code
 * Full internationalization with the countries names in every language provided by Android
 
## Usage

You can easily add the library to your project using [jitpack.io](https://jitpack.io/#ialokim/android-phone-field/):

* If not already present, add jitpack to your root `build.gradle` at the end of `repositories`:

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

* In your module's gradle file add the following dependency

```
dependencies {
    implementation 'com.github.ialokim:android-phone-field:0.2.1'
}
```

 In your layout you can use the `PhoneInputLayout`
 
```xml
<com.github.ialokim.phonefield.PhoneInputLayout
     android:id="@+id/phone_input_layout"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"/>
```
 
 or the `PhoneEditText`
 
```xml
 <com.github.ialokim.phonefield.PhoneEditText
     android:id="@+id/edit_text"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"/>
```

Both support the following xml attributes:

* `hint`: Sets a string as hint displayed when field is empty
* `defaultCountry`: Set the country that should be automatically selected when field is empty. Be sure to use the two letter [ISO 3166](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2) format
* `autoFill`: Whether the international country code should be automatically inserted on picking a country, defaults to `false`
* `autoFormat`: Whether the phone number should be displayed automatically while typing, defaults to `false`

All of these properties can also be set within your Java code. Please refer to the [sample app](sample) for some examples.

## Customization

In case the default style doesn't match your app styles, you can extend the PhoneInputLayout, or PhoneEditText and provide your own xml, but keep in mind that you have to provide a valid xml file with at least an EditText (`tag = phone_edit_text`) and Spinner (`tag = flag_spinner`), otherwise the library will throw an `IllegalStateException`.

You can also create your own custom view by extending the abstract `PhoneField` directly. 

## Countries generation
For better performance and to avoid using json data and then parse it to be used in the library, a simple nodejs is used to convert the `countries.json` file in raw/countries-generator/ into a plain java utility class that has a two-level static list of countries.

The generation script works as follows:
```
node gen.js

or

./gen.js
```

## Motivation

This is probably not the the first library with the same purpose, but this one is different for the following reasons: 
 
 * This library provides two implementations of `PhoneField` using `EditText` and `TextInputLayout`
 * This library allows users to extend the functionality and use custom layouts if needed to match the application theme
 * This library uses a static list of countries generated from the `countries.json` file in the raw resources 
 * This library allows to format phone numbers on the fly
 * This library provides full support even for complicated international phone prefixes
 * This library has full i18n support for every language provided by the Android system

## Attributions  

 1. Inspired by [intl-tel-input for jQuery](https://github.com/jackocnr/intl-tel-input) and [IntlPhoneInput](https://github.com/Rimoto/IntlPhoneInput)
 2. Flag images from [flags](https://www.gosquared.com/resources/flag-icons/)
 3. Original country data from mledoze's [World countries in JSON, CSV and XML](https://github.com/mledoze/countries) which was highly modified and which is then used to generate a plain Java file
 4. Formatting/validation using [libphonenumber](https://github.com/googlei18n/libphonenumber)
 5. Forked from [lamudi-gmbh](/lamudi-gmbh/android-phone-field), to add further features
