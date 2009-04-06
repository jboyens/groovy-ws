package groovyx.net.ws.cxf;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;


/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Mar 29, 2009
 * Time: 3:24:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoapHelper extends AbstractSettingHelper {

    private SoapBindingInfo soapBindingInfo;

    /**
     * Default constructor
     *
     */
    public SoapHelper() {
    }

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
     *
     */
    public void setPreferredSoapVersion(SoapVersion soapVersion) {
        this.preferredSoapVersion = soapVersion;
    }

    /**
     * @return the Binding {@link BindingInfo}
     * corresponding to the preferred SOAP version.
     */
    public SoapBindingInfo getBinding() {
        return this.soapBindingInfo;
    }

    /**
     * Sets the user and the password for the proxy authorization.
     *
     * @param client .
     */
    @Override
    protected void configureClientParameters(Client client) {
        for (ServiceInfo sInfo : client.getEndpoint().getService().getServiceInfos()) {
            for (BindingInfo bInfo : sInfo.getBindings()) {
                if (bInfo instanceof SoapBindingInfo) {
                    SoapBindingInfo sbi = (SoapBindingInfo)bInfo;

                    if (sbi.getSoapVersion().getVersion() == this.preferredSoapVersion.value()) {
                        this.soapBindingInfo = sbi;
                    }
                }
            }
        }
    }
}
