package it.greenvulcano.gvesb.core.flow;

import it.greenvulcano.gvesb.core.exc.GVCoreConfException;
import it.greenvulcano.gvesb.core.exc.GVCoreException;

import java.util.Map;

import org.w3c.dom.Node;

public interface GVFlowNodeIF
{
    public void init(Node defNode) throws GVCoreConfException;
    
    public String execute(Map<String, Object> environment) throws GVCoreException;
    
    public String execute(Map<String, Object> environment, boolean onDebug) throws GVCoreException;
    
    public void cleanUp() throws GVCoreException;
    
    public void destroy() throws GVCoreException;
    
    public String getId();
    
    public String getOutput();
}
