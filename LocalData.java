package com.joyofplaying.babytracker;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.Utility.Formats;

public class LocalData extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 3;
	public static final float SqlTableVersion = (float) 2.1;
	private Context appContext = null;
	private SQLiteDatabase appDatabase = null;

	public SQLiteDatabase getDatabase() {
		return this.appDatabase;
	}

	private final static String database_name = "BabyTracker";
	private final String drop_reg_user_table_sql = "DROP TABLE IF EXISTS BabyTrackerUsers";
	private final String drop_reg_children_table_sql = "DROP TABLE IF EXISTS BabyTrackerChildren";
	private final String drop_child_data_table_sql = "DROP TABLE IF EXISTS BabyTrackerChildren";

	private static final String ID_COLUMN = "_id";

	LocalData(Context context) {
		super(context, database_name, null, DATABASE_VERSION);
		Utility.LogMethod();
		appContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Utility.LogMethod();
		Utility.Log("LocalData.onCreate starting");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		Utility.LogMethod();
		Utility.Log("LocalData onUpgrade");
	}

	public void open() throws SQLException {
		Utility.LogMethod();
		if (appDatabase == null) {
			appDatabase = getWritableDatabase();
		}
	}

	@Override
	public void close() {
		Utility.LogMethod();
		Utility.Log("LocalData close");
		super.close();
	}

	/**
	 * Sets the initial database up
	 * 
	 * @return
	 */
	public boolean setupDatabase() {
		Utility.LogMethod();

		String sql = appContext.getString(R.string.create_reg_user_table_sql);
		appDatabase.execSQL(sql);

		sql = appContext.getString(R.string.create_reg_children_table_sql);
		appDatabase.execSQL(sql);

		return true;
	}

	public synchronized boolean resetDatabase() {
		Utility.LogMethod();

		if (appDatabase == null)
			return false;

		for (RegisteredChild regChild : createNewChild().getAll()) {
			appDatabase.execSQL("DROP TABLE IF EXISTS " + regChild.table_name);
		}

		String sql = appContext.getString(R.string.drop_reg_user_table_sql);
		appDatabase.execSQL(sql);

		sql = appContext.getString(R.string.drop_reg_children_table_sql);
		appDatabase.execSQL(sql);

		sql = appContext.getString(R.string.drop_child_data_table_sql);
		appDatabase.execSQL(sql);

		// TODO: Erase all child tables
		return true;
	}

	public synchronized void dumpDatabase() {
		Utility.LogMethod();
		try {
			RegisteredUserTable user = createNewUser();
			for (RegisteredUser regUser : user.getAll()) {
				Utility.Log("{0}", regUser.toString());
			}
			Utility.Log("*********************");

			RegisteredChildTable child = createNewChild();
			for (RegisteredChild regChild : child.getAll()) {
				Utility.Log("{0}", regChild.toString());

				if (Utility.IsValid(regChild.table_name)) {
					ChildDataTable data = createChildDataRecorder(regChild);
					data.dumpAll();
					/*
					 * for (ChildData row : data.getAll()) { Utility.Log("{0}",
					 * row.toStringFlat()); }
					 */

				}
				Utility.Log("---------------------------");
			}
		} catch (Exception ex) {
			Utility.LogError("Failed to dump database {0}", ex.toString());
		}
	}

	public RegisteredUserTable createNewUser() {
		return new RegisteredUserTable();
	}

	public RegisteredChildTable createNewChild() {
		return new RegisteredChildTable();
	}

	public ChildDataTable createChildDataRecorder(RegisteredChild child) {
		return new ChildDataTable(child);
	}

	public ChildDataTable openChildDataTable(RegisteredChild child) {
		RegisteredChildTable childStore = createNewChild();
		RegisteredChild regChild = childStore.restoreChild(child);
		if (regChild != null) {
			return createChildDataRecorder(child);
		}
		return null;
	}

	public Query createNewQuery(RegisteredChild child) {
		return new Query(child);
	}

	private Cursor rawQueryData(String query) {
		Utility.Log("Query = {0}", query);
		try {
			return appDatabase.rawQuery(query, null);
		} catch (Exception ex) {
			Utility.LogError("Exception {0}", ex.toString());
		}
		return null;
	}

	private Cursor queryTable(String table, String[] columns, String where) {
		Utility.Log("Query = {0} columns={1} {2}", table, columns.length, where);
		try {
			return appDatabase.query(table, columns, where, null, null, null, null);
		} catch (Exception ex) {
			Utility.LogError("Exception {0}", ex.toString());
		}
		return null;
	}

	public void dumpTable(String table) {
		if (!Utility.IsValid(table))
			return;

		Utility.LogMethod();

		Cursor cursor = rawQueryData("SELECT * FROM " + table);

		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Utility.LogCursor(cursor);
				cursor.moveToNext();
			}
			cursor.close();
		}
	}

	public class RegisteredUserTable {

		private String Table = "";

		public RegisteredUserTable() {
			Utility.LogMethod();
			Table = appContext.getString(R.string.reg_user_table);
			appContext.getResources().getStringArray(R.array.reg_user_table_columns);
		}

		public synchronized RegisteredUser create(RegisteredUser Input) {
			Utility.LogMethod();
			ContentValues values = new ContentValues();

			if (!Utility.IsValid(Input.token)) {
				int token = Utility.RandomInt(99999999);
				Input.token = Integer.toString(token);
			}

			values.put("name", Input.name);
			values.put("user_id", Input.user_id);
			values.put("password", Input.password);
			values.put("token", Input.token);
			Utility.Log(values);

			// Insert into table
			long insertId = appDatabase.insertWithOnConflict(Table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (insertId == -1) {
				return null;
			}

			RegisteredUser user = null;
			Cursor cursor = rawQueryData("SELECT * FROM " + Table + " WHERE _id = " + String.valueOf(insertId));
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					user = cursorToRegisteredUser(cursor);
				}
				cursor.close();
			}
			return user;
		}

		public synchronized void delete(RegisteredUser Input) {
			appDatabase.delete(Table, ID_COLUMN + " = " + Input.id, null);
		}

		public RegisteredUser get(String user_id) {
			Utility.LogMethod();
			RegisteredUser user = null;

			Cursor cursor = rawQueryData("SELECT * FROM " + Table + " WHERE user_id='" + user_id + "'");
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					user = cursorToRegisteredUser(cursor);
				}
				// Make sure to close the cursor
				cursor.close();
			}
			return user;
		}

		public List<RegisteredUser> getAll() {
			Utility.LogMethod();
			List<RegisteredUser> users = new ArrayList<RegisteredUser>();

			Cursor cursor = rawQueryData("SELECT * FROM " + Table);
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					RegisteredUser RegisteredUser = cursorToRegisteredUser(cursor);
					users.add(RegisteredUser);
					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			}
			return users;
		}

		private RegisteredUser cursorToRegisteredUser(Cursor cursor) {
			// Utility.LogCursor(cursor);
			RegisteredUser user = new RegisteredUser();
			int idx = 0;
			user.id = cursor.getInt(idx++);
			user.name = cursor.getString(idx++);
			user.user_id = cursor.getString(idx++);
			user.password = cursor.getString(idx++);
			user.token = cursor.getString(idx++);
			user.version = (float) cursor.getDouble(idx++);
			user.timestamp = cursor.getInt(idx++);
			return user;
		}
	} // User

	public class RegisteredChildTable {

		private String Table = "";

		public RegisteredChildTable() {
			Utility.LogMethod();
			Table = appContext.getString(R.string.children_table);
			appContext.getResources().getStringArray(R.array.children_table_columns);
		}

		public synchronized RegisteredChild create(RegisteredChild Input) {
			Utility.LogMethod();
			ContentValues values = new ContentValues();

			if (!Utility.IsValid(Input.user_token)) {
				throw new RuntimeException("Missing registered user token");
			}

			if (!Utility.IsValid(Input.token)) {
				int token = Utility.RandomInt(99999999);
				Input.token = Integer.toString(token);
			}

			if (!Utility.IsValid(Input.table_name)) {
				Input.table_name = Input.name + "_Table";
				Input.table_name = Input.table_name.replaceAll(" ", "_");
			}

			if (!Utility.IsValid(Input.title)) {
				Input.title = Input.name + "'s Data";
			}

			values.put("user_token", Input.user_token);
			values.put("name", Input.name);
			values.put("dob", Input.dob);
			values.put("token", Input.token);
			values.put("table_name", Input.table_name);
			values.put("title", Input.title);
			values.put("photoUrl", Input.photoUrl);
			Utility.Log(values);

			// Insert into table
			long insertId = appDatabase.insertWithOnConflict(Table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if (insertId == -1) {
				return null;
			}

			String sql = appContext.getString(R.string.create_child_data_table_sql);
			appDatabase.execSQL(String.format(sql, Input.table_name));

			RegisteredChild child = null;
			Cursor cursor = rawQueryData("SELECT * FROM " + Table + " WHERE _id = " + String.valueOf(insertId));
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					child = cursorToRegisteredChild(cursor);
				}
				cursor.close();
				return child;
			}

			return null;
		}

		public RegisteredChild restoreChild(RegisteredChild child) {
			Utility.LogMethod();

			RegisteredChild regChild = null;
			Cursor cursor = rawQueryData("SELECT * FROM " + Table + " WHERE token = " + child.token);
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					regChild = cursorToRegisteredChild(cursor);
				}
				cursor.close();
			}
			return regChild;
		}

		public synchronized void delete(RegisteredChild Input) {
			String table = appContext.getString(R.string.children_table);
			appDatabase.delete(table, ID_COLUMN + " = " + Input.id, null);
		}

		public List<RegisteredChild> getAll() {
			Utility.LogMethod();

			List<RegisteredChild> users = new ArrayList<RegisteredChild>();
			Cursor cursor = rawQueryData("SELECT * FROM " + Table);
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					RegisteredChild registeredChild = cursorToRegisteredChild(cursor);
					users.add(registeredChild);
					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			}
			return users;
		}

		private RegisteredChild cursorToRegisteredChild(Cursor cursor) {
			// Utility.LogCursor(cursor);
			RegisteredChild child = new RegisteredChild();

			if (cursor.getCount() > 0) {
				int idx = 0;
				child.id = cursor.getInt(idx++);
				child.user_token = cursor.getString(idx++);
				child.name = cursor.getString(idx++);
				child.dob = cursor.getString(idx++);
				child.token = cursor.getString(idx++);
				child.table_name = cursor.getString(idx++);
				child.title = cursor.getString(idx++);
				child.photoUrl = cursor.getString(idx++);
				child.version = cursor.getDouble(idx++);
				child.timestamp = cursor.getInt(idx++);
			}
			return child;
		}
	}

	public class ChildDataTable {

		private String table = "";
		private String[] Columns = {};
		private String limit = "";

		public ChildDataTable(RegisteredChild child) {
			Utility.LogMethod();
			if (child != null)
				table = child.table_name;
			Columns = appContext.getResources().getStringArray(R.array.child_data_table_columns);
		}

		public void setLimit(String limit) {
			this.limit = limit;
		}

		private ContentValues fill(ChildData Input) {
			Utility.LogMethod();
			ContentValues values = new ContentValues();

			Date date = Utility.toDate(Input.date);
			String dateOnly = Utility.toString(date, Formats.Date);
			date = Utility.toDate(dateOnly);

			// y-M-D h:m a
			String dateString = String.format("%s-%s-%s %s %s", date.getYear() + 1900, date.getMonth() + 1,
					date.getDate(), Input.time, Input.ampm);
			Date datetime = Utility.toDate(dateString);
			values.put("datetime", Utility.toString(datetime));
			values.put("type", Input.type);
			switch (DataTypes.values()[Input.type]) {
			case Wet:
			case Poop:
				values.put("amount", 0);
				values.put("ozml", "");
				values.put("amount_oz", 0);
				break;
			case Nurse:
				values.put("amount", Input.amount);
				values.put("ozml", Input.ozml);
				// values.put("amount_oz", 0);
			case Bottle:
			case Pump:
				values.put("amount", Input.amount);
				values.put("ozml", Input.ozml);
				values.put("amount_oz", getAmountOz(Input));
				break;
			}
			values.put("description", Input.description);

			return values;
		}

		private double getAmountOz(ChildData data) {
			double amount_oz = 0;
			if (0 == data.ozml.compareTo("oz")) {
				amount_oz = data.amount;
			} else if (0 == data.ozml.compareTo("ml")) {
				amount_oz = data.amount * 0.033814;
			}
			return amount_oz;
		}

		public synchronized ChildData create(ChildData Input) {
			Utility.LogMethod();
			ContentValues values = fill(Input);
			Utility.Log(values);

			long insertId = appDatabase.insert(this.table, null, values);
			if (insertId == -1) {
				return null;
			}

			// updateAmountOz(insertId);

			return getFromId(insertId);
		}

		public ChildData getFromId(long id) {
			if (!Utility.IsValid(this.table))
				return null;

			ChildData data = null;
			String query = "SELECT * FROM " + this.table + " WHERE _id = " + String.valueOf(id);
			Cursor cursor = rawQueryData(query);
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					data = cursorToChildData(cursor);
					// Utility.LogCursor(cursor);
					Utility.Log("{0}", data.toString());
				}
				cursor.close();
			}
			return data;
		}

		public synchronized ChildData update(ChildData Input) {
			Utility.LogMethod();
			ContentValues values = fill(Input);

			int count = appDatabase.update(this.table, values, ID_COLUMN + " = " + Input.id, null);
			if (count == 0) {
				return null;
			}

			ChildData data = null;
			Cursor cursor = queryTable(this.table, Columns, ID_COLUMN + " = " + Input.id);
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					Utility.LogCursor(cursor);
					data = cursorToChildData(cursor);
					Utility.Log("{0}", data.toString());
				}
				cursor.close();
			}

			return data;
		}

		public synchronized boolean delete(ChildData Input) {
			Utility.LogMethod();

			int count = appDatabase.delete(this.table, ID_COLUMN + " = " + Input.id, null);
			if (count == 0) {
				return false;
			}
			return true;
		}

		public void dumpAll() {
			if (Constants.ReleaseMode)
				return;

			if (!Utility.IsValid(this.table))
				return;

			Utility.LogMethod();

			Cursor cursor = rawQueryData("SELECT * FROM " + this.table);
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					ChildData data = cursorToChildData(cursor);
					Utility.Log("{0}", data.toString());

					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			}

		}

		public void exportToFileStream(FileOutputStream fos) {
			if (!Utility.IsValid(this.table))
				return;

			Utility.LogMethod();
			String NEW_LINE = System.getProperty("line.separator");

			// UNDONE: Format oz/ml data
			Cursor cursor = rawQueryData("SELECT * FROM " + this.table);
			if (cursor != null) {
				cursor.moveToFirst();

				try {
					String data = Utility.toCsvRow(cursor) + NEW_LINE;
					fos.write(data.getBytes());
				} catch (Exception e) {
					Utility.LogError(e.toString());
				}

				while (!cursor.isAfterLast()) {
					try {
						String data = Utility.toCsvRow(cursor) + NEW_LINE;
						fos.write(data.getBytes());
					} catch (Exception e) {
						Utility.LogError(e.toString());
					}
					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			}
		}

		public List<ChildData> getAll() {
			Utility.LogMethod();
			return select("");
		}

		public List<ChildData> select(String whereClause) {
			if (!Utility.IsValid(this.table))
				return new ArrayList<ChildData>();

			Utility.LogMethod();

			whereClause += " ORDER BY datetime DESC";
			if (this.limit != "") {
				whereClause += String.format(" LIMIT %s", this.limit);
			}

			List<ChildData> rows = new ArrayList<ChildData>();
			Cursor cursor = rawQueryData("SELECT * FROM " + this.table + whereClause);

			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					ChildData ChildData = cursorToChildData(cursor);
					rows.add(ChildData);
					cursor.moveToNext();
				}
				// Make sure to close the cursor
				cursor.close();
			}
			return rows;
		}

		private ChildData cursorToChildData(Cursor Row) {
			// Utility.LogCursor(Row);

			ChildData user = new ChildData();
			user.id = Row.getInt(0);
			user.datetime = Utility.toDate(Row.getString(1));
			user.type = Row.getInt(2);
			user.amount = Row.getDouble(3);
			user.ozml = Row.getString(4);
			user.description = Row.getString(5);
			user.amount_oz = Row.getDouble(6);
			user.timestamp = Row.getInt(7);

			user.date = Utility.toString(user.datetime, Formats.Date);
			user.time = Utility.toString(user.datetime, Formats.TimeAmPm);
			user.ampm = user.datetime.getHours() < 12 || user.datetime.getHours() == 24 ? "am" : "pm";
			return user;
		}

	} // StoredChildData

	/*
	 * || / % + - << <> & | < <= > >= = == != <> IS IN LIKE GLOB BETWEEN AND OR
	 */
	public class Query {
		private RegisteredChild child = null;
		private String table = null;
		private String minDate = null;
		private String maxDate = null;
		private DataTypes type = null;
		private String limit = "";

		public void clearType() {
			this.type = null;
		}

		public void setType(DataTypes type) {
			this.type = type;
		}

		public void setLimit(String limit) {
			this.limit = limit;
		}

		public void setDateRange(String min, String max) {
			this.minDate = min;
			this.maxDate = max;
		}

		public Query(RegisteredChild child) {
			// Utility.LogMethod("{0}", child != null ? child.table_name :
			// "<null>");

			this.child = child;
			if (this.child != null) {
				this.table = this.child.table_name;
			}
		}

		private Cursor rawQuery(String query) {
			if (this.limit != "") {
				query += String.format(" LIMIT %s", this.limit);
			}

			return rawQueryData(query);
		}

		public int dumpQuery(String query) {
			if (this.limit != "") {
				query += String.format(" LIMIT %s", this.limit);
			}

			String sql = "select " + query + " from " + this.table;

			Cursor cursor = rawQueryData(sql);
			int count = 0;
			if (cursor != null) {
				count = cursor.getCount();
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					Utility.LogCursor(cursor);
					cursor.moveToNext();
				}
				cursor.close();
			}
			return count;
		}

		public String filteredQueryValue(String sql, String value) {
			Cursor cursor = filteredQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public int filteredQueryValue(String sql, int value) {
			Cursor cursor = filteredQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public double filteredQueryValue(String sql, double value) {
			Cursor cursor = filteredQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public String rawQueryValue(String sql, String value) {
			Cursor cursor = rawQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public int rawQueryValue(String sql, int value) {
			Cursor cursor = rawQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public double rawQueryValue(String sql, double value) {
			Cursor cursor = rawQuery(sql);
			if (cursor != null) {
				value = extractValue(cursor, value);
				cursor.close();
			}
			return value;
		}

		public Date rawQueryValue(String sql) {
			Cursor cursor = rawQuery(sql);
			if (cursor != null) {
				Date value = extractDate(cursor);
				cursor.close();
				return value;
			}
			return new Date();
		}

		public String queryMaxDateTime() {
			if (!Utility.IsValid(this.table))
				return "";

			String sql = "select max(datetime) from " + this.table;
			String value = rawQueryValue(sql, "");
			if (!value.isEmpty()) {
				return Utility.normalizeDate(value);
			}

			sql = "SELECT datetime('now', 'localtime')";
			return rawQueryValue(sql, Utility.toSqlDate());
		}

		public String queryMinDateTime() {
			if (!Utility.IsValid(this.table))
				return "";

			String sql = "select min(datetime) from " + this.table;
			String value = rawQueryValue(sql, Utility.toSqlDate());
			if (!value.isEmpty()) {
				return value;
			}

			sql = "SELECT datetime('now', 'localtime')";
			return rawQueryValue(sql, Utility.toSqlDate());
		}

		public String queryNowDate() {
			if (!Utility.IsValid(this.table))
				return "";

			String query = String.format("SELECT datetime('now', 'localtime')");
			String value = rawQueryValue(query, Utility.toSqlDate());
			Utility.Log("{0}", value);
			return value;
		}

		public String queryNowDate(String delta) {
			if (!Utility.IsValid(this.table))
				return "";

			String query = String.format("SELECT datetime('now', 'localtime', '%s')", delta);
			String value = rawQueryValue(query, Utility.toSqlDate());
			Utility.Log("{0}", value);
			return value;
		}

		public Date queryNowDateObject() {
			if (!Utility.IsValid(this.table))
				return new Date();

			String query = String.format("SELECT datetime('now', 'localtime')");
			return rawQueryValue(query);
		}

		public Date queryNowDateObject(String delta) {
			if (!Utility.IsValid(this.table))
				return new Date();

			String query = String.format("SELECT datetime('now', 'localtime', '%s')", delta);
			return rawQueryValue(query);
		}

		public String queryMaxDate() {
			if (!Utility.IsValid(this.table))
				return "";

			String value = queryMaxDateTime();
			String query = String.format("SELECT date('%s')", value);
			return rawQueryValue(query, Utility.toSqlDate());
		}

		public String queryMinDate() {
			if (!Utility.IsValid(this.table))
				return "";

			String value = queryMinDateTime();
			String query = String.format("SELECT date('%s')", value);
			return rawQueryValue(query, Utility.toSqlDate());
		}

		private int extractValue(Cursor cursor, int other) {
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					// Utility.LogCursor(cursor);
					return cursor.getInt(0);
				}
			}

			return other;
		}

		private double extractValue(Cursor cursor, double other) {
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					// Utility.LogCursor(cursor);
					return cursor.getDouble(0);
				}
			}

			return other;
		}

		private String extractValue(Cursor cursor, String other) {
			if (cursor != null) {
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					String value = cursor.getString(0);
					if (Utility.IsValid(value)) {
						return value;
					}
				}
			}

			return other;
		}

		private Date extractDate(Cursor cursor) {
			String value = extractValue(cursor, "");
			if (Utility.IsValid(value)) {
				return Utility.toDate(value);
			}

			return new Date();
		}

		public String getFilteredWhereClause() {
			// Utility.LogMethod();

			StringBuilder query = new StringBuilder("");
			if (this.type != null || this.minDate != null || this.maxDate != null) {
				query.append(" where 1=1 ");
				if (this.type != null) {
					query.append(" and type = ");
					query.append(this.type.ordinal());
				}

				if (this.minDate != null) {
					query.append(" and datetime >= ");
					query.append(this.minDate);
				}

				if (this.maxDate != null) {
					query.append(" and datetime < ");
					query.append(this.maxDate);
				}
			}
			return query.toString();
		}

		public String getFilteredQuery(String sql) {
			// Utility.LogMethod();

			StringBuilder query = new StringBuilder("select ");
			query.append(sql);
			query.append(" from ");
			query.append(this.table);
			query.append(getFilteredWhereClause());
			return query.toString();
		}

		private Cursor filteredQuery(String sql) {
			// Utility.LogMethod();
			if (this.table == null || this.table.compareTo("") == 0) {
				return null;
			}
			Cursor cursor = rawQuery(getFilteredQuery(sql));
			return cursor;
		}

	} // Query
}
