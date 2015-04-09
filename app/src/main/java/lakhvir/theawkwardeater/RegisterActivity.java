package lakhvir.theawkwardeater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends ActionBarActivity {

    private final Context context = this;
    private Button allergies, intolerances, diets, register;
    private EditText username, password;

    private JSONParser jParser = new JSONParser();

    private static final String POPULATE_CHECKBOXES_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/generateAllergyChecbox.php";
    private static final String STORE_PREFERENCES_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/saveUserPreferences.php";
    private static final String STORE_LOGIN_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/saveUserLogin.php";
    private static final String GET_USER_ID = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/retrieveUserID.php";
    private static final String GET_USERNAMES = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/retrieveListOfUsernames.php";

    private HashMap<String, Integer> dietsMap = new HashMap<String,Integer>();
    private HashMap<String, Integer> intolerancesMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> allergiesMap = new HashMap<String, Integer>();

    //arrays to store the values the user has selected
    private boolean [] dietsCheckedItems;
    private boolean [] intolerancesCheckedItems;
    private boolean [] allergiesCheckedItems;

    String dataToPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        allergies = (Button) findViewById(R.id.regAllergies);
        intolerances = (Button) findViewById(R.id.regIntolerances);
        diets = (Button) findViewById(R.id.regDiet);
        register = (Button) findViewById(R.id.btnRegister);
        username = (EditText) findViewById(R.id.reg_fullname);
        password = (EditText) findViewById(R.id.reg_password);

        new GenerateCheckBox().execute();

        allergies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createDialog(allergiesMap, "Select Intolerances", allergiesCheckedItems);
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

        });
        intolerances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createDialog(intolerancesMap, "Select Allergies", intolerancesCheckedItems);
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }

        });
        diets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    createDialog(dietsMap, "Select a diet", dietsCheckedItems);
                } catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }

        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new registerUserPreferences().execute();
            }

        });

    }

    private void createDialog(HashMap<String,Integer> array, String title, final boolean [] checkedItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        CharSequence[] items = new CharSequence[array.size()];
        int i = 0;
        for (String value : array.keySet()) {
            items[i] = value;
            i ++;
        }
        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedItems[which] = isChecked;
            }
        });
        builder.show();
    }

    class GenerateCheckBox extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("Request made", "starting");
            String json = jParser.getJSONFromUrl(POPULATE_CHECKBOXES_URL);
            Log.d("Attempt Creation", json.toString());

            JSONArray jArray = null;
            try {
                JSONObject jsonObj = new JSONObject(json);
                // Getting JSON Array node
                jArray = jsonObj.getJSONArray("condition");
                // looping through All conditions
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject c = jArray.getJSONObject(i);
                    if (c.getString("conditionType").equals("Diet")) {
                        dietsMap.put(c.getString("conditionName"), c.getInt("conditionID"));
                        //dietsList.add(dietsMap);
                    } else if (c.getString("conditionType").equals("Allergy")) {
                        allergiesMap.put(c.getString("conditionName"), c.getInt("conditionID"));
                        //allergiesList.add(allergiesMap);
                    } else if (c.getString("conditionType").equals("Intolerance")) {
                        intolerancesMap.put(c.getString("conditionName"), c.getInt("conditionID"));
                        //intolerancesList.add(intolerancesMap);
                    }
                    //conditionIDs.add(c.getInt("conditionID"));
                }

                dietsCheckedItems = new boolean[dietsMap.size()];
                intolerancesCheckedItems = new boolean[dietsMap.size()];
                allergiesCheckedItems = new boolean[dietsMap.size()];

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        //update gui here as this uses runonuithread
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }


    class registerUserPreferences extends AsyncTask<String, String, String>{

        int errorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        //TODO: Need to ensure the same values aren't re added to the set of user conditions.
        @Override
        protected String doInBackground(String... params) {
            String enteredUsername = username.getText().toString();
            String enteredPassword = password.getText().toString();

            for (int i = 0; i < enteredUsername.length(); i ++) {
                if (!Character.isLetterOrDigit(enteredUsername.charAt(i))) {
                    errorCode = 2;
                    return null;
                }
            }

            if(enteredPassword.length() < 6){
                errorCode = 3;
                return null;
            }

            Log.d("Request made", "starting");
            String json = jParser.getJSONFromUrl(GET_USERNAMES);
            Log.d("Attempt Creation", json.toString());

            JSONArray jArray = null;
            try {
                JSONObject jsonObj = new JSONObject(json);
                // Getting JSON Array node
                jArray = jsonObj.getJSONArray("usernames");
                // looping through All conditions
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject c = jArray.getJSONObject(i);
                    if (c.getString("username").equals(enteredUsername)) {
                        errorCode = 1;
                        return null;
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }

            //username and password storing
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("enteredUsername", enteredUsername));
            nameValuePairs.add(new BasicNameValuePair("enteredPassword", enteredPassword));
            Log.d("Attempt Insert", "starting");
            json = jParser.makeHttpRequest(STORE_LOGIN_URL, "POST", nameValuePairs);


            //getting userid for entered username
            nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("userName", enteredUsername));
            Log.d("Request made", "starting");
            json = jParser.makeHttpRequest(GET_USER_ID, "POST", nameValuePairs);

            JSONArray userID = null;
            String userIDtoPost = null;
            try {
                JSONObject jsonObj = new JSONObject(json.toString());

                // Getting JSON Array node
                userID = jsonObj.getJSONArray("user");

                JSONObject c = userID.getJSONObject(0);
                userIDtoPost = c.getString("userID");
                // User.set_userid(Integer.parseInt(userIDtoPost)); //fixme


            } catch (JSONException e) {
                e.printStackTrace();
            }


            ArrayList<Integer> setOfUserConditions = new ArrayList<>();
            int i = 0;
            for(int value : dietsMap.values()){
                if(dietsCheckedItems[i]){
                    setOfUserConditions.add(value);
                }
                i ++;
            }
            i = 0;
            for(int value : intolerancesMap.values()){
                if(intolerancesCheckedItems[i]){
                    setOfUserConditions.add(value);
                }
                i ++;
            }
            i = 0;
            for(int value : allergiesMap.values()){
                if(allergiesCheckedItems[i]){
                    setOfUserConditions.add(value);
                }
                i ++;
            }
            dataToPost = new Gson().toJson(setOfUserConditions);


            System.out.println(dataToPost);
            nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("registrationList", dataToPost));
            nameValuePairs.add(new BasicNameValuePair("userID", userIDtoPost));
            Log.d("Attempt Insert", "starting");
            json = jParser.makeHttpRequest(STORE_PREFERENCES_URL, "POST", nameValuePairs);

            User.username = enteredUsername;
            User.set_userid(Integer.parseInt(userIDtoPost)); //fixme
            return null;
        }


        //update gui here as this uses runonuithread
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch (errorCode) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Registration Complete!:)",
                            Toast.LENGTH_SHORT).show();
                    Intent in = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(in);
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "USERNAME IS ALREADY TAKEN",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "USERNAME CAN ONLY CONTAIN LETTERS AND NUMBERS",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "MINIMUM PASSWORD LENGTH IS SIX",
                            Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    }



}



