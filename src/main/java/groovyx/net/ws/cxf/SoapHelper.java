package groovyx.net.ws.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;


/**
 * Helper to configure the SOAP connection
 * 
 * @see SettingConstants#SOAP_SERVICE_NAMESPACE
 * @see SettingConstants#SOAP_SERVICE_LOCALPART
 * @see SettingConstants#SOAP_PORT_NAMESPACE
 * @see SettingConstants#SOAP_PORT_LOCALPART
 * 
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.5
 */
public class SoapHelper extends AbstractSettingHelper {

    /**
     * The SOAP binding info.
     */
    private SoapBindingInfo soapBindingInfo;

    /**
     * The preferred SOAP version
     */
    private SoapVersion preferredSoapVersion = SoapVersion.SOAP_1_1;

    /**
     * Select the default SOAP version in
     * {@link SoapVersion#SOAP_1_1}, {@link SoapVersion#SOAP_1_2},
     * currently {@link SoapVersion#SOAP_1_1} is used.
     */
    @Override
    protected void setDefaultProperties() {
        this.preferredSoapVersion = SoapVersion.SOAP_1_1;
    }

    /**
     * Sets the value of the SOAP version.
     *
     * @param soapVersion value of the preferred SOAP version
     */
    public void setPreferredSoapVersion(SoapVersion soapVersion) {
        this.preferredSoapVersion = soapVersion;
    }

    /**
     * @return the Binding {@link BindingInfo}
     *         corresponding to the preferred SOAP version.
     */
    public SoapBindingInfo getBinding() {
        return this.soapBindingInfo;
    }

    /**
     * Sets the user and the password for the proxy authorization.
     *
     * @param client The client ro configure.
     */
    @Override
    protected void configureClientParameters(Client client) {
        SoapBindingInfo sbi = null;

        for (ServiceInfo sInfo : client.getEndpoint().getService().getServiceInfos()) {
            for (BindingInfo bInfo : sInfo.getBindings()) {
                if (bInfo instanceof SoapBindingInfo) {
                    sbi = (SoapBindingInfo)bInfo;

                    if (sbi.getSoapVersion().getVersion() == this.preferredSoapVersion.value()) {
                        this.soapBindingInfo = sbi;
                    }
                }
            }
        }

        if (this.soapBindingInfo == null) this.soapBindingInfo = sbi;
        assert this.soapBindingInfo != null;
    }
    
}
