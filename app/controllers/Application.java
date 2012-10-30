package controllers;

import play.*;
import play.mvc.*;
import play.libs.Json;
import play.libs.F.*;

import views.html.*;
import static play.mvc.Results.async;
import static play.libs.Akka.future;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import static akka.pattern.Patterns.ask;
import play.libs.Akka;

import java.util.concurrent.Callable;
import org.codehaus.jackson.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		return async(
			future(new Callable<Integer>() {
				public Integer call() {
					try{
						Thread.sleep(10000);
					} catch(InterruptedException e){
						System.out.println(e.getMessage());
					}
					return 4;
				}
			}).map(new Function<Integer,Result>() {
				public Result apply(Integer i) {

					ObjectNode result = Json.newObject();

					result.put("id", i);
					return ok(result);
				}
			})
		);
	}


    /**
     * Handles the form submission.
     */
    public static Result sayHello(String data) {

 		Logger.debug("Got the request: {}" + data);

		ActorSystem system = ActorSystem.create("MySystem");
		ActorRef myActor = system.actorOf(new Props(MyUntypedActor.class), "myactor");

		return async(
			Akka.asPromise(ask(myActor, data, 1000)).map(
			  new Function<Object,Result>() {
				public Result apply(Object response) {
					ObjectNode result = Json.newObject();

					result.put("message", response.toString());
					return ok(result);
				}
			  }
			)
		);
    }

	static public class MyUntypedActor extends UntypedActor {

		public void onReceive(Object message) throws Exception {
			if (message instanceof String){
				Logger.debug("Received String message: {}" + message);

				//Here is where you can put your long running blocking code like getting the product feed from various sources

				getSender().tell("Hello world");
			}
			else {
				unhandled(message);
			}
		}
	}
}