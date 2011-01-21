package sqlwatch4.console;

import org.apache.commons.io.IOUtils;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.web.QueryException;
import sqlwatch4.model.TracesSlice;
import sqlwatch4.rebase.com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dmitry.mamonov
 */
public class ConsoleMain {
    public static void main(String[] args) {
        GetQuery query = new GetQuery("127.0.0.1",8666,"/traces",false);
        query.setSerializer(new Serializer(){
            private Gson gson = new Gson();
            @Override
            public Object readObject(InputStream inputStream) throws IOException, SerializationException {
                String json = IOUtils.toString(inputStream,"UTF-8");
                return gson.fromJson(json, TracesSlice.class);
            }

            @Override
            public void writeObject(Object o, OutputStream outputStream) throws IOException, SerializationException {
                throw new UnsupportedOperationException("Read only!");
            }

            @Override
            public String getMIMEType(Object o) {
                throw new UnsupportedOperationException("Read only!");
            }
        });
        while(true){
            try {
                Object json = query.execute();
                System.out.println("json = "+json);

            } catch (QueryException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
