package com.docker.errors;



/**
 * General common error codes(0 ~ 999), other service has its own error codes, such as 
 * AcuComErrorCodes(1000 ~ 3999), AccountsErrorCodes(5000 ~ 5999), WallboardErrorCodes(6000 ~ 6999),
 * DocsErrorCodes(7000 ~ 7999), specially, Session error code(4000 ~ 4999) 
 */
public interface CoreErrorCodes {
   
	
	public static final int CODE_CORE = 0;//0~999
	public static final int CODE_MEMBERSHIP = 3000;//3000~3999
	public static final int CODE_SESSION = 4000;//4000~4999
	
	public static final int ERROR_SESSION_NOT_EXIST = CODE_SESSION + 1;
	public static final int ERROR_SESSION_ILLEGAL = CODE_SESSION + 2;
	public static final int ERROR_SESSION_USER_ID_NOT_EXIST = CODE_SESSION + 3;
	public static final int ERROR_SESSION_FIRM_ID_NOT_EXIST = CODE_SESSION + 4;
	public static final int ERROR_SESSION_TERMINAL_NOT_EXIST = CODE_SESSION + 5;
	public static final int ERROR_SESSION_HTTPCHANNELID_INCORRECT = CODE_SESSION + 6;
	public static final int ERROR_SESSION_SERVER_INVALID = CODE_SESSION + 7;
	public static final int ERROR_SESSION_PACKAGE_ILLEGAL = CODE_SESSION + 8;
	public static final int ERROR_SESSION_SERVER_SHUTTINGDOWN= CODE_SESSION + 9;
	
	public static final int ERROR_CORE_NOT_SUPPORTED = CODE_CORE + 1;
	public static final int ERROR_CORE_QUEUEPATH_NOT_EXIST = CODE_CORE + 2;
	public static final int ERROR_CORE_QUEUE_ADDELEMENT_FAILED = CODE_CORE + 3;
	public static final int ERROR_CORE_QUEUEELEMENT_OUTPUT_FAILED = CODE_CORE + 4;
	public static final int ERROR_CORE_QUEUE_CONSUME_T_NULL = CODE_CORE + 5;
	public static final int ERROR_CORE_QUEUE_CONSUME_GETCHILDREN_FAILED = CODE_CORE + 6;
	public static final int ERROR_CORE_QUEUE_CONSUME_INTERRUPTED = CODE_CORE + 7;
	public static final int ERROR_CORE_QUEUE_CONSUME_FAILED = CODE_CORE + 8;
	public static final int ERROR_CORE_QUEUE_CONSUME_DATA_NULL = CODE_CORE + 9;
	public static final int ERROR_CORE_CONSUME_DATA_RESURRECT_FAILED = CODE_CORE + 10;
	public static final int ERROR_CORE_QUEUE_NODEEXIST = CODE_CORE + 11;
	public static final int ERROR_CORE_ZOOKEEPERPATH_ILLEGAL = CODE_CORE + 12;
	public static final int ERROR_CORE_ZOOKEEPERPATH_CONNECTFAILED = CODE_CORE + 13;
	public static final int ERROR_CORE_ZOOKEEPERPATH_DELETE_FAILED = CODE_CORE + 14;
	public static final int ERROR_CORE_ZKADDWATCHEREX_FAILED = CODE_CORE + 15;
	public static final int ERROR_SSH_CONNECT_FAILED = CODE_CORE + 16;
	public static final int ERROR_SSH_EXEC_FAILED = CODE_CORE + 17;
	public static final int ERROR_CORE_SERVERPORT_ILLEGAL = CODE_CORE + 18;
	public static final int ERROR_CORE_ZKSERVER_SAVE_FAILED = CODE_CORE + 19;
	public static final int ERROR_CORE_ZKDATA_PERSISTENT_FAILED = CODE_CORE + 20;
	public static final int ERROR_CORE_ZKENSUREPATH_FAILED = CODE_CORE + 21;
	public static final int ERROR_CORE_ZKGETDATA_FAILED = CODE_CORE + 22;
	public static final int ERROR_CORE_ZKDATA_RESURRECT_FAILED = CODE_CORE + 23;
	public static final int ERROR_CORE_ZKDATA_NEWINSTANCE_FAILED = CODE_CORE + 24;
	public static final int ERROR_CORE_ADDSERVER_BAD_RESPONSECODE = CODE_CORE + 25;
	public static final int ERROR_CORE_ADDSERVER_JSON_PARSE_FAILED = CODE_CORE + 26;
	public static final int ERROR_CORE_ADDSERVER_INPUTSTREAM_NULL = CODE_CORE + 27;
	public static final int ERROR_CORE_ADDSERVER_REQUEST_ERROR = CODE_CORE + 28;
	public static final int ERROR_CORE_ADDSERVER_RESPONSE_ILLEGAL = CODE_CORE + 29;
	public static final int ERROR_CORE_ADDSERVER_CONNECT_FAILED = CODE_CORE + 30;

	public static final int ERROR_CORE_UPLOAD_DB_FAILED = CODE_CORE + 31;
	public static final int ERROR_CORE_LOADRESOURCE_NOT_EXIST = CODE_CORE + 32;
	public static final int ERROR_CORE_LOADRESOURCE_FAILED = CODE_CORE + 33;
	public static final int ERROR_CORE_QUERYFIRM_DB_FAILED = CODE_CORE + 34;
	public static final int ERROR_CORE_FIRMSUBDOMAIN_DUPLICATED = CODE_CORE + 35;
	public static final int ERROR_CORE_ADDFIRM_FAILED = CODE_CORE + 36;
	public static final int ERROR_CORE_QUERYGROUPUSERINDEX_FAILED = CODE_CORE + 37;
	public static final int ERROR_CORE_ID_ILLEGAL = CODE_CORE + 38;
	public static final int ERROR_CORE_FIRMNAME_ILLEGAL = CODE_CORE + 39;
	public static final int ERROR_CORE_SUBDOMAIN_ILLEGAL = CODE_CORE + 40;
	public static final int ERROR_CORE_DELETEFIRM_FAILED = CODE_CORE + 41;
	public static final int ERROR_CORE_USERGROUP_NAME_IS_NULL = CODE_CORE + 42;
	public static final int ERROR_CORE_ROLEID_ILLEGAL = CODE_CORE + 43;
	public static final int ERROR_CORE_DELETEUSERTOUSERGROUP_DB_FAILED = CODE_CORE + 44;
	public static final int ERROR_CORE_SEARCHGROUPUSERINDEX_FAILED = CODE_CORE + 45;
	public static final int ERROR_CORE_UPDATEUSERTOUSERGROUP_DB_FAILED = CODE_CORE + 46;
	public static final int ERROR_CORE_ADDUSERTOUSERGROUP_DB_FAILED = CODE_CORE + 48;
	public static final int ERROR_CORE_USERG = CODE_CORE + 49;
	public static final int ERROR_CORE_DELETEUSERGROUP_DB_FAILED = CODE_CORE + 50;
	public static final int ERROR_CORE_QUERYUSERGROUP_DB_FAILED = CODE_CORE + 51;
	public static final int ERROR_CORE_ADDUSERGROUP_DB_FAILED = CODE_CORE + 52;
	public static final int ERROR_CORE_UPDATEUSERGROUP_DB_FAILED = CODE_CORE + 53;
	public static final int ERROR_QUERYUSER_DB_FAILED = CODE_CORE + 54;
	public static final int ERROR_UPDATEUSER_NO_UPDATE = CODE_CORE + 55;
	public static final int ERROR_LOGIN_DB_FAILED = CODE_CORE + 56;
	/**
	 * Use for mobile to jump out from auto login. 
	 */
	public static final int ERROR_LOGIN_FAILED = CODE_CORE + 57;
	public static final int ERROR_ID_ILLEGAL = CODE_CORE + 58;
	public static final int ERROR_ACCOUNTNAME_ILLEGAL = CODE_CORE + 59;
	public static final int ERROR_PASSWORD_ILLEGAL = CODE_CORE + 60;
	public static final int ERROR_TERMINAL_ILLEGAL = CODE_CORE + 61;
	public static final int ERROR_DEVICETOKEN_ILLEGAL = CODE_CORE + 62;

	public static final int ERROR_OAUTH_LOGINURL_NULL = CODE_CORE + 63;
	public static final int ERROR_OAUTH_CLIENTKEY_NULL = CODE_CORE + 64;

	public static final int ERROR_CORE_ADDSESSION_FAILED = CODE_CORE + 66;
	public static final int ERROR_CORE_QUERYSESSION_FAILED = CODE_CORE + 67;
	public static final int ERROR_CORE_ADDCLIENT_FAILED = CODE_CORE + 68;
	public static final int ERROR_CORE_QUERYCLIENT_FAILED = CODE_CORE + 69;
	public static final int ERROR_CORE_UPDATECLIENT_FAILED = CODE_CORE + 70;
	public static final int ERROR_CORE_REGISTERADMINIDS_FAILED = CODE_CORE + 71;
	public static final int ERROR_CORE_UNREGISTERADMINIDS_FAILED = CODE_CORE + 72;
	public static final int ERROR_CORE_QUERYMANAGEDUSERGROUPS_FAILED = CODE_CORE + 73;
	public static final int ERROR_CORE_ADDCLIENTAUTHORIZEDUSERSANDUSERGROUPS_FAILED = CODE_CORE + 74;
	public static final int ERROR_SEARCH_NOT_AVAILABLE = CODE_CORE + 75;
	public static final int ERROR_SEARCH_TOPIC_FAILED = CODE_CORE + 76;
	public static final int ERROR_CORE_SEARCHUSERGROUPS_FAILED = CODE_CORE + 77;
	public static final int ERROR_AUTOINC_FAILED = CODE_CORE + 78;
	public static final int ERROR_AUTOINC_GENERATE_FAILED = CODE_CORE + 79;
	public static final int ERROR_MESSAGE_SEQUENCE_NEED_NULL = CODE_CORE + 80;
	public static final int ERROR_MESSAGEADD_FAILED = CODE_CORE + 81;
	public static final int ERROR_ITERATOR_NULL = CODE_CORE + 82;
	public static final int ERROR_CORE_MESSAGEQUEUE_INITSEQUENCEAFTER_FAILED = CODE_CORE + 83;

	public static final int ERROR_ILLEGAL_ENCODE = CODE_CORE + 84;
	public static final int ERROR_READCONTENT_FAILED = CODE_CORE + 85;
	public static final int ERROR_UPLOAD_FAILED = CODE_CORE + 86;
	public static final int ERROR_ACCOUNTDOMAIN_ILLEGAL = CODE_CORE + 87;
	public static final int ERROR_PARSE_REQUEST_FAILED = CODE_CORE + 88;
	
	public static final int ERROR_UNKNOWN = CODE_CORE + 89;
    public static final int ERROR_URLENCODE_FAILED = CODE_CORE + 90;
    public static final int ERROR_TERMINAL_NOT_EXIST = CODE_CORE + 91;
    public static final int ERROR_USERID_NOT_EXIST = CODE_CORE + 92;
    public static final int ERROR_FIRM_NULL = CODE_CORE + 93;
    public static final int ERROR_LOGINACCOUNT_NOT_FOUND = CODE_CORE + 94;
    public static final int ERROR_CLIENT_NOT_AUTHORIZED = CODE_CORE + 95;
    public static final int ERROR_USERLOGIN_CLIENT_NOT_AUTHORISED = CODE_CORE + 96;
    public static final int ERROR_ILLEGAL_PARAMETER = CODE_CORE + 97;
    public static final int ERROR_DELETE_FAILED = CODE_CORE + 98;
    public static final int ERROR_USERGROUP_TYPE_ILLEGAL = CODE_CORE + 99;
    public static final int ERROR_USERGROUP_NAME_IS_NULL = CODE_CORE + 100;
    public static final int ERROR_PASSWORD_DECODE_FAILED = CODE_CORE + 101;
    public static final int ERROR_FIRM_NOT_FOUND = CODE_CORE + 102;
    public static final int ERROR_UPDATE_FAILED = CODE_CORE + 103;

	public static final int ERROR_DOMAIN_CONFLICT = CODE_CORE + 104;
	public static final int ERROR_DAOINIT_FAILED = CODE_CORE + 105;
	public static final int ERROR_ADD_WALLBOARD_TOPIC_FAILED = CODE_CORE + 106;
	public static final int ERROR_ACCOUNT_EXIST = CODE_CORE + 107;
	public static final int ERROR_USERGROUP_NOTFOUND = CODE_CORE + 108;
	public static final int ERROR_SEARCHKEY_BLANK = CODE_CORE + 109;
	public static final int ERROR_QUERY_DB_FAILED = CODE_CORE + 110;
	public static final int ERROR_SAVE2_DB_FAILED = CODE_CORE + 111;
	public static final int ERROR_SAVE2_DISK_FAILED = CODE_CORE + 112;
	public static final int ERROR_READ_DATA_FROM_DISK = CODE_CORE + 113;
	public static final int ERROR_CORE_ADDFIRMADMIN_FAILED = CODE_CORE + 114;
	public static final int ERROR_CORE_DELETEFIRMADMIN_FAILED = CODE_CORE + 115;
	public static final int ERROR_HTTP_CODE_FAILED = CODE_CORE + 116;
	public static final int ERROR_HTTP_SERVER_FAILED = CODE_CORE + 117;
	public static final int ERROR_HTTP_IO_FAILED = CODE_CORE + 118;
	public static final int ERROR_CLIENT_NOTFOUND = CODE_CORE + 119;
	public static final int ERROR_CORE_DELETECLIENT_FAILED = CODE_CORE + 120;
	
	public static final int ERROR_ZK_DISCONNECTED = CODE_CORE + 121;
	public static final int ERROR_DEVICETOKEN_LOGIN_FAILED = CODE_CORE + 122;
	
	public static final int ERROR_RMICALL_FAILED = CODE_CORE + 123;
	public static final int ERROR_RPC_PERSISTENT_FAILED = CODE_CORE + 124;
	public static final int ERROR_RPC_REQUESTTYPE_ILLEGAL = CODE_CORE + 125;
	public static final int ERROR_RPC_REQUESTDATA_NULL = CODE_CORE + 126;
	public static final int ERROR_RPC_TYPE_NOMAPPING = CODE_CORE + 127;
	public static final int ERROR_RPC_TYPE_REQUEST_NOMAPPING = CODE_CORE + 128;
	public static final int ERROR_RPC_TYPE_RESPONSE_NOMAPPING = CODE_CORE + 129;
	public static final int ERROR_RPC_RESURRECT_FAILED = CODE_CORE + 130;
	public static final int ERROR_RPC_TYPE_NOSERVERADAPTER = CODE_CORE + 131;
	public static final int ERROR_RPC_ENCODE_PB_PARSE_FAILED = CODE_CORE + 132;
	public static final int ERROR_RPC_ENCODER_NOTFOUND = CODE_CORE + 133;
	public static final int ERROR_RPC_ENCODER_NULL = CODE_CORE + 134;
	public static final int ERROR_RSA_DECRYPT_FAILED = CODE_CORE + 135;
	public static final int ERROR_BALANCER_CHATSERVER_FAILED = CODE_CORE + 136;
	public static final int ERROR_BALANCER_CHATSERVER_STATUS_ILLEGAL = CODE_CORE + 137;
	public static final int ERROR_BALANCER_MONITORACTION_ILLEGAL = CODE_CORE + 138;
	public static final int ERROR_BALANCER_CHATSERVER_ISCHANGINGSTATUS = CODE_CORE + 139;
	public static final int ERROR_BALANCER_CHATSERVER_NOTFOUND = CODE_CORE + 140;
	public static final int ERROR_BALANCER_DOMAIN_NEEDED = CODE_CORE + 141;
	public static final int ERROR_BALANCER_CHATSERVER_WRONGSERVER = CODE_CORE + 142;
	public static final int ERROR_BALANCER_NO_CHATSERVER_AVAILABLE = CODE_CORE + 143;
	public static final int ERROR_BALANCER_CHATSERVER_NOTEXIST = CODE_CORE + 144;
	public static final int ERROR_REPORT_SORT_WRONG = CODE_CORE + 145;
	public static final int ERROR_REPORT_ADD_FAILED = CODE_CORE + 146;
	public static final int ERROR_REPORT_QUERY_FAILED = CODE_CORE + 147;
	public static final int ERROR_REPORT_UPDATE_FAILED = CODE_CORE + 148;
	public static final int ERROR_REPORT_DELETE_FAILED = CODE_CORE + 149;
	public static final int ERROR_REPORT_ORDER_WRONG = CODE_CORE + 150;
	public static final int ERROR_REPORT_STATE_WRONG = CODE_CORE + 151;
	public static final int ERROR_DUPLICATE_KEY_ERROR_INDEX = CODE_CORE + 152;
	public static final int ERROR_USERSTATUS_QUERY_DB_FAILED = CODE_CORE + 153;
	public static final int ERROR_MOMENT_ADD_FAILED = CODE_CORE + 154;
	public static final int ERROR_MOMENT_DELETE_FAILED = CODE_CORE + 155;
	public static final int ERROR_MOMENT_QUERY_FAILED = CODE_CORE + 156;
	public static final int ERROR_MOMENT_CITY_LENGTH_WRONG = CODE_CORE + 157;
	public static final int ERROR_MOMENT_LIKE_ID_EXIST = CODE_CORE + 158;
	public static final int ERROR_UPDATE_MOMENT_LIKE_ID_ERROR = CODE_CORE + 159;
	public static final int ERROR_COMMENT_ADD_FAILED = CODE_CORE + 160;
	public static final int ERROR_COMMENT_DELETE_FAILED = CODE_CORE + 161;
	public static final int ERROR_COMMENT_QUERY_FAILED = CODE_CORE + 162;
	public static final int ERROR_INCREASE_COMMENTCOUNT_ERROR = CODE_CORE + 163;
	public static final int ERROR_DECREASE_COMMENTCOUNT_ERROR = CODE_CORE + 164;
	public static final int ERROR_BALANCERDATA_MOSTIDLE_FAILED = CODE_CORE + 165;
	public static final int ERROR_TICKET_ADD_FAILED = CODE_CORE + 166;
	public static final int ERROR_TICKET_DELETE_FAILED = CODE_CORE + 167;
	public static final int ERROR_USE_TICKET_FAILED = CODE_CORE + 168;
	public static final int ERROR_BALANCERDATA_LOGIN_FAILED = CODE_CORE + 169;
	public static final int ERROR_BALANCERDATA_SAVED = CODE_CORE + 170;
	public static final int ERROR_BALANCERDATA_QUERY = CODE_CORE + 171;
	public static final int ERROR_SELECTBALANCER_NOBEST = CODE_CORE + 172;
	public static final int ERROR_SELECTBALANCER_LOGINFAILED = CODE_CORE + 173;
	public static final int ERROR_UPDATE_MOMENT_LIKE_COUNT_ERROR = CODE_CORE + 168;
	public static final int ERROR_CHARACTER_OVER_MAXIMUM_LIMITS = CODE_CORE + 169;
	public static final int ERROR_BALANCERDATA_ALREADY_LOGIN = CODE_CORE + 170;
	public static final int ERROR_BALANCERDATA_NOTAVAILABLE = CODE_CORE + 171;
	public static final int ERROR_NOT_IMPLEMENTED = CODE_CORE + 172;
	public static final int ERROR_URL_VARIABLE_NULL = CODE_CORE + 173;
	public static final int ERROR_URL_PARAMETER_NULL = CODE_CORE + 174;
	public static final int ERROR_RPC_DISCONNECTED = CODE_CORE + 175;
	public static final int ERROR_BALANCERDATA_STATUS_ILLEGAL = CODE_CORE + 176;
	public static final int ERROR_FORBIDDEN = CODE_CORE + 177;
	public static final int ERROR_EXCEEDED_MAX_RECORDS = CODE_CORE + 178;
	public static final int ERROR_BALANCERDATA_NOTFOUND = CODE_CORE + 179;
	public static final int ERROR_CONTACT_GROUP_ADD_FAILED = CODE_CORE + 180;
	public static final int ERROR_CONTACT_GROUP_UPDATE_FAILED = CODE_CORE + 181;
	public static final int ERROR_CONTACT_GROUP_QUERY_FAILED = CODE_CORE + 182;
	public static final int ERROR_CONTACT_GROUP_DELETE_FAILED = CODE_CORE + 183;
	public static final int ERROR_CONTACT_GROUP_MEMBER_ADD_FAILED = CODE_CORE + 184;
	public static final int ERROR_CONTACT_GROUP_MEMBER_UPDATE_FAILED = CODE_CORE + 185;
	public static final int ERROR_CONTACT_GROUP_MEMBER_QUERY_FAILED = CODE_CORE + 186;
	public static final int ERROR_CONTACT_GROUP_MEMBER_DELETE_FAILED = CODE_CORE + 187;
	public static final int ERROR_STICKERSUIT_ADD_FAILED = CODE_CORE + 188;
	public static final int ERROR_STICKERSUIT_UPDATE_FAILED = CODE_CORE + 189;
	public static final int ERROR_STICKERSUIT_QUERY_FAILED = CODE_CORE + 190;
	public static final int ERROR_STICKERSUIT_DELETE_FAILED = CODE_CORE + 191;
	public static final int ERROR_RPC_ILLEGAL = CODE_CORE + 192;
	public static final int ERROR_STRING_ENCODE_FAILED = CODE_CORE + 193;
	public static final int ERROR_TCPCHANNEL_ENCODE_ILLEGAL = CODE_CORE + 194;
	public static final int ERROR_HAILPACK_IO_ERROR = CODE_CORE + 195;
	public static final int ERROR_HAILPACK_UNKNOWNERROR = CODE_CORE + 196;
	public static final int ERROR_OFFLINETOPIC_MISSING_MESSAGE = CODE_CORE + 197;
	public static final int ERROR_OFFLINETOPIC_MISSING_PENDINGUSERIDS = CODE_CORE + 198;
	public static final int ERROR_OFFLINETOPIC_SAVE_FAILED = CODE_CORE + 199;
	public static final int ERROR_OFFLINETOPIC_QUERY_FAILED = CODE_CORE + 200;
	public static final int ERROR_OFFLINETOPIC_REMOVE_FAILED = CODE_CORE + 201;
	public static final int ERROR_MESSAGESENDING_NOTPREPARED = CODE_CORE + 202; 
	////////////Need clear cache. 
	public static final int ERROR_RMICALL_CONNECT_FAILED = CODE_CORE + 203;
	public static final int ERROR_RMICALL_TARGET_NOTPRESENT = CODE_CORE + 204;
	public static final int ERROR_RMICALL_TARGET_ONANOTHERSERVER = CODE_CORE + 205;
	public static final int ERROR_RMICALL_RETRY = CODE_CORE + 206;
	//////////////
	public static final int ERROR_APPLICATION_DELETE_FAILED = CODE_CORE + 207;
	public static final int ERROR_APPLICATION_UPDATE_FAILED = CODE_CORE + 208;
	public static final int ERROR_APPLICATION_QUERY_FAILED = CODE_CORE + 209;
	public static final int ERROR_TARGETID_NOTINBALANCER = CODE_CORE + 210;
	public static final int ERROR_SEND_MESSAGE_FAILED = CODE_CORE + 211;
	public static final int ERROR_APPLICATION_ADD_FAILED = CODE_CORE + 212;
	public static final int ERROR_GROUP_TICKET_ADD_FAILED = CODE_CORE + 213;
	public static final int ERROR_GROUP_TICKET_DELETE_FAILED = CODE_CORE + 214;
	public static final int ERROR_USE_GROUP_TICKET_FAILED = CODE_CORE + 215;
	public static final int ERROR_GROUP_TICKET_OVERDUE = CODE_CORE + 216;
	public static final int ERROR_STRINGS_GETUPDATESTRINGS_PARAMETER_NULL = CODE_CORE + 217;
	public static final int ERROR_SEND_MESSAGE_TO_OWN = CODE_CORE + 218;
	public static final int ERROR_SERVERROUTE_ADD_FAILED = CODE_CORE + 219;
	public static final int ERROR_SERVERROUTE_DELETE_FAILED = CODE_CORE + 220;
	public static final int ERROR_SERVERROUTE_UPDATE_FAILED = CODE_CORE + 221;
	public static final int ERROR_SERVERROUTE_QUERY_FAILED = CODE_CORE + 222;
	public static final int ERROR_SERVERROUTE_NULL = CODE_CORE + 223;
	public static final int ERROR_CITY_NULL = CODE_CORE + 224;
	public static final int ERROR_LOCATION_NULL = CODE_CORE + 225;
	public static final int ERROR_CITY_LOCATION_ADD_FAILED = CODE_CORE + 226;
	public static final int ERROR_CITY_LOCATION_DELETE_FAILED = CODE_CORE + 227;
	public static final int ERROR_CITY_LOCATION_UPDATE_FAILED = CODE_CORE + 228;
	public static final int ERROR_CITY_LOCATION_QUERY_FAILED = CODE_CORE + 229;
	public static final int ERROR_LOCATIONNAME_NULL = CODE_CORE + 230;
	public static final int ERROR_APPVERSION_UPDATE_FAILED = CODE_CORE + 231;
	public static final int ERROR_APPVERSION_QUERY_FAILED = CODE_CORE + 232;
	public static final int ERROR_APPVERSION_DOWNLOAD_NULL = CODE_CORE + 233;
	public static final int ERROR_APPVERSION_LATESTVERSION_NULL = CODE_CORE + 234;
	public static final int ERROR_APPVERSION_MINVERSION_NULL = CODE_CORE + 235;
	public static final int ERROR_APPVERSION_RELEASENOTES_NULL = CODE_CORE + 236;
	public static final int ERROR_APPVERSION_UPDATETIME_NULL = CODE_CORE + 237;
	public static final int ERROR_APPVERSION_TERMINAL_NULL = CODE_CORE + 238;
	public static final int ERROR_TAG_ADD_FAILED = CODE_CORE + 240;
	public static final int ERROR_TAG_UPDATE_FAILED = CODE_CORE + 241;
	public static final int ERROR_TAG_QUERY_FAILED = CODE_CORE + 242;
	public static final int ERROR_TAG_SIMPLEMOMENT_QUERY_FAILED = CODE_CORE + 243;
	public static final int ERROR_HOTMOMENT_ADD_FAILED = CODE_CORE + 244;
	public static final int ERROR_HOTMOMENT_UPDATE_FAILED = CODE_CORE + 245;
	public static final int ERROR_HOTMOMENT_QUERY_FAILED = CODE_CORE + 246;
	public static final int ERROR_HOTMOMENT_SIMPLEMOMENT_QUERY_FAILED = CODE_CORE + 247;
	public static final int ERROR_AFFILIATE_UPDATE_FAILED = CODE_CORE + 248;
	public static final int ERROR_AFFILIATE_DELETE_FAILED = CODE_CORE + 249;
	public static final int ERROR_AFFILIATE_QUERY_FAILED = CODE_CORE + 250;
	public static final int ERROR_AFFILIATE_ID_NULL = CODE_CORE + 251;
	public static final int ERROR_VERSION_UPDATE_FAILED = CODE_CORE + 252;
	public static final int ERROR_VERSION_ADD_FAILED = CODE_CORE + 253;
	public static final int ERROR_VERSION_GET_FAILED = CODE_CORE + 254;
	public static final int ERROR_VERSION_QUERY_FAILED = CODE_CORE + 255;
	public static final int ERROR_BODY_PARAMETER_NULL = CODE_CORE + 256;
	public static final int ERROR_PERSISTENT_FAILED = CODE_CORE + 257;
	public static final int ERROR_RESURRECT_FAILED = CODE_CORE + 258;
	public static final int ERROR_USER_NOTIN_BALANCER = CODE_CORE + 259;
	public static final int ERROR_UDP_EXCEED_MAXMTU = CODE_CORE + 260;
	public static final int ERROR_PACKET_IO_ERROR = CODE_CORE + 261;
	public static final int ERROR_SETTINGS_ADD_FAILED = CODE_CORE + 262;
	public static final int ERROR_BALANCER_ILLEGAL_URL = CODE_CORE + 263;
	public static final int ERROR_SETTINGS_QUERY_FAILED = CODE_CORE + 264;
	public static final int ERROR_BANNER_ADD_FAILED = CODE_CORE + 265;
	public static final int ERROR_BANNER_QUERY_FAILED = CODE_CORE + 266;
	public static final int ERROR_BANNER_UPDATE_FAILED = CODE_CORE + 267;
	public static final int ERROR_BANNER_DELETE_FAILED = CODE_CORE + 268;
	public static final int ERROR_BANNER_PARAMETER_NULL = CODE_CORE + 269;
	public static final int ERROR_BATCHMEDIARESOURCE_ADD_FAILED = CODE_CORE + 270;
	public static final int ERROR_BATCHMEDIARESOURCE_QUERY_FAILED = CODE_CORE + 271;
	public static final int ERROR_BATCHMEDIARESOURCE_UPDATE_FAILED = CODE_CORE + 272;
	public static final int ERROR_BATCHMEDIARESOURCE_DELETE_FAILED = CODE_CORE + 273;
	public static final int ERROR_TAGINTERVENTION_ADD_FAILED = CODE_CORE + 274;
	public static final int ERROR_TAGINTERVENTION_QUERY_FAILED = CODE_CORE + 275;
	public static final int ERROR_TAGINTERVENTION_UPDATE_FAILED = CODE_CORE + 276;
	public static final int ERROR_TAGINTERVENTION_DELETE_FAILED = CODE_CORE + 278;
	public static final int ERROR_MOMENTINTERVENTION_ADD_FAILED = CODE_CORE + 279;
	public static final int ERROR_MOMENTINTERVENTION_QUERY_FAILED = CODE_CORE + 280;
	public static final int ERROR_MOMENTINTERVENTION_UPDATE_FAILED = CODE_CORE + 281;
	public static final int ERROR_MOMENTINTERVENTION_DELETE_FAILED = CODE_CORE + 282;
	public static final int ERROR_TOPICNOTIFICATION_ADD_FAILED = CODE_CORE + 283;
	public static final int ERROR_TOPICNOTIFICATION_QUERY_FAILED = CODE_CORE + 284;
	public static final int ERROR_TOPICNOTIFICATION_UPDATE_FAILED = CODE_CORE + 285;
	public static final int ERROR_TOPICNOTIFICATION_DELETE_FAILED = CODE_CORE + 286;
	public static final int ERROR_NOTIFICATION_ADD_FAILED = CODE_CORE + 287;
	public static final int ERROR_NOTIFICATION_QUERY_FAILED = CODE_CORE + 288;
	public static final int ERROR_NOTIFICATION_UPDATE_FAILED = CODE_CORE + 289;
	public static final int ERROR_NOTIFICATION_DELETE_FAILED = CODE_CORE + 290;
	public static final int ERROR_ACTIVITY_ADD_FAILED = CODE_CORE + 291;
	public static final int ERROR_ACTIVITY_QUERY_FAILED = CODE_CORE + 292;
	public static final int ERROR_ACTIVITY_UPDATE_FAILED = CODE_CORE + 293;
	public static final int ERROR_ACTIVITY_DELETE_FAILED = CODE_CORE + 294;
	public static final int ERROR_VIPPRODUCT_ADD_FAILED = CODE_CORE + 295;
	public static final int ERROR_VIPPRODUCT_QUERY_FAILED = CODE_CORE + 296;
	public static final int ERROR_VIPPRODUCT_UPDATE_FAILED = CODE_CORE + 297;
	public static final int ERROR_VIPPRODUCT_DELETE_FAILED = CODE_CORE + 298;
	public static final int ERROR_VIPORDER_ADD_FAILED = CODE_CORE + 299;
	public static final int ERROR_VIPORDER_QUERY_FAILED = CODE_CORE + 300;
	public static final int ERROR_VIPORDER_UPDATE_FAILED = CODE_CORE + 301;
	public static final int ERROR_VIPORDER_DELETE_FAILED = CODE_CORE + 302;
	public static final int ERROR_VIPORDER_NULL = CODE_CORE + 303;
	public static final int ERROR_VIPORDER_STATUS_NULL = CODE_CORE + 304;
	public static final int ERROR_VIPORDER_STATUS_ILLEGAL = CODE_CORE + 305;
	public static final int ERROR_VIPORDER_TERMINAL_NULL = CODE_CORE + 306;
	public static final int ERROR_VIPORDER_ITEMID_NULL = CODE_CORE + 307;
	public static final int ERROR_VIPORDER_ORDERID_NULL = CODE_CORE + 308;
	public static final int ERROR_VIPORDER_TERMINAL_ILLEGAL = CODE_CORE + 309;
	public static final int ERROR_PAYMENT_CLASS_NULL = CODE_CORE + 310;
	public static final int ERROR_VIPORDER_ALREADY_DELIVERED = CODE_CORE + 311;
	public static final int ERROR_VIPPRODUCT_NULL = CODE_CORE + 312;
	public static final int ERROR_USER_NULL = CODE_CORE + 314;
	public static final int ERROR_EXCEEDED_MAX_OWNED_GROUP_COUNT = CODE_CORE + 315;
	public static final int ERROR_PURCHASE_RESULT_NULL = CODE_CORE + 316;
	public static final int ERROR_PURCHASE_INVALID_NOT_INCLUDE_ORDERID = CODE_CORE + 317;
	public static final int ERROR_PURCHASE_INVALID_NOT_INCLUDE_EXPIRESTIME = CODE_CORE + 318;
	public static final int ERROR_PURCHASE_INVALID_UNKNOWN_ERROR = CODE_CORE + 319;
	public static final int ERROR_PURCHASE_INVALID_PURCHASE_CANCELLED = CODE_CORE + 320;
	public static final int ERROR_PURCHASE_INVALID_THIRDPARTYITEMID_PRODUCT_NULL = CODE_CORE + 321;
	public static final int ERROR_PURCHASE_INVALID_THIRDPARTYITEMID_NULL = CODE_CORE + 322;
	public static final int ERROR_PURCHASE_INVALID_PLATFORM_NULL = CODE_CORE + 323;
	public static final int ERROR_VIPPRODUCT_ITEMTYPE_ERROR = CODE_CORE + 324;
	public static final int ERROR_CURRENCYEXCHANGEDRATE_ADD_FAILED = CODE_CORE + 325;
	public static final int ERROR_CURRENCYEXCHANGEDRATE_QUERY_FAILED = CODE_CORE + 326;
	public static final int ERROR_CURRENCYEXCHANGEDRATE_UPDATE_FAILED = CODE_CORE + 327;
	public static final int ERROR_CURRENCYEXCHANGEDRATE_DELETE_FAILED = CODE_CORE + 328;
	public static final int ERROR_VIPPRODUCT_AMOUNT_NULL = CODE_CORE + 329;
	public static final int ERROR_MOL_PAYMENT_AMOUNT_IS_NULL = CODE_CORE + 330;
	public static final int ERROR_MOL_PAYMENT_PRODUCTID_IS_NULL = CODE_CORE + 331;
	public static final int ERROR_MOL_PAYMENT_DESCRIPTION_IS_NULL = CODE_CORE + 332;
	public static final int ERROR_MOL_PAYMENT_ORDERID_IS_NULL = CODE_CORE + 333;
	public static final int ERROR_MOL_PAYMENT_RESPONSE_NULL = CODE_CORE + 334;
	public static final int ERROR_MOL_PAYMENT_RESPONSE_SIGNATURE_ERROR = CODE_CORE + 335;
	public static final int ERROR_MOL_PAYMENT_RESPONSE_PAYMENTURL_NULL = CODE_CORE + 336;
	public static final int ERROR_MANAGEMENTROLE_ADD_FAILED = CODE_CORE + 337;
	public static final int ERROR_MANAGEMENTROLE_QUERY_FAILED = CODE_CORE + 338;
	public static final int ERROR_MANAGEMENTROLE_UPDATE_FAILED = CODE_CORE + 339;
	public static final int ERROR_MANAGEMENTROLE_DELETE_FAILED = CODE_CORE + 340;
	
	public static final int ERROR_APN_FAILED = CODE_CORE + 341;
	public static final int ERROR_SCRIPT_UNZIP_FAILED = CODE_CORE + 342;
	public static final int ERROR_LANID_ILLEGAL = CODE_CORE + 343;
	public static final int ERROR_REDIS = CODE_CORE + 344;
	public static final int ERROR_PRESENCE_ALREADYLOGIN = CODE_CORE + 345;
	public static final int ERROR_PRESENCE_LOGINFAILED = CODE_CORE + 346;
	public static final int ERROR_AUTHORIZATION_SAVEFAILED = CODE_CORE + 347;
	public static final int ERROR_USERINFO_DELETEFAILED = CODE_CORE + 348;
	public static final int ERROR_USERINFO_NOTFOUND = CODE_CORE + 349;
	public static final int ERROR_USERINFO_QUERYFAILED = CODE_CORE + 350;
	public static final int ERROR_USERINFO_ADDFAILED = CODE_CORE + 351;
	int ERROR_METHODREQUEST_CRC_ILLEGAL = CODE_CORE + 352;
	int ERROR_METHODREQUEST_METHODNOTFOUND = CODE_CORE + 353;
	int ERROR_METHODMAPPING_INSTANCE_NULL = CODE_CORE + 354;
	int ERROR_METHODMAPPING_METHOD_NULL = CODE_CORE + 355;
	int ERROR_METHODMAPPING_ACCESS_FAILED = CODE_CORE + 356;
	int ERROR_METHODMAPPING_INVOKE_FAILED = CODE_CORE + 357;
	int ERROR_METHODMAPPING_INVOKE_UNKNOWNERROR = CODE_CORE + 358;
	int ERROR_POST_FAILED = CODE_CORE + 359;
	int ERROR_LANSERVERS_NOSERVERS = CODE_CORE + 360;
	int ERROR_RPC_CALLOUTSIDE_FAILED = CODE_CORE + 361;
	int ERROR_METHODRESPONSE_NULL = CODE_CORE + 362;
	int ERROR_USER_NOTIN_GATEWAY = CODE_CORE + 363;
	//vip权限错误
	public static final int ERROR_HAVENT_GOT_VIP_PERMISSION = CODE_MEMBERSHIP + 1;
	public static final int ERROR_APPROVE_GROUP_APPLICATION_EXCEEDED_MAX_MEMBER_COUNT = CODE_MEMBERSHIP + 2;
	public static final int ERROR_JOIN_GROUP_WITH_TICKET_EXCEEDED_MAX_MEMBER_COUNT = CODE_MEMBERSHIP + 3;
	public static final int ERROR_SUBSCRIPTION_USER_CANNOT_PURCHASE_VIP = CODE_MEMBERSHIP + 4;
	public static final int ERROR_DOMAIN_REDIRECT = CODE_CORE + 302;
	public static final int ERROR_TRIAL_LIMIT = CODE_CORE + 500;
}
