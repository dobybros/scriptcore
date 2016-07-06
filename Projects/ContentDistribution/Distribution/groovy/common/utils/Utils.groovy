package common.utils

import org.bson.types.ObjectId
import script.file.FileAdapter

import java.text.SimpleDateFormat

/**
 * Created by aplombchen on 3/7/16.
 */
class Utils {
    static SimpleDateFormat gmtFormatter = new SimpleDateFormat("yyyy/MM/dd");
    static {
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    public static String getDocumentPath(String resourceId, String fileName) throws IOException {
        if(resourceId != null && ObjectId.isValid(resourceId)) {
            StringBuilder builder = new StringBuilder(FileAdapter.DOC_ROOT_PATH);
            ObjectId oid = new ObjectId(resourceId);
            String timePath = null;
            //XXX format is not thread safe, so sync it. But need better performance solution.
            synchronized (gmtFormatter) {
                timePath = gmtFormatter.format(oid.getDate());
            }
            builder.append(timePath).append("/").append(resourceId).append("/");
            if(fileName != null)
                builder.append(fileName);
            return builder.toString();
        } else {
            throw new IOException("Invalid resourceId " + resourceId);
        }
    }
}
