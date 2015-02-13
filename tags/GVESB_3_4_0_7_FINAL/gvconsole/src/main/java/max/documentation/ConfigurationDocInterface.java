/*
 * Created on 21-feb-2005
 *
 */
package max.documentation;

import org.w3c.dom.Node;

public interface ConfigurationDocInterface {
    /**
     * The init method reads the configuration file
     * @param node The documentation relative node
     * @throws Exception
     */
    void init(Node node) throws Exception;

    /**
     * @throws Exception
     *
     */
    Node createFop() throws Exception;
}