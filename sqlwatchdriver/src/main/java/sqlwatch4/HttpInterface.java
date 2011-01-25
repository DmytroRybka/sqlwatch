package sqlwatch4;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author dmitry.mamonov
 */
public class HttpInterface {
    public static void startup() throws IOException {
        class MyHandler implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
                String json = TracesSliceContainer.get().cutSlice().toJson();
                byte[] jsonBytes = json.getBytes("UTF-8");
                t.sendResponseHeaders(200, jsonBytes.length);
                OutputStream os = t.getResponseBody();
                os.write(jsonBytes);
                os.close();
            }
        }

        final String resource = "/traces";
        InetSocketAddress address = new InetSocketAddress("localhost",8666);
        System.out.println("SqlWatch http interface is available on: "+address.toString()+resource);
        HttpServer server = HttpServer.create(address, 10);
        server.createContext(resource, new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
