package lakhvir.theawkwardeater;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchRecipesActivity extends ActionBarActivity {

    private EditText editTextSearchString;
    private  ListView resultsList;
    JSONParser jParser = new JSONParser();
    private static final String RECIPE_SEARCH = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/newRecipes.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);
        resultsList = (ListView)findViewById(android.R.id.list);
        editTextSearchString = (EditText) findViewById(R.id.editText);
        editTextSearchString.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editTextSearchString.getText().length() != 0) {
                    try {
                        new AttemptSearch().execute();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                } else{
                    resultsList.setAdapter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    class AttemptSearch extends AsyncTask<String, String, String> {

        InputStream inputStream;
        Drawable[] images;
        private ArrayList<HashMap<String, String>> retrievedRecipesList;
        int count =0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultsList.setAdapter(null);
            Toast.makeText(getApplicationContext(), "Searching:", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            retrievedRecipesList = new ArrayList<HashMap<String, String>>();
            String enteredRecipe = editTextSearchString.getText().toString();

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeToSearch", enteredRecipe));
            nameValuePairs.add(new BasicNameValuePair("currentUserID", Integer.toString(User.get_userid())));

            String json = jParser.makeHttpRequest(RECIPE_SEARCH, "POST", nameValuePairs);
            Log.d("jsonserarch",json.toString());
            JSONArray recipes = null;

            try {
                JSONObject jsonObj = new JSONObject(json);
                 recipes = jsonObj.getJSONArray("recipes");
                Log.d("recipeLength", Integer.toString(recipes.length()));
                Log.d("jarray", json);

                String name;
                String recipeID;
                String ingredients;
                String imageUrl;
                String recipeDescription;
                String recipeMethod;
                String averageRating;
                List<String> items;
                images = new Drawable[recipes.length()];

                // looping through All recipes
                for (int i = 0; i < recipes.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject c = recipes.getJSONObject(i);
                    recipeID = c.getString("recipeID");
                    name = c.getString("recipeName");
                    ingredients = c.getString("ingredients");
                    recipeMethod = c.getString("method");
                    imageUrl = c.getString("imageUrl");
                    recipeDescription = c.getString("recipeDescription");
                    averageRating = c.getString("averageRating");
                    items = Arrays.asList(ingredients.split(";"));

                    StringBuilder sb = new StringBuilder();
                    for (String elem : items) {
                        sb.append(elem + " \n");
                    }
                    map.put("recipeID", recipeID);
                    map.put("recipeName", name);
                    map.put("ingredients", ingredients);
                    map.put("formIngredients", sb.toString());
                    map.put("recipeMethod", recipeMethod);
                    map.put("imageUrl", imageUrl);
                    map.put("recipeDescription", recipeDescription);
                    map.put("averageRating", averageRating);
                    retrievedRecipesList.add(map);

                    URL myUrl = new URL(imageUrl);
                    inputStream = (InputStream) myUrl.getContent();
                    images[i] = Drawable.createFromStream(inputStream, null);
                    count++;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //update gui here as this uses runonuithread
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("count", Integer.toString(count));
            Log.d("listSize", Integer.toString(retrievedRecipesList.size()));

            for(int i=0; i<retrievedRecipesList.size(); i++){
                System.out.println(retrievedRecipesList.get(i).get("recipeName"));
                System.out.println(retrievedRecipesList.get(i).get("recipeID"));
                System.out.println( retrievedRecipesList.get(i).get("formIngredients"));
                System.out.println( retrievedRecipesList.get(i).get("recipeMethod"));
                System.out.println( retrievedRecipesList.get(i).get("recipeDescription"));
            }

            if (editTextSearchString.getText().length() != 0) {
                float[] ratings = new float[retrievedRecipesList.size()];
                for (int i = 0; i < retrievedRecipesList.size(); i ++) {
                    ratings[i] = Float.parseFloat(retrievedRecipesList.get(i).get("averageRating"));
                }

                ListAdapter adapter = new SearchItemAdapter(
                        SearchRecipesActivity.this, retrievedRecipesList,
                        R.layout.list_item, new String[]{"recipeName", "recipeDescription"},
                        new int[]{R.id.recipeName, R.id.recipeDescription}, ratings, images);
                resultsList.setAdapter(adapter);

                resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent in = new Intent(getApplicationContext(), RecipeMainPage.class);
                        in.putExtra("recipeName", retrievedRecipesList.get(position).get("recipeName"));
                        in.putExtra("recipeID", retrievedRecipesList.get(position).get("recipeID"));
                        in.putExtra("ingredients", retrievedRecipesList.get(position).get("formIngredients"));
                        in.putExtra("recipeMethod", retrievedRecipesList.get(position).get("recipeMethod"));
                        in.putExtra("recipeDescription", retrievedRecipesList.get(position).get("recipeDescription"));
                        startActivity(in);
                    }
                });
         }
        }
    }

    class SearchItemAdapter extends SimpleAdapter{

        float[] ratings;
        Drawable[] images;

        public SearchItemAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, float[] ratings, Drawable[] images) {
            super(context, data, resource, from, to);
            this.ratings = ratings;
            this.images = images;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((RatingBar) v.findViewById(R.id.rating)).setRating(ratings[position]);
            ((ImageView) v.findViewById(R.id.recipeThumbnail)).setImageDrawable(images[position]);
            return v;
        }
    }
}

