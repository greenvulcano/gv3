/*
 * Copyright (c) 2004 E@I Software - All right reserved
 *
 * Created on 30-Sep-2004
 *
 * $Date: 2010-04-03 15:28:55 $
 * $Header: /usr/local/cvsroot/gvesb-devel/GreenVulcanoOS/core/webapps/gvconsole/src/main/java/max/util/MaxConsole.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Id: MaxConsole.java,v 1.1 2010-04-03 15:28:55 nlariviera Exp $
 * $Name:  $
 * $Locker:  $
 * $Revision: 1.1 $
 * $State: Exp $
 */
package max.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class MaxConsole
{
    private static Frame frm;
    private static TextArea textArea;
    private static TextField inputField;
    private static Label promptLbl;

    private static Object pauseObj = new Object();
    private static boolean pauseFlag;

    public static void init()
    {
        if(frm != null) return;

        frm = new Frame("MaxConsole");
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (sdim.width * 80) / 100;
        int height = (sdim.height * 90) / 100;
        frm.setBounds((sdim.width - width) / 2, (sdim.height - height) / 2, width, height);
        frm.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) { endProgram(); }
        });

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", 0, 12));
        frm.add(textArea, BorderLayout.CENTER);

        Panel inputPanel = new Panel();
        inputPanel.setLayout(new BorderLayout());

        inputField = new TextField();
        inputField.setEditable(false);
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { inputEnd(); }
        });
        inputPanel.add(inputField, BorderLayout.SOUTH);

        promptLbl = new Label("");
        inputPanel.add(promptLbl, BorderLayout.NORTH);

        frm.add(inputPanel, BorderLayout.SOUTH);

        MenuBar menuBar = new MenuBar();

        Menu commands = new Menu("Commands");
        commands.add("Clear");
        commands.addSeparator();
        commands.add("Pause and Edit");
        commands.add("Resume");
        commands.addSeparator();
        commands.add("Exit");
        commands.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { commandSelected(evt); }
        });
        menuBar.add(commands);

        Menu fonts = new Menu("Fonts");
        fonts.add("Default");
        fonts.add("Dialog");
        fonts.add("DialogInput");
        fonts.add("Monospaced");
        fonts.add("Serif");
        fonts.add("SansSerif");
        fonts.addSeparator();
        fonts.add("Plain");
        fonts.add("Bold");
        fonts.add("Italic");
        fonts.add("Bold + italic");
        fonts.addSeparator();
        fonts.add("7");
        fonts.add("8");
        fonts.add("9");
        fonts.add("10");
        fonts.add("11");
        fonts.add("12");
        fonts.add("13");
        fonts.add("14");
        fonts.add("15");
        fonts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { fontSelected(evt); }
        });
        menuBar.add(fonts);

        frm.setMenuBar(menuBar);

        frm.setVisible(true);
    }

    private static void endProgram()
    {
        System.exit(1);
    }

    private static void inputEnd()
    {
        synchronized(PagedOutput.class)
        {
            PagedOutput.class.notify();
        }
    }

    private static void commandSelected(ActionEvent evt)
    {
        String command = evt.getActionCommand();
        if(command.equals("Clear")) {
            textArea.setText("");
        }
        else if(command.equals("Exit")) {
            endProgram();
        }
        else if(command.equals("Pause and Edit")) {
            synchronized(pauseObj) {
                pauseFlag = true;
                textArea.setEditable(true);
                textArea.requestFocus();
            }
        }
        else if(command.equals("Resume")) {
            synchronized(pauseObj) {
                pauseObj.notifyAll();
                pauseFlag = false;
                textArea.setEditable(false);
                inputField.requestFocus();
            }
        }
    }

    private static void fontSelected(ActionEvent evt)
    {
        String command = evt.getActionCommand();
        Font font = textArea.getFont();
        if(command.equals("Plain")) {
            font = new Font(font.getName(), Font.PLAIN, font.getSize());
        }
        else if(command.equals("Bold")) {
            font = new Font(font.getName(), Font.BOLD, font.getSize());
        }
        else if(command.equals("Italic")) {
            font = new Font(font.getName(), Font.ITALIC, font.getSize());
        }
        else if(command.equals("Bold + italic")) {
            font = new Font(font.getName(), Font.BOLD + Font.ITALIC, font.getSize());
        }
        else {
            try {
                int size = Integer.parseInt(command);
                font = new Font(font.getName(), font.getStyle(), size);
            }
            catch(NumberFormatException exc) {
                font = new Font(command, font.getStyle(), font.getSize());
            }
        }
        textArea.setFont(font);
    }

    //----------------------------------------------------------------------------------

    public static String input(String prompt)
    {
        if(frm == null) init();
        synchronized(inputField) { // sincronizzazione per la concorrenza
            print(prompt);
            promptLbl.setText(prompt);
            inputField.setText("");
            inputField.setEditable(true);
            inputField.requestFocus();
            synchronized(PagedOutput.class) // sincronizzazione per l'attesa
            {
                try {
                    PagedOutput.class.wait(); // la sincronizzazione per la concorrenza � ancora valida
                }
                catch(InterruptedException exc) {}
            }
            inputField.setEditable(false);
            String ret = inputField.getText();
            inputField.setText("");
            promptLbl.setText("");
            println(ret);
            return ret;
        }
    }

    public static int inputInt(String prompt) throws NumberFormatException
    {
        String ret = input(prompt);
        return Integer.parseInt(ret);
    }

    public static void println(String str)
    {
        print(str);
        print("\n");
    }

    public static void print(String str)
    {
        synchronized(pauseObj) {
            if(pauseFlag) {
                try {
                    pauseObj.wait();
                }
                catch(InterruptedException exc) {}
            }
        }
        if(frm == null) init();
        if(str == null) {
            textArea.append("null");
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(str, "\r\n", true);
        while(tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            if(s.equals("\n")) textArea.append(s);
            else if(!s.equals("\r")) textArea.append(s);
        }
    }
}
