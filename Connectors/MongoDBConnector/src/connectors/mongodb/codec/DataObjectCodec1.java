//package connectors.mongodb.codec;
//
//import java.util.HashMap;
//
//import org.bson.BsonReader;
//import org.bson.BsonWriter;
//import org.bson.Document;
//import org.bson.codecs.Codec;
//import org.bson.codecs.DecoderContext;
//import org.bson.codecs.DocumentCodec;
//import org.bson.codecs.EncoderContext;
//import org.bson.types.ObjectId;
//
//import chat.utils.HashTree;
//import connectors.mongodb.annotations.handlers.MongoDBHandler;
//import connectors.mongodb.annotations.handlers.MongoDBHandler.CollectionHolder;
//
//public class DataObjectCodec1 implements Codec<DataObject> {  
//	private Class<?> collectionClass;
//	private Codec<Document> documentCodec;
//    public DataObjectCodec1(Class<?> collectionClass){  
//    	this.collectionClass = collectionClass;
//    	this.documentCodec = new DocumentCodec();
//    }  
//  
//    /** 
//     * @see org.bson.codecs.Decoder#decode(org.bson.BsonReader, org.bson.codecs.DecoderContext) 
//     */  
//    @Override  
//    public DataObject decode(BsonReader reader, DecoderContext decoderContext) {  
//    	Document document = documentCodec.decode(reader, decoderContext);
//		System.out.println("document "+document);
//		
//		HashMap<Class<?>, CollectionHolder> map = MongoDBHandler.getInstance().getCollectionMap();
//		if(map != null) {
//			CollectionHolder holder = map.get(collectionClass);
//			if(holder != null) {
//				HashTree<String, String> tree = holder.getFilters();
//				if(tree != null) {
//					Class<?> collectionClass = null;
//					while((collectionClass = (Class<?>) tree.getParameter(MongoDBHandler.CLASS)) != null) {
//						
//					}
//				}
//			}
//		}
//		Grades grade = new Grades();
//
//		grade.setId(document.getObjectId("_id"));
//
//		grade.setStudentId(document.getInteger("student_id"));
//
//		grade.setType(document.getString("type"));
//
//		grade.setScore(document.getDouble("score"));
//		
//		return grade;
//    }  
//      
//    /** 
//     * @see org.bson.codecs.Encoder#encode(org.bson.BsonWriter, java.lang.Object, org.bson.codecs.EncoderContext) 
//     */  
//    @Override  
//    public void encode(BsonWriter writer, DataObject value, EncoderContext encoderContext) {  
//    	Document document = new Document();
//
//		ObjectId id = value.getId();
//		Double score = value.getScore();
//		Integer studentId = value.getStudentId();
//		String type = value.getType();
//
//		if (null != id) {
//			document.put("_id", id);
//		}
//
//		if (null != score) {
//			document.put("score", score);
//		}
//
//		if (null != studentId) {
//			document.put("student_id", studentId);
//		}
//		if (null != type) {
//			document.put("type", type);
//		}
//
//		documentCodec.encode(writer, document, encoderContext);
//    }  
//      
//    /** 
//     * @see org.bson.codecs.Encoder#getEncoderClass() 
//     */  
//    @Override  
//    public Class<DataObject> getEncoderClass() {  
//        return DataObject.class;  
//    }  
//}  