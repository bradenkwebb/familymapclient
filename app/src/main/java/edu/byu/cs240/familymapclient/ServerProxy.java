package edu.byu.cs240.familymapclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Event;
import model.Person;
import requests.LoginRequest;
import requests.RegisterRequest;
import requests.Request;
import results.AllEventsResult;
import results.LoginResult;
import results.PeopleResult;
import results.PersonResult;
import results.RegisterResult;
import results.Result;

public class ServerProxy { // also known as ServerFacade

    private static final String LOG_TAG = "ServerProxy";
    private static String serverHost;
    private static String serverPort;

    public LoginResult login(LoginRequest request) {
        LoginResult result = new LoginResult();
        result.setSuccess(false);
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/login");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true); // Indicates that the request contains a request body
            http.addRequestProperty("Accept", "application/json");
            http.connect();
            String reqData =
                            "{" +
                                "\"username\": \"" + request.getUsername() + "\"," +
                                "\"password\": \"" + request.getPassword() + "\"" +
                            "}";

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);

            reqBody.close();

            InputStream responseBody = http.getInputStream();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("Login successful");
                result = deserialize(responseBody, LoginResult.class);
                populateDataCache(result.getAuthtoken());
            } else {
                System.out.println("Error: " + http.getResponseMessage());
                InputStream respBody = http.getErrorStream();
                result = deserialize(respBody, LoginResult.class);
                Log.e(LOG_TAG, result.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public LoginResult register(RegisterRequest request) {
        LoginResult result = new LoginResult();
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true); // Indicates that the request contains a request body
            http.addRequestProperty("Accept", "application/json");
            http.connect();
            String reqData =
                    "{" +
                            "\"username\": \"" + request.getUsername() + "\"," +
                            "\"password\": \"" + request.getPassword() + "\"," +
                            "\"email\": \"" + request.getEmail() + "\"," +
                            "\"firstName\": \"" + request.getFirstName() + "\"," +
                            "\"lastName\": \"" + request.getLastName() + "\"," +
                            "\"gender\": \"" + request.getGender() + "\"" +
                    "}";

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);

            reqBody.close();

            InputStream respBody;

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(LOG_TAG, "Registration successful");
                respBody = http.getInputStream();
                result = deserialize(respBody, LoginResult.class);
                populateDataCache(result.getAuthtoken());
            } else {
                Log.e(LOG_TAG, "Unsuccessful registration: " + http.getResponseMessage());
                respBody = http.getErrorStream();
                result = deserialize(respBody, LoginResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void populateDataCache(String authToken) {
        getData(authToken, Person.class);
        getData(authToken, Event.class);
        DataCache.getInstance().generatePersonEvents();
    }

    private <T> void getData(String authToken, Class<T> classType) {
        String urlExtension;
        try {
            if (classType == Person.class) {
                urlExtension = "person";
            } else if (classType == Event.class) {
                urlExtension = "event";
            } else {
                throw new IOException("Invalid data type parameter");
            }

            URL url = new URL("http://" + serverHost + ":" + serverPort + "/" + urlExtension);

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setDoOutput(false); // Indicates that the request contains a request body
            http.setRequestProperty("Authorization", authToken);
            http.addRequestProperty("Accept", "application/json");
            http.connect();

            InputStream response = http.getInputStream();
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(LOG_TAG, "Got " + urlExtension + "s successfully");
            } else {
                response = http.getErrorStream();
                Log.e(LOG_TAG, "Error occurred when collecting " + urlExtension + "s");
            }

            if (classType == Person.class) {
                DataCache.getInstance().resultToPeople(deserialize(response, PeopleResult.class));
            } else if (classType == Event.class) {
                DataCache.getInstance().resultToEvents(deserialize(response, AllEventsResult.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    // THIS IS DUPLICATE CODE
    private void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    // ALSO DUPLICATE CODE
    private <T> T deserialize(InputStream bodyStream, Class<T> classType) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(bodyStream, StandardCharsets.UTF_8));
        Gson gson = new Gson();
        return gson.fromJson(br, classType);
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static void setServerHost(String serverHost) {
        ServerProxy.serverHost = serverHost;
    }

    public static String getServerPort() {
        return serverPort;
    }

    public static void setServerPort(String serverPort) {
        ServerProxy.serverPort = serverPort;
    }

//           DON'T WORRY ABOUT THESE, THEY SHOULDN'T BE NEEDED
//    clear
//    fill
//    getPerson
//    getEvent
//    load
}
