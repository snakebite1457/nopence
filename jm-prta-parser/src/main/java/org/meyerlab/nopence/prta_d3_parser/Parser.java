package org.meyerlab.nopence.prta_d3_parser;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import org.meyerlab.nopence.prta_d3_parser.Model.*;
import org.meyerlab.nopence.util.FileHelper;
import org.meyerlab.nopence.utils.Helper;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * @author Dennis Meyer
 */
public class Parser {


    private DocumentBuilder _builder;
    private JsonAutomataObject _automataObject;

    private HashMap<Integer, Node> _variables;

    public Parser() {
        _variables = new HashMap<>();

        DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();

        try {
            _builder = _factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }


    public void parse() throws IOException, SAXException {
        // Read out the given variables-histogramm file, which has been used
        // to create the automata
        readVarHist();
        try {
            buildJsonFile();
        } catch (FileNotValidException e) {
            e.printStackTrace();
        }

    }

    private void buildJsonFile() throws IOException, FileNotValidException {
        _automataObject = new JsonAutomataObject();
        _automataObject.setAttributes(getJsonAttributes());
        readAutomatonOutput();

        Gson gson = new Gson();
        Writer writer = null;


        Helper.createFile(new File(D3ParserOption.OutJsonAutomataFilePath));
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(D3ParserOption.OutJsonAutomataFilePath), "utf-8"));
            writer.write(gson.toJson(_automataObject));
        } catch (IOException ex) {
            // report
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private void readAutomatonOutput() throws IOException, FileNotValidException {
        FileHelper fileHelper = new FileHelper(new File(D3ParserOption.AutomataFilePath), false);

        // Skip first to lines
        fileHelper.nextLine();
        fileHelper.nextLine();

        List<JsonNode> jsonNodes = new ArrayList<>();
        List<JsonEdge> jsonEdges = new ArrayList<>();

        String line = fileHelper.nextLine();
        while (!line.equals("") || line.equals("edges")) {
            // Read Nodes
            jsonNodes.add(getNodeFromLine(line));
            line = fileHelper.nextLine();
        }

        line = fileHelper.nextLine();

        while (!line.equals("")) {
            // Read Edges
            jsonEdges.add(getEdgeFromLine(line));

            if (!fileHelper.hasNextLine()) {
                break;
            }
            line = fileHelper.nextLine();
        }

        _automataObject.setEdges(jsonEdges.toArray(new JsonEdge[jsonEdges.size()]));
        _automataObject.setNodes(jsonNodes.toArray(new JsonNode[jsonNodes.size()]));
    }

    private JsonEdge getEdgeFromLine(String line) {
        if (!line.contains("Edge")) {
            return null;
        }

        JsonEdge jsonEdge = new JsonEdge();

        int closeNodeIdTag = line.indexOf("]", 2);
        String id = line.substring(6, closeNodeIdTag);
        jsonEdge.Id = Integer.parseInt(id);

        int pathIndex = line.indexOf("(");
        int pathIndexEnd = line.indexOf(")", pathIndex);
        String[] path = line.substring(pathIndex + 1, pathIndexEnd).split(",");
        jsonEdge.Source = Integer.parseInt(path[0]);
        jsonEdge.Target = Integer.parseInt(path[1]);

        int tdsIndex = line.indexOf(":",pathIndexEnd);
        int tdsIndexEnd = line.indexOf("[", tdsIndex);
        jsonEdge.Tds = Integer.parseInt(line.substring(tdsIndex + 1, tdsIndexEnd));

        return jsonEdge;
    }

    private JsonNode getNodeFromLine(String line) {
        if (!line.contains("node")) {
            return null;
        }

        JsonNode jsonNode = new JsonNode();

        int closeNodeIdTag = line.indexOf("]", 2);
        String id = line.substring(6, closeNodeIdTag);
        jsonNode.Id = Integer.parseInt(id);

        int infIndex = line.indexOf("[", closeNodeIdTag);
        String arr = line.substring(infIndex).replace(":1.0", "").replace
                ("[", "").replace("]", "");


        String[] attributes = arr.split(" ");

        List<Integer> attrIds = new ArrayList<>();
        for (int i = 0; i < attributes.length; i++) {
            Integer attrId = Ints.tryParse(attributes[i]);
            if (attrId != null) {
                attrIds.add(attrId);
            }
        }

        jsonNode.Attributes = Ints.toArray(attrIds);
        return jsonNode;
    }

    private void readVarHist() throws IOException, SAXException {
        Document attrInfoDocument = _builder.parse(D3ParserOption.VarHistogrammFilePath);
        NodeList nodeList = attrInfoDocument.getElementsByTagName("variable");


        for (int i = 0; i < nodeList.getLength(); i++) {
            int childIdIndex = getXmlChildIndex("VarID", nodeList.item(i));
            String varId = nodeList.item(i).getChildNodes().item
                    (childIdIndex).getTextContent();

            _variables.put(Integer.valueOf(varId), nodeList.item(i));
        }
    }

    private int getXmlChildIndex(String name, Node parent) {
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            if (parent.getChildNodes().item(i).getNodeName().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    private JsonAttribute[] getJsonAttributes() {
        if (_variables == null || _variables.size() == 0) {
            return null;
        }

        int counter = 0;
        JsonAttribute[] jsonAttributes = new JsonAttribute[_variables.size()];
        for (Map.Entry<Integer, Node> entry : _variables.entrySet()) {
            JsonAttribute jsonAttribute = new JsonAttribute();
            jsonAttribute.Id = entry.getKey();

            int childLabelShortIndex = getXmlChildIndex("LabelShort", entry
                    .getValue());
            int childOriginalIdIndex = getXmlChildIndex("originalAttrId", entry
                    .getValue());
            int childTypeIndex = getXmlChildIndex("type",
                    entry.getValue());

            jsonAttribute.Name = entry.getValue().getChildNodes().item
                    (childLabelShortIndex).getTextContent();
            jsonAttribute.OriginalId = Integer.parseInt(entry.getValue().getChildNodes
                    ().item(childOriginalIdIndex).getTextContent());
            jsonAttribute.Type = entry.getValue().getChildNodes().item
                    (childTypeIndex).getTextContent();

            jsonAttributes[counter++] = jsonAttribute;
        }

        return jsonAttributes;
    }

    private boolean isAttributeOrdinal(int attrId, int ordinalId) {
        return _variables.entrySet()
            .stream()
            .filter(entry -> entry.getKey() != attrId)
            .filter(entry -> {
                int childOrdinalIdIndex = getXmlChildIndex("OrdinalId", entry
                        .getValue());

                return Integer.parseInt(entry.getValue().getChildNodes()
                        .item(childOrdinalIdIndex).getTextContent()) == ordinalId;
            })
            .findFirst().orElse(null) != null;
    }
}
