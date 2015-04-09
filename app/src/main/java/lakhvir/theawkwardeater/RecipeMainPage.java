package lakhvir.theawkwardeater;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class RecipeMainPage extends ActionBarActivity {

    private static final String GET_REVIEWS_RATINGS = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/reviewRating.php";
    private static final String GET_IMAGE = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/getImage.php";
    private JSONParser jParser = new JSONParser();
    String recipeName, recipeID, recipeIngredients, recipeMethod, recipeDescription;
    private Button submitReview,getCooking, moreReviews;
    private RatingBar ratingBar;
    private ImageView recipeImage;
    private ArrayList<RelativeLayout> reviewLayouts = new ArrayList<RelativeLayout>();




    private static final String SUBMIT_NEW_REVIEW_URL = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/submitNewReview.php";
    private  AlertDialog.Builder builder;
    private AlertDialog alert;
    private  View layout ;

    private EditText reviewText;
    private TextView reviewDetails;
    private RatingBar ratingBars;
    private float userRating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_main_page);


        moreReviews = (Button) findViewById(R.id.moreReviews);

        recipeID = getIntent().getStringExtra("recipeID");
        recipeMethod = getIntent().getStringExtra("recipeMethod");

        recipeName = getIntent().getStringExtra("recipeName");
        ((TextView) findViewById(R.id.showRecipeName)).setText(recipeName);

        recipeIngredients = getIntent().getStringExtra("ingredients");
        ((TextView) findViewById(R.id.showRecipeIngredients)).setText(recipeIngredients);
        setTitle(recipeName);

        recipeImage = (ImageView) findViewById(R.id.showRecipeImage);

        recipeDescription = getIntent().getStringExtra("recipeDescription");
        ((TextView) findViewById(R.id.showrecipeDescription)).setText(recipeDescription);


        reviewLayouts.add((RelativeLayout) findViewById(R.id.review1));
        reviewLayouts.add((RelativeLayout) findViewById(R.id.review2));
        reviewLayouts.add((RelativeLayout) findViewById(R.id.review3));

        submitReview = (Button) findViewById(R.id.submitReview);
        getCooking = (Button) findViewById(R.id.getCooking);

        ratingBar = (RatingBar) findViewById(R.id.recipeAverageRating);


        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitNewReviewDialog();

            }
        });

        getCooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), ScreenSlideActivity.class);
                in.putExtra("recipeMethod", getIntent().getStringExtra("recipeMethod"));
                startActivity(in);
            }
        });
        new GetRatingsAndImage().execute();

    }

    private void SubmitNewReviewDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        layout = inflater.inflate(R.layout.activity_submit_review, null);
        reviewText = (EditText) layout.findViewById(R.id.reviewText);
        reviewDetails = (TextView) layout.findViewById(R.id.reviewDetails);
        ((TextView) layout.findViewById(R.id.reviewDetails)).setText(getIntent().getStringExtra("recipeName"));
        ratingBars = (RatingBar) layout.findViewById(R.id.recipeRating);
        ratingBars.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRating = ratingBar.getRating();
            }
        });
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Submit a review");
        builder.setView(layout);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SendReviewData().execute();
                finish();
                startActivity(getIntent());
            }
        });
        alert = builder.create();
        alert.show();

    }

    class SendReviewData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Submitting review now...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {

            String reviewBody = reviewText.getText().toString();
            String ratingValue = String.valueOf(userRating);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy");
            String userSubmittedReviewDate = sdf.format(c.getTime());

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", getIntent().getStringExtra("recipeID")));
            nameValuePairs.add(new BasicNameValuePair("userIdToPost", Integer.toString(User.get_userid())));
            nameValuePairs.add(new BasicNameValuePair("userSubmittedReviewDate", userSubmittedReviewDate));
            nameValuePairs.add(new BasicNameValuePair("userReviewText", reviewBody));
            nameValuePairs.add(new BasicNameValuePair("userReviewRating", ratingValue));

            String json = jParser.makeHttpRequest(SUBMIT_NEW_REVIEW_URL, "POST", nameValuePairs);
            Log.d("Attempt Submit Review", json.toString());

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }

    }

    class GetRatingsAndImage extends AsyncTask<String, String, String> {

        private ArrayList<HashMap<String, String>> retrievedReviews;
        String url;
        InputStream inputStream;
        Drawable drawable;
        JSONArray reviews = new JSONArray();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            retrievedReviews = new ArrayList<HashMap<String, String>>();

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", recipeID));
            String json = jParser.makeHttpRequest(GET_REVIEWS_RATINGS, "POST", nameValuePairs);
            Log.d("Attempt Get Recipe", json.toString());

            String review;
            String value;
            String reviewer;
            String reviewDate;
            String averageRating;
            try {
                JSONObject jsonObj = new JSONObject(json);
                reviews = jsonObj.getJSONArray("ratings");

                for (int i = 0; i < reviews.length(); i++) {

                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject c = reviews.getJSONObject(i);
                    review = c.getString("review");
                    value = c.getString("rating");
                    reviewer = c.getString("username");
                    reviewDate = c.getString("reviewDate");
                    averageRating = c.getString("averageRating");
                    map.put("reviewText", review);
                    map.put("reviewValue", value);
                    map.put("reviewUsername", reviewer);
                    map.put("reviewDate", reviewDate);
                    map.put("averageRating", averageRating);

                    retrievedReviews.add(map);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



            nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", recipeID));
            json = jParser.makeHttpRequest(GET_IMAGE, "POST", nameValuePairs);
            Log.d("json2", json.toString());
            JSONArray urls;


            try {
                JSONObject a = new JSONObject(json);
                urls = a.getJSONArray("imageurl");

                JSONObject c = urls.getJSONObject(0);
                url = c.getString("imageUrl");
                Log.d("path", url.toString());

                URL myUrl = new URL(url);
                inputStream = (InputStream) myUrl.getContent();
                drawable = Drawable.createFromStream(inputStream, null);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            recipeImage.setImageDrawable(drawable);

            if (reviews.length() > 0) {
                moreReviews.setVisibility(View.VISIBLE);
                ratingBar.setVisibility(View.VISIBLE);
                ratingBar.setRating(Float.parseFloat(retrievedReviews.get(0).get("averageRating")));
                //setreviewratingnexttobar
                try {
                            if(retrievedReviews.size() == 1) {
                                for (int i = 0; i < 1; i++) {
                                    reviewLayouts.get(i).setVisibility(View.VISIBLE);
                                    ((RatingBar) reviewLayouts.get(i).findViewById(R.id.reviewRating)).setRating(Float.parseFloat(retrievedReviews.get(i).get("reviewValue")));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewText)).setText(retrievedReviews.get(i).get("reviewText"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewDate)).setText(retrievedReviews.get(i).get("reviewDate"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewName)).setText(retrievedReviews.get(i).get("reviewUsername"));
                                }
                            }
                            else if(retrievedReviews.size() == 2) {
                                for (int i = 0; i < 2; i++) {
                                    reviewLayouts.get(i).setVisibility(View.VISIBLE);
                                    ((RatingBar) reviewLayouts.get(i).findViewById(R.id.reviewRating)).setRating(Float.parseFloat(retrievedReviews.get(i).get("reviewValue")));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewText)).setText(retrievedReviews.get(i).get("reviewText"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewDate)).setText(retrievedReviews.get(i).get("reviewDate"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewName)).setText(retrievedReviews.get(i).get("reviewUsername"));
                                }
                            }
                             else {
                                for (int i = 0; i < reviewLayouts.size(); i++) {
                                    reviewLayouts.get(i).setVisibility(View.VISIBLE);
                                    ((RatingBar) reviewLayouts.get(i).findViewById(R.id.reviewRating)).setRating(Float.parseFloat(retrievedReviews.get(i).get("reviewValue")));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewText)).setText(retrievedReviews.get(i).get("reviewText"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewDate)).setText(retrievedReviews.get(i).get("reviewDate"));
                                    ((TextView) reviewLayouts.get(i).findViewById(R.id.reviewName)).setText(retrievedReviews.get(i).get("reviewUsername"));
                                }
                            }

                } catch(NullPointerException e) {
                    e.printStackTrace();
                }

                moreReviews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent in = new Intent(getApplicationContext(), AllReviewsActivity.class);
                        in.putExtra("data", retrievedReviews);
                        in.putExtra("recipeID", recipeID);
                        in.putExtra("recipeName", recipeName);
                        startActivity(in);
                    }
                });
            }
        }

    }
}
