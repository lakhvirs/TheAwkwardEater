package lakhvir.theawkwardeater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LoginActivity extends ActionBarActivity {

    private EditText usernameText, passwordText;
    private Button login, register;
    private JSONParser jParser = new JSONParser();
    private static final String LOGIN_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/loginScript.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameText = (EditText) findViewById(R.id.login_username);
        passwordText = (EditText) findViewById(R.id.login_password);

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.startRegistration);

        SharedPreferences sharedPref = getSharedPreferences("data",MODE_PRIVATE);
        String username = sharedPref.getString("username", "");
        String password = sharedPref.getString("password", "");
        usernameText.setText(username);
        passwordText.setText(password);
        if (!username.equals("") && !password.equals("")) {
            new UserLogin().execute();
        } else{
           Toast.makeText (getApplicationContext(), "Incorrect password or username", Toast.LENGTH_SHORT).show();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UserLogin().execute();
            }

        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(in);
            }

        });
    }

    class UserLogin extends AsyncTask<String, String, String>{

        String loginUsername = usernameText.getText().toString();
        String loginPassword = passwordText.getText().toString();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Logging in...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {

            //username and password storing
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("enteredUsername", loginUsername));
            nameValuePairs.add(new BasicNameValuePair("enteredPassword", loginPassword));
            String json = jParser.makeHttpRequest(LOGIN_URL, "POST", nameValuePairs);
            Log.d("json", json);
            JSONArray userLoginDetails = null;
            String retrievedUsername = null;
            String retrievedPassword = null;
            try {
                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                userLoginDetails = jsonObj.getJSONArray("userLogin");

                JSONObject c = userLoginDetails.getJSONObject(0);
                retrievedUsername = c.getString("username");
                Log.d("retUser", retrievedUsername);
                User.set_userid(Integer.parseInt(c.getString("userID")));
                retrievedPassword = c.getString("password");
                Log.d("retPass", retrievedPassword);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putString("username", loginUsername);
            prefEditor.putString("password", loginPassword);
            prefEditor.commit();
            Intent in = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(in);
        }

    }


}
