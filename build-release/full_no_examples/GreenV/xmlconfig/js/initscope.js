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
importClass(Packages.it.greenvulcano.util.crypto.CryptoHelper);
importClass(Packages.it.greenvulcano.gvesb.identity.GVIdentityHelper);

importClass(Packages.org.apache.commons.codec.digest.DigestUtils);

importPackage(Packages.java.lang);
importClass(Packages.java.util.Vector);
importClass(Packages.java.util.HashMap);
importClass(Packages.java.util.ArrayList);
importClass(Packages.java.util.Calendar);
importClass(Packages.java.util.TimeZone);

// Math function definition
Math.trunc = Math.trunc || function(x) {
  return x - x % 1;
}
Math.sign = Math.sign || function(x) {
  x = +x; // convert to a number
  if (x === 0 || isNaN(x)) {
    return x;
  }
  return x > 0 ? 1 : -1;
}

/**
 Remove leading and tailing spaces from str
 */
function trim(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
