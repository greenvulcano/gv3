package it.greenvulcano.util.remotefs.hdfs.utility;

public interface HDFSParamsKey
{
	public String	deleteSource			= "dfs.file.delete-source"; //
	public String	useRawLocalFileSystem	= "dfs.file.raw-local-filesystem"; //
	public String	bufferSize				= "dfs.stream-buffer-size";

	public String 	overwrite				= "dfs.file.overwrite"; //

	public String	permission				= "dfs.permissions.path"; //
	public String	replication				= "dfs.replication";
	public String	blockSize				= "dfs.blocksize";
	public String	ownerUserName			= "dfs.permissions.user";
	public String	ownerGroupName			= "dfs.permissions.group";
}
