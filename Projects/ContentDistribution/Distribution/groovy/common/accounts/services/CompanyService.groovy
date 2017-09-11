package common.accounts.services

import chat.errors.CoreException
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.result.DeleteResult
import common.accounts.data.Company
import common.accounts.databases.CompanyDatabase
import connectors.mongodb.MongoCollectionHelper
import connectors.mongodb.annotations.DBCollection
import connectors.mongodb.codec.DataObject
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import script.groovy.annotation.Bean

@DBCollection(name = "company", databaseClass = "common.accounts.services.CompanyDatabase")
@Bean
class CompanyService extends MongoCollectionHelper {
	public static final int ERRORCODE_COMPANY_DELETE_FAILED = 500;
	private Company findCompany(Bson query) {
		MongoCollection<Company> collection = this.getMongoCollection();
		FindIterable<Company> iterable = collection.find(query);
		if(iterable != null) {
			MongoCursor<Company> cursor = iterable.iterator();
			if(cursor.hasNext()) {
				return cursor.next();
			}
		}
		return null;
	}
	
	public Company getCompany(String id) {
		return findCompany(new Document().append("_id", id));
	}

	public void deleteCompany(String id) {
		MongoCollection<Company> collection = this.getMongoCollection();
		DeleteResult result = collection.deleteOne(new Document().append(DataObject.FIELD_ID, id));
		if(result.deletedCount == 0)
			throw new CoreException(ERRORCODE_COMPANY_DELETE_FAILED, "Delete company by id " + id + " failed.");
	}

	public void addCompany(Company company) {
		if(company.getId() == null)
			company.setId(ObjectId.get().toString());
		company.setCreateTime(System.currentTimeMillis());
		company.setUpdateTime(company.getCreateTime());
		MongoCollection<Company> collection = this.getMongoCollection();
		collection.insertOne(company);
	}

	public List<Company> getMyCompanies(String userId) {
		if(userId == null)
			return null;
		Document query = new Document().append(Company.FIELD_EMPLOYEEIDS, userId);
		return findCompanies(query);
	}

	public List<Company> getAllCompanies() {
		Document query = new Document();
		return findCompanies(query);
	}

	private List<Company> findCompanies(Bson query) {
		MongoCollection<Company> collection = this.getMongoCollection();
		FindIterable<Company> iterable = collection.find(query);
		if(iterable != null) {
			MongoCursor<Company> cursor = iterable.iterator();
			List<Company> list = new ArrayList<>();
			while(cursor.hasNext()) {
				list.add(cursor.next());
			}
			return list;
		}
		return null;
	}
}
