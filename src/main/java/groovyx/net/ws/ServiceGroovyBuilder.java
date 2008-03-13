package groovyx.net.ws;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.*;
import org.apache.cxf.jaxb.JAXBUtils;
import org.apache.ws.commons.schema.*;

import javax.xml.namespace.QName;
import java.util.logging.Logger;
import java.util.*;
import java.math.BigInteger;

import com.sun.tools.xjc.api.XJC;
import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Jan 10, 2008
 * Time: 1:59:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceGroovyBuilder extends ServiceModelVisitor {
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceGroovyBuilder.class);
    private StringBuilder code;
    private SchemaCollection xmlSchemaCollection;
    private boolean isInUnwrappedOperation;
    private boolean isWrapped;
    private boolean twice;
    private OperationInfo currentOperation;
    private Map<QName, String>  mapQName2Code = new HashMap<QName, String>();
    private Map<QName, Boolean> mapQName2Init = new HashMap<QName, Boolean>();


    public ServiceGroovyBuilder(ServiceInfo si) {
        super(si);
        code = new StringBuilder();

        xmlSchemaCollection = si.getXmlSchemaCollection();

    }

    public String getCode() {
        return code.toString();
    }

    private void generateTypes() throws Exception {
//        code.append("def ges = [:]\ndef ged = [:]\n\n");
        for (XmlSchema schema : xmlSchemaCollection.getXmlSchemas()) {
            code.append("//Processing schema " + schema.getVersion() + "\n//\n");

            XmlSchemaObjectTable xmlSchemaObjectTable = schema.getSchemaTypes();

            Iterator it = xmlSchemaObjectTable.getNames();
            while (it.hasNext()) {
                QName name = (QName) it.next();

                if (name.toString().startsWith("{http://www.w3.org/2001/XMLSchema}")) {
                    mapQName2Init.put(name, false);
                    continue;
                }
                System.out.println("Processing "+name);

                StringBuilder sb = new StringBuilder();

                sb.append("\n//\n// Processing " + name + "\n//\n");
                XmlSchemaObject xmlSchemaObject = xmlSchemaObjectTable.getItem(name);
                
                if (xmlSchemaObject instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) xmlSchemaObject;

                    sb.append("def a" + complexType.getQName().getLocalPart() + "=");
                    sb.append("proxy.create(\"");
                    sb.append(NameConverter.standard.toPackageName(complexType.getQName().getNamespaceURI()));
                    sb.append("." + complexType.getQName().getLocalPart());
                    sb.append("\")\n");

                    //
                    // Extract the sequence from the complex type
                    //

                    XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) complexType.getParticle();
                    if (xmlSchemaSequence == null) {
                        System.out.println("// the sequence was found null");
                        xmlSchemaSequence = new XmlSchemaSequence();
                    }
                    //
                    for (int i = 0; i < xmlSchemaSequence.getItems().getCount(); i++) {
                        XmlSchemaElement xse = (XmlSchemaElement) xmlSchemaSequence.getItems().getItem(i);
                        XmlSchemaType xmlSchemaType = xse.getSchemaType();

                        sb.append("a" + complexType.getQName().getLocalPart() + ".");
                        sb.append(xse.getQName().getLocalPart() + "=");

                        if (xmlSchemaType instanceof XmlSchemaComplexType) {
                            XmlSchemaComplexType xmlSchemaComplexType = (XmlSchemaComplexType) xmlSchemaType;
                            //@TODO complex types may have no name (inlined)
                            sb.append("a" + xmlSchemaComplexType.getQName().getLocalPart() + "\n");
                        } else if (xmlSchemaType instanceof XmlSchemaSimpleType) {

                            XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) xmlSchemaType;

                            String clazzName = JAXBUtils.namespaceURIToPackage(xmlSchemaSimpleType.getQName().getNamespaceURI());
                            clazzName += "." + xmlSchemaSimpleType.getQName().getLocalPart();

                            sb.append(WSXmlUtils.instForName(clazzName));

                            //code.append("a" + xmlSchemaSimpleType.getQName().getLocalPart());
                            sb.append("\t\t// " + xmlSchemaSimpleType.getQName() + "\n");
                        } else {
                            throw new Exception(xmlSchemaType.getQName().toString()+" is not known");
                        }
                    } // end for
                    sb.append("\n");
                    // end of complex type
                } else if (xmlSchemaObject instanceof XmlSchemaSimpleType) {
                    XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) xmlSchemaObject;
                    //
                    // Extract content of simple type
                    //
                    XmlSchemaSimpleTypeContent content = simpleType.getContent();

                    if (content instanceof XmlSchemaSimpleTypeList) {
                        XmlSchemaSimpleTypeList simpleTypeList = (XmlSchemaSimpleTypeList) content;

                        List<String> values = WSXmlUtils.enumerateValues(simpleTypeList.getItemType());
                        sb.append("// " + simpleType.getQName().getLocalPart() + " is one of:\n");
                        for (String value : values) sb.append("//      - " + value + "\n");
                        sb.append("//\ndef a" + simpleType.getQName().getLocalPart() + "=[");
                        int length = 1;
                        if (values.size() > 2) length = 2;
                        for (int i = 0; i < length; i++) sb.append("\"" + values.get(i) + "\",");
                        sb.setLength(sb.length()-1);
                        sb.append("]\n");
                    } else if (content instanceof XmlSchemaSimpleTypeRestriction) {
                        // We only support restriction on xs:string.
                        XmlSchemaSimpleTypeRestriction simpleTypeRestriction = (XmlSchemaSimpleTypeRestriction) content;

                        List<String> values = WSXmlUtils.enumerateValues(simpleType);
                        sb.append("// " + simpleType.getQName().getLocalPart() + " is one of:\n");
                        for (String value : values) sb.append("//      - " + value + "\n");
                        sb.append("//\ndef a" + simpleType.getQName().getLocalPart() + "=\"" + values.get(0) + "\"\n");
                    }

                }
                sb.append("\n\n");
                mapQName2Code.put(name, sb.toString());
                mapQName2Init.put(name, true);

                LOG.finest(sb.toString());
            }
        }
    }

    @Override
    public void begin(ServiceInfo serviceInfo) {
        code.append("//\n");
        code.append("//Entering Begin ServiceInfo\n");
        code.append("//Definitions for service: " + serviceInfo.getName().toString() + "\n");

        for (BindingInfo bi : serviceInfo.getBindings()) {
            String bId = bi.getBindingId();
            code.append(bi.getName().toString() + " is " + bId + " and style is " + ((SoapBindingInfo) bi).getStyle() + "\n");
        }

        code.append("import groovyx.net.ws.WSClient\n\n");
        code.append("def proxy = new WSClient(");
        code.append("...");
        code.append(")\n");

        code.append("//Exiting Begin ServiceInfo\n");
    }

    @Override
    public void begin(InterfaceInfo interfaceInfo) {
        code.append("//Groovy Script for " + interfaceInfo.getName() + "\n\n");

        try {
            generateTypes();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        code.append("def result = null\n");
    }

    @Override
    public void begin(OperationInfo operationInfo) {
        if (!operationInfo.equals(currentOperation)){
            twice = false;
            currentOperation = operationInfo;
        }
    }

    @Override
    public void begin(MessageInfo messageInfo) {
    }

    @Override
    public void begin(FaultInfo faultInfo) {
    }

    @Override
    public void end(ServiceInfo serviceInfo) {
        LOG.finer(getCode());
    }

    @Override
    public void end(InterfaceInfo interfaceInfo) {
    }

    @Override
    public void end(OperationInfo operationInfo) {

        if (twice) return;

        code.append("//    Generating code for "+operationInfo.getName().toString()+"\n");
        code.append("//    This operation is unwrapped: "+operationInfo.isUnwrapped()+"\n");


        List<QName> inputParameterNames = new ArrayList<QName>();

        isWrapped = !operationInfo.isUnwrapped();

        code.append("//    This operation has the following inputs:\n");
        for (MessagePartInfo mpi:operationInfo.getInput().getMessageParts()){
            code.append("//        - "+mpi.getTypeQName()+"\n");
            inputParameterNames.add(mpi.getTypeQName());
        }
        code.append("//\n");

        code.append("//    This operation has the following outputs:\n");

        boolean nonVoidOutput = false;
        MessageInfo messageInfo = operationInfo.getOutput();
        if ((messageInfo != null) && (messageInfo.getMessageParts().size() > 0)) nonVoidOutput = true ;


        List<QName> outputParameterNames = new ArrayList<QName>();
        if (nonVoidOutput) {
            code.append("//      MessageInfo name: " + messageInfo.getName()+"\n");
            for (MessagePartInfo mpi:messageInfo.getMessageParts()){
                code.append("//        - "+mpi.getName().getLocalPart()+" of type "+mpi.getTypeQName().getLocalPart()+"\n");
                outputParameterNames.add(mpi.getTypeQName());
            }
        }
        //
        // 1-Code generation
        // @TODO Initialize the variables
        for (QName q:inputParameterNames) {
            System.out.println("Looking for "+q);
            if (mapQName2Init.get(q)) {
                code.append(mapQName2Code.get(q));
                mapQName2Init.put(q, false);
            }
        }
        //
        // 1.1-Append the operation name
        //
        if (nonVoidOutput) code.append("result = ");
        code.append("proxy."+operationInfo.getName().getLocalPart()+"(");
        //
        // 1.2-Append the parameters
        //
        for (QName q:inputParameterNames) {
            String clazzname = JAXBUtils.namespaceURIToPackage(q.getNamespaceURI())+"."+q.getLocalPart();
            code.append(WSXmlUtils.instForName(clazzname));
            code.append(", ");
        }
        code.setLength(code.length() - 2);
        code.append(")\n//\n");
        //
        // 2-Generate
        //
        for (MessagePartInfo mpi:messageInfo.getMessageParts()) {
            code.append("println result?."+mpi.getName().getLocalPart()+"\n");

            if (mpi.isElement()) {
                code.append("// this is an element\n");
            } else {
                code.append("// this is not an element\n");
            }

            XmlSchemaAnnotated tp = xmlSchemaCollection.getElementByQName(mpi.getElementQName());
            code.append("// annotated schema: "+tp+"\n");

            XmlSchemaElement xmlSchemaElement = xmlSchemaCollection.getElementByQName(mpi.getTypeQName());
            code.append("// fetching xmlelement for type "+mpi.getTypeQName()+": "+xmlSchemaElement+"\n");
            code.append("println result?."+mpi.getTypeQName().getLocalPart()+"\n");


        }
        
        twice = true;

        code.append("\n");
    }

    @Override
    public void end(MessageInfo messageInfo) {
    }

    @Override
    public void end(MessagePartInfo messagePartInfo) {
    }

    @Override
    public void end(FaultInfo faultInfo) {
    }
}
