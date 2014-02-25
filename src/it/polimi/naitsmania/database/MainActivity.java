package it.polimi.naitsmania.database;

import java.util.ArrayList;
import java.util.Random;

import javax.security.auth.PrivateCredentialPermission;

import android.R.integer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;

public class MainActivity extends Activity {
	
	// structure
	public enum gender{M,F};
	private int[] minAge = {13,18,25,35,50,65};
	private int[] maxAge = {17,24,34,49,64,100};
	private int[] vote = {1,2,3,4,5};
	// data chosen
	private gender gender;
	private int min;
	private int max;
	private int num;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		SQLiteDatabaseHelper db = new SQLiteDatabaseHelper(getBaseContext(), "studenti", null, 1);
		
		for (int i = 1; i <= 10; i++) {
			String place = "Duomo";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 11; i <= 20; i++) {
			String place = "Parco Sempione";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 21; i <= 30; i++) {
			String place = "Navigli";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 31; i <= 40; i++) {
			String place = "Museo Scienza e della Tecnica";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		for (int i = 41; i <= 50; i++) {
			String place = "Cenacolo di Leonardo";
			generateRandomValues();
			db.insertAll(i, gender, min, max, place, num);
		}
		
		db.showResults();
		String pageToBeLoad = db.getResourceURI();
		Uri uri = Uri.parse("http://naitsmania.com/" + pageToBeLoad);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
		
	}
	
	private void generateRandomValues(){
		int pick = new Random().nextInt(gender.values().length);
		gender = gender.values()[pick];
		pick = new Random().nextInt(minAge.length);
		min = minAge[pick];
		max = maxAge[pick];
		pick = new Random().nextInt(vote.length);
		num = vote[pick];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
