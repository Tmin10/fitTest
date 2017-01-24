package ru.tmin10.fitTest;/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.fitness.*;
import com.google.api.services.fitness.Fitness.Users.DataSources.Datasets.Get;
import com.google.api.services.fitness.Fitness.Users.Dataset.Aggregate;
import com.google.api.services.fitness.model.*;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Command-line sample for the Google OAuth2 API described at <a
 * href="http://code.google.com/apis/accounts/docs/OAuth2Login.html">Using OAuth 2.0 for Login
 * (Experimental)</a>.
 *
 * @author Yaniv Inbar
 */
public class OAuth2Sample {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "fit reader";

  /** Directory to store user credentials. */
  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".store/oauth2_sample");
  
  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static FileDataStoreFactory dataStoreFactory;

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** OAuth 2.0 scopes. */
  private static final List<String> SCOPES = Arrays.asList(
      "https://www.googleapis.com/auth/userinfo.profile",
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/fitness.activity.read",
      "https://www.googleapis.com/auth/fitness.body.read",
      "https://www.googleapis.com/auth/fitness.location.read",
      "https://www.googleapis.com/auth/fitness.nutrition.read");

  private static Oauth2 oauth2;
  private static GoogleClientSecrets clientSecrets;

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new InputStreamReader(OAuth2Sample.class.getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
          + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
        dataStoreFactory).build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public static void main(String[] args) {
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
      // authorization
      Credential credential = authorize();
      // set up global Oauth2 instance
      oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
          APPLICATION_NAME).build();
      // run commands
      tokenInfo(credential.getAccessToken());
      //userInfo();
      // success!
      
      
      Fitness f = new Fitness(httpTransport, JSON_FACTORY, credential);
//      Fitness.Users.DataSources.List r = f.users().dataSources().list("me");
//      ListDataSourcesResponse l = r.execute();
//      List<DataSource> list = l.getDataSource();
//      for (int i = 0; i < list.size(); i++)
//      {
//    	  System.out.println(list.get(i).getDataStreamId());
//      }
      
//      Get g = f.users().dataSources().datasets().get("me", "derived:com.google.step_count.delta:com.google.android.gms:estimated_steps", getNow()+"-"+getDayStart());
//      Dataset d = g.execute();    
//      System.out.println(d.toPrettyString());
      
      AggregateRequest content = new AggregateRequest();
      content.setStartTimeMillis(getDayStartMillis());
      content.setEndTimeMillis(getNowMillis());
      ArrayList<AggregateBy> aggregatebyList = new ArrayList<AggregateBy>();
      AggregateBy agby = new AggregateBy();
      agby.setDataSourceId("derived:com.google.step_count.delta:com.google.android.gms:estimated_steps");
      aggregatebyList.add(agby);
      content.setAggregateBy(aggregatebyList);
      BucketByTime bucketByTime = new BucketByTime();
      bucketByTime.setDurationMillis((long) (1000*60*60));
      content.setBucketByTime(bucketByTime);
      Aggregate agg = f.users().dataset().aggregate("me", content);
      AggregateResponse resp = agg.execute();
      System.out.println(resp.toPrettyString());
      
      List<AggregateBucket> results = resp.getBucket();
      for (int i = 0; i < results.size(); i++)
      {
    	  List<Dataset> ds = results.get(i).getDataset();
    	  for (int j = 0; j < ds.size(); j++)
    	  {
    		  List<DataPoint> dp = ds.get(j).getPoint();
    		  for (int k = 0; k < dp.size(); k++)
    		  {
    			  List<Value> v = dp.get(k).getValue();
    			  for (int x = 0; x < v.size(); x++)
    			  {
    				  System.out.println(v.get(x).getIntVal());
    				  
    			  }
    		  }
    	  }
      }
      
      return;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
  
  private static String getNow()
  {
      return String.valueOf(getNowMillis())+"000000";
  }
  
  private static Long getNowMillis()
  {
	  Calendar calendar = Calendar.getInstance();
      long now = calendar.getTimeInMillis();
      now -= 1000*60*60*24;
      return now;
  }

  private static String getDayStart()
  {
      return String.valueOf(getDayStartMillis())+"000000";
  }
  
  private static Long getDayStartMillis()
  {
	  Calendar calendar = Calendar.getInstance();
	  calendar.set(Calendar.HOUR_OF_DAY, 0);
	  calendar.set(Calendar.MINUTE, 0);
	  calendar.set(Calendar.SECOND, 0);
	  calendar.set(Calendar.MILLISECOND, 0);
      long start = calendar.getTimeInMillis();
      start -= 1000*60*60*24;
      return start;
  }

  private static void tokenInfo(String accessToken) throws IOException {
    header("Validating a token");
    Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
    System.out.println(tokeninfo.toPrettyString());
    if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
      System.err.println("ERROR: audience does not match our client ID!");
    }
  }

  private static void userInfo() throws IOException {
    header("Obtaining User Profile Information");
    Userinfoplus userinfo = oauth2.userinfo().get().execute();
    System.out.println(userinfo.toPrettyString());
  }

  static void header(String name) {
    System.out.println();
    System.out.println("================== " + name + " ==================");
    System.out.println();
  }
}