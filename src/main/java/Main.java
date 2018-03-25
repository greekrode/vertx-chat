import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        vertx.deployVerticle(new WebsocketVerticle(router),h ->{ if(h.succeeded()) logger.info("Websocket ready.");});
        vertx.deployVerticle(new WebserverVerticle(router),h ->{ if(h.succeeded()) logger.info("Webserver ready.");});
    }
}
