package groovyx.net.ws.cxf;

import org.apache.cxf.endpoint.Client;

/**
 * Helper class to configure the MTOM settings.
 *
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.5
 */
public class MtomHelper extends AbstractSettingHelper {

    /**
     * Flag to indicate whether Mtom should be used. 
     */
    private boolean isMtom = false;

    /**
     * @param isMtom <code>true</code> if mtom is enabled, otherwise <code>false</code>.
     */
    public void setMtom(boolean isMtom) {
        this.isMtom = isMtom;
    }

    /**
     * Sets the default value to not use MTOM
     */
    @Override
    protected void setDefaultProperties() {
        this.isMtom = false;
    }

    /**
     * Sets the MTOM property in the request context.
     *
     * @param client The client to configure.
     */
    @Override
    protected void configureClientParameters(Client client) {
        client.getRequestContext().put("mtom-enabled",
                Boolean.valueOf(this.isMtom));
    }

}
