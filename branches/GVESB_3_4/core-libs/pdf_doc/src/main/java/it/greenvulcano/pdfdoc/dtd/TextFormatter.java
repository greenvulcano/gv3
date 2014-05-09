/*
 * Copyright (c) 2004 Maxime Informatica s.n.c. - All right reserved
 *
 */
package it.greenvulcano.pdfdoc.dtd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * @author Maxime Informatica s.n.c. - Copyright (c) 2003 - All right reserved
 */
public class TextFormatter
{
    //--------------------------------------------------------------------------
    // CONSTANTS
    //--------------------------------------------------------------------------

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_LIST = "list";

    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------

    private String str;
    private Document document;

    //--------------------------------------------------------------------------
    // CONSTRUCTORS
    //--------------------------------------------------------------------------

    public TextFormatter(Document targetDocument, String str)
    {
        this.str = str;
        this.document = targetDocument;
    }

    //--------------------------------------------------------------------------
    // METHODS
    //--------------------------------------------------------------------------

    public Node format()
    {
        return format(document, str);
    }

    //--------------------------------------------------------------------------
    // FORMATTING
    //--------------------------------------------------------------------------

    /**
     *
     */
    public static Node format(Document document, String str)
    {
        // Ottiene la lista di blocchi
        //
        ArrayList blocks = getBlocks(str);

        // Raffina i blocchi ottenuti
        //
        blocks = refinesBlocks(blocks);

        if(blocks.size() == 0) {
            return null;
        }

        // Produce the output
        //
        DocumentFragment fragment = document.createDocumentFragment();
        Iterator i = blocks.iterator();
        Node listNode = null;
        boolean mustPutP = false;
        while(i.hasNext()) {
            TextBlock block = (TextBlock)i.next();
            if(block.isA(TextFormatter.TYPE_TEXT)) {
                if(mustPutP || (listNode != null)) {
                    fragment.appendChild(
                        document.createElement("p")
                    );
                    if(listNode != null) {
                        listNode = null;
                    }
                }
                TextBlockFormatter.format(fragment, block, 0);
                mustPutP = true;
            }
            else if(block.isA(TextFormatter.TYPE_LIST)) {
                if(mustPutP) {
                    fragment.appendChild(
                        document.createElement("p")
                    );
                    mustPutP = false;
                }
                if(listNode == null) {
                    listNode = document.createElement("ul");
                    fragment.appendChild(listNode);
                }
                Node li = document.createElement("li");
                listNode.appendChild(li);
                ListBlockFormatter.format(li, block);
            }
        }

        return fragment;
    }

    /**
     * Raffina la lista di blocchi eliminando i blocchi non significativi,
     * inserisce le righe di spazi, inserisce le liste.
     *
     * @param blocks
     * @return A refined ArrayList of TextBlock
     */
    public static ArrayList refinesBlocks(ArrayList blocks)
    {
        ArrayList refinedBlocks = new ArrayList();

        Iterator i = blocks.iterator();
        while(i.hasNext()) {
            TextBlock block = (TextBlock)i.next();

            // Se inizia per - o * allora vediamo di interpretarlo come un insieme
            // di elementi di una lista.

            String firstRow = block.get(0);
            if(firstRow.startsWith("-") || firstRow.startsWith("*")) {
                String listChar = firstRow.substring(0, 1);

                produceListBlocks(block, listChar, refinedBlocks);
            }
            else {
                refinedBlocks.add(block);
            }
        }

        return refinedBlocks;
    }

    /**
     * Refines a TextBlock in order to build many TextBlock that are member of a list.
     *
     * @param block TextBlock to refine
     * @param result ArrayList to store the result
     * @param listChar Starting of the list
     */
    private static void produceListBlocks(TextBlock block, String listChar, ArrayList result)
    {
        Iterator rows = block.iterator();
        TextBlock currentBlock = null;
        while(rows.hasNext()) {
            String row = (String)rows.next();
            if(row.startsWith(listChar)) {
                if(currentBlock != null) {
                    result.add(currentBlock);
                }
                currentBlock = new TextBlock(TextFormatter.TYPE_LIST);
                currentBlock.append(row.substring(listChar.length()));
            }
            else {
                currentBlock.append(row);
            }
        }
        if(currentBlock != null) {
            result.add(currentBlock);
        }
    }

    /**
     * Restituisce una lista di blocchi. Ogni blocco � formato da un insieme
     * di righe di tipo omogeneo: testo semplice o pre-formattato.
     *
     * @param str stringa da cui estrarre i blocchi
     * @return lista di blocchi
     */
    public static ArrayList getBlocks(String str)
    {
        ArrayList blocks = new ArrayList();

        StringTokenizer tokenizer = new StringTokenizer(str, "\n", true);

        TextBlock currentBlock = null;
        int countCR = 0;
        while(tokenizer.hasMoreTokens()) {
            String row = tokenizer.nextToken();
            if(row.equals("\n")) {
                ++countCR;
                if(countCR == 2) {
                    if(currentBlock != null) {
                        blocks.add(currentBlock);
                        currentBlock = null;
                    }
                }
            }
            else {
                countCR = 0;
                if(currentBlock == null) {
                    currentBlock = new TextBlock(TYPE_TEXT);
                }
                currentBlock.append(row);
            }
        }
        if((currentBlock != null) && !currentBlock.isEmpty()) {
            blocks.add(currentBlock);
        }

        return blocks;
    }

}
