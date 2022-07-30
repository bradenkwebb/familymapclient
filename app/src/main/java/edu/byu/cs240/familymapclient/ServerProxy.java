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

import requests.LoginRequest;
import requests.RegisterRequest;
import requests.Request;
import results.AllEventsResult;
import results.LoginResult;
import results.PeopleResult;
import results.RegisterResult;
import results.Result;

public class ServerProxy { // also known as ServerFacade

    private static String serverHost;
    private static String serverPort;

    private static final String LOG_TAG = "ServerProxy";
// Should serialize requests, send them to server,
    // get results, and deserialize those

    public static void main(String[] args) {
        serverHost = args[0];
        serverPort = args[1];
    }

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

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = http.getInputStream();
                System.out.println("Registration successful");
                result = deserialize(responseBody, LoginResult.class);
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

    public RegisterResult register(RegisterRequest request) {
        RegisterResult result = new RegisterResult();
        try {
            URL url = new URL("http://" + serverHost + ":" + serverPort + "/register");

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

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // do something here to demonstrate success
                System.out.println("Registration successful");
                result = deserialize(http.getInputStream(), RegisterResult.class);
            } else {
                System.out.println("Error: " + http.getResponseMessage());
                InputStream respBody = http.getErrorStream();
                result = deserialize(http.getInputStream(), RegisterResult.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    PeopleResult getPeople(Request request) {
        return null;
    }
    AllEventsResult getEvents(Request request) {
        return null;
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
