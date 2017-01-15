package db

import connectors.mongodb.annotations.DBDocument
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.BaseObject

@DBDocument
class QuestStatusInfo extends BaseObject {
	
	public static final String FIELD_QUESTID = "qid";
	public static final String FIELD_QUESTTYPE = "qtype";
	public static final String FIELD_COUNT = "cnt";
	public static final String FIELD_ACCOMPLISHTIME = "atime";
	public static final String FIELD_RECEIVETIME = "rtime";
	public static final String FIELD_COUNTTIME = "ctime";
	
	/**
	 * 任务id
	 */
	@DocumentField(key = "qid")
	private String questId;
	
	/**
	 * 任务type
	 */
	@DocumentField(key = "qtype")
	private String questType;
	
	/**
	 * 任务计数
	 */
	@DocumentField(key = "cnt")
	private Integer count;
	
	/**
	 * 完成时间
	 */
	@DocumentField(key = "atime")
	private Long accomplishTime;
	
	/**
	 * 领取时间
	 */
	@DocumentField(key = "rtime")
	private Long receiveTime;

	/**
	 * 计数时间
	 */
	@DocumentField(key = "ctime")
	private Long countTime;

	public String getQuestId() {
		return questId;
	}

	public void setQuestId(String questId) {
		this.questId = questId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Long getAccomplishTime() {
		return accomplishTime;
	}

	public void setAccomplishTime(Long accomplishTime) {
		this.accomplishTime = accomplishTime;
	}

	public Long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public Long getCountTime() {
		return countTime;
	}

	public void setCountTime(Long countTime) {
		this.countTime = countTime;
	}

	public String getQuestType() {
		return questType;
	}

	public void setQuestType(String questType) {
		this.questType = questType;
	}
	
}
