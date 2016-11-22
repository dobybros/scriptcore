package db.services


import org.bson.Document
import org.bson.conversions.Bson

import script.groovy.annotation.Bean
import chat.errors.CoreException

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult

import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection
import db.QuestStatus
import db.QuestStatusInfo

@DBCollection(name = "queststatus", databaseClass = "db.QuestDatabase")
@Bean
class QuestStatusService extends MongoCollectionHelper {
	
	public boolean deleteQuestStatus(String userId) {
		MongoCollection<QuestStatus> collection = this.getMongoCollection();
		DeleteResult result = collection.deleteOne(new Document().append(QuestStatus.FIELD_ID, userId));
		return result.getDeletedCount() > 0;
	}
	
	/**
	 * 根据userId与questType获取QuestStatusInfo
	 * @param userId
	 * @param questType
	 * @return
	 */
	public QuestStatusInfo getQuestStatusInfo(String userId, String questType) {
		MongoCollection<QuestStatus> collection = this.getMongoCollection();
		Bson query = Filters.and(Filters.eq(QuestStatus.FIELD_ID, userId), Filters.eq(QuestStatus.FIELD_QUESTS + "." + QuestStatusInfo.FIELD_QUESTTYPE, questType));
		FindIterable<QuestStatus> iterable = collection.find(query);
		iterable.projection(Projections.elemMatch(QuestStatus.FIELD_QUESTS));
		MongoCursor<QuestStatus> cursor = iterable.iterator();
		if(cursor.hasNext()) {
			QuestStatus questStatus = cursor.next();
			if(questStatus != null) {
				Map<String, QuestStatusInfo> questStatusInfoMap = questStatus.getQuestStatusInfoMap();
				QuestStatusInfo questStatusInfo = questStatusInfoMap.get(questType);
				if(questStatusInfo != null)
					return questStatusInfo;
			}
		}
		return null;
	}
	
	/**
	 * 更新QuestStatus中的Info
	 * @param userId
	 * @param questStatusInfo
	 * @return
	 */
	public Long updateQuestStatusInfo(String userId, QuestStatusInfo questStatusInfo) {
		long currentTime = System.currentTimeMillis();
		MongoCollection<QuestStatus> collection = this.getMongoCollection();
		Bson query = Filters.and(Filters.eq(QuestStatus.FIELD_ID, userId), Filters.eq(QuestStatus.FIELD_QUESTS + "." + QuestStatusInfo.FIELD_QUESTID, questStatusInfo.getQuestId()));
		
		List<Bson> updateList = new ArrayList<Bson>();
		if(questStatusInfo.getQuestId() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_QUESTID, questStatusInfo.getQuestId()));
		}
		if(questStatusInfo.getQuestType() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_QUESTTYPE, questStatusInfo.getQuestType()));
		}
		if(questStatusInfo.getCount() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_COUNT, questStatusInfo.getCount()));
		}
		if(questStatusInfo.getAccomplishTime() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_ACCOMPLISHTIME, questStatusInfo.getAccomplishTime()));
		}
		if(questStatusInfo.getReceiveTime() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_RECEIVETIME, questStatusInfo.getReceiveTime()));
		}
		if(questStatusInfo.getCountTime() != null) {
			updateList.add(Updates.set(QuestStatus.FIELD_QUESTS + ".\$." + QuestStatusInfo.FIELD_COUNTTIME, questStatusInfo.getCountTime()));
		}
		updateList.add(Updates.set(QuestStatus.FIELD_TIME, currentTime));
		Bson update = Updates.combine(updateList);
		UpdateResult result = collection.updateOne(query, update);
		if(result.getMatchedCount() <= 0)
			throw new CoreException(CoreErrorCodes.ERROR_UPDATE_FAILED, "No matched document found with : " + query.toString());
		return currentTime;
	}
	
	public QuestStatus getQuestStatus(String userId) {
		MongoCollection<QuestStatus> collection = this.getMongoCollection();
		Document query = new Document();
		query.append(QuestStatus.FIELD_ID, userId);
		FindIterable<QuestStatus> iterable = collection.find(query);
		MongoCursor<QuestStatus> cursor = iterable.iterator();
		if(cursor.hasNext()) {
			QuestStatus questStatus = cursor.next();
			return questStatus;
		}
		return null;
	}
	
	public void addQuestStatus(QuestStatus qs) {
		MongoCollection<QuestStatus> collection = this.getMongoCollection();
		collection.insertOne(qs);
	}
}