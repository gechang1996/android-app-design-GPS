package edu.msu.gechang1.cse476map;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewUserActivity extends AppCompatActivity {

    String username;
    String password;
    String password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

//        Log.i("username",username);
    }
    public void onClickBack(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickCreate(View view){
        EditText ed1=(EditText)findViewById(R.id.username);
        username=ed1.getText().toString();
        EditText ed2=(EditText)findViewById(R.id.password);
        password=ed2.getText().toString();
        EditText ed3=(EditText)findViewById(R.id.password2);
        password2=ed3.getText().toString();

        if (username.isEmpty()||password.isEmpty()||password2.isEmpty()){
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
        else{
            if(password2.equals(password)){

                save(username,password);

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

            }
            else{
                // Instantiate a dialog box builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(view.getContext());

                // Parameterize the builder
                builder.setTitle(R.string.fail);
                builder.setMessage(R.string.passNotSame);
                builder.setPositiveButton(R.string.ok, null);

                // Create the dialog box and show it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }
    /**
     * Actually save the hatting
     * @param name name to save it under
     */
    private void save(final String name, final String pass) {


        new Thread(new Runnable() {

            @Override
            public void run() {
                Cloud cloud = new Cloud();
                final boolean ok = cloud.saveToCloud(name, pass);
                if(!ok) {


                }

            }

        }).start();


    }

}
