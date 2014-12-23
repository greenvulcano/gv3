package it.greenvulcano.util.remotefs.hdfs.utility;

import it.greenvulcano.log.GVLogger;
import it.greenvulcano.util.MapUtils;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;

public class RegexHdfsPathFilter implements PathFilter
{
	private static final Logger logger = GVLogger.getLogger(RegexHdfsPathFilter.class);

	/**
	 * Search for <code>files-only</code>
	 */
	public static final int FILES_ONLY          = 0;

	/**
	 * Search for <code>directories-only</code>
	 */
	public static final int DIRECTORIES_ONLY    = 1;

	/**
	 * Search for <code>all</code>
	 */
	public static final int ALL                 = 2;

	private String          namePattern         = "";
	private Pattern         pattern             = null;
	private boolean         checkLastModified   = false;
	private boolean         selectModifiedSince = true;
	private long            lastTimestamp       = -1;
	private int             fileType;

	private FileSystem 		fileSystem;


	//    public static RegexHdfsPathFilter buildHdfsFileFilter(Node node) throws Exception {
	//        String fileType = XMLConfig.get(node, "@file-type", "files-only");
	//        String namePattern = XMLConfig.get(node, "@file-mask", "");
	//        int type = -1;
	//        if (fileType.equals("all")) {
	//            type = ALL;
	//        }
	//        else if (fileType.equals("directories-only")) {
	//            type = DIRECTORIES_ONLY;
	//        }
	//        else {
	//            type = FILES_ONLY;
	//        }
	//        return new RegexHdfsPathFilter(namePattern, type);
	//    }
	//
	//    public static RegexHdfsPathFilter buildHdfsFileFilter(Node node, Map<String, String> properties) throws Exception {
	//        try {
	//            PropertiesHandler.enableExceptionOnErrors();
	//            String fileType = XMLConfig.get(node, "@file-type", "files-only");
	//            String namePattern = PropertiesHandler.expand(XMLConfig.get(node, "@file-mask", ""),
	//                    MapUtils.convertToHMStringObject(properties));
	//            int type = -1;
	//            if (fileType.equals("all")) {
	//                type = ALL;
	//            }
	//            else if (fileType.equals("directories-only")) {
	//                type = DIRECTORIES_ONLY;
	//            }
	//            else {
	//                type = FILES_ONLY;
	//            }
	//            return new RegexHdfsPathFilter(namePattern, type);
	//        }
	//        finally {
	//            PropertiesHandler.disableExceptionOnErrors();
	//        }
	//    }
	//
	//    public static RegexHdfsPathFilter buildHdfsFileFilter(Node node, Object propsObj) throws Exception {
	//        try {
	//            PropertiesHandler.enableExceptionOnErrors();
	//            String fileType = XMLConfig.get(node, "@file-type", "files-only");
	//            String namePattern = PropertiesHandler.expand(XMLConfig.get(node, "@file-mask", ""), null, propsObj);
	//            int type = -1;
	//            if (fileType.equals("all")) {
	//                type = ALL;
	//            }
	//            else if (fileType.equals("directories-only")) {
	//                type = DIRECTORIES_ONLY;
	//            }
	//            else {
	//                type = FILES_ONLY;
	//            }
	//            return new RegexHdfsPathFilter(namePattern, type);
	//        }
	//        finally {
	//            PropertiesHandler.disableExceptionOnErrors();
	//        }
	//    }

	public RegexHdfsPathFilter(FileSystem hdfsFileSystem, String namePattern, int fileType) {
		this.fileSystem = hdfsFileSystem;
		this.namePattern = namePattern;
		if ((namePattern != null) && (namePattern.length() > 0)) {
			if (PropertiesHandler.isExpanded(namePattern)) {
				this.pattern = Pattern.compile(namePattern);
			}
		}
		else {
			this.pattern = null;
		}

		this.fileType = fileType;
	}

	public RegexHdfsPathFilter(FileSystem hdfsFileSystem, String namePattern, int fileType, long lastTimestamp) {
		this.fileSystem = hdfsFileSystem;
		this.namePattern = namePattern;
		
		if ((namePattern != null) && (namePattern.length() > 0)) {
			if (PropertiesHandler.isExpanded(namePattern)) {
				this.pattern = Pattern.compile(namePattern);
			}
		}
		else {
			this.pattern = null;
		}

		this.fileType = fileType;
		if (lastTimestamp > 0) {
			this.checkLastModified = true;
			this.lastTimestamp = lastTimestamp;
		}
	}

	public RegexHdfsPathFilter(FileSystem hdfsFileSystem, String namePattern, int fileType, long timestamp, boolean selectModifiedSince) {
		this.fileSystem = hdfsFileSystem;
		this.namePattern = namePattern;
		if ((namePattern != null) && (namePattern.length() > 0)) {
			if (PropertiesHandler.isExpanded(namePattern)) {
				this.pattern = Pattern.compile(namePattern);
			}
		}
		else {
			this.pattern = null;
		}

		this.fileType = fileType;
		if (timestamp > 0) {
			this.checkLastModified = true;
			this.lastTimestamp = timestamp;
			this.selectModifiedSince = selectModifiedSince;
		}
	}

	/**
	 * Defines if the file search must use file timestamp.
	 * 
	 * @param value
	 * @param lastTimestamp
	 */
	public void setCheckLastModified(boolean value, long timestamp, boolean selectModifiedSince) {
		checkLastModified = value;
		this.lastTimestamp = timestamp;
		this.selectModifiedSince = selectModifiedSince;
	}

	/**
	 * Resolves the meatadata in search file pattern definition.
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public void compileNamePattern(Map<String, String> properties) throws Exception {
		String locNamePattern = PropertiesHandler.expand(this.namePattern, MapUtils.convertToHMStringObject(properties));
		this.pattern = Pattern.compile(locNamePattern);
	}

	/**
	 * Resolves the meatadata in search file pattern definition.
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public void compileNamePattern(Object propsObj) throws Exception {
		String locNamePattern = PropertiesHandler.expand(this.namePattern, null, propsObj);
		this.pattern = Pattern.compile(locNamePattern);
	}


	@Override
	public boolean accept(Path path) {
		boolean fileTypeMatches = false;
		boolean nameMatches = false;
		boolean isModified = false;

		try {
			boolean isFile = !fileSystem.isDirectory(path);
			
			fileTypeMatches = ((fileType == ALL) || ((fileType == FILES_ONLY) && isFile) || ((fileType == DIRECTORIES_ONLY) && !isFile));
			
			if (fileTypeMatches) {
				FileStatus file = fileSystem.getFileStatus(path);

				if (pattern != null) {
					Matcher m = pattern.matcher(file.getPath().toString());
					nameMatches = m.matches();
				}
				else {
					nameMatches = true;
				}
				
				if (nameMatches) {
					if (checkLastModified) {
						isModified = selectModifiedSince ? (file.getModificationTime() > lastTimestamp) : (file.getModificationTime() <= lastTimestamp);
					}
					else {
						isModified = true;
					}
				}
			}
		}
		catch(Exception ex)
		{
			logger.error("Generic error filtering data from HDFS path: "+path, ex);
		}

		return fileTypeMatches && nameMatches && isModified;
	}


	public static int getFileType(String fileType) {
		if (fileType.equals("all")) {
			return ALL;
		}
		else if (fileType.equals("directories-only")) {
			return DIRECTORIES_ONLY;
		}
		else {
			return FILES_ONLY;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String regExp = (pattern != null) ? pattern.pattern() : namePattern;
		if (fileType == ALL) {
			return regExp + "||all";
		}
		else if (fileType == DIRECTORIES_ONLY) {
			return regExp + "||directories-only";
		}
		return regExp + "||files-only";
	}

}
