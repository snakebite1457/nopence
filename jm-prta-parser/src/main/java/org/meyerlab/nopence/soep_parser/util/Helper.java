package org.meyerlab.nopence.soep_parser.util;

/**
 * @author Dennis Meyer
 */
public class Helper {

    private static final int firstFullYear = 1984;

    public static String completeVarName(String fileName,
                                         String varShortName,
                                         AttributeType attributeType) {
        String filePrefix = fileName.replace(SoepOption.FilePostfix, "");

        if (attributeType == AttributeType.LETTER) {
            return filePrefix.toUpperCase() + varShortName;
        }
        else {
            return varShortName + getSmallYear((firstFullYear +
                    getAssociateNumber(filePrefix)));
        }
    }

    public static int getYearFromFileName(String fileName) {
        String filePrefix = fileName.replace(SoepOption.FilePostfix, "");

        return firstFullYear + getAssociateNumber(filePrefix);
    }

    private static int getAssociateNumber(String columnName) {
        String upperColumnName = columnName.toUpperCase();

        int sum = 0;

        char ch;
        for (int i = 0; i < columnName.length(); i++)
        {
            ch = upperColumnName.charAt(i);

            sum *= 26;
            sum += (ch - 'A');
        }

        return sum;
    }

    private static String getSmallYear(int fullYear) {
        String year = String.valueOf(fullYear);
        return year.substring(2);
    }
}
