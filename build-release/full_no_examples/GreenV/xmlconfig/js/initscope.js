importPackage(Packages.it.greenvulcano.gvesb.buffer);
importPackage(Packages.it.greenvulcano.gvesb.utils);
importPackage(Packages.it.greenvulcano.util.xml);
importPackage(Packages.it.greenvulcano.util.xpath);
importPackage(Packages.it.greenvulcano.util.txt);
importPackage(Packages.it.greenvulcano.util.metadata);
importPackage(Packages.it.greenvulcano.configuration);
importPackage(Packages.it.greenvulcano.gvesb.core.exc);

importClass(Packages.it.greenvulcano.gvesb.j2ee.XAHelper);
importClass(Packages.it.greenvulcano.util.thread.ThreadMap);

importPackage(Packages.java.lang);

importClass(Packages.java.util.HashMap);
importClass(Packages.java.util.ArrayList);
importClass(Packages.java.util.Calendar);


/**
 Remove leading and tailing spaces from str
 */
function trim(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
