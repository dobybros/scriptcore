package connectors.solr;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;

import chat.errors.CoreException;
import chat.utils.ClassFieldsHolder.FieldEx;
import connectors.solr.annotations.handlers.SolrDocumentHandler;
import connectors.solr.annotations.handlers.SolrDocumentHandler.SolrClassFieldsHolder;
import connectors.solr.annotations.handlers.SolrDocumentHandler.SolrFieldIdentifier;

public class SolrIndexService {
	private static final int ERRORCODE_INDEX_OBJECT_NULL = 800;
	private static final int ERRORCODE_INDEX_SOLRHANDLER_NULL = 801;
	private static final int ERRORCODE_INDEX_FIELDHODLERMAP_NULL = 802;
	private static final int ERRORCODE_INDEX_FIELDHODLER_NULL = 803;
	private static final int ERRORCODE_INDEX_CORE_NULL = 804;
	private static final int ERRORCODE_INDEX_FIELDMAP_NULL = 805;
	private static final int ERRORCODE_INDEX_CREATE_FAILED = 806;
	private static final int ERRORCODE_INDEX_DELETE_FAILED = 807;
	private static final int ERRORCODE_INDEX_QUERY_FAILED = 808;
	private static final int ERRORCODE_INDEX_PING_FAILED = 809;
	private String host;
//	private String solrUrl = "http://localhost:8983/solr/techtrial";

	private static SolrIndexService instance;
	
	public SolrIndexService() {
		instance = this;
	}
	
	public static SolrIndexService getInstance() {
		return instance;
	}
	
	private SolrClassFieldsHolder getFieldHolderMap(SolrData obj) throws CoreException {
		if(obj == null) 
			throw new CoreException(ERRORCODE_INDEX_OBJECT_NULL, "Object is null");
		Class<?> objClass = obj.getClass();
		return getFieldHolderMap(objClass);
	}
	
	private SolrClassFieldsHolder getFieldHolderMap(Class<?> objClass) throws CoreException {
		SolrDocumentHandler handler = SolrDocumentHandler.getInstance();
		if(handler == null) 
			throw new CoreException(ERRORCODE_INDEX_SOLRHANDLER_NULL, "SolrDocumentHandler is null");
		HashMap<Class<?>, SolrClassFieldsHolder> map = handler.getDocumentMap();
		if(map == null)
			throw new CoreException(ERRORCODE_INDEX_FIELDHODLERMAP_NULL, "SolrClassFieldsHolder Map is null");
		SolrClassFieldsHolder holder = map.get(objClass);
		if(holder == null)
			throw new CoreException(ERRORCODE_INDEX_FIELDHODLER_NULL, "SolrClassFieldsHolder is null");
		if(holder.getCore() == null)
			throw new CoreException(ERRORCODE_INDEX_CORE_NULL, "core is null for holder " + holder);
		if(holder.getFieldMap() == null) 
			throw new CoreException(ERRORCODE_INDEX_FIELDMAP_NULL, "field map is null for holder " + holder);
		return holder;
	}
	
	private SolrInputDocument getDocument(SolrData data, SolrClassFieldsHolder holder) throws CoreException {
		SolrInputDocument document = new SolrInputDocument();
		HashMap<String, FieldEx> map = holder.getFieldMap();
		Set<String> keys = map.keySet();
		for(String key : keys) {
			FieldEx field = map.get(key);
			String theKey = (String) field.get(SolrFieldIdentifier.KEY);
			Number boost = (Number) field.get(SolrFieldIdentifier.BOOST);
			Object value = null;
			try {
				Field f = field.getField();
				if(!f.isAccessible()) 
					f.setAccessible(true);
				value = field.getField().get(data);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			if(value != null) {
				if(value instanceof SolrData) {
					SolrData theData = (SolrData) value;
					SolrClassFieldsHolder theHolder = getFieldHolderMap(theData);
					value = getDocument(theData, theHolder);
				} else  if (value instanceof Iterable) {
		            Iterable<Object> values = (Iterable<Object>) value;
		            Class<?> clazz = null;
		            Type type = field.getField().getGenericType();
		            if(type instanceof ParameterizedType) {
		            	ParameterizedType pType = (ParameterizedType) type;
		            	Type[] params = pType.getActualTypeArguments();  
		            	if(params != null && params.length == 1) {
		            		clazz = (Class<?>) params[0];
		            	}
		            }
		            if(SolrData.class.isAssignableFrom(clazz)) {
		            	Collection<SolrInputDocument> docs = new ArrayList<>();
		            	for(Object v : values) {
		            		SolrData sData = (SolrData) v;
		            		SolrClassFieldsHolder theHolder = getFieldHolderMap(sData);
		            		SolrInputDocument theDoc = getDocument(sData, theHolder);
		            		if(theDoc != null) {
		            			docs.add(theDoc);
		            		}
		            	}
		            	value = docs;
		            }
				}
			}
			document.addField(theKey, value, boost.floatValue());
		}
		return document;
	}
	
	public UpdateResponse addIndex(SolrData obj) throws CoreException {
		SolrClassFieldsHolder holder = getFieldHolderMap(obj);
		SolrClient solr = new HttpSolrClient(host + "/" + holder.getCore());
		try {
			SolrInputDocument document = getDocument(obj, holder);
			UpdateResponse response = solr.add(document);
			solr.commit();
			return response;
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			throw new CoreException(ERRORCODE_INDEX_CREATE_FAILED, "Create index " + obj + " failed, " + e.getMessage());
		} finally {
			try {
				solr.close();
			} catch (IOException e) {
			}
		}
	}
	public UpdateResponse deleteIndex(String id, String core) throws CoreException {
		SolrClient solr = new HttpSolrClient(host + "/" + core);
		try {
			UpdateResponse response = solr.deleteById(id);
			solr.commit();
			return response;
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			throw new CoreException(ERRORCODE_INDEX_DELETE_FAILED, "Delete index " + id + " failed, " + e.getMessage());
		} finally {
			try {
				solr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public UpdateResponse deleteIndex(String id, Class<?> solrDataClass) throws CoreException {
		SolrClassFieldsHolder holder = getFieldHolderMap(solrDataClass);
		return deleteIndex(id, holder.getCore());
	}

	public SolrDocumentList query(String query, String core) throws CoreException {
		SolrClient solr = new HttpSolrClient(host + "/" + core);
		try {
			Map<String, String> map = new HashMap<String, String>();
//			map.put("q", "a:hello");
			map.put("q", query);
			SolrParams params = new MapSolrParams(map);
			QueryResponse resp = solr.query(params);
			//second way
			//String queryString="content:test";
			//MultiMapSolrParams mParams = SolrRequestParsers.parseQueryString("queryString");
			//QueryResponse resp = solr.query(mParams);
			SolrDocumentList docsList = resp.getResults();
			return docsList;
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			throw new CoreException(ERRORCODE_INDEX_QUERY_FAILED, "Query index " + query + " failed, " + e.getMessage());
		} finally {
			try {
				solr.close();
			} catch (IOException e) {
			}
		}
	}
	public SolrDocumentList query(String query, Class<?> solrDataClass) throws CoreException {
		SolrClassFieldsHolder holder = getFieldHolderMap(solrDataClass);
		return query(query, holder.getCore());
	}

	public SolrPingResponse ping() throws CoreException {
		SolrClient solr = new HttpSolrClient(host);
		try {
			SolrPingResponse response = solr.ping();
			return response;
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
			throw new CoreException(ERRORCODE_INDEX_PING_FAILED, "Ping failed, " + e.getMessage());
		} finally {
			try {
				solr.close();
			} catch (IOException e) {
			}
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}