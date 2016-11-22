package db


import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject

/**
 * Id of Quest is user input. 
 * 
 * @author aplombchen
 *
 */
@DBDocument(collectionClass = "db.services.QuestStatusService")
class QuestStatus extends DataObject{
	public static final String FIELD_QUESTS = "ques";
	public static final String FIELD_TIME = "time";
	
	//未完成
	public static final int STATUS_UNACCOMPLISHED = -1;
	//等待领取(已完成)
	public static final int STATUS_WAITING_TO_RECEIVE = 0;
	//已领取
	public static final int STATUS_RECEIVED = 1;

	@DocumentField(key = "ques", mapKey = "qtype")
	private Map<String, QuestStatusInfo> questStatusInfoMap;
	
	@DocumentField(key = "time")
	private Long time;

	public Map<String, QuestStatusInfo> getQuestStatusInfoMap() {
		return questStatusInfoMap;
	}

	public void setQuestStatusInfoMap(
			Map<String, QuestStatusInfo> questStatusInfoMap) {
		this.questStatusInfoMap = questStatusInfoMap;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
	
}