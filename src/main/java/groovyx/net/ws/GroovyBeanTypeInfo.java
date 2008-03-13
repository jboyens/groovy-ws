package groovyx.net.ws;

import java.beans.PropertyDescriptor;

import org.apache.cxf.aegis.type.basic.BeanTypeInfo;

public class GroovyBeanTypeInfo extends BeanTypeInfo {

    public GroovyBeanTypeInfo(Class<?> typeClass, String defaultNamespace) {
        super(typeClass, defaultNamespace);
        // TODO Auto-generated constructor stub
    }

    protected boolean isElement(PropertyDescriptor desc){
        System.out.println("GroovyBeanTypeInfo --> " + desc + " is of Type ["+ desc.getPropertyType()+"]");

        return true;
    }
}
