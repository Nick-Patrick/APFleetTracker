package com.aphaulage.apfleettracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LoginJson {

	private static final String TAG = "LoginJson";
	
	static InputStream is = null;
	static JSONObject jObject = null;
	static String jsonString = "";
	private String userKey = "9c36c7108a73324100bc9305f581979071d45ee9";
	
	static public String driverId = "";
	
	private static Boolean successful = false;

	
	public JSONObject getUserCreds(String email, String password){
		//HTTP Post Parameters
		Log.i("JSON", email);
		Log.i("JSON", password);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", email));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("key", userKey));
		
		JSONObject jsonObject = null;
		JSONObject jsonDriverObject = null;
		String jsonValue = "";

		try {
			jsonObject = getJsonFromUrl("http://aphaulage.co.uk/apTracker/users/driversByEmailPassword.json", params);
			Log.i("jsonObject123", jsonObject.toString());
			jsonValue = jsonObject.getString("username");
			Log.i("jsonValue123", jsonValue);
			List<NameValuePair> paramEmail = new ArrayList<NameValuePair>();
			paramEmail.add(new BasicNameValuePair("email", jsonValue));
			paramEmail.add(new BasicNameValuePair("key", "9c36c7108a73324100bc9305f581979071d45ee9"));
			jsonDriverObject = getJsonFromUrl("http://aphaulage.co.uk/apTracker/drivers/driversByEmail.json", paramEmail);
			Log.i("jsonDriverObject",jsonDriverObject.toString());
			driverId = jsonDriverObject.getString("email");
			Log.i("driverId",driverId);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		
		if(jsonObject != null) {
			
			Log.i("Working", jsonValue);
			LoginJson.successful = true;
		}
		else {
			Log.i("jsonValue", "empty");
		}
		

		return jsonObject;
	}
	
	public JSONObject getJsonFromUrl(String url, List<NameValuePair> params){
		//Make HTTP Request
		try {
			Log.i("getJsonFromUrl", "starting");
			DefaultHttpClient httpClient = new DefaultHttpClient();
			//Set HTTPPost for PHP login service.
			HttpPost httpPost = new HttpPost(url);
			//Send post parameters (email / password).
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
			Log.i("is", is.toString());
		}
		//Catch errors
		catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		catch (ClientProtocolException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		try {
			//Build StringBuilder for exporting the JSON from WebService.
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			//While there are lines left.
			while((line = reader.readLine()) != null){
				//Seperate json onto individual lines.
				sb.append(line +"\n");
				Log.i("line", line);
			}
			//Close InputStream.
			is.close();
			//Add stringbuilder to String.
			jsonString = sb.toString();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		//Parse string into a JSONObject.
		try {
			jObject = new JSONObject(jsonString);
			Log.i("jObject", jObject.toString());

		}
		catch (JSONException e){
			e.printStackTrace();
		}
		
		//Return JSONArray.
		return jObject;
	}
	
	public void setSuccessful(Boolean success){
		LoginJson.successful = success;
	}
	
	public Boolean getSuccessful(){
		return LoginJson.successful;
	}


}