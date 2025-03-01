package com.opencsv;

import com.opencsv.enums.CSVReaderNullFieldIndicator;

/** composed by Andrey Senov for :redump project */
public class ACsvParser extends RFC4180Parser {

    public ACsvParser() {
        super(ICSVParser.DEFAULT_QUOTE_CHARACTER, ICSVParser.DEFAULT_SEPARATOR, CSVReaderNullFieldIndicator.EMPTY_SEPARATORS);
    }

    @Override
    public String parseToLine(String[] values, boolean applyQuotesToAll) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {

            if (i != 0) {
                sb.append(separator);
            }

            String value = values[i];

            if (value == null) {
                continue;
            }

            sb.append(convertToCsvValue(value, applyQuotesToAll));
        }

        return sb.toString();
    }
}
