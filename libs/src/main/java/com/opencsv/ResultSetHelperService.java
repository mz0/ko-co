/*
 Copyright 2005 Bytecode Pty Ltd.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.opencsv;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

public class ResultSetHelperService implements ResultSetHelper {

   static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
   static final String DEFAULT_TIMESTAMP_FORMAT = "dd-MMM-yyyy HH:mm:ss";
   private static final String NULL_DEFAULT_VALUE = "";

   protected String dateFormat = DEFAULT_DATE_FORMAT;
   protected String dateTimeFormat = DEFAULT_TIMESTAMP_FORMAT;
   protected NumberFormat integerFormat;
   protected NumberFormat floatingPointFormat;
   protected String nullDefault = NULL_DEFAULT_VALUE;

   public ResultSetHelperService() {
   }

   @Override
   public String[] getColumnNames(ResultSet rs) throws SQLException {
      ResultSetMetaData metadata = rs.getMetaData();
      String[] nameArray = new String[metadata.getColumnCount()];
      for (int i = 0; i < metadata.getColumnCount(); i++) {
         nameArray[i] = metadata.getColumnLabel(i+1);
      }
      return nameArray;
   }

   @Override
   public String[] getColumnValues(ResultSet rs) throws SQLException, IOException {
      return this.getColumnValues(rs, false, dateFormat, dateTimeFormat);
   }

   @Override
   public String[] getColumnValues(ResultSet rs, boolean trim) throws SQLException, IOException {
      return this.getColumnValues(rs, trim, dateFormat, dateTimeFormat);
   }

   @Override
   public String[] getColumnValues(ResultSet rs, boolean trim, String dateFormatString, String timeFormatString) throws SQLException, IOException {
      ResultSetMetaData metadata = rs.getMetaData();
      String[] valueArray = new String[metadata.getColumnCount()];
      for (int i = 1; i <= metadata.getColumnCount(); i++) {
         valueArray[i-1] = getColumnValue(rs, metadata.getColumnType(i), i,
               trim, dateFormatString, timeFormatString);
      }
      return valueArray;
   }

   /**
    * The formatted timestamp.
    * @param timestamp Timestamp read from ResultSet
    * @param timestampFormatString Format string
    * @return Formatted time stamp.
    */
   protected String handleTimestamp(Timestamp timestamp, String timestampFormatString) {
      SimpleDateFormat timeFormat = new SimpleDateFormat(timestampFormatString);
      return timestamp == null ? null : timeFormat.format(timestamp);
   }

   private String getColumnValue(ResultSet rs, int colType, int colIndex, boolean trim, String dateFormatString, String timestampFormatString)
         throws SQLException, IOException {

      String value;

      switch (colType) {
         case Types.BOOLEAN:
            value = Objects.toString(rs.getBoolean(colIndex));
            break;
         case Types.NCLOB:
            value = handleNClob(rs, colIndex);
            break;
         case Types.CLOB:
            value = handleClob(rs, colIndex);
            break;
         case Types.BIGINT:
            value = applyFormatter(integerFormat, rs.getBigDecimal(colIndex));
            break;
         case Types.DECIMAL:
         case Types.REAL:
         case Types.NUMERIC:
            value = applyFormatter(floatingPointFormat, rs.getBigDecimal(colIndex));
            break;
         case Types.DOUBLE:
            value = applyFormatter(floatingPointFormat, rs.getDouble(colIndex));
            break;
         case Types.FLOAT:
            value = applyFormatter(floatingPointFormat, rs.getFloat(colIndex));
            break;
         case Types.INTEGER:
         case Types.TINYINT:
         case Types.SMALLINT:
            value = applyFormatter(integerFormat, rs.getInt(colIndex));
            break;
         case Types.DATE:
            value = handleDate(rs, colIndex, dateFormatString);
            break;
         case Types.TIME:
            value = Objects.toString(rs.getTime(colIndex), nullDefault);
            break;
         case Types.TIMESTAMP:
            value = handleTimestamp(rs.getTimestamp(colIndex), timestampFormatString);
            break;
         case Types.NVARCHAR:
         case Types.NCHAR:
         case Types.LONGNVARCHAR:
            value = handleNVarChar(rs, colIndex, trim);
            break;
         case Types.LONGVARCHAR:
         case Types.VARCHAR:
         case Types.CHAR:
            value = handleVarChar(rs, colIndex, trim);
            break;
         default:
            // This takes care of Types.BIT, Types.JAVA_OBJECT, and anything
            // unknown.
            value = Objects.toString(rs.getObject(colIndex), nullDefault);
      }


      if (rs.wasNull() || value == null) {
         value = nullDefault;
      }

      return value;
   }

   private String applyFormatter(NumberFormat formatter, Number value) {
      if (formatter != null && value != null) {
         return formatter.format(value);
      }
      return Objects.toString(value, nullDefault);
   }

   /**
    * retrieves the data from an VarChar in a result set
    *
    * @param rs       - result set
    * @param colIndex - column location of the data in the result set
    * @param trim     - should the value be trimmed before being returned
    * @return a string representing the VarChar from the result set
    */
   protected String handleVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
      String value;
      String columnValue = rs.getString(colIndex);
      if (trim && columnValue != null) {
         value = columnValue.trim();
      } else {
         value = columnValue;
      }
      return value;
   }

   /**
    * retrieves the data from an NVarChar in a result set
    *
    * @param rs       - result set
    * @param colIndex - column location of the data in the result set
    * @param trim     - should the value be trimmed before being returned
    * @return a string representing the NVarChar from the result set
    */
   protected String handleNVarChar(ResultSet rs, int colIndex, boolean trim) throws SQLException {
      String value;
      String nColumnValue = rs.getNString(colIndex);
      if (trim && nColumnValue != null) {
         value = nColumnValue.trim();
      } else {
         value = nColumnValue;
      }
      return value;
   }

   /**
    * retrieves a date from a result set
    *
    * @param rs               - result set
    * @param colIndex         - column location of the data in the result set
    * @param dateFormatString - desired format of the date
    * @return - a string representing the data from the result set in the format set in dateFormatString.
    */
   protected String handleDate(ResultSet rs, int colIndex, String dateFormatString) throws SQLException {
      String value = nullDefault;
      Date date = rs.getDate(colIndex);
      if (date != null) {
         SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
         value = df.format(date);
      }
      return value;
   }

   /**
    * retrieves the data out of a CLOB
    *
    * @param rs       - result set
    * @param colIndex - column location of the data in the result set
    * @return the data in the Clob as a string.
    */
   protected String handleClob(ResultSet rs, int colIndex) throws SQLException, IOException {
      String value = nullDefault;
      Clob c = rs.getClob(colIndex);
      if (c != null) {
         TextStringBuilder sb = new TextStringBuilder();
         sb.readFrom(c.getCharacterStream());
         value = sb.toString();
      }
      return value;
   }

   /**
    * retrieves the data out of a NCLOB
    *
    * @param rs       - result set
    * @param colIndex - column location of the data in the result set
    * @return the data in the NCLOB as a string.
    */
   protected String handleNClob(ResultSet rs, int colIndex) throws SQLException, IOException {
      String value = nullDefault;
      NClob nc = rs.getNClob(colIndex);
      if (nc != null) {
         TextStringBuilder sb = new TextStringBuilder();
         sb.readFrom(nc.getCharacterStream());
         value = sb.toString();
      }
      return value;
   }

    // org.apache.commons:commons-text:1.13.0 replacement
    static class TextStringBuilder {
        private final StringBuilder sb = new StringBuilder();

        public void readFrom(Reader characterStream) throws IOException {
            char[] buffer = new char[127];
            int readCount = characterStream.read(buffer);
            if (readCount != -1) {
                do {
                    sb.append(Arrays.copyOf(buffer, readCount));
                    readCount = characterStream.read(buffer);
                } while (readCount != -1);
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
