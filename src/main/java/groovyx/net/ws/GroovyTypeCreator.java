package groovyx.net.ws;

import org.apache.cxf.aegis.type.DefaultTypeCreator;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.basic.BeanTypeInfo;

public class GroovyTypeCreator extends DefaultTypeCreator {
    @Override
    public Type createDefaultType(TypeClassInfo info) {
        GroovyBeanType type = new GroovyBeanType();
        
        type.setSchemaType(createQName(info.getTypeClass()));
        type.setTypeClass(info.getTypeClass());
        type.setTypeMapping(getTypeMapping());
        
        BeanTypeInfo typeInfo = type.getTypeInfo();
        typeInfo.setDefaultMinOccurs(getConfiguration().getDefaultMinOccurs());
        typeInfo.setExtensibleAttributes(getConfiguration().isDefaultExtensibleAttributes());
        typeInfo.setExtensibleElements(getConfiguration().isDefaultExtensibleElements());
        
        return type;        
    }
}
