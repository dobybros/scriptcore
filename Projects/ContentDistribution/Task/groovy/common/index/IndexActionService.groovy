package common.index

import chat.errors.CoreException

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult

import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection
import connectors.mongodb.codec.DataObject

import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

import script.groovy.annotation.Bean

@DBCollection(name = "indexaction", databaseClass = "common.index.IndexActionDatabase")
@Bean
class IndexActionService extends MongoCollectionHelper {
	public static final int ERRORCODE_INDEXACTION_DELETE_FAILED = 900;
	private IndexAction findIndexAction(Bson query) {
		MongoCollection<IndexAction> collection = this.getMongoCollection();
		FindIterable<IndexAction> iterable = collection.find(query);
		if(iterable != null) {
			MongoCursor<IndexAction> cursor = iterable.iterator();
			if(cursor.hasNext()) {
				return cursor.next();
			}
		}
		return null;
	}
	
	public IndexAction getIndexAction(String id) {
		return findIndexAction(new Document().append("_id", id));
	}

	public void deleteIndexAction(String id) {
		MongoCollection<IndexAction> collection = this.getMongoCollection();
		DeleteResult result = collection.deleteOne(new Document().append(DataObject.FIELD_ID, id));
		if(result.deletedCount == 0)
			throw new CoreException(ERRORCODE_INDEXACTION_DELETE_FAILED, "Delete company by id " + id + " failed.");
	}

	public void addIndexAction(IndexAction indexAction) {
		if(indexAction.getId() == null)
			indexAction.setId(ObjectId.get().toString());
		indexAction.setCreateTime(System.currentTimeMillis());
		MongoCollection<IndexAction> collection = this.getMongoCollection();
		collection.insertOne(indexAction);
	}

	public List<IndexAction> getAllIndexActions() {
		Document query = new Document();
		return findIndexActions(query);
	}

	private List<IndexAction> findIndexActions(Bson query) {
		MongoCollection<IndexAction> collection = this.getMongoCollection();
		FindIterable<IndexAction> iterable = collection.find(query);
		if(iterable != null) {
			MongoCursor<IndexAction> cursor = iterable.iterator();
			List<IndexAction> list = new ArrayList<>();
			while(cursor.hasNext()) {
				list.add(cursor.next());
			}
			return list;
		}
		return null;
	}
	
	public IndexAction findAndDelete() {
		MongoCollection<IndexAction> collection = this.getMongoCollection();
		IndexAction indexAction = collection.findOneAndDelete(new Document(), new FindOneAndDeleteOptions().sort(new Document().append(IndexAction.FIELD_CREATETIME, 1)));
		return indexAction;
	}
}
