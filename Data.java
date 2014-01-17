package com.joyofplaying.babytracker;

import java.util.Date;

import android.content.SharedPreferences;

public class Data {

	public static class RegisteredUser {
		private static final String REGISTERED_USER_TOKEN = "RegisteredUser_token";
		private static final String REGISTERED_USER_USER_ID = "RegisteredUser_user_id";
		public int id;
		public String name;
		public String user_id;
		public String password;
		public String token;
		public float version;
		public double timestamp;

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");

			result.append(this.getClass().getName() + " Object {" + NEW_LINE);
			result.append(" id: " + id + NEW_LINE);
			result.append(" name: " + name + NEW_LINE);
			result.append(" user_id: " + user_id + NEW_LINE);
			result.append(" password: " + password + NEW_LINE);
			result.append(" token: " + token + NEW_LINE);
			result.append(" version: " + version + NEW_LINE);
			result.append(" timestamp: " + timestamp + NEW_LINE);
			result.append("}");

			return result.toString();
		}

		public static RegisteredUser restoreFromSettings(SharedPreferences settings) {
			RegisteredUser user = new RegisteredUser();
			user.restore(settings);
			return user;
		}

		private void restore(SharedPreferences settings) {
			// this.name = settings.getString("RegisteredUser_name", "");
			this.user_id = settings.getString(REGISTERED_USER_USER_ID, "");
			this.token = settings.getString(REGISTERED_USER_TOKEN, "");
		}

		public void saveToSettings(SharedPreferences.Editor editor) {
			// editor.putString("RegisteredUser_name", this.name);
			editor.putString(REGISTERED_USER_USER_ID, this.user_id);
			editor.putString(REGISTERED_USER_TOKEN, this.token);
			// editor.putFloat("RegisteredUser_version", this.version);
		}
	}

	/*
	 * <string-array name="children_table_columns"> <item>_id</item>
	 * <item>user_token</item> <item>name</item> <item>dob</item>
	 * <item>token</item> <item>tablename</item> <item>title</item>
	 * <item>version</item> <item>photoUrl</item> <item>timestamp</item>
	 * </string-array>
	 */
	public static class RegisteredChild {
		private static final String REGISTERED_CHILD_DOB = "RegisteredChild_dob";
		private static final String REGISTERED_CHILD_PHOTO_URL = "RegisteredChild_photoUrl";
		private static final String REGISTERED_CHILD_NAME = "RegisteredChild_name";
		private static final String REGISTERED_CHILD_TABLE_NAME = "RegisteredChild_table_name";
		private static final String REGISTERED_CHILD_TOKEN = "RegisteredChild_token";
		public int id;
		public String user_token = "";
		public String name = "";
		public String dob = "";
		public String token = "";
		public String table_name = "";
		public String title = "";
		public double version = 0.0;
		public String photoUrl = "";
		public double timestamp = 0.0;

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");

			result.append(this.getClass().getName() + " Object {" + NEW_LINE);
			result.append(" ID: " + id + NEW_LINE);
			result.append(" UserToken: " + user_token + NEW_LINE);
			result.append(" Name: " + name + NEW_LINE);
			result.append(" DOB: " + dob + NEW_LINE);
			result.append(" Token: " + token + NEW_LINE);
			result.append(" TableName: " + table_name + NEW_LINE);
			result.append(" Title: " + title + NEW_LINE);
			result.append(" Version: " + version + NEW_LINE);
			result.append(" PhotoUrl: " + photoUrl + NEW_LINE);
			result.append(" Timestamp: " + timestamp + NEW_LINE);
			result.append("}");

			return result.toString();
		}

		public static RegisteredChild restoreFromSettings(SharedPreferences settings) {
			RegisteredChild child = new RegisteredChild();
			child.restoreFromSettingsInner(settings);
			return child;
		}

		private void restoreFromSettingsInner(SharedPreferences appSettings) {
			this.token = appSettings.getString(REGISTERED_CHILD_TOKEN, "");
			this.table_name = appSettings.getString(REGISTERED_CHILD_TABLE_NAME, "");
			this.name = appSettings.getString(REGISTERED_CHILD_NAME, "");
			this.dob = appSettings.getString(REGISTERED_CHILD_DOB, "");
			this.photoUrl = appSettings.getString(REGISTERED_CHILD_PHOTO_URL, "");
		}

		public void saveToSettings(SharedPreferences.Editor editor) {
			editor.putString(REGISTERED_CHILD_TOKEN, this.token);
			editor.putString(REGISTERED_CHILD_TABLE_NAME, this.table_name);
			editor.putString(REGISTERED_CHILD_NAME, this.name);
			editor.putString(REGISTERED_CHILD_DOB, this.dob);
			editor.putString(REGISTERED_CHILD_PHOTO_URL, this.photoUrl);
		}
	}

	public static class ChildData {
		public int id;
		public Date datetime;
		public String date;
		public String time;
		public String ampm;
		public int type;
		public double amount;
		public String ozml;
		public String description;
		public double amount_oz;
		public double timestamp;

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");

			result.append(Utility.getClassName(this.getClass().getName()) + " Object {" + NEW_LINE);
			result.append(" id:        " + id + NEW_LINE);
			result.append(" datetime:  " + datetime + NEW_LINE);
			result.append(" date:      " + date + NEW_LINE);
			result.append(" time:      " + time + NEW_LINE);
			result.append(" ampm:      " + ampm + NEW_LINE);
			result.append(" type:      " + type + NEW_LINE);
			result.append(" amount:    " + amount + NEW_LINE);
			result.append(" ozml:      " + ozml + NEW_LINE);
			result.append(" note:      " + description + NEW_LINE);
			result.append(" amount_oz: " + amount_oz + NEW_LINE);
			result.append(" timestamp: " + timestamp + NEW_LINE);
			result.append("}");

			return result.toString();
		}

		public String toStringFlat() {
			StringBuilder result = new StringBuilder();
			String NEW_LINE = System.getProperty("line.separator");

			result.append("ChildData {");
			result.append(id + ",");
			result.append(datetime + ",");
			result.append(date + ",");
			result.append(time + ",");
			result.append(ampm + ",");
			result.append(type + ",");
			result.append(amount + ",");
			result.append(ozml + ",");
			result.append(description + ",");
			result.append(amount_oz + ",");
			result.append(timestamp + ",");
			result.append("}" + NEW_LINE);

			return result.toString();
		}
	}

}
