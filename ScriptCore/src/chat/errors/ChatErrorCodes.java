package chat.errors;



/**
 */
public interface ChatErrorCodes {
   
	
	public static final int CODE_CORE = 10000;
	
	
	public static final int ERROR_ONLINESERVER_NOT_FOUND = CODE_CORE + 121;
	public static final int ERROR_ONLINESERVER_UPDATE_FAILED = CODE_CORE + 122;
	public static final int ERROR_ONLINESERVER_DELETE_FAILED = CODE_CORE + 123;
	public static final int ERROR_ONLINESERVER_ADD_FAILED = CODE_CORE + 124;
	public static final int ERROR_ONLINESERVER_QUERY_FAILED = CODE_CORE + 125;
	
	public static final int ERROR_USERPRESENT_ADD_FAILED = CODE_CORE + 126;
	public static final int ERROR_USERPRESENT_UPDATE_FAILED = CODE_CORE + 127;
	public static final int ERROR_USERPRESENT_QUERY_FAILED = CODE_CORE + 128;
	public static final int ERROR_USERPRESENT_NOTFOUND = CODE_CORE + 129;
	public static final int ERROR_USERPRESENT_DELETE_FAILED = CODE_CORE + 130;
	public static final int ERROR_ILLEGAL_PARAMETER = CODE_CORE + 131;
	public static final int ERROR_SSH_CONNECT_FAILED = CODE_CORE + 132;
	public static final int ERROR_SSH_EXEC_FAILED = CODE_CORE + 133;
	public static final int ERROR_MESSAGEADD_FAILED = CODE_CORE + 134;
	public static final int ERROR_ITERATOR_NULL = CODE_CORE + 135;
	public static final int ERROR_UNKNOWN = CODE_CORE + 136;
	public static final int ERROR_CORE_LOADRESOURCE_FAILED = CODE_CORE + 137;
	public static final int ERROR_CORE_LOADRESOURCE_NOT_EXIST = CODE_CORE + 138;
	public static final int ERROR_CORE_UPLOAD_DB_FAILED = CODE_CORE + 139;
	public static final int ERROR_CORE_ZKDATA_PERSISTENT_FAILED = CODE_CORE + 140;
	public static final int ERROR_CORE_ZKENSUREPATH_FAILED = CODE_CORE + 141;
	public static final int ERROR_CORE_ZKGETDATA_FAILED = CODE_CORE + 142;
	public static final int ERROR_CORE_ZKDATA_RESURRECT_FAILED = CODE_CORE + 143;
	public static final int ERROR_CORE_ZKDATA_NEWINSTANCE_FAILED = CODE_CORE + 144;
	public static final int ERROR_CORE_ZKADDWATCHEREX_FAILED = CODE_CORE + 145;
	public static final int ERROR_ZK_DISCONNECTED = CODE_CORE + 146;
	public static final int ERROR_CORE_SERVERPORT_ILLEGAL = CODE_CORE + 147;
	public static final int ERROR_DAOINIT_FAILED = CODE_CORE + 148;
	public static final int ERROR_ILLEGAL_ENCODE = CODE_CORE + 149;
	public static final int ERROR_READCONTENT_FAILED = CODE_CORE + 150;
	public static final int ERROR_UPLOAD_FAILED = CODE_CORE + 151;
	public static final int ERROR_PARSE_REQUEST_FAILED = CODE_CORE + 152;
	public static final int ERROR_ACCOUNTNAME_ILLEGAL = CODE_CORE + 153;
	public static final int ERROR_ACCOUNTDOMAIN_ILLEGAL = CODE_CORE + 154;
	public static final int ERROR_IO_FAILED = CODE_CORE + 155;
	public static final int ERROR_LOGINUSER_NOT_FOUND = CODE_CORE + 156;
	public static final int ERROR_BALANCER_NOT_READY = CODE_CORE + 157;
	public static final int ERROR_CHARACTER_OVER_MAXIMUM_LIMITS = CODE_CORE + 158;
	public static final int ERROR_TASKADD_FAILED = CODE_CORE + 159;
	public static final int ERROR_FILE_EMPTY = CODE_CORE + 160;
	
	
	//Groovy related codes. 
	public static final int ERROR_GROOVY_CLASSNOTFOUND = CODE_CORE + 8001;
	public static final int ERROR_GROOY_NEWINSTANCE_FAILED = CODE_CORE + 8002;
	public static final int ERROR_GROOY_CLASSCAST = CODE_CORE + 8003;
	public static final int ERROR_GROOVY_INVOKE_FAILED = CODE_CORE + 8004;
	public static final int ERROR_GROOVYSERVLET_SERVLET_NOT_INITIALIZED = CODE_CORE + 8005;
	public static final int ERROR_URL_PARAMETER_NULL = CODE_CORE + 8006;
	public static final int ERROR_URL_VARIABLE_NULL = CODE_CORE + 8007;
	public static final int ERROR_GROOVY_PARSECLASS_FAILED = CODE_CORE + 8008;
	public static final int ERROR_GROOVY_UNKNOWN = CODE_CORE + 8009;
	public static final int ERROR_GROOVY_CLASSLOADERNOTFOUND = CODE_CORE + 8010;
	public static final int ERROR_JAVASCRIPT_LOADFILE_FAILED = CODE_CORE + 8011;
	public static final int ERROR_URL_HEADER_NULL = CODE_CORE + 8012;







}
