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
public class ListBlockFormatter {
    /**
     * Format a TextBlock as a list block.
     * Applies several euristics.
     *
     * @param li parent for generated nodes
     * @param block TextBlock to format.
     */
    public static void format(Node li, TextBlock block) {
        if (block.isEmpty()) {
            return;
        }

        Document document = li.getOwnerDocument();
        boolean isSpecial = false;

        // La prima riga e' 'speciale' se termina senza punto, ma la seconda
        // inizia con una maiuscola, oppure si compone di una sola parola.

        if (block.size() > 1) {
            String first = block.get(0).trim();
            char lastChar = first.charAt(first.length() - 1);
            if (lastChar != '.') {
                String second = block.get(1).trim();
                char firstChar = second.charAt(0);
                if (Character.isUpperCase(firstChar)) {
                    isSpecial = true;
                }
            }
            if (!isSpecial) {
                isSpecial = true;
                for (int i = 0; (i < first.length()) && !isSpecial; ++i) {
                    char c = first.charAt(i);
                    if (Character.isWhitespace(c)) {
                        isSpecial = false;
                    }
                }
            }
        }

        // Qui niente di speciale

        processFirstLine(li, block.get(0));
        if (isSpecial) {
            li.appendChild(document.createElement("br"));
        }
        TextBlockFormatter.format(li, block, 1);
    }

    /**
     * Processa la prima linea di una lista. Estrae la parte a sinistra di un
     * eventuale sequenza ': ' (due punti e almeno uno spazio, un tab o un fine riga)
     * e la mette in grassetto.
     *
     * Se i due punti non sono presenti, mette in grassetto l'intera riga se questa
     * inizia e termina con ' o ".
     *
     * @param parent partent Node for the generated nodes
     * @param line String to process
     */
    public static void processFirstLine(Node parent, String line) {
        line = line.trim();

        int idx = -1;
        int idx1 = line.indexOf(": ");
        int idx2 = line.indexOf(":\t");

        if (idx1 == -1) {
            idx1 = Integer.MAX_VALUE;
        }

        if (idx2 == -1) {
            idx2 = Integer.MAX_VALUE;
        }

        idx = Math.min(idx1, idx2);

        if (idx == Integer.MAX_VALUE) {
            char lastChar = line.charAt(line.length() - 1);
            if (lastChar == ':') {
                idx = line.length() - 1;
            }
        }

        Document document = parent.getOwnerDocument();

        if (idx == Integer.MAX_VALUE) {
            // Nessun :

            char start = line.charAt(0);
            char end = line.charAt(line.length() - 1);

            if ((start == end) && ((start == '\'') || (start == '"'))) {
                Node b = document.createElement("b");
                parent.appendChild(b);
                b.appendChild(document.createTextNode(line.substring(1, line.length() - 1)));
            }
            else {
                parent.appendChild(document.createTextNode(line));
            }
        }
        else {
            // Trovati i :

            String left = line.substring(0, idx);
            String right = line.substring(idx);

            Node b = document.createElement("b");
            parent.appendChild(b);
            b.appendChild(document.createTextNode(left));
            parent.appendChild(document.createTextNode(right));
        }
    }
}
