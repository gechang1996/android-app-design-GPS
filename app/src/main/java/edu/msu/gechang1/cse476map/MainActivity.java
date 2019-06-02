package edu.msu.gechang1.cse476map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private CheckBox saveLoginCheckBox;
    private EditText editTextUsername,editTextPassword;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private String username;
    private String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextUsername = (EditText)findViewById(R.id.username);
        editTextPassword = (EditText)findViewById(R.id.password);
        saveLoginCheckBox = (CheckBox)findViewById(R.id.remember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            editTextUsername.setText(loginPreferences.getString("username", ""));
            editTextPassword.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }
    }

    public void onClickAdd(View view){
        Intent intent = new Intent(MainActivity.this, NewUserActivity.class);
        startActivity(intent);
    }
    public void onHelpShow(View view)
    {
        // Instantiate a dialog box builder
        AlertDialog.Builder builder =
                new AlertDialog.Builder(view.getContext());

        // Parameterize the builder
        builder.setTitle(R.string.GameDescription);
        builder.setMessage(R.string.Detail);
        builder.setPositiveButton(R.string.ok, null);

        // Create the dialog box and show it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onClickStart(final View view){
        EditText ed1=(EditText)findViewById(R.id.username);
        username=ed1.getText().toString();
        EditText ed2=(EditText)findViewById(R.id.password);
        password=ed2.getText().toString();
        if (username.isEmpty()||password.isEmpty()){
            // Instantiate a dialog box builder
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(view.getContext());

            // Parameterize the builder
            builder.setTitle(R.string.fail);
            builder.setMessage(R.string.empty);
            builder.setPositiveButton(R.string.ok, null);

            // Create the dialog box and show it
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Cloud cloud = new Cloud();
                    InputStream stream = cloud.openFromCloud(username,password);
                    // Test for an error
                    boolean fail = stream == null;
                    if(!fail)
                    {
                        try{
                            XmlPullParser xml = Xml.newPullParser();
                            xml.setInput(stream, "UTF-8");

                            xml.nextTag();      // Advance to first tag
                            xml.require(XmlPullParser.START_TAG, null, "starCollector");
                            String status = xml.getAttributeValue(null, "status");
                            if(status.equals("yes")) {
                                fail = false;
//                                Intent intent = new Intent(this, PlaceShip1.class);
//                                startActivity(intent);

                            } else {
                                fail = true;
                            }


                        }catch(IOException ex) {
                            fail = true;
                        } catch(XmlPullParserException ex) {
                            fail = true;
                        } finally {
                            try {
                                stream.close();
                            } catch(IOException ex) {
                            }
                        }
                    }
                    final boolean fail1=fail;
                    view.post(new Runnable() {

                        @Override
                        public void run() {

                            if(fail1) {
                                Toast.makeText(view.getContext(),
                                        R.string.WrongPass,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                // Success!
                                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                                intent.putExtra("username",username);

                                if (saveLoginCheckBox.isChecked()) {
                                    loginPrefsEditor.putBoolean("saveLogin", true);
                                    loginPrefsEditor.putString("username", username);
                                    loginPrefsEditor.putString("password", password);
                                    loginPrefsEditor.commit();
                                } else {
                                    loginPrefsEditor.clear();
                                    loginPrefsEditor.commit();
                                }
                                intent.putExtra("thisUser",username);
                                startActivity(intent);
                            }

                        }

                    });
                }


            }
            ).start();



        }
    }


}
