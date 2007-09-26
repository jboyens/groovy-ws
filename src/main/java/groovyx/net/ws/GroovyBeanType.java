package groovyx.net.ws;

import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.type.basic.BeanTypeInfo;

public class GroovyBeanType extends BeanType {
    public BeanTypeInfo createTypeInfo() {
        GroovyBeanTypeInfo info = new GroovyBeanTypeInfo(getTypeClass(), getSchemaType().getNamespaceURI());

        info.setTypeMapping(getTypeMapping());
        
        return info;
    }
}
