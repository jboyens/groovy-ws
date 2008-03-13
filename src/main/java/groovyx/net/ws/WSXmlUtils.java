package groovyx.net.ws;

import org.apache.ws.commons.schema.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Jan 16, 2008
 * Time: 3:21:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class WSXmlUtils {
        /**
     * Return true if a simple type is a straightforward XML Schema representation of an enumeration.
     * If we discover schemas that are 'enum-like' with more complex structures, we might
     * make this deal with them.
     * @param type Simple type, possible an enumeration.
     * @return true for an enumeration.
     */
    public static boolean isEnumeration(XmlSchemaSimpleType type) {
        XmlSchemaSimpleTypeContent content = type.getContent();
        if (!(content instanceof XmlSchemaSimpleTypeRestriction)) {
            return false;
        }
        XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) content;
        XmlSchemaObjectCollection facets = restriction.getFacets();
        for (int x = 0; x < facets.getCount(); x++) {
            XmlSchemaFacet facet = (XmlSchemaFacet) facets.getItem(x);
            if (!(facet instanceof XmlSchemaEnumerationFacet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve the string values for an enumeration.
     * @param type
     * @return
     */
    public static List<String> enumerateValues(XmlSchemaSimpleType type) {
        XmlSchemaSimpleTypeContent content = type.getContent();
        XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) content;
        XmlSchemaObjectCollection facets = restriction.getFacets();
        List<String> values = new ArrayList<String>();
        for (int x = 0; x < facets.getCount(); x++) {
            XmlSchemaFacet facet = (XmlSchemaFacet) facets.getItem(x);
            XmlSchemaEnumerationFacet enumFacet = (XmlSchemaEnumerationFacet) facet;
            values.add(enumFacet.getValue().toString());
        }
        return values;
    }

    public static String instForName(String clazzName) {
        if (clazzName.equals("org.w3._2001.xmlschema.byte")) 	return "1";
        if (clazzName.equals("org.w3._2001.xmlschema.short")) 	return "1";
        if (clazzName.equals("org.w3._2001.xmlschema.int")) 	return "1";
        if (clazzName.equals("org.w3._2001.xmlschema.long")) 	return "1L";
        if (clazzName.equals("org.w3._2001.xmlschema.char")) 	return "'c'";
        if (clazzName.equals("org.w3._2001.xmlschema.float")) 	return "1.0f";
        if (clazzName.equals("org.w3._2001.xmlschema.double"))  return "1.0d";
        if (clazzName.equals("org.w3._2001.xmlschema.boolean")) return "false";
        if (clazzName.equals("org.w3._2001.xmlschema.string"))  return "\"foo\"";
        if (clazzName.equals("org.w3._2001.xmlschema.void")) 	return "";
        if (clazzName.equals("org.w3._2001.xmlschema.dateTime"))return "new Date()";
        if (clazzName.equals("org.w3._2001.xmlschema.positiveInteger")) return "1";
        System.out.println(">>[posible bug in WSXmlUtils.instForName] "+clazzName+" <<");

        String inst;
        try {
            inst = Class.forName(clazzName).getName();
        } catch (ClassNotFoundException e) {
            // if the class is not resolved, then it is assumed to be a simple type (with restriction)
            inst = clazzName.substring(clazzName.lastIndexOf('.')+1);

        }
        return "a"+inst;
    }
                
/*
        else if (clazz == java.math.BigDecimal.class)
            code.append("1.0 as BigDecimal");
        else if (clazz == BigInteger.class)
            code.append("1 as BigInteger");
        else
            code.append("Unhandled type");
*/
}
