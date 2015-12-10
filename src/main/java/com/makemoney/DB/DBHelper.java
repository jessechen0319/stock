package com.makemoney.DB;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class DBHelper {
	
	private static MongoDatabase database;
	
	public static MongoDatabase generateDB(){
		if(database != null){
			return database;
		} else {
			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			MongoDatabase mongoDatabase = mongoClient.getDatabase("Make_Money");
			database = mongoDatabase;
			return database;
		}
	}
}
