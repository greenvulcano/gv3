importPackage(Packages.it.greenvulcano.gvesb.buffer);
importPackage(Packages.it.greenvulcano.gvesb.utils);
importPackage(Packages.it.greenvulcano.util.xml);
importPackage(Packages.it.greenvulcano.util.xpath);
importPackage(Packages.it.greenvulcano.util.txt);
importPackage(Packages.it.greenvulcano.configuration);
importPackage(Packages.java.lang);
//importPackage(Packages.java.util);

// TEST
importPackage(Packages.tests.unit.gvrules.bean.figure);

/**
 Remove leading and tailing spaces from str
 */
function trim(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
