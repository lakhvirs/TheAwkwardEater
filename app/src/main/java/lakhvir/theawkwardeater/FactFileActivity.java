package lakhvir.theawkwardeater;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class FactFileActivity extends ActionBarActivity {

    private Button searchIntoleranceFactFiles, searchAllergyFactFiles, searchDietFactFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact_file);
        searchIntoleranceFactFiles = (Button) findViewById(R.id.factFileIntolerances);
        searchAllergyFactFiles = (Button) findViewById(R.id.factFileAllergies);
        searchDietFactFiles = (Button) findViewById(R.id.factFileDiets);


        searchIntoleranceFactFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), IntoleranceFactFile.class);
                startActivity(in);
            }
        });
        searchAllergyFactFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), AllergyFactFile.class);
                startActivity(in);
            }
        });
        searchDietFactFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), DietFactFile.class);
                startActivity(in);
            }
        });
    }


}
