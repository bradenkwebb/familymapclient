package edu.byu.cs240.familymapclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import requests.LoginRequest;
import results.LoginResult;


public class LoginFragment extends Fragment {

    private Listener listener;
    private static final String LOG_TAG = "LoginFragment";
    private static final String USERNAME_KEY = "Username";
    private static final String AUTH_TOKEN_KEY = "AuthToken";
    private static final String PERSON_ID_KEY = "PersonID";
    private static final String SUCCESS_KEY = "SuccessfulLogin";
    private static final String ERR_MESSAGE_KEY = "ErrorMessage";

    public interface Listener{
        void notifyDone(); // probably need to change this a bit?
    }

    public void registerListener(Listener listener) { this.listener = listener; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button signInButton = view.findViewById(R.id.signInButton);
        Button registerButton = view.findViewById(R.id.registerButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText serverHost = (EditText) view.findViewById(R.id.server_host);
                EditText serverPort = (EditText) view.findViewById(R.id.server_port);

                ServerProxy.setServerHost("10.0.2.2");
                ServerProxy.setServerPort("8080");
//                ServerProxy.setServerHost(serverHost.getText().toString());
//                ServerProxy.setServerPort(serverPort.getText().toString());

                // Set up a handler that will process messages from the task and make updates on the UI thread
                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        // Add code here to react appropriately to the LoginResult
                        Bundle data = message.getData();
                        if (data.getBoolean(SUCCESS_KEY, false)) {
                            listener.notifyDone();
                            Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                            Log.d(LOG_TAG, "Handling message for login");
                        } else {
                            Toast.makeText(getActivity(),
                                    "Login unsuccessful;\n" +
                                        data.getString(ERR_MESSAGE_KEY, "an error occurred"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };

                // Create and execute the download task on a separate thread
                LoginTask task = new LoginTask(uiThreadMessageHandler, "bkwebb23", "password");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText serverHost = (EditText) view.findViewById(R.id.server_host);
                EditText serverPort = (EditText) view.findViewById(R.id.server_port);
                ServerProxy.setServerHost(serverHost.getText().toString());
                ServerProxy.setServerPort(serverPort.getText().toString());

                // Set up a handler that will process messages from the task and make updates on the UI thread
                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        // Add code here to react appropriately to the registerresult
//                        Toast.makeText(
//                                LoginFragment.this,
//                                R.string.invalid_credentials,
//                                Toast.LENGTH_SHORT)
//                                .show();
                        Log.d(LOG_TAG, "Handling the message");
                        System.out.println("At least we got this far!");
                    }
                }; // MIGHT NEED TO USE AN ANONYMOUS CLASS HERE

                // Create and execute the download task on a separate thread
                LoginTask task = new LoginTask(uiThreadMessageHandler, "bkwebb23", "password");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });

        return view;
    }

    private static class LoginTask implements Runnable {

        private final Handler messageHandler;
        private String username;
        private String password;

        public LoginTask(Handler messageHandler, String username, String password) {
            this.messageHandler = messageHandler;
            this.username = username;
            this.password = password;
        }

        @Override
        public void run() {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);
            LoginResult result = new ServerProxy().login(request);
            sendMessage(result);
        }

        private void sendMessage(LoginResult result) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            messageBundle.putBoolean(SUCCESS_KEY, result.isSuccess());
            if (result.isSuccess()) {
                messageBundle.putString(USERNAME_KEY, result.getUsername());
                messageBundle.putString(AUTH_TOKEN_KEY, result.getAuthtoken());
                messageBundle.putString(PERSON_ID_KEY, result.getPersonID());
            } else {
                messageBundle.putString(ERR_MESSAGE_KEY, result.getMessage());
            }

            message.setData(messageBundle);
            messageHandler.sendMessage(message);
        }
    }

    private static class RegisterTask implements Runnable {

        private final Handler messageHandler;
        public RegisterTask(Handler messageHandler, String username, String password,
                            String firstName, String lastName, String gender) {
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {

        }
    }
}