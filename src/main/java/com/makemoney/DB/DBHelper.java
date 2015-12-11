package com.makemoney.DB;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class DBHelper {
	
	private static MongoClient database;
	
	public static MongoClient generateDB(){
		
		if(database != null){
			return database;
		} else {
			database = new MongoClient( "localhost" , 27017 );
			return database;
		}
	}
}
