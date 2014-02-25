package it.polimi.naitsmania.database;

import it.polimi.naitsmania.database.MainActivity.gender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import android.R.integer;
import android.R.menu;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;


public class SQLiteDatabaseHelper extends SQLiteOpenHelper{

	private float maxValue = 0;
	public Cursor tobesaved;
	private ArrayList<String> allPlacesList = new ArrayList<String>();
	
	
	public ArrayList<String> getAllPlacesList() {
		return allPlacesList;
	}

	public SQLiteDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS 'tablee';");
		db.execSQL("CREATE TABLE 'tableee' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'username' TEXT, " +
				"'gender' TEXT, 'minAgeSection' Integer, 'maxAgeSection' Integer, 'place' TEXT, 'vote' Integer);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	public void insertAll(int count, gender gender, int min, int max, String place, int vote){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("id", count);
		cv.put("gender", gender.toString());
		cv.put("minAgeSection", min);
		cv.put("maxAgeSection", max);
		cv.put("place", place);
		cv.put("vote", vote);
		db.insert("tableee", "id", cv);		
	}
    
    public void showResults(){
    	SQLiteDatabase database = this.getReadableDatabase();
		//Cursor cur = database.rawQuery("SELECT id as _id, gender, minAgeSection, maxAgeSection, place, vote FROM 'tableee';", new String[]{});
		//Cursor curAvg = database.rawQuery("SELECT AVG(vote), place as _id FROM tableee GROUP BY place", new String[]{});
		Cursor allPlaces = database.rawQuery("SELECT place FROM tableee", new String[]{}); 
		allPlaces.moveToFirst();
		int i = 0;
		while (!allPlaces.isLast()) {
			allPlacesList.add(allPlaces.getString(i));
			i++;
		}
		//cur.moveToFirst();
		//curAvg.moveToFirst();
		/*while (!curAvg.isLast()) {
			Log.w("asd", String.valueOf(curAvg.getFloat(0)) + " " + curAvg.getString(1));
			if (curAvg.getFloat(0) > maxValue){
				tobesaved = curAvg;
				maxValue = curAvg.getFloat(0);
			}
			curAvg.moveToNext();
		}
		while(!cur.isLast()){
			Log.w("tag", String.valueOf(cur.getInt(0) + " " + cur.getString(1) + " " + cur.getInt(2) + " " + cur.getInt(3) + " " + cur.getString(4) + " " + cur.getInt(5)));
			cur.moveToNext();
		}*/
    }
    
    public String getResourceURI(){
		return tobesaved.getString(1);
    }

	
}
