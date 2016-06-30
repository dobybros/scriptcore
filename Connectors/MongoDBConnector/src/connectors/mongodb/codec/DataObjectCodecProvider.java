package connectors.mongodb.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class DataObjectCodecProvider implements CodecProvider {
	private Class<?> collectionClass;
    public DataObjectCodecProvider(Class<?> collectionClass) {
    	this.collectionClass = collectionClass;
	}

	@Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (DataObject.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new DataObjectCodec(this.collectionClass, registry);
        }
        return null;
    }
}