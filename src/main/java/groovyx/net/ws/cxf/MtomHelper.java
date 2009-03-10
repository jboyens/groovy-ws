package groovyx.net.ws.cxf;

import java.util.HashMap;

import groovyx.net.ws.WSClient;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * Helper class to configure the MTOM settings.
 *
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Mar 10, 2009
 * Time: 9:31:54 AM
 * To change this template use File | Settings | File Templates.
 */

public class MtomHelper extends AbstractSettingHelper {
    private boolean isMtom;

    /**
     * Default constructor
     *
     */
    public MtomHelper() {
    }

    /**
     * @param isMtom <code>true</code> if mtom is enabled, otherwise <code>false</code>.
     */
    public void setMtom(boolean isMtom){
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
     * @param conduit .
     */
    @Override
    protected void configureClientParameters(Client client) {
        client.getRequestContext().put("mtom-enabled", Boolean.valueOf(isMtom));
    }

}
