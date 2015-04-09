package lakhvir.theawkwardeater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class MainMenuActivity extends ActionBarActivity {

    private Button submitRecipes, searchRecipes, myProfile, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        submitRecipes = (Button) findViewById(R.id.submitRecipes);
        searchRecipes = (Button) findViewById(R.id.searchRecipes);
        myProfile = (Button) findViewById(R.id.myProfile);
        logout = (Button) findViewById(R.id.logout);

        submitRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), SubmitRecipesActivity.class);
                startActivity(in);
            }
        });
        searchRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), SearchRecipesActivity.class);
                startActivity(in);
            }
        });
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(), MyProfileActivity.class);
                startActivity(in);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("data",MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString("username","");
                prefEditor.putString("password","");
                prefEditor.commit();
                User.userID = 0;
                User.username = null;
                Intent in = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(in);
            }
        });
    }
}
