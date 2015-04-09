package lakhvir.theawkwardeater;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AllReviewsActivity extends ActionBarActivity {

    String recipeID, recipeName;

    private static final String GET_REVIEWS_RATINGS = "http://www.nothingdata.co.uk/lakhvir/TheAwkwardEater/countStars.php";
    private JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        recipeID = getIntent().getStringExtra("recipeID");
        recipeName = getIntent().getStringExtra("recipeName");
        setTitle("All reviews for " + recipeName);

        ArrayList<HashMap<String, String>> arl = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("data");

        float[] ratings = new float[arl.size()];
        for (int i = 0; i < arl.size(); i ++) {
            ratings[i] = Float.parseFloat(arl.get(i).get("reviewValue"));
        }

        ListView lv = (ListView) findViewById(R.id.reviewList);
        lv.setAdapter(new MyAdapter(
                AllReviewsActivity.this, arl,
                R.layout.review_list_item, new String[]{"reviewUsername","reviewDate", "reviewText"},
                new int[]{R.id.reviewName, R.id.reviewDate, R.id.reviewText}, ratings));
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        new GetCountOfRatings().execute();
    }

    private void setRatingsFull(int[] ratingCounts) {
        //Find ratings full layout
        RelativeLayout layoutRatingsFull = (RelativeLayout) findViewById(R.id.ratingsFull);

        //Find each bar in the layout
        View[] bars = new View[5];
        bars[0] = layoutRatingsFull.findViewById(R.id.bar5stars);
        bars[1] = layoutRatingsFull.findViewById(R.id.bar4stars);
        bars[2] = layoutRatingsFull.findViewById(R.id.bar3stars);
        bars[3] = layoutRatingsFull.findViewById(R.id.bar2stars);
        bars[4] = layoutRatingsFull.findViewById(R.id.bar1stars);

        //Find each counter text view in the layout
        TextView[] counterTextViews = new TextView[5];
        counterTextViews[0] = (TextView) layoutRatingsFull.findViewById(R.id.textView5starsCounter);
        counterTextViews[1] = (TextView) layoutRatingsFull.findViewById(R.id.textView4starsCounter);
        counterTextViews[2] = (TextView) layoutRatingsFull.findViewById(R.id.textView3starsCounter);
        counterTextViews[3] = (TextView) layoutRatingsFull.findViewById(R.id.textView2starsCounter);
        counterTextViews[4] = (TextView) layoutRatingsFull.findViewById(R.id.textView1starsCounter);

        //Get the maximum rating count
        int maxRatingCount = 0;
        for (int i = 0; i < 5; i ++) {
            if (ratingCounts[i] > maxRatingCount) {
                maxRatingCount = ratingCounts[i];
            }
        }

        //Get the max width of the bars
        int maxWidth = bars[0].getWidth();

        //Set the widths of the bars and the counters
        for (int i = 0; i < 5; i ++) {
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) bars[i].getLayoutParams();
            params.width = (int) (maxWidth * ((float) ratingCounts[i]/maxRatingCount));
            bars[i].setLayoutParams(params);
            counterTextViews[i].setText(String.valueOf(ratingCounts[i]));
        }
    }


    class MyAdapter extends SimpleAdapter{

        float[] ratings;

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, float[] ratings) {
            super(context, data, resource, from, to);
            this.ratings = ratings;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((RatingBar) v.findViewById(R.id.reviewRating)).setRating(ratings[position]);
            return v;
        }
    }


    class GetCountOfRatings extends AsyncTask<String, String, String> {

        private ArrayList<HashMap<String, String>> ratingValues;
        int arrayOfValues [] = new int[5];


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {


            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("recipeIdToPost", recipeID));
            String json = jParser.makeHttpRequest(GET_REVIEWS_RATINGS, "POST", nameValuePairs);
            Log.d("Attempt Get Recipe", json.toString());
            String ratingValueOne;
            String ratingValueTwo;
            String ratingValueThree;
            String ratingValueFour;
            String ratingValueFive;
            try {
                JSONObject response = new JSONObject(json);
                JSONObject Data = response.getJSONObject("CountAll");

                ratingValueOne = Data.getString("1");
                ratingValueTwo = Data.getString("2");
                ratingValueThree = Data.getString("3");
                ratingValueFour = Data.getString("4");
                ratingValueFive = Data.getString("5");

                arrayOfValues[0] = (Integer.parseInt(ratingValueOne.toString()));
                arrayOfValues[1] = (Integer.parseInt(ratingValueTwo.toString()));
                arrayOfValues[2] = (Integer.parseInt(ratingValueThree.toString()));
                arrayOfValues[3] = (Integer.parseInt(ratingValueFour.toString()));
                arrayOfValues[4] = (Integer.parseInt(ratingValueFive.toString()));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            setRatingsFull(arrayOfValues);
        }
    }

}
