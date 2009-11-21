package groovyx.net.ws;

import groovy.lang.GroovyObject;

import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.model.OperationInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class GroovyConfiguration extends AbstractServiceConfiguration  {
    private static Method isSynthetic = null;

    static {
        try {
            isSynthetic = Modifier.class.getDeclaredMethod("isSynthetic", Integer.TYPE);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
        isSynthetic.setAccessible(true);
    }

    public Boolean isOperation(Method m) {
        //skip all synthetic methods
        try {
            if ((Boolean)isSynthetic.invoke(null, m.getModifiers())) return false;
        } catch (Exception e) {
            throw new Error(e);
        }

        // skip all methods that are specific to a Groovy object
        try {
          GroovyObject.class.getMethod(m.getName(), m.getParameterTypes());
          return false;
        } catch (SecurityException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          return false;
        } catch (NoSuchMethodException e) {
          // TODO Auto-generated catch block
          return super.isOperation(m);
        }
    }

    public String getAction(OperationInfo op, Method m) {
        return op.getName().getLocalPart();
    }

}
