package org.meyerlab.nopence.util;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import org.meyerlab.nopence.prta_d3_parser.Model.JsonAttribute;
import org.meyerlab.nopence.prta_d3_parser.Model.JsonEdge;
import org.meyerlab.nopence.prta_d3_parser.Model.JsonNode;
import org.meyerlab.nopence.prta_d3_parser.Model.JsonNodeAttribute;
import org.meyerlab.nopence.utils.exceptions.FileNotValidException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis Meyer
 */
public class ReadAutomatonFileHelper {

    private FileHelper _fileHelper;
    private File _automatonFile;

    private List<JsonNode> _storedNodes;
    private List<JsonEdge> _storedEdges;

    public ReadAutomatonFileHelper(String automatonFile)
            throws IOException, FileNotValidException {
        _automatonFile = new File(automatonFile);
        _fileHelper = new FileHelper(_automatonFile, false);
    }

    public List<JsonEdge> getEdges() {
        if (_storedEdges != null) {
            return _storedEdges;
        }

        _storedEdges = new ArrayList<>();

        try {
            _fileHelper.reset(_automatonFile, false);
            String line = _fileHelper.nextLine();
            while (!line.equals("edges:")) {
                // Skip nodes lines
                line = _fileHelper.nextLine();
            }

            line = _fileHelper.nextLine();

            while (!line.equals("")) {
                // Read Edges
                _storedEdges.add(getEdgeFromLine(line));

                if (!_fileHelper.hasNextLine()) {
                    break;
                }
                line = _fileHelper.nextLine();
            }


        } catch (IOException | FileNotValidException e) {
            e.printStackTrace();
        }

        return _storedEdges;
    }

    public List<JsonNode> getNodes() {
        if (_storedNodes != null ) {
            return _storedNodes;
        }

        _storedNodes = new ArrayList<>();

        try {
            _fileHelper.reset(_automatonFile, false);

            // Skip first two lines
            _fileHelper.nextLine();
            _fileHelper.nextLine();

            String line = _fileHelper.nextLine();
            while (!line.equals("") || line.equals("edges")) {
                // Read Nodes
                _storedNodes.add(getNodeFromLine(line));
                line = _fileHelper.nextLine();
            }


        } catch (IOException | FileNotValidException e) {
            e.printStackTrace();
        }

        return _storedNodes;
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

        int instancesBeginIndex = line.indexOf(":") + 2;
        int instancesEndIndex = line.indexOf("i", instancesBeginIndex) - 1;
        jsonNode.NumInstances =
                Integer.parseInt(line.substring(instancesBeginIndex, instancesEndIndex));

        int infIndex = line.indexOf("[", closeNodeIdTag);
        String arr = line.substring(infIndex).replace("[", "").replace("]", "");

        String[] attributes = arr.split(" ");

        if (attributes[0].equals("")) {
            return jsonNode;
        }

        List<JsonNodeAttribute> jsonNodeAttributes = new ArrayList<>();
        for (int i = 0; i < attributes.length; i++) {
            String[] attrWithProb = attributes[i].split(":");

            Integer attrId = Ints.tryParse(attrWithProb[0]);
            Double attrProb = Doubles.tryParse(attrWithProb[1]);

            if (attrId != null && attrProb != null) {
                JsonNodeAttribute nodeAttribute = new JsonNodeAttribute();
                nodeAttribute.Id = attrId;
                nodeAttribute.Probability = Math.floor(attrProb * 1000) / 1000.0;
                jsonNodeAttributes.add(nodeAttribute);
            }
        }

        JsonNodeAttribute[] nodeAttributes = new JsonNodeAttribute[jsonNodeAttributes.size()];
        for (int i = 0; i < jsonNodeAttributes.size(); i++) {
            nodeAttributes[i] = jsonNodeAttributes.get(i);
        }

        jsonNode.Attributes = nodeAttributes;
        return jsonNode;
    }
}
