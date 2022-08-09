package edu.byu.cs240.familymapclient;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import model.Event;
import model.Person;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.AllEventsResult;
import results.LoginResult;
import results.PeopleResult;

public class ServerProxy {

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
            String reqData = makeLoginJson(request);

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);

            reqBody.close();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = http.getInputStream();
                System.out.println("Login successful");
                result = deserialize(responseBody, LoginResult.class);
                populateDataCache(result);
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
            String reqData = makeRegisterJson(request);

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);

            reqBody.close();

            InputStream respBody;

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(LOG_TAG, "Registration successful");
                respBody = http.getInputStream();
                result = deserialize(respBody, LoginResult.class);
                populateDataCache(result);
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

    public static void setServerHost(String serverHost) {
        ServerProxy.serverHost = serverHost;
    }

    public static void setServerPort(String serverPort) {
        ServerProxy.serverPort = serverPort;
    }

    private String makeLoginJson(LoginRequest request) {
        return  "{" +
                        "\"username\": \"" + request.getUsername() + "\"," +
                        "\"password\": \"" + request.getPassword() + "\"" +
                "}";
    }

    private String makeRegisterJson(RegisterRequest request) {
        return  "{" +
                        "\"username\": \"" + request.getUsername() + "\"," +
                        "\"password\": \"" + request.getPassword() + "\"," +
                        "\"email\": \"" + request.getEmail() + "\"," +
                        "\"firstName\": \"" + request.getFirstName() + "\"," +
                        "\"lastName\": \"" + request.getLastName() + "\"," +
                        "\"gender\": \"" + request.getGender() + "\"" +
                "}";
    }

    private void populateDataCache(LoginResult r) {
        DataCache.getInstance().setUserPersonID(r.getPersonID());
        getData(r.getAuthtoken(), Person.class);
        getData(r.getAuthtoken(), Event.class);
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
            } else {
                DataCache.getInstance().resultToEvents(deserialize(response, AllEventsResult.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    private <T> T deserialize(InputStream bodyStream, Class<T> classType) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(bodyStream, StandardCharsets.UTF_8));
        Gson gson = new Gson();
        return gson.fromJson(br, classType);
    }
}
