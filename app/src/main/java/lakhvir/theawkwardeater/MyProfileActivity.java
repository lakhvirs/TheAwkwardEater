package lakhvir.theawkwardeater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
public class MyProfileActivity extends ActionBarActivity {


    private Button editIntolerances, editDiets, editAllergies, changePassword, resetAccount, savePass;
    private EditText password1, password2;
    private TextView error;

    private final Context context = this;

    private HashMap<String, Integer> dietsMap = new HashMap<String,Integer>();
    private HashMap<String, Integer> intolerancesMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> allergiesMap = new HashMap<String, Integer>();

    private ArrayList<Integer> userConditions;

    private JSONParser jParser = new JSONParser();

    private boolean [] dietsCheckedItems;
    private boolean [] intolerancesCheckedItems;
    private boolean [] allergiesCheckedItems;


    private String password;
    private  AlertDialog.Builder builder;
    private AlertDialog alert;
    private  View layout ;


    private static final String POPULATE_CHECKBOXES_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/generateAllergyChecbox.php";
    private static final String GET_USER_CONDITIONS_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/getUserConditions.php";
    private static final String UPDATE_USER_PREFERENCES_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/updateUserPreferences.php";
    private static final String UPDATE_USER_PASSWORD_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/updateUserPassword.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        editIntolerances = (Button) findViewById(R.id.editIntolerances);
        editDiets = (Button) findViewById(R.id.editDiets);
        editAllergies = (Button) findViewById(R.id.editAllergies);
        changePassword = (Button) findViewById(R.id.changePassword);
        resetAccount = (Button) findViewById(R.id.resetAccount);
        SharedPreferences shared = getSharedPreferences("data", MODE_PRIVATE);
        password = shared.getString("password", "");
        new GenerateCheckBox().execute();

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEditPasswordAlert();
            }
        });
        editIntolerances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(intolerancesMap, "Select/Deselect Allergies", intolerancesCheckedItems);
            }
        });
        editDiets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(dietsMap, "Select/Deselect diet(s)", dietsCheckedItems);
            }
        });
        editAllergies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(allergiesMap, "Select/Deselect Intolerances", allergiesCheckedItems);
            }
        });
    }
    private void createDialog(HashMap<String,Integer> array, String title, final boolean [] checkedItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AsyncTask<String, String, String> UpdateUserPreferences = new AsyncTask<String, String, String>() {
                            @Override
                            protected String doInBackground(String... params) {
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
                                 String dataToPost = new Gson().toJson(setOfUserConditions);
                               ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                                //key value pair - parameter to pass to php, value entered by user
                                nameValuePairs.add(new BasicNameValuePair("registrationList", dataToPost));
                                nameValuePairs.add(new BasicNameValuePair("userID", Integer.toString(User.get_userid())));
                                Log.d("Attempt Insert", "starting");
                                String json = jParser.makeHttpRequest(UPDATE_USER_PREFERENCES_URL, "POST", nameValuePairs);
                                return null;
                            }
                            @Override
                            protected void onPostExecute(String result) {

                            }
                        };
                        UpdateUserPreferences.execute();
                    }
                });

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

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createEditPasswordAlert(){
        LayoutInflater inflater = LayoutInflater.from(this);
        layout = inflater.inflate(R.layout.password_change_layout, null);
        password1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);
        password2 = (EditText) layout.findViewById(R.id.EditText_Pwd2);
        savePass = (Button) layout.findViewById(R.id.savePass);
        error = (TextView) layout.findViewById(R.id.TextView_PwdProblem);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");
        builder.setView(layout);
        builder.setNegativeButton("Cancel", null);
        alert = builder.create();
        alert.show();
        savePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChangePassword().execute();

            }
        });
    }


    class GenerateCheckBox extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
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
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userIdToPost", Integer.toString(User.get_userid())));
                json = jParser.makeHttpRequest(GET_USER_CONDITIONS_URL, "POST", nameValuePairs);
                JSONArray recipes = null;

                userConditions = new ArrayList<Integer>();
                JSONObject obj = new JSONObject(json);
                recipes = obj.getJSONArray("userconditions");
                Integer conditionID;
                Log.d("jarray", json);
                for (int i = 0; i < recipes.length(); i++) {
                    JSONObject c = recipes.getJSONObject(i);
                    conditionID = c.getInt("conditionID");
                    userConditions.add(conditionID);
                }

                for (Integer id : userConditions) {
                    int i = 0;
                    for (Integer dietsId : dietsMap.values()) {
                        if (id.equals(dietsId)) {
                            dietsCheckedItems[i] = true;
                        }
                        i ++;
                    }
                    i = 0;
                    for (Integer allergiesId : allergiesMap.values()) {
                        if (id.equals(allergiesId)) {
                            allergiesCheckedItems[i] = true;
                        }
                        i ++;
                    }
                    i = 0;
                    for (Integer intolerancesId : intolerancesMap.values()) {
                        if (id.equals(intolerancesId)) {
                            intolerancesCheckedItems[i] = true;
                        }
                        i ++;
                    }
                }

            }
            catch (JSONException e) {
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

    class ChangePassword extends AsyncTask<String, String, String>{

        int errorCode = 0;

        String enteredNewPassword;
        String enteredOldPassword;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            enteredNewPassword = password2.getText().toString().trim();
            enteredOldPassword = password1.getText().toString().trim();

            if(!enteredOldPassword.equals(password)){
                errorCode=2;
                return null;
            }

            if(enteredNewPassword.length() < 6){
                errorCode = 1;
                return null;
            }

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("newPassword", enteredNewPassword));
            nameValuePairs.add(new BasicNameValuePair("oldPassword", enteredOldPassword));
            nameValuePairs.add(new BasicNameValuePair("currentUserID", Integer.toString(User.get_userid())));
            String json = jParser.makeHttpRequest(UPDATE_USER_PASSWORD_URL, "POST", nameValuePairs);
             return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch (errorCode) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                    prefEditor.putString("password", enteredNewPassword);
                    prefEditor.commit();
                    alert.dismiss();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "MINIMUM PASSWORD LENGTH IS SIX",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "CURRENT PASSWORD INCORRECT",
                            Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    }

}