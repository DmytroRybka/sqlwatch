package sqlwatch4.ui;


import org.apache.commons.io.IOUtils;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.web.QueryException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;
import sqlwatch4.model.TracesSlice;
import sqlwatch4.rebase.com.google.gson.Gson;
import sqlwatch4.rebase.com.google.gson.annotations.Expose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dmitry.mamonov
 */
public class UIMain implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
            throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window) wtkxSerializer.readObject(this, "./markup/frame.wtkx");
        window.open(display);

        RecurrentTask recurrentTask = new RecurrentTask();
        recurrentTask.execute(new TaskListener<Integer>() {
            @Override
            public void taskExecuted(Task<Integer> integerTask) {
                repeat(integerTask);
            }

            @Override
            public void executeFailed(Task<Integer> integerTask) {
                repeat(integerTask);
            }

            private void repeat(Task<Integer> integerTask) {
                try {
                    Thread.sleep(1000);
                    integerTask.execute(this);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //TODO [DM] exit.
                }
            }
        });

    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(UIMain.class, args);

    }
}

class RecurrentTask extends Task<Integer> {
    @Override
    public Integer execute() throws TaskExecutionException {
        try {
            GetQuery query = new GetQuery("127.0.0.1", 8666, "/traces", false);
            query.setSerializer(new Serializer() {
                private Gson gson = new Gson();

                @Override
                public Object readObject(InputStream inputStream) throws IOException, SerializationException {
                    String json = IOUtils.toString(inputStream, "UTF-8");
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
            Object json = query.execute();
            TracesSlice tracesSlice = (TracesSlice) json;
            System.out.println(new Gson().toJson(tracesSlice));
            return tracesSlice.getTraces().size();
        } catch (Exception e) {
            System.out.println("Failed: "+e.getMessage());
            throw new TaskExecutionException(e);
        }
    }
}