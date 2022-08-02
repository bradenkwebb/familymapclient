package edu.byu.cs240.familymapclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Person;
import model.User;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.PeopleResult;


public class LoginFragment extends Fragment {

    private Listener listener;
    private static final String LOG_TAG = "LoginFragment";
    private static final String USERNAME_KEY = "Username";
    private static final String AUTH_TOKEN_KEY = "AuthToken";
    private static final String PERSON_ID_KEY = "PersonID";
    private static final String SUCCESS_KEY = "SuccessfulLogin";
    private static final String ERR_MESSAGE_KEY = "ErrorMessage";

    public interface Listener{
        void notifyDone();
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
                EditText serverHost = getActivity().findViewById(R.id.server_host);
                EditText serverPort = getActivity().findViewById(R.id.server_port);
                EditText username = getActivity().findViewById(R.id.user_name);
                EditText password = getActivity().findViewById(R.id.password);

                ServerProxy.setServerHost(serverHost.getText().toString());
                ServerProxy.setServerPort(serverPort.getText().toString());

                // I should fix this suppression
                @SuppressLint("HandlerLeak")Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Log.d(LOG_TAG, "Handling message for login");

                        // Add code here to react appropriately to the LoginResult
                        Bundle data = message.getData();
                        if (data.getBoolean(SUCCESS_KEY, false)) {
                            listener.notifyDone();
                            Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                            displayName(data);
                        } else {
                            Toast.makeText(getActivity(),
                                    "Login unsuccessful\n" +
                                        data.getString(ERR_MESSAGE_KEY, "an error occurred"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };

                // Create and execute the download task on a separate thread
                LoginTask task = new LoginTask(uiThreadMessageHandler,
                                            username.getText().toString(),
                                            password.getText().toString());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText serverHost = getActivity().findViewById(R.id.server_host);
                EditText serverPort = getActivity().findViewById(R.id.server_port);
                ServerProxy.setServerHost(serverHost.getText().toString());
                ServerProxy.setServerPort(serverPort.getText().toString());

                EditText username = getActivity().findViewById(R.id.user_name);
                EditText password = getActivity().findViewById(R.id.password);
                EditText email = getActivity().findViewById(R.id.email);
                EditText firstName = getActivity().findViewById(R.id.firstName);
                EditText lastName = getActivity().findViewById(R.id.lastName);
                RadioGroup radio = getActivity().findViewById(R.id.gender_radio);
                RadioButton genderButton = getActivity().findViewById(radio.getCheckedRadioButtonId());

                if (genderButton == null) {
                    Toast.makeText(getActivity(),
                                    "You must select a gender",
                                    Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                String gender = genderButton.getText().toString();
                gender = (gender.equalsIgnoreCase("female")) ? "f": "m";

                // I should fix this suppression
                @SuppressLint("HandlerLeak") Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Log.d(LOG_TAG, "Handling the registration message");

                        // Add code here to react appropriately to the registerresult
                        Bundle data = message.getData();
                        if (data.getBoolean(SUCCESS_KEY, false)) {
                            listener.notifyDone();

                            Toast.makeText(getActivity(),
                                        "Registration successful",
                                            Toast.LENGTH_SHORT)
                                    .show();

                            displayName(data);
                        } else {
                            String err_message = data.getString(ERR_MESSAGE_KEY,
                                    "An error occurred when registering");
                            Toast.makeText(getActivity(),
                                        "Registration unsuccessful:\n" + err_message,
                                            Toast.LENGTH_LONG)
                                            .show();
                            Log.e(LOG_TAG, "Unsuccessful registration: " + err_message);
                        }
                    }
                };

                // Create and execute the download task on a separate thread
                RegisterTask task = new RegisterTask(uiThreadMessageHandler,
                                    username.getText().toString(), password.getText().toString(),
                                    email.getText().toString(), firstName.getText().toString(),
                                    lastName.getText().toString(), gender);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);

            }
        });

        return view;
    }

    private static class LoginTask implements Runnable {

        private final Handler messageHandler;
        private final LoginRequest request;

        public LoginTask(Handler messageHandler, String username, String password) {
            this.messageHandler = messageHandler;
            request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);
        }

        @Override
        public void run() {
            LoginResult result = new ServerProxy().login(request);
            messageHandler.sendMessage(toMessage(result));
        }
    }

    private static class RegisterTask implements Runnable {

        private final Handler messageHandler;
        private RegisterRequest request;

        public RegisterTask(Handler messageHandler, String username, String password, String email,
                            String firstName, String lastName, String gender) {
            this.messageHandler = messageHandler;
            request = new RegisterRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setEmail(email);
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setGender(gender);
        }

        @Override
        public void run() {
            LoginResult result = new ServerProxy().register(request);
            messageHandler.sendMessage(toMessage(result));
        }
    }

    private void displayName(Bundle data) {
        String userPersonId = data.getString(PERSON_ID_KEY, "error");
        DataCache.getInstance().setUserPersonID(userPersonId);
        Person userPerson = DataCache.getInstance().getPeople().get(userPersonId);

        Toast.makeText(getActivity(),
                userPerson.getFirstName() + " " + userPerson.getLastName(),
                Toast.LENGTH_SHORT).show();
    }


    protected static Message toMessage(LoginResult result) {
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
        return message;
    }
}