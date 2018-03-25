import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebsocketVerticle extends AbstractVerticle{

    private static final Logger logger = LoggerFactory.getLogger(WebsocketVerticle.class);

    private final Router router;

    public WebsocketVerticle(Router router) {
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        super.start();
        router.route("/ws/websocket/*").handler(request -> {
            ServerWebSocket webSocket = request.request().upgrade();
            logger.debug("New connection {}", webSocket.binaryHandlerID());
            webSocket.handler(buffer -> {
                logger.debug("Received: {} {} ", webSocket.binaryHandlerID(), new String(buffer.getBytes()));
                JsonObject json = new JsonObject(new String(buffer.getBytes()));
                json.put("senderId", webSocket.binaryHandlerID());
                vertx.eventBus().publish("chat.broadcast", json);
            });
            MessageConsumer<JsonObject> consumer = vertx.eventBus().<JsonObject>consumer("chat.broadcast", messageHandler->{
                JsonObject body = messageHandler.body();
                String senderId = body.getString("senderId");
                if(!senderId.equals(webSocket.binaryHandlerID())){
                    body.remove("senderId");
                    webSocket.writeBinaryMessage(Buffer.buffer(body.encode()));
                }
            });
            webSocket.exceptionHandler(e -> logger.error(e.getMessage(), e));
            webSocket.closeHandler(c -> {
                logger.debug("Websocket {} closed.", webSocket.binaryHandlerID());
                consumer.unregister();
            });
        });
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
