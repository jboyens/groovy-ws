package groovyx.net.ws;

import groovy.lang.GroovyObject;

import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.model.OperationInfo;

import java.lang.reflect.Method;

public class GroovyConfiguration extends AbstractServiceConfiguration  {
	
	public Boolean isOperation(Method m) {

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
