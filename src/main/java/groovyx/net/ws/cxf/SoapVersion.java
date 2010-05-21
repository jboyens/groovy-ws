package groovyx.net.ws.cxf;

/**
 * Enum to define supported SOAP versions.
 * 
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.5
 */
public enum SoapVersion {
	
    SOAP_1_1(1.1), SOAP_1_2(1.2);

    /**
     * The internal value of the version.
     */
    private final double value;
    
    /**
     * Constructor
     * 
     * @param value The version value as used in cxf.
     */
    SoapVersion(double value){
        this.value = value;
    }
    
    /**
     * @return The value of the version.
     */
    public double value(){ 
        return this.value; 
    }
    
}
