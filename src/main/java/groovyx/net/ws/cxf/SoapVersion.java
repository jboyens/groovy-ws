package groovyx.net.ws.cxf;

/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Mar 29, 2009
 * Time: 4:59:18 PM
 * To change this template use File | Settings | File Templates.
 */
public enum SoapVersion {
	SOAP_1_1(1.1), SOAP_1_2(1.2);

    private final double value;
    SoapVersion(double value){
        this.value = value;
    }
    public double value(){ return this.value; }
}
