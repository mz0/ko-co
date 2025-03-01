package com.opencsv;

import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.processor.RowProcessor;
import com.opencsv.validators.LineValidatorAggregator;
import com.opencsv.validators.RowValidatorAggregator;

import java.io.Reader;
import java.util.Locale;

import static com.exactpro.uncommon.NullAware.defaultIfNull;

/**
 * Base class for the builders of various incarnations of CSVReaders.
 * @param <T> The type pf the CSVReader class to return
 *
 * @author Andrew Rucker Jones
 */
abstract public class CSVReaderBaseBuilder<T> {
    protected final Reader reader;
    protected final LineValidatorAggregator lineValidatorAggregator = new LineValidatorAggregator();
    protected final RowValidatorAggregator rowValidatorAggregator = new RowValidatorAggregator();
    private final CSVParserBuilder parserBuilder = new CSVParserBuilder();
    protected int skipLines = CSVReader.DEFAULT_SKIP_LINES;
    protected ICSVParser icsvParser = null;
    protected boolean keepCR;
    protected boolean verifyReader = CSVReader.DEFAULT_VERIFY_READER;
    protected CSVReaderNullFieldIndicator nullFieldIndicator = CSVReaderNullFieldIndicator.NEITHER;
    protected int multilineLimit = CSVReader.DEFAULT_MULTILINE_LIMIT;
    protected Locale errorLocale = Locale.getDefault();
    protected RowProcessor rowProcessor = null;

    /**
     * Base Constructor
     *
     * @param reader The reader to an underlying CSV source.
     */
    protected CSVReaderBaseBuilder(final Reader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new {@link ICSVParser} if the class doesn't already hold one.
     *
     * @return The injected {@link ICSVParser} or a default parser.
     */
    protected ICSVParser getOrCreateCsvParser() {
        return defaultIfNull(icsvParser,
                parserBuilder
                        .withFieldAsNull(nullFieldIndicator)
                        .build());
    }

    /**
     * Must create the CSVReader type requested.
     * @return A new instance of {@link CSVReader} or derived class
     */
    public abstract T build();
}
