package fr.xtof54.twitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.Location.Coordinate;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

/**
This is a very simple adaptation of https://github.com/twitter/hbc
to continuously grab tweets from all over France, without using any search term.

Just run this program and redirect output to a (json) file.
*/
public class TwitterGrab {
	public static void main(String args[]) throws Exception {
		/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
//		List<Long> followings = Lists.newArrayList(1234L, 566788L);
//		List<String> terms = Lists.newArrayList("twitter", "api");
//		hosebirdEndpoint.followings(followings);

//		ArrayList<String> terms = new ArrayList<String>();
//		terms.add("charlie");
		
//		hosebirdEndpoint.trackTerms(terms);

		ArrayList<Location> locations = new ArrayList<Location>();
		final double latSW_France=43.46887;
		final double lonSW_France=-1.47217;
		final double latNE_France=49.39668;		
		final double lonNE_France=6.35010;
		locations.add(new Location(new Coordinate(lonSW_France, latSW_France), new Coordinate(lonNE_France, latNE_France)));

		hosebirdEndpoint.locations(locations);

		// These secrets should be read from a config file
		String consumerKey;
		String consumerSecret;
		String token;
		String tokenSecret;
		{
			BufferedReader f = new BufferedReader(new FileReader("/home/xtof/.twitterapi/myapp"));
			consumerKey = f.readLine().trim();
			consumerSecret = f.readLine().trim();
			token = f.readLine().trim();
			tokenSecret = f.readLine().trim();
			f.close();
		}
		
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, tokenSecret);
		
		ClientBuilder builder = new ClientBuilder()
		  .name("xtofclient")                              // optional: mainly for the logs
		  .hosts(hosebirdHosts)
		  .authentication(hosebirdAuth)
		  .endpoint(hosebirdEndpoint)
		  .processor(new StringDelimitedProcessor(msgQueue))
		  .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

		Client hosebirdClient = builder.build();
		// Attempts to establish a connection.
		hosebirdClient.connect();
		
		for (int i=0;i<100;i++) {
			Thread.sleep(1000);
			System.out.println("q size "+hosebirdClient.isDone()+" "+msgQueue.size());
			for (;;) {
				String msg = msgQueue.take();
				System.out.println("\t"+msg);
			}
		}
	}
}

