package com.opencsv;

import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an Iterator over the data found in opencsv.
 * <p><em>Fair warning!</em> This mechanism of getting at the data opencsv
 * delivers has limitations when used with the opencsv annotations. Locales and
 * custom converters are not supported. Further features may or may not work.</p>
 */
public class CSVIterator implements Iterator<String[]> {
   private final CSVReader reader;
   private String[] nextLine;

   /**
    * @param reader Reader for the CSV data.
    * @throws IOException If unable to read data from the reader.
    * @throws CsvValidationException if custom defined validator fails.
    */
   public CSVIterator(CSVReader reader) throws IOException, CsvValidationException {
      this.reader = reader;
      nextLine = reader.readNext();
   }

   @Override
   public boolean hasNext() {
      return nextLine != null;
   }

   @Override
   public String[] next() {
      String[] temp = nextLine;
      try {
         nextLine = reader.readNext();
      } catch (IOException | CsvValidationException e) {
         NoSuchElementException nse = new NoSuchElementException(e.getLocalizedMessage());
         nse.initCause(e);
         throw nse;
      }
      return temp;
   }

   /**
    * This method is not supported by opencsv and will throw an
    * {@link java.lang.UnsupportedOperationException} if called.
    */
   @Override
   public void remove() {
      throw new UnsupportedOperationException("read-only iterator");
   }
}
