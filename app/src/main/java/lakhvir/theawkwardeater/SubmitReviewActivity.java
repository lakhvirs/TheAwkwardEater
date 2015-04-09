package lakhvir.theawkwardeater;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class SubmitReviewActivity extends ActionBarActivity {

    private Button sendReview;
    String postedRecipeName;
    private EditText reviewText;
    private RatingBar ratingBar;
    private float userRating;
    private JSONParser jParser = new JSONParser();
    private static final String SUBMIT_NEW_REVIEW_URL = "http://195.26.90.15/lakhvir/TheAwkwardEater/submitNewReview.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_review);
        postedRecipeName = getIntent().getStringExtra("name");
        ((TextView) findViewById(R.id.reviewDetails)).setText(postedRecipeName);
        reviewText = (EditText) findViewById(R.id.reviewText);
        ratingBar = (RatingBar) findViewById(R.id.recipeRating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRating = ratingBar.getRating();
            }
        });

    }

    class SendReviewData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Submitting review now...",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {

            String reviewBody = reviewText.getText().toString();
            String ratingValue = String.valueOf(userRating);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy");
            String userSubmittedReviewDate = sdf.format(c.getTime());

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", getIntent().getStringExtra("id")));
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
}