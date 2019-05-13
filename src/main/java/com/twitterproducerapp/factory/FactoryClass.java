package com.twitterproducerapp.factory;

import java.util.ArrayList;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.twitterproducerapp.models.Tweet;
import com.twitterproducerapp.models.User;

import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

@Component
public class FactoryClass {

	@Autowired
	NewTopic newTopic;

	@Autowired
	KafkaTemplate<String, Tweet> kafkaTemplate;

	public Twitter getTwitterInstance() {
		return TwitterFactory.getSingleton();
	}

	public TwitterStream getTwitterStreamInstance() {
		return new TwitterStreamFactory().getInstance();
	}

	public StatusListener getListener() {
		return new StatusListener() {
			@Override
			public void onStatus(Status status) {
				try {
					System.out.println("......................");

					Tweet tweet = new Tweet();
					tweet.setTweetText(status.getText());
					tweet.setCreatedAt(status.getCreatedAt());
					tweet.setGeoLocation(status.getGeoLocation());
					tweet.setLanguage(status.getLang());

					ArrayList<String> hashtags = new ArrayList<String>();
					for (HashtagEntity hashtag : status.getHashtagEntities()) {
						hashtags.add(hashtag.getText());
					}
					tweet.setHashtags(hashtags);

					User user = new User();
					user.setScreenName(status.getUser().getScreenName());
					user.setLocation(status.getUser().getLocation());
					user.setId(status.getUser().getId());

					tweet.setUser(user);

					System.out.println(tweet.toString());

					System.out.println("......................");
					kafkaTemplate.send(newTopic.name(), tweet);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
			}

			@Override
			public void onStallWarning(StallWarning warning) {
			}

		};
	}

}
