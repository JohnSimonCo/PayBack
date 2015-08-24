package com.johnsimon.payback.currency;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtils {

    /*
        This two dimensional string[] contains all ISO 4217
        currencies ( http://www.xe.com/iso4217.php ) and
        for those that have it, their symbols
        ( http://www.xe.com/symbols.php ). To generate a new
        code paste the following into http://www.xe.com/symbols.php:



                var list = [[ "AED", "" ],[ "AFN", "" ],[ "ALL", "" ],[ "AMD", "" ],[ "ANG", "" ],[ "AOA", "" ],[ "ARS", "" ],[ "AUD", "" ],[ "AWG", "" ],[ "AZN", "" ],[ "BAM", "" ],[ "BBD", "" ],[ "BDT", "" ],[ "BGN", "" ],[ "BHD", "" ],[ "BIF", "" ],[ "BMD", "" ],[ "BND", "" ],[ "BOB", "" ],[ "BRL", "" ],[ "BSD", "" ],[ "BTN", "" ],[ "BWP", "" ],[ "BYR", "" ],[ "BZD", "" ],[ "CAD", "" ],[ "CDF", "" ],[ "CHF", "" ],[ "CLP", "" ],[ "CNY", "" ],[ "COP", "" ],[ "CRC", "" ],[ "CUC", "" ],[ "CUP", "" ],[ "CVE", "" ],[ "CZK", "" ],[ "DJF", "" ],[ "DKK", "" ],[ "DOP", "" ],[ "DZD", "" ],[ "EGP", "" ],[ "ERN", "" ],[ "ETB", "" ],[ "EUR", "" ],[ "FJD", "" ],[ "FKP", "" ],[ "GBP", "" ],[ "GEL", "" ],[ "GGP", "" ],[ "GHS", "" ],[ "GIP", "" ],[ "GMD", "" ],[ "GNF", "" ],[ "GTQ", "" ],[ "GYD", "" ],[ "HKD", "" ],[ "HNL", "" ],[ "HRK", "" ],[ "HTG", "" ],[ "HUF", "" ],[ "IDR", "" ],[ "ILS", "" ],[ "IMP", "" ],[ "INR", "" ],[ "IQD", "" ],[ "IRR", "" ],[ "ISK", "" ],[ "JEP", "" ],[ "JMD", "" ],[ "JOD", "" ],[ "JPY", "" ],[ "KES", "" ],[ "KGS", "" ],[ "KHR", "" ],[ "KMF", "" ],[ "KPW", "" ],[ "KRW", "" ],[ "KWD", "" ],[ "KYD", "" ],[ "KZT", "" ],[ "LAK", "" ],[ "LBP", "" ],[ "LKR", "" ],[ "LRD", "" ],[ "LSL", "" ],[ "LYD", "" ],[ "MAD", "" ],[ "MDL", "" ],[ "MGA", "" ],[ "MKD", "" ],[ "MMK", "" ],[ "MNT", "" ],[ "MOP", "" ],[ "MRO", "" ],[ "MUR", "" ],[ "MVR", "" ],[ "MWK", "" ],[ "MXN", "" ],[ "MYR", "" ],[ "MZN", "" ],[ "NAD", "" ],[ "NGN", "" ],[ "NIO", "" ],[ "NOK", "" ],[ "NPR", "" ],[ "NZD", "" ],[ "OMR", "" ],[ "PAB", "" ],[ "PEN", "" ],[ "PGK", "" ],[ "PHP", "" ],[ "PKR", "" ],[ "PLN", "" ],[ "PYG", "" ],[ "QAR", "" ],[ "RON", "" ],[ "RSD", "" ],[ "RUB", "" ],[ "RWF", "" ],[ "SAR", "" ],[ "SBD", "" ],[ "SCR", "" ],[ "SDG", "" ],[ "SEK", "" ],[ "SGD", "" ],[ "SHP", "" ],[ "SLL", "" ],[ "SOS", "" ],[ "SPL", "" ],[ "SRD", "" ],[ "STD", "" ],[ "SVC", "" ],[ "SYP", "" ],[ "SZL", "" ],[ "THB", "" ],[ "TJS", "" ],[ "TMT", "" ],[ "TND", "" ],[ "TOP", "" ],[ "TRY", "" ],[ "TTD", "" ],[ "TVD", "" ],[ "TWD", "" ],[ "TZS", "" ],[ "UAH", "" ],[ "UGX", "" ],[ "USD", "" ],[ "UYU", "" ],[ "UZS", "" ],[ "VEF", "" ],[ "VND", "" ],[ "VUV", "" ],[ "WST", "" ],[ "XAF", "" ],[ "XCD", "" ],[ "XDR", "" ],[ "XOF", "" ],[ "XPF", "" ],[ "YER", "" ],[ "ZAR", "" ],[ "ZMW", "" ],[ "ZWD", "" ]];

                for (var a = 0; a < list.length; a++) {
                    for (var i = 0; i < 115; i++) {
                        if (document.querySelector('#contentL > div.module.clearfix.iTools > div.cSmbl_bx > table > tbody > tr:nth-child(' + (i + 2) + ') > td:nth-child(2)').innerHTML == list[a][0]) {
                            list[a][1] = document.querySelector('#contentL > div.module.clearfix.iTools > div.cSmbl_bx > table > tbody > tr:nth-child(' + (i + 2) + ') > td:nth-child(4)').innerHTML;
                        }
                    }
                }

                var res = ''

                for (var i = 0; i < list.length; i++) {
                    if (list[i][1] == '') {
                        list[i][1] = list[i][0];
                    }

                    res += '{ ' + '\"' + list[i][0] + '\", \"' + list[i][1] + '\" },\n'

                }

                console.log(res);

     */
    public final static String[][] list = new String[][] {
            { "AED", "AED" },
            { "AFN", "؋" },
            { "ALL", "Lek" },
            { "AMD", "AMD" },
            { "ANG", "ƒ" },
            { "AOA", "AOA" },
            { "ARS", "$" },
            { "AUD", "$" },
            { "AWG", "ƒ" },
            { "AZN", "ман" },
            { "BAM", "KM" },
            { "BBD", "$" },
            { "BDT", "BDT" },
            { "BGN", "лв" },
            { "BHD", "BHD" },
            { "BIF", "BIF" },
            { "BMD", "$" },
            { "BND", "$" },
            { "BOB", "$b" },
            { "BRL", "R$" },
            { "BSD", "$" },
            { "BTN", "BTN" },
            { "BWP", "P" },
            { "BYR", "p." },
            { "BZD", "BZ$" },
            { "CAD", "$" },
            { "CDF", "CDF" },
            { "CHF", "CHF" },
            { "CLP", "$" },
            { "CNY", "¥" },
            { "COP", "$" },
            { "CRC", "₡" },
            { "CUC", "CUC" },
            { "CUP", "₱" },
            { "CVE", "CVE" },
            { "CZK", "Kč" },
            { "DJF", "DJF" },
            { "DKK", "kr" },
            { "DOP", "RD$" },
            { "DZD", "DZD" },
            { "EGP", "£" },
            { "ERN", "ERN" },
            { "ETB", "ETB" },
            { "EUR", "€" },
            { "FJD", "$" },
            { "FKP", "£" },
            { "GBP", "£" },
            { "GEL", "GEL" },
            { "GGP", "£" },
            { "GHS", "GHS" },
            { "GIP", "£" },
            { "GMD", "GMD" },
            { "GNF", "GNF" },
            { "GTQ", "Q" },
            { "GYD", "$" },
            { "HKD", "$" },
            { "HNL", "L" },
            { "HRK", "kn" },
            { "HTG", "HTG" },
            { "HUF", "Ft" },
            { "IDR", "Rp" },
            { "ILS", "₪" },
            { "IMP", "£" },
            { "INR", "INR" },
            { "IQD", "IQD" },
            { "IRR", "﷼" },
            { "ISK", "kr" },
            { "JEP", "£" },
            { "JMD", "J$" },
            { "JOD", "JOD" },
            { "JPY", "¥" },
            { "KES", "KES" },
            { "KGS", "лв" },
            { "KHR", "៛" },
            { "KMF", "KMF" },
            { "KPW", "₩" },
            { "KRW", "₩" },
            { "KWD", "KWD" },
            { "KYD", "$" },
            { "KZT", "лв" },
            { "LAK", "₭" },
            { "LBP", "£" },
            { "LKR", "₨" },
            { "LRD", "$" },
            { "LSL", "LSL" },
            { "LYD", "LYD" },
            { "MAD", "MAD" },
            { "MDL", "MDL" },
            { "MGA", "MGA" },
            { "MKD", "ден" },
            { "MMK", "MMK" },
            { "MNT", "₮" },
            { "MOP", "MOP" },
            { "MRO", "MRO" },
            { "MUR", "₨" },
            { "MVR", "MVR" },
            { "MWK", "MWK" },
            { "MXN", "$" },
            { "MYR", "RM" },
            { "MZN", "MT" },
            { "NAD", "$" },
            { "NGN", "₦" },
            { "NIO", "C$" },
            { "NOK", "kr" },
            { "NPR", "₨" },
            { "NZD", "$" },
            { "OMR", "﷼" },
            { "PAB", "B/." },
            { "PEN", "S/." },
            { "PGK", "PGK" },
            { "PHP", "₱" },
            { "PKR", "₨" },
            { "PLN", "zł" },
            { "PYG", "Gs" },
            { "QAR", "﷼" },
            { "RON", "lei" },
            { "RSD", "Дин." },
            { "RUB", "руб" },
            { "RWF", "RWF" },
            { "SAR", "﷼" },
            { "SBD", "$" },
            { "SCR", "₨" },
            { "SDG", "SDG" },
            { "SEK", "kr" },
            { "SGD", "$" },
            { "SHP", "£" },
            { "SLL", "SLL" },
            { "SOS", "S" },
            { "SPL", "SPL" },
            { "SRD", "$" },
            { "STD", "STD" },
            { "SVC", "$" },
            { "SYP", "£" },
            { "SZL", "SZL" },
            { "THB", "฿" },
            { "TJS", "TJS" },
            { "TMT", "TMT" },
            { "TND", "TND" },
            { "TOP", "TOP" },
            { "TRY", "TRY" },
            { "TTD", "TT$" },
            { "TVD", "$" },
            { "TWD", "NT$" },
            { "TZS", "TZS" },
            { "UAH", "₴" },
            { "UGX", "UGX" },
            { "USD", "$" },
            { "UYU", "$U" },
            { "UZS", "лв" },
            { "VEF", "Bs" },
            { "VND", "₫" },
            { "VUV", "VUV" },
            { "WST", "WST" },
            { "XAF", "XAF" },
            { "XCD", "$" },
            { "XDR", "XDR" },
            { "XOF", "XOF" },
            { "XPF", "XPF" },
            { "YER", "﷼" },
            { "ZAR", "R" },
            { "ZMW", "ZMW" },
            { "ZWD", "Z$" },
    };

    public final static String[][] listPrioritized = new String[][] {
            { "USD", "$" },
            { "EUR", "€" },
            { "GBP", "£" },
            { "INR", "INR" },
            { "SAR", "﷼" },
            { "CAD", "$" },
			{ "SEK", "kr" },
			{ "AED", "AED" },
            { "AFN", "؋" },
            { "ALL", "Lek" },
            { "AMD", "AMD" },
            { "ANG", "ƒ" },
            { "AOA", "AOA" },
            { "ARS", "$" },
            { "AUD", "$" },
            { "AWG", "ƒ" },
            { "AZN", "ман" },
            { "BAM", "KM" },
            { "BBD", "$" },
            { "BDT", "BDT" },
            { "BGN", "лв" },
            { "BHD", "BHD" },
            { "BIF", "BIF" },
            { "BMD", "$" },
            { "BND", "$" },
            { "BOB", "$b" },
            { "BRL", "R$" },
            { "BSD", "$" },
            { "BTN", "BTN" },
            { "BWP", "P" },
            { "BYR", "p." },
            { "BZD", "BZ$" },
            { "CAD", "$" },
            { "CDF", "CDF" },
            { "CHF", "CHF" },
            { "CLP", "$" },
            { "CNY", "¥" },
            { "COP", "$" },
            { "CRC", "₡" },
            { "CUC", "CUC" },
            { "CUP", "₱" },
            { "CVE", "CVE" },
            { "CZK", "Kč" },
            { "DJF", "DJF" },
            { "DKK", "kr" },
            { "DOP", "RD$" },
            { "DZD", "DZD" },
            { "EGP", "£" },
            { "ERN", "ERN" },
            { "ETB", "ETB" },
            { "EUR", "€" },
            { "FJD", "$" },
            { "FKP", "£" },
            { "GBP", "£" },
            { "GEL", "GEL" },
            { "GGP", "£" },
            { "GHS", "GHS" },
            { "GIP", "£" },
            { "GMD", "GMD" },
            { "GNF", "GNF" },
            { "GTQ", "Q" },
            { "GYD", "$" },
            { "HKD", "$" },
            { "HNL", "L" },
            { "HRK", "kn" },
            { "HTG", "HTG" },
            { "HUF", "Ft" },
            { "IDR", "Rp" },
            { "ILS", "₪" },
            { "IMP", "£" },
            { "INR", "INR" },
            { "IQD", "IQD" },
            { "IRR", "﷼" },
            { "ISK", "kr" },
            { "JEP", "£" },
            { "JMD", "J$" },
            { "JOD", "JOD" },
            { "JPY", "¥" },
            { "KES", "KES" },
            { "KGS", "лв" },
            { "KHR", "៛" },
            { "KMF", "KMF" },
            { "KPW", "₩" },
            { "KRW", "₩" },
            { "KWD", "KWD" },
            { "KYD", "$" },
            { "KZT", "лв" },
            { "LAK", "₭" },
            { "LBP", "£" },
            { "LKR", "₨" },
            { "LRD", "$" },
            { "LSL", "LSL" },
            { "LYD", "LYD" },
            { "MAD", "MAD" },
            { "MDL", "MDL" },
            { "MGA", "MGA" },
            { "MKD", "ден" },
            { "MMK", "MMK" },
            { "MNT", "₮" },
            { "MOP", "MOP" },
            { "MRO", "MRO" },
            { "MUR", "₨" },
            { "MVR", "MVR" },
            { "MWK", "MWK" },
            { "MXN", "$" },
            { "MYR", "RM" },
            { "MZN", "MT" },
            { "NAD", "$" },
            { "NGN", "₦" },
            { "NIO", "C$" },
            { "NOK", "kr" },
            { "NPR", "₨" },
            { "NZD", "$" },
            { "OMR", "﷼" },
            { "PAB", "B/." },
            { "PEN", "S/." },
            { "PGK", "PGK" },
            { "PHP", "₱" },
            { "PKR", "₨" },
            { "PLN", "zł" },
            { "PYG", "Gs" },
            { "QAR", "﷼" },
            { "RON", "lei" },
            { "RSD", "Дин." },
            { "RUB", "руб" },
            { "RWF", "RWF" },
            { "SAR", "﷼" },
            { "SBD", "$" },
            { "SCR", "₨" },
            { "SDG", "SDG" },
            { "SEK", "kr" },
            { "SGD", "$" },
            { "SHP", "£" },
            { "SLL", "SLL" },
            { "SOS", "S" },
            { "SPL", "SPL" },
            { "SRD", "$" },
            { "STD", "STD" },
            { "SVC", "$" },
            { "SYP", "£" },
            { "SZL", "SZL" },
            { "THB", "฿" },
            { "TJS", "TJS" },
            { "TMT", "TMT" },
            { "TND", "TND" },
            { "TOP", "TOP" },
            { "TRY", "TRY" },
            { "TTD", "TT$" },
            { "TVD", "$" },
            { "TWD", "NT$" },
            { "TZS", "TZS" },
            { "UAH", "₴" },
            { "UGX", "UGX" },
            { "USD", "$" },
            { "UYU", "$U" },
            { "UZS", "лв" },
            { "VEF", "Bs" },
            { "VND", "₫" },
            { "VUV", "VUV" },
            { "WST", "WST" },
            { "XAF", "XAF" },
            { "XCD", "$" },
            { "XDR", "XDR" },
            { "XOF", "XOF" },
            { "XPF", "XPF" },
            { "YER", "﷼" },
            { "ZAR", "R" },
            { "ZMW", "ZMW" },
            { "ZWD", "Z$" },
    };

    public static String[] allCurrenciesWithPrioritizedAsDisplay;

    public static String[][] getAllCurrencies() {
        return list;
    }

    public static String[][] getAllCurrenciesWithPrioritized() {
        return listPrioritized;
    }

    public static void generateAllCurrenciesWithPrioritizedAsDisplay() {
        String[] result = new String[listPrioritized.length];
        for (int i = 0; i < listPrioritized.length; i++) {
            result[i] = listPrioritized[i][0] + (listPrioritized[i][0].equals(listPrioritized[i][1]) ? "" : " (" + listPrioritized[i][1] + ")");
        }

        allCurrenciesWithPrioritizedAsDisplay = result;
    }

    public static UserCurrency guessUserCurrency() {
        String code = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        if (TextUtils.isEmpty(code)) {
            return new UserCurrency("USD", "$", true, UserCurrency.DECIMAL_SEPARATOR_DOT, UserCurrency.THOUSAND_SEPARATOR_SPACE, false);
        }

        for (String[] aList : list) {
            if (aList[0].equals(code)) {
                return new UserCurrency(code, aList[1], true, UserCurrency.DECIMAL_SEPARATOR_DOT, UserCurrency.THOUSAND_SEPARATOR_SPACE, false);
            }
        }

        return new UserCurrency(code, code, true, UserCurrency.DECIMAL_SEPARATOR_DOT, UserCurrency.THOUSAND_SEPARATOR_SPACE, false);
    }

}
