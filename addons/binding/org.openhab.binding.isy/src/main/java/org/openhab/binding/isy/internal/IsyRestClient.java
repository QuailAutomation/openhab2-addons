package org.openhab.binding.isy.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openhab.binding.isy.internal.protocol.NodeInfo;
import org.openhab.binding.isy.internal.protocol.Properties;
import org.openhab.binding.isy.internal.protocol.Property;
import org.openhab.binding.isy.internal.protocol.StateVariable;
import org.openhab.binding.isy.internal.protocol.VariableEvent;
import org.openhab.binding.isy.internal.protocol.VariableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class IsyRestClient implements OHIsyClient {
    private Logger logger = LoggerFactory.getLogger(IsyRestClient.class);
    public static final String NODES = "nodes";
    public static final String PROGRAMS = "programs";
    public static final String VARIABLES_SET = "vars/set";
    public static final String VARIABLES = "vars/get";
    public static final String VARIABLES_DEFINITIONS = "vars/definitions";
    public static final String SCENES = "scenes";
    public static final String STATUS = "status";
    public static final String VAR_INTEGER_TYPE = "1";
    public static final String VAR_STATE_TYPE = "2";

    private static String AUTHORIZATIONHEADERNAME = "Authorization";
    private String authorizationHeaderValue;

    // REST Client API variables
    protected Client isyClient;
    protected WebTarget isyTarget;
    protected WebTarget nodesTarget;
    protected WebTarget programsTarget;
    protected WebTarget scenesTarget;
    protected WebTarget statusTarget;
    protected WebTarget variablesTarget;
    protected WebTarget stateVariablesTarget;
    protected WebTarget stateVariablesDefinitionsTarget;
    private XStream xStream;

    // TODO should support startup, shutdown lifecycle
    public IsyRestClient(String url, String authHeader, XStream xStream) {

        authorizationHeaderValue = authHeader;
        this.isyClient = ClientBuilder.newClient();
        // this.isyClient.register(Variable.class);
        this.isyTarget = isyClient.target("http://" + url + "/rest");
        this.nodesTarget = isyTarget.path(NODES);// .register(NodeResponseInterceptor.class);
        this.programsTarget = isyTarget.path(PROGRAMS);
        this.scenesTarget = nodesTarget.path(SCENES);
        this.statusTarget = isyTarget.path(STATUS);
        this.variablesTarget = isyTarget.path(VARIABLES);
        this.stateVariablesTarget = isyTarget.path(VARIABLES).path(VAR_STATE_TYPE);
        this.stateVariablesDefinitionsTarget = isyTarget.path(VARIABLES_DEFINITIONS);
        this.xStream = xStream;
    }

    @Override
    public boolean changeProgramState(String programId, String command) {
        Builder changeNodeTarget = programsTarget.path(programId).path(command).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue);
        Response result = changeNodeTarget.get();
        return result.getStatus() == 200;
    }

    @Override
    public Property getNodeStatus(String node, String propertyName) {
        WebTarget target = statusTarget.path(node);
        logger.trace("getNodeStatus url: {}", target.getUri().toString());
        String theResult = target.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        logger.trace("theResult is: {}", theResult);

        Object objResult = xStream.fromXML(theResult);
        if (objResult instanceof Properties) {
            for (Property property : ((Properties) objResult).getProperties()) {
                logger.debug("[property] id: {}, value: {}", property.id, property.value);
                if (propertyName.equals(property.id)) {
                    return property;
                }
            }
        }
        return null;
    }

    @Override
    public Property getNodeStatus(String node) {
        return this.getNodeStatus(node, "ST");
    }

    @Override
    public NodeInfo getNodeInfo(String node) {
        WebTarget target = nodesTarget.path(node);
        logger.trace("getNodeProperties url: {}", target.getUri().toString());
        String theResult = target.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        logger.debug("theResult is: {}", theResult);

        Object objResult = xStream.fromXML(theResult);
        if (objResult instanceof NodeInfo) {
            return ((NodeInfo) objResult);
        }
        return null;
    }

    @Override
    public Properties getNodeProperties(String node) {

        NodeInfo nodeInfo = this.getNodeInfo(node);
        if (nodeInfo != null) {
            return nodeInfo.getProperties();
        }
        return null;
    }

    @Override
    public boolean changeNodeState(String command, String value, String address) {
        logger.debug("changeNodeState called, command: {}, value: {}, address: {}", command, value, address);
        WebTarget changeNodeWebTarget = nodesTarget.path(address).path("cmd").path(command);
        if (value != null) {
            changeNodeWebTarget = changeNodeWebTarget.path(value);
        }
        logger.debug("changeNodeState url: {}", changeNodeWebTarget.getUri().toString());
        Builder changeNodeTarget = changeNodeWebTarget.request().header(AUTHORIZATIONHEADERNAME,
                authorizationHeaderValue);
        Response result = changeNodeTarget.get();
        logger.debug("Result of call: {} ", result.toString());
        logger.debug("Result status:  {}", result.getStatus());
        return result.getStatus() == 200;
    }

    @Override
    public boolean changeNodeProperty(String property, String value, String address) {
        logger.debug("changeNodeProperty called, property: {}, value: {}, address: {}", property, value, address);
        WebTarget changeNodeWebTarget = nodesTarget.path(address).path("set").path(property);
        if (value != null) {
            changeNodeWebTarget = changeNodeWebTarget.path(value);
        }
        logger.debug("changeNodeProperty url: {}", changeNodeWebTarget.getUri().toString());
        Builder changeNodeTarget = changeNodeWebTarget.request().header(AUTHORIZATIONHEADERNAME,
                authorizationHeaderValue);
        Response result = changeNodeTarget.get();
        logger.debug("Result of changeNodeProperty call: {} ", result.toString());
        logger.debug("Result status:  {}", result.getStatus());
        return result.getStatus() == 200;
    }

    private String testGetString(WebTarget endpoint) {
        return endpoint.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).get(String.class);
    }

    @Override
    public Collection<Program> getPrograms() {
        List<Program> returnValue = new ArrayList<Program>();
        String variables = programsTarget.queryParam("subfolders", true).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.TEXT_XML).get(String.class);
        logger.debug("nodes xml: {}", variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//program");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;
                    String id = firstElement.getAttribute("id");
                    String folder = firstElement.getAttribute("folder");
                    String name = getValue(firstElement, "name");
                    if (!"true".equals(folder)) {
                        returnValue.add(new Program(id, name));
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    public Program getProgram(String programId) {
        return programsTarget.path(programId).request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.APPLICATION_XML).get(Program.class);
    }

    @Override
    public List<Node> getNodes() {
        List<Node> returnValue = new ArrayList<Node>();
        String variables = nodesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        System.out.println("nodes xml: " + variables);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(variables)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//node");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;
                    String name = getValue(firstElement, "name");
                    String address = getValue(firstElement, "address");
                    String type = getValue(firstElement, "type");
                    returnValue.add(new Node(removeBadChars(name), address, type));
                }
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return returnValue;
    }

    private String getValue(Element firstElement, String valueName) {
        NodeList firstNameList = firstElement.getElementsByTagName(valueName);
        Element firstNameElement = (Element) firstNameList.item(0);
        NodeList textFNList = firstNameElement.getChildNodes();
        String name = textFNList.item(0).getNodeValue().trim();
        return name;
    }

    public static String removeBadChars(String text) {
        return text.replace("(", "").replace(")", "").replace("-", "_");
    }

    @Override
    public List<Scene> getScenes() {
        List<Scene> returnValue = new ArrayList<Scene>();
        String scenesXml = scenesTarget.request().header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue)
                .accept(MediaType.TEXT_XML).get(String.class);
        logger.trace("scenes xml: {}", scenesXml);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(scenesXml)));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile("//group");
            NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            logger.debug("Number scenes found from rest call: " + list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element firstElement = (Element) node;

                    String name = getValue(firstElement, "name");
                    if ("ISY".equalsIgnoreCase(name)) {
                        continue;
                    }
                    String address = getValue(firstElement, "address");
                    List<String> links = new ArrayList<String>();
                    org.w3c.dom.NodeList linklist = firstElement.getElementsByTagName("link");

                    for (int ii = 0; ii < linklist.getLength(); ii++) {
                        org.w3c.dom.Node linknode = linklist.item(ii);
                        if (linknode == null) {
                            logger.debug("a link is null in scene '{}'", name);
                            continue;
                        }
                        if (linknode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                            logger.debug("a link is not a XML node in scene '{}'", name);
                            continue;
                        }
                        Element firstLinkElement = (Element) linknode;
                        String link = firstLinkElement.getTextContent();
                        if (link != null) {
                            links.add(link);
                        }
                    }
                    logger.trace("read another scene from xml: " + name + " with address " + address + " and "
                            + links.size() + " links");
                    returnValue.add(new Scene(removeBadChars(name), address, links));
                }

            }

            // TODO need correct response instead of swallowing exceptions
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    private void dumpNodes() {
        String nodes = testGetString(nodesTarget);
        System.out.println(nodes);
    }

    private void dumpStatus() {
        String nodes = testGetString(statusTarget);
        System.out.println(nodes);
    }

    public void doTests() {
        getScenes();
        getPrograms();
        System.out.println("Dumping nodes");
        dumpNodes();

        List<Node> theNodes = getNodes();
        System.out.println("Nodes count: " + theNodes.size());

        for (Node node : theNodes) {
            System.out.println(node);
        }

        System.out.println("programs text value: " + testGetString(programsTarget));
        // List<Program> returnValue = getPrograms("0045");
        // System.out.println("text value: " + returnValue);

        // System.out.println("vars text value: " + testGetString(integerVariablesTarget.path("1")));

        System.out.println("Dumping status");
        dumpStatus();
    }

    @Override
    public boolean changeVariableState(VariableType type, int id, int value) {
        Response result = isyTarget.path(VARIABLES_SET).path(Integer.toString(type.getType()))
                .path(Integer.toString(id)).path(Integer.toString(value)).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).get();
        // TODO implement return value
        return result.getStatus() == 200;
    }

    @Override
    public boolean changeSceneState(String address, int value) {
        String cmd = null;
        if (value == 255) {
            cmd = "DON";
        } else if (value == 0) {
            cmd = "DOF";
        }

        if (cmd != null) {
            Builder changeNodeTarget = nodesTarget.path(address).path("cmd").path(cmd).request()
                    .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue);
            Response result = changeNodeTarget.get();
            return result.getStatus() == 200;
        }
        return false;
    }

    @Override
    public VariableList getVariableDefinitions(VariableType type) {
        String message = stateVariablesDefinitionsTarget.path(Integer.toString(type.getType())).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.TEXT_XML).get(String.class);
        logger.debug("theResult is: {}", message);

        Object objResult = xStream.fromXML(message);
        if (objResult instanceof VariableList) {

            List<StateVariable> variableList = ((VariableList) objResult).getStateVariables();
            if (variableList != null) {
                for (StateVariable variable : variableList) {
                    logger.debug("[variable] id: {}, name: {}", variable.getId(), variable.getName());
                }
            } else {
                logger.debug("variable list is null");
            }
        }
        return (VariableList) objResult;
    }

    @Override
    public VariableEvent getVariableValue(VariableType type, int id) {
        String message = variablesTarget.path(Integer.toString(type.getType())).path(Integer.toString(id)).request()
                .header(AUTHORIZATIONHEADERNAME, authorizationHeaderValue).accept(MediaType.TEXT_XML).get(String.class);
        logger.trace("retrieving value for variable, type: {}, id: {}, message returned: {}", type.getType(), id,
                message);
        Object obj = xStream.fromXML(message);
        return ((VariableEvent) obj);
        // logger.debug("returned obj was: {}", obj);
        // throw new IllegalArgumentException(
        // "Could not retrieve value for variable type: " + type.getType() + ", id: " + id);
    }
}
