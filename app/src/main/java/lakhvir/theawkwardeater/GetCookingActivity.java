package lakhvir.theawkwardeater;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class GetCookingActivity extends ActionBarActivity {

    List<String> items = null;
    String recipeMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_cooking);

        recipeMethod = getIntent().getStringExtra("recipeMethod");

            items = Arrays.asList(recipeMethod.split(";"));

            StringBuilder sb = new StringBuilder();
            for(String elem : items) {
                sb.append(elem + " ");
            }
        ((TextView) findViewById(R.id.showRecipeMethod)).setText(sb.toString());
    }

}
