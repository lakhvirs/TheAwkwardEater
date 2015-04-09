package lakhvir.theawkwardeater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class SubmitRecipesActivity extends ActionBarActivity {

    private final Context context = this;

    private EditText recipeName, recipeIngredients, recipeMethod;
    private TextView stepText, ingredientText;
    private Button submitRecipeButton, appendNewStep, appendNewInstruction, recipeAllergies, recipeIntolerances, recipeDiets, addImage, removeNewStep, removeNewIngredient;
    private ArrayList<String> stepByStepInstructions = new ArrayList<String>();
    private ArrayList<String> listOfIngredients = new ArrayList<String>();
    private JSONParser jParser = new JSONParser();

    private static final String SUBMIT_NEW_RECIPE_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/submitNewRecipe.php";
    private static final String GET_RECIPE_ID = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/getSubmittedRecipeID.php";
    private static final String SAVE_IMAGE = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/saveRecipeImage.php";
    private static final String COMPLETE_RECIPE_CONDITIONS = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/submitRecipeConditions.php";
    private static final String CLOUDINARY_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/getCloudinary.php";


    private int methodStepCounter = 0, ingredientCounter = 0;

    private HashMap<String, Integer> dietsMap = new HashMap<String,Integer>();
    private HashMap<String, Integer> intolerancesMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> allergiesMap = new HashMap<String, Integer>();

    //arrays to store the values the user has selected
    private boolean [] dietsCheckedItems;
    private boolean [] intolerancesCheckedItems;
    private boolean [] allergiesCheckedItems;
    private static final String POPULATE_CHECKBOXES_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/generateAllergyChecbox.php";
    String newRecipeConditions;

    Cloudinary cloudinary;
    File file;
    String newImageName;

    private void clearLastMethodStep(){
        stepText.setText("");
        for (int i = 0; i < stepByStepInstructions.size(); i ++) {
            stepText.setText(stepText.getText() + "\n" + (i+1) + ". " + stepByStepInstructions.get(i));
        }
    }

    private void clearLastRecipeIngredient(){
        ingredientText.setText("");
        for(int i = 0; i < listOfIngredients.size(); i++){
            ingredientText.setText(ingredientText.getText() + "\n" + (i+1) + ". " + listOfIngredients.get(i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_recipes);

        recipeAllergies = (Button) findViewById(R.id.newRecipeAllergies);
        recipeIntolerances = (Button) findViewById(R.id.newRecipeIntolerances);
        recipeDiets = (Button) findViewById(R.id.newRecipeDiets);

        appendNewInstruction = (Button) findViewById(R.id.addNewIngredient);
        removeNewIngredient = (Button) findViewById(R.id.removeNewIngredient);
        ingredientText = (TextView) findViewById(R.id.textViewIngredients);

        appendNewStep = (Button) findViewById(R.id.addNewStep);
        removeNewStep = (Button) findViewById(R.id.removeNewStep);
        stepText = (TextView) findViewById(R.id.textViewSteps);

        recipeName = (EditText) findViewById(R.id.submittedRecipeName);
        recipeIngredients = (EditText) findViewById(R.id.submittedRecipeIngredients);
        recipeMethod = (EditText) findViewById(R.id.submittedRecipeMethod);

        submitRecipeButton = (Button) findViewById(R.id.sendRecipe);
        addImage =(Button) findViewById(R.id.startCam);

        new GenerateCheckBox().execute();

        recipeAllergies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createDialog(allergiesMap, "Select intolerances", allergiesCheckedItems);
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }

        });
        recipeIntolerances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(intolerancesMap, "Select allergies", intolerancesCheckedItems);
            }

        });
        recipeDiets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog(dietsMap, "Select a diet", dietsCheckedItems);
            }
        });

        submitRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SubmitNewRecipe().execute();
            }

        });

        appendNewInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recipeIngredients.getText().length() != 0) {
                    ingredientCounter++;
                    ingredientText.setText(ingredientText.getText() + "\n" + ingredientCounter + ". " + recipeIngredients.getText().toString());
                    listOfIngredients.add(recipeIngredients.getText().toString());
                    recipeIngredients.setText("");
                }
            }
        });

        appendNewStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recipeMethod.getText().length() != 0) {
                    methodStepCounter++;
                    stepText.setText(stepText.getText() + "\n" + methodStepCounter + ". " + recipeMethod.getText().toString());
                    stepByStepInstructions.add(recipeMethod.getText().toString());
                    recipeMethod.setText("");
                }
            }
        });

        removeNewIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listOfIngredients !=null && !listOfIngredients.isEmpty()) {
                    listOfIngredients.remove(listOfIngredients.get(listOfIngredients.size() - 1));
                    ingredientCounter--;
                    clearLastRecipeIngredient();
                }
            }
        });

        removeNewStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stepByStepInstructions !=null && !stepByStepInstructions.isEmpty()) {
                    stepByStepInstructions.remove(stepByStepInstructions.get(stepByStepInstructions.size() - 1));
                    methodStepCounter--;
                    clearLastMethodStep();
                }
            }
        });

        /*Initialize Cloudinary*/
                /*Initialize Cloudinary*/

        new InitializeCloudinary().execute();


        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                Random randomGenerator = new Random();
                int x = randomGenerator.nextInt();
                newImageName = Integer.toString(x) + ".jpg";
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +File.separator + newImageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Check that request code matches ours:
        if (requestCode == 0)
        {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator + newImageName);
            // Bitmap bitmap = decodeSampledBitmapFromFile(file.getAbsolutePath(), 1000, 700);
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    { // BEST QUALITY MATCH

        //First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize, Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight)
        {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth)
        {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
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
                    } else if (c.getString("conditionType").equals("Allergy")) {
                        allergiesMap.put(c.getString("conditionName"), c.getInt("conditionID"));
                    } else if (c.getString("conditionType").equals("Intolerance")) {
                        intolerancesMap.put(c.getString("conditionName"), c.getInt("conditionID"));
                    }
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

    class SubmitNewRecipe extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Submitting recipe now...",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String userRecipeNameToPost = recipeName.getText().toString() ;

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy");
            String userSubmittedRecipeDate = sdf.format(c.getTime());

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("userIdToPost", Integer.toString(User.get_userid())));
            nameValuePairs.add(new BasicNameValuePair("userSubmittedRecipeDate", userSubmittedRecipeDate));
            nameValuePairs.add(new BasicNameValuePair("userRecipeNameToPost", userRecipeNameToPost));

            String allIngredients = "";
            int j = 0;
            for (String entry : listOfIngredients){
                allIngredients += entry;
                if (j < listOfIngredients.size() - 1) {
                    allIngredients += ";";
                }
                j ++;
            }

            nameValuePairs.add(new BasicNameValuePair("userRecipeIngredientsToPost", allIngredients));

            String completeMethod = "";
            int i = 0;
            for (String entry : stepByStepInstructions){
                completeMethod += entry;
                if (i < stepByStepInstructions.size() - 1) {
                    completeMethod += ";";
                }
                i ++;
            }

            nameValuePairs.add(new BasicNameValuePair("userRecipeMethodToPost", completeMethod));

            String json = jParser.makeHttpRequest(SUBMIT_NEW_RECIPE_URL, "POST", nameValuePairs);
            Log.d("a", json);

            nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("recipeName", userRecipeNameToPost));

            json = jParser.makeHttpRequest(GET_RECIPE_ID, "POST", nameValuePairs);
            JSONArray array = null;
            String recipeIdToPost = null;
            try {
                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                array = jsonObj.getJSONArray("recipe");

                JSONObject value = array.getJSONObject(0);
                recipeIdToPost = value.getString("recipeID");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("secure_url");
                nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", recipeIdToPost));
                nameValuePairs.add(new BasicNameValuePair("urlOfImage", url));
                json = jParser.makeHttpRequest(SAVE_IMAGE, "POST", nameValuePairs);
                Log.d("url", url);
                // Log.d("pid", publicId);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Integer> setOfRecipeConditions = new ArrayList<>();
            int x = 0;
            for(int value : dietsMap.values()){
                System.out.println(dietsMap.keySet().toString());
                if(dietsCheckedItems[x]){
                    setOfRecipeConditions.add(value);
                }
                x ++;
            }
            x = 0;
            for(int value : intolerancesMap.values()){
                System.out.println(intolerancesMap.keySet().toString());
                if(intolerancesCheckedItems[x]){
                    setOfRecipeConditions.add(value);
                }
                x ++;
            }
            x = 0;
            for(int value : allergiesMap.values()){
                System.out.println(allergiesMap.keySet().toString());
                if(allergiesCheckedItems[x]){
                    setOfRecipeConditions.add(value);
                }
                x ++;
            }
            newRecipeConditions = new Gson().toJson(setOfRecipeConditions);

            nameValuePairs = new ArrayList<NameValuePair>();
            //key value pair - parameter to pass to php, value entered by user
            nameValuePairs.add(new BasicNameValuePair("conditionsList", newRecipeConditions));
            nameValuePairs.add(new BasicNameValuePair("recipeID", recipeIdToPost));

            json = jParser.makeHttpRequest(COMPLETE_RECIPE_CONDITIONS, "POST", nameValuePairs);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    class InitializeCloudinary extends AsyncTask<String,String, String>{

        String cloudName;
        String apiKey;
        String apiSecret;

        @Override
        protected String doInBackground(String... params) {
            String json = jParser.getJSONFromUrl(CLOUDINARY_URL);
            try{
                JSONObject jsonObj = new JSONObject(json);
                cloudName =  jsonObj.getString("cloud_name");
                apiKey =  jsonObj.getString("api_key");
                apiSecret = jsonObj.getString("api_secret");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Map config = new HashMap();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            cloudinary = new Cloudinary(config);
        }
    }

}
