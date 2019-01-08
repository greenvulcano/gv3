/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package max.documentation.dtd;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public class TextBlockFormatter {
    /**
     * Format a TextBlock as a list block.
     * Applies several euristics.
     *
     * @param parent parent for generated nodes
     * @param block TextBlock to format.
     */
    public static void format(Node parent, TextBlock block, int start) {
        // Se non e' l'ultima riga e termina con un '.' allora inserisce un <br/>

        Document document = parent.getOwnerDocument();

        int n = block.size();

        for (int i = start; i < n; ++i) {
            String row = block.get(i);
            parent.appendChild(document.createTextNode(row));
            if (row.trim().endsWith(".") && (i < n - 1)) {
                Node br = document.createElement("br");
                parent.appendChild(br);
            }
            else {
                parent.appendChild(document.createTextNode("\n"));
            }
        }
    }
}
