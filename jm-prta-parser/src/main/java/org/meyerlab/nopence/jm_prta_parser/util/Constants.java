package org.meyerlab.nopence.jm_prta_parser.util;

/**
 * @author Dennis Meyer
 */
public class Constants {

    public static final String ATTR_INFO_ROOT_NAME = "attributes";

    public static final String ATTR_INFO_ATTR_ROOT = "attribute";
    public static final String ATTR_INFO_ATTR_NODE_ID = "id";
    public static final String ATTR_INFO_ATTR_NODE_NAME = "name";
    public static final String ATTR_INFO_ATTR_NODE_TYPE = "type";

    public static final String ATTR_INFO_VALS_ROOT = "values";

    public static final String ATTR_INFO_VAL_ROOT = "value";
    public static final String ATTR_INFO_VAL_NODE_NUM = "number";
    public static final String ATTR_INFO_VAL_NODE_NAME = "name";

    public static final String ATTR_INFO_VAL_NODE_NUMTYPE_AFTERTYPE = "afterDiscretizationType";
    public static final String ATTR_INFO_VAL_NODE_NUMTYPE_DISTYPE = "discretizationType";
    public static final String ATTR_INFO_VAL_NODE_NUMTYPE_BINS = "discretizationNumberBins";

    public static final String VAR_HIST_ELEMENT_VAR_ID = "VarID";
    public static final String VAR_HIST_ELEMENT_LABEL_SHORT = "LabelShort";
    public static final String VAR_HIST_ELEMENT_LABEL_LONG = "LabelLong";
    public static final String VAR_HIST_ELEMENT_LABEL_LONG_TEXT = "";
    public static final String VAR_HIST_ELEMENT_LABEL_CLUSTER = "Cluster";
    public static final String VAR_HIST_ELEMENT_LABEL_ADDS = "Adds";
    public static final String VAR_HIST_ELEMENT_LABEL_ADDS_TEXT = "Additional Information";


    public static final String FILE_ENCODING = "UTF-8";
    public static final String FILE_EXTENSION_XML = "xml";
    public static final String FILE_CSV_SEPARATION = ";";


    public static final int NUM_INST_FOR_DISCRETIZATION = 3000;
    public static final int NUM_DEFAULT_SEQ_BUFFER_SIZE = 25;

    public static final int PERSON_ID_COLUMN = 0;
    public static final int SPELL_BEGIN_COLUMN = 4;
    public static final int SPELL_END_COLUMN = 5;
    public static final String DATE_FORMAT = "MM/dd/yyyy";

    public static final String ATTR_INFO_FILE_PREFIX = "Attribute-Information-";

    public static final String CLI_DES_ATTR_INFO_DIR = "path to directory which contains the attribute information files";
    public static final String CLI_DES_OUT_DIR = "path to directory which will be contain the output files";
    public static final String CLI_DES_VAR_HIST_DIR = "path to directory which will be contain the variables histogram files.";
    public static final String CLI_DES_DATA_FILE = "path to data file";
    public static final String CLI_DES_NUM_SEQ = "max number of sequences. Missing means infinity.";

    public static final String EX_FILE_NOT_VALID = "The File %s can not be found or the given file is a directory";
    public static final String EX_DIR_NOT_VALID = "The Directory %s can not be found.";
    public static final String EX_FILE_DEL = "The file %s can not be deleted.";
    public static final String EX_FILE_CREATE = "The file %s can not be created.";
    public static final String EX_ATTR_NOT_CONTAIN_VAL = "The attribute %s does not contain the value %.1f.";
}
