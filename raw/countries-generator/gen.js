#!/usr/bin/env node

'use strict';

var _ = require("lodash"),
	_s = require("underscore.string"),
	fs = require("fs"),
	countries = require('./countries');

/**
 * Orders countries by dialCode (e.g. all NANP (North American Numbering Plan) under 1)
 * Also, checks for dialCodes with zero or more priority countries set.
 * 
 * @returns object with key (dialCode) and value (array of country objects), if no errors were found
 * @returns null, if errors were found
 */
function orderCountries(countries) {
    var orderedCountries = {}

    countries.forEach(function(country) {
        if (!orderedCountries[country.dialCode]) {
            orderedCountries[country.dialCode] = []
        }
        orderedCountries[country.dialCode].push(country);
    });

    var error = false;

    Object.keys(orderedCountries).forEach(function(code) {
        var found = -1;
        if (orderedCountries[code].length === 1)
            return;
        
        orderedCountries[code].forEach(function(country, idx) {
            if (country.priority === 1) {
                if (found !== -1) {
                    console.error("Two countries with priority 1 for +" + code);
                    error = true;
                }
                found = idx;
            }
        });
        if (found === -1) {
            console.error("No country with priority 1 for +" + code);
            error = true;
        } else {
            //put country with priority = 1 at the end to prevent later find in Java before other countries were checked
            var c = orderedCountries[code].splice(found,1);
            orderedCountries[code].push(c[0]);
        }
    });

    if (error)
        return null;

    return orderedCountries;
}

/*
 * Generates Countries.java
 * 
 * Expectes its parameter to be an object with key (dialCode) and value (array of country objects)
 */
function generateCountriesClass(orderedCountries) {
    /*
     * Returns the given string (l) with prepanded spaces as defined by (indent)
     */
    var indentize = function (l, indent) {
        return (indent ? _s.repeat(" ", indent) : "") + (l||"");
    };

    var generatedClass = "";
    /*
     * Adds a string to the generated class.
     */
    var a = function (l, indent) {
        generatedClass += indentize(l, indent) + "\n";
    }

    /*
     * Generates the country initializer code.
     */
    var generateCountry = function (country, offset) {
        // country priority is expressed as boolean in Java
        var start = "new Country(\""+country.iso2+"\", "+country.dialCode+", "+(country.priority===0 ? "false" : "true")
        var end = ")"
        // only use extended constructor if areaCodes available
        if (country.areaCodes !== null) {
            var areaCodesStart = ", "
            var areaCodesEnd = ")"
            if (country.areaCodes.length == 1) {
                // Collections.singletonList is a bit faster than Arrays.asList for one item
                return start+areaCodesStart+"Collections.singletonList(\""+country.areaCodes[0]+"\""+areaCodesEnd+end;
            } else {
                var generatedCountry = start+areaCodesStart+"Arrays.asList(\n";
                country.areaCodes.forEach(function(code, idx) {
                    //comma for all lines except the last one
                    generatedCountry += indentize("\""+code+"\"" + ((idx==country.areaCodes.length-1) ? "" : ",")+"\n",offset+8);
                });
                generatedCountry += indentize(areaCodesEnd+end,offset+4);
                return generatedCountry;
            }
        } else {
            return start+end;
        }
    }

    a("package com.github.ialokim.phonefield;");
    a();
    a("import java.util.Arrays;")
    a("import java.util.Collections;")
    a("import java.util.HashMap;");
    a("import java.util.List;");
    a("import java.util.Map;");
    a();
    a("public final class Countries {");
    a();
    a("public static final Map<Integer,List<Country>> COUNTRIES = new HashMap<>();",4);
    a("static {",4);
    Object.keys(orderedCountries).forEach(function(dialCode) {
        var start = "COUNTRIES.put("+dialCode+", ";
        var end = "));";

        if (orderedCountries[dialCode].length === 1) {
            var c = generateCountry(orderedCountries[dialCode][0]);
            // Collections.singletonList is a bit faster than Arrays.asList for one item
            a(start+"Collections.singletonList("+c+end, 8);
        } else {
            a(start+"Arrays.asList(",8);
            orderedCountries[dialCode].forEach(function(country,idx) {
                //comma for all lines except the last one
                var c = generateCountry(country, 8) + ((idx==orderedCountries[dialCode].length-1) ? "" : ",");
                a(c,12);
            });
            a(end,8);
        }
	});
	a("}",4);
    a();
    a("}");

    return generatedClass;
}



var ordered = orderCountries(countries);
if (ordered === null)
    return;
var generated = generateCountriesClass(ordered);
fs.writeFileSync("Countries.java", generated);
