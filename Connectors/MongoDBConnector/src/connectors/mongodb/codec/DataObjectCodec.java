/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors.mongodb.codec;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bson.BsonBinarySubType;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.Transformer;
import org.bson.assertions.Assertions;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import chat.logs.LoggerEx;
import chat.utils.HashTree;
import connectors.mongodb.annotations.handlers.MongoDBHandler;
import connectors.mongodb.annotations.handlers.MongoDBHandler.CollectionHolder;
import connectors.mongodb.annotations.handlers.MongoDBHandler.FieldHolder;

/**
 * A Codec for Document instances.
 *
 * @see org.bson.Document
 * @since 3.0
 */
public class DataObjectCodec implements CollectibleCodec<DataObject> {
	private Class<?> collectionClass;
    private static final String ID_FIELD_NAME = "_id";
    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(),
            new BsonValueCodecProvider(),
            new DocumentCodecProvider()));
    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();
	private static final String TAG = DataObjectCodec.class.getSimpleName();

    private final BsonTypeClassMap bsonTypeClassMap;
    private final CodecRegistry registry;
    private final Transformer valueTransformer;
    private Codec<Document> documentCodec;
    /**
     * Construct a new instance with a default {@code CodecRegistry} and
     */
    public DataObjectCodec(Class<?> collectionClass, final CodecRegistry registry) {
        this(registry, DEFAULT_BSON_TYPE_CLASS_MAP);
        this.collectionClass = collectionClass;
    }

    /**
     * Construct a new instance with the given registry and BSON type class map.
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     */
    public DataObjectCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap) {
        this(registry, bsonTypeClassMap, null);
    }

    /**
     * Construct a new instance with the given registry and BSON type class map. The transformer is applied as a last step when decoding
     * values, which allows users of this codec to control the decoding process.  For example, a user of this class could substitute a
     * value decoded as a Document with an instance of a special purpose class (e.g., one representing a DBRef in MongoDB).
     *
     * @param registry         the registry
     * @param bsonTypeClassMap the BSON type class map
     * @param valueTransformer the value transformer to use as a final step when decoding the value of any field in the document
     */
    public DataObjectCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.registry = Assertions.notNull("registry", registry);
        this.bsonTypeClassMap = Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
        this.valueTransformer = valueTransformer != null ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        };
        this.documentCodec = new DocumentCodec();
    }

    @Override
    public boolean documentHasId(final DataObject document) {
        return document.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(final DataObject document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        Object id = document.getId();
        if (id instanceof BsonValue) {
            return (BsonValue) id;
        }

        BsonDocument idHoldingDocument = new BsonDocument();
        BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
        writer.writeStartDocument();
        writer.writeName(ID_FIELD_NAME);
        writeValue(writer, EncoderContext.builder().build(), id);
        writer.writeEndDocument();
        return idHoldingDocument.get(ID_FIELD_NAME);
    }

    @Override
    public DataObject generateIdIfAbsentFromDocument(final DataObject document) {
        if (!documentHasId(document)) {
//            document.put(ID_FIELD_NAME, idGenerator.generate());
        	document.setId(ObjectId.get().toString());
        }
        return document;
    }

    @Override
    public void encode(final BsonWriter writer, final DataObject document, final EncoderContext encoderContext) {
    	writeDataObject(writer, document, encoderContext);
    }

    @Override
    public DataObject decode(final BsonReader reader, final DecoderContext decoderContext) {
    	Document document = documentCodec.decode(reader, decoderContext);
		System.out.println("document " + document);
		
		HashMap<Class<?>, CollectionHolder> map = MongoDBHandler.getInstance().getCollectionMap();
		if(map != null) {
			CollectionHolder holder = map.get(collectionClass);
			if(holder != null) {
				HashTree<String, String> tree = holder.getFilters();
				if(tree != null) {
					Class<?> documentClass = null;
					Object value = null;
					boolean toEnd = false;
					while(true) {
						if(tree == null)
							break;
						Set<String> keys = tree.getChildrens();
						if(keys.isEmpty() || toEnd)
							break;
						for(String key : keys) {
							value = document.get(key);
							if(value != null) {
								if(value instanceof Document) {
									tree = tree.getChildren(key);
								} else {
									tree = tree.getChildren(key);
									if(tree != null) {
										tree = tree.getChildren(value.toString());
									}
									toEnd = true;
								}
								break;
							}
						}
					}
					if(tree != null)
						documentClass = (Class<?>) tree.getParameter(MongoDBHandler.CLASS);
					System.out.println(documentClass + " " + value);
					if(documentClass != null) {
						return convert(document, documentClass);
					}
				}
			}
		}
		return null;
    }

    static DataObject convert(Document document, Class<?> documentClass) {
    	try {
			DataObject dataObj = (DataObject) documentClass.newInstance();
			Object id = document.get(ID_FIELD_NAME);
			if(id != null) {
				dataObj.setId(id.toString());
			}
			FieldHolder holder = MongoDBHandler.getInstance().getDocumentMap().get(documentClass);
			if(holder != null) {
				HashMap<String, Field> fieldMap = holder.getFieldMap();
				if(fieldMap != null) {
					Set<String> keys = fieldMap.keySet();
					for(String key : keys) {
						Object value = document.get(key);
						Field field = fieldMap.get(key);
						if(value instanceof Document) {
							if(DataObject.class.isAssignableFrom(field.getType())) {
								DataObject valueObj = DataObjectCodec.convert((Document) value, field.getType());
								holder.assignField(dataObj, key, valueObj);
							} else if(BaseObject.class.isAssignableFrom(field.getType())) {
								BaseObject valueObj = BaseObjectCodec.convert((Document) value, field.getType());
								holder.assignField(dataObj, key, valueObj);
							}
						} else if (value instanceof Iterable) {
				            Iterable<Object> values = (Iterable<Object>) value;
				            ArrayList<Object> list = new ArrayList<Object>();
				            Class<?> clazz = null;
				            Type type = field.getGenericType();
				            if(type instanceof ParameterizedType) {
				            	ParameterizedType pType = (ParameterizedType) type;
				            	Type[] params = pType.getActualTypeArguments();  
				            	if(params != null && params.length == 1) {
				            		clazz = (Class<?>) params[0];
				            	}
				            }
				            for(Object o : values) {
				            	if(o instanceof Document) {
									if(DataObject.class.isAssignableFrom(clazz)) {
										DataObject valueObj = DataObjectCodec.convert((Document) o, clazz);
										list.add(valueObj);
//										holder.assignField(dataObj, key, valueObj);
									} else if(BaseObject.class.isAssignableFrom(clazz)) {
										BaseObject valueObj = BaseObjectCodec.convert((Document) o, clazz);
										list.add(valueObj);
//										holder.assignField(dataObj, key, valueObj);
									}
								} else {
//									holder.assignField(dataObj, key, o);
									list.add(o);
								}
				            }
				            holder.assignField(dataObj, key, list);
				        } else {
							holder.assignField(dataObj, key, value);
						}
					}
				}
			}
			return dataObj;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "convert document " + document + " to DataObject " + documentClass + " failed, " + e.getMessage());
		}
    	return null;
    }
    
    @Override
    public Class<DataObject> getEncoderClass() {
        return DataObject.class;
    }

    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final DataObject document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.getId() != null) {
            bsonWriter.writeName(ID_FIELD_NAME);
            writeValue(bsonWriter, encoderContext, document.getId());
        }
    }

    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals(ID_FIELD_NAME);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
        if (value == null) {
            writer.writeNull();
        } else if (value instanceof Iterable) {
            writeIterable(writer, (Iterable<Object>) value, encoderContext.getChildContext());
        } else if (value instanceof Map) {
            writeMap(writer, (Map<String, Object>) value, encoderContext.getChildContext());
        } else {
            Codec codec = registry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
    }
    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final Map<String, Object> document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.containsKey(ID_FIELD_NAME)) {
            bsonWriter.writeName(ID_FIELD_NAME);
            writeValue(bsonWriter, encoderContext, document.get(ID_FIELD_NAME));
        }
    }
    private void writeMap(final BsonWriter writer, final Map<String, Object> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();

        beforeFields(writer, encoderContext, map);

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (skipField(encoderContext, entry.getKey())) {
                continue;
            }
            writer.writeName(entry.getKey());
            writeValue(writer, encoderContext, entry.getValue());
        }
        writer.writeEndDocument();
    }
    
    private void writeDataObject(final BsonWriter writer, final DataObject map, final EncoderContext encoderContext) {
        writer.writeStartDocument();

        beforeFields(writer, encoderContext, map);

        HashMap<Class<?>, FieldHolder> documentMap = MongoDBHandler.getInstance().getDocumentMap();
    	FieldHolder fieldHolder = documentMap.get(map.getClass());
    	if(fieldHolder != null) {
    		HashMap<String, Field> fields = fieldHolder.getFieldMap();
    		if(fields != null) {
    			for (final Map.Entry<String, Field> entry : fields.entrySet()) {
    	            if (skipField(encoderContext, entry.getKey())) {
    	                continue;
    	            }
    	            Field field = entry.getValue();
    	            Object value = null;
					try {
						if(!field.isAccessible()) 
							field.setAccessible(true);
						value = field.get(map);
					} catch (IllegalArgumentException
							| IllegalAccessException e) {
						e.printStackTrace();
					}
					if(value != null) {
						writer.writeName(entry.getKey());
						writeValue(writer, encoderContext, value);
					}
    	        }
    		}
    	}
//        for (final Map.Entry<String, Object> entry : map.entrySet()) {
//            if (skipField(encoderContext, entry.getKey())) {
//                continue;
//            }
//            writer.writeName(entry.getKey());
//            writeValue(writer, encoderContext, entry.getValue());
//        }
        writer.writeEndDocument();
    }

    private void writeIterable(final BsonWriter writer, final Iterable<Object> list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }

    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        } else if (bsonType == BsonType.ARRAY) {
           return readList(reader, decoderContext);
        } else if (bsonType == BsonType.BINARY) {
            byte bsonSubType = reader.peekBinarySubType();
            if (bsonSubType == BsonBinarySubType.UUID_STANDARD.getValue() || bsonSubType == BsonBinarySubType.UUID_LEGACY.getValue()) {
                return registry.get(UUID.class).decode(reader, decoderContext);
            }
        }
        return valueTransformer.transform(registry.get(bsonTypeClassMap.get(bsonType)).decode(reader, decoderContext));
    }

    private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return list;
    }
}
