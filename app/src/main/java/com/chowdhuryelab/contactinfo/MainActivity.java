package com.chowdhuryelab.contactinfo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private EditText userName, userPhone1, userPhone2,userEmail;
    private ImageView userPhoto;
    private Button cln, save;

    private String name, email, phn1,phn2;
    private Bitmap thumbnail;

    static final int CAMERA_CODE = 1;
    static final int GALLERY_CODE = 0;

    final int bmpHeight = 160;
    final int bmpWidth = 160;

    private CharSequence[] Items = {"Camera", "Gallery", "Remove"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.setTitle("Contact Form");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(getLayoutInflater().inflate(R.layout.action_bar_home, null),
                new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                )
        );

        SharedPreferences preferences = getSharedPreferences("Quiz2",MODE_PRIVATE);
        userName=(EditText)findViewById(R.id.username);
        userPhone1=(EditText)findViewById(R.id.userphone);
        userPhone2=(EditText)findViewById(R.id.userphone2);
        userEmail=(EditText)findViewById(R.id.useremail);
        userPhoto=(ImageView) findViewById(R.id.userphoto);

        save = (Button) findViewById(R.id.save);
        cln = (Button) findViewById(R.id.exit);

        // If value for key not exist then show blank
        userName.setText(preferences.getString("username", ""));
        userEmail.setText(preferences.getString("useremail", ""));
        userPhone1.setText(preferences.getString("userphone1", ""));
        userPhone2.setText(preferences.getString("userphone2", ""));


        String img_str=preferences.getString("userphoto", "");

        // Chech if photos string is avail able or not
        if (!img_str.equals("")){
            //decode string to image
            String base=img_str;
            byte[] imageAsBytes = Base64.decode(base.getBytes(), Base64.DEFAULT);
            userPhoto.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length) );
        }

    }
    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public void setProfile(View view) {

        name = userName.getText().toString();
        email = userEmail.getText().toString().trim();
        phn1 = userPhone1.getText().toString().trim();
        phn2 = userPhone2.getText().toString().trim();

        String error = "";

        if(name.length()<2){
            userName.setError("Error");
            error = error +"\nName";
        }

        if (!isValidMail(email))
        {
            userEmail.setError("email");
            error = error +"\nEmail";
        }

        if(!isValidMobile(phn1) || phn1.length()<11){
            userPhone1.setError("Error");
            error = error +"\nPhone(Home)";
        }
        if(!phn2.isEmpty()){
            if(!isValidMobile(phn2) || phn2.length()<11) {
                userPhone2.setError("Error");
                error = error + "\nPhone(Office)";
            }
        }

        if(error.isEmpty()){
            showDialog("Do you want to save the event info?","info","Yes","No");
        }else{
            showDialog(error,"Please check invalid fields","","Back");
        }

    }

    public void ImageOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select Any Option_");
        builder.setItems(Items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Items[which].equals("Camera")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_CODE);
                }else if (Items[which].equals("Gallery")){
                    Log.i("GalleryCode",""+GALLERY_CODE);
                    Intent GalleryIntent = null;
                    GalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    GalleryIntent.setType("image/*");
                    GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(GalleryIntent,GALLERY_CODE);
                }
                else{
                    userPhoto.setImageResource(R.drawable.profileimg);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            switch(requestCode){
                case 1:
                    Log.i("CameraCode",""+CAMERA_CODE);
                    Bundle bundle = data.getExtras();
                    Bitmap bmp = (Bitmap) bundle.get("data");
                    //Bitmap resized = Bitmap.createScaledBitmap(bmp, bmpWidth,bmpHeight, true);
                    //userPhoto.setImageBitmap(resized);
                    userPhoto.setImageBitmap(bmp);

                case 0:
                    Log.i("GalleryCode",""+requestCode);
                    Uri ImageURI = data.getData();
                    userPhoto.setImageURI(ImageURI);
            }


        }
    }


    public void saveProfilePhoto() {

        ImageView ivphoto = (ImageView)findViewById(R.id.userphoto);
        //Convert image to string
        ivphoto.buildDrawingCache();
        Bitmap bitmap = ivphoto.getDrawingCache();
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] image=stream.toByteArray();
        System.out.println("byte array:"+image);

        // convert byteArray to string using base64 encoding
        String img_str = Base64.encodeToString(image, 0);
        System.out.println("Base64 string:"+img_str);

        //save in sharedpreferences
        SharedPreferences preferences = getSharedPreferences("Quiz2",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userphoto",img_str);

        save.setText("Saved");
        editor.commit();
    }

    private void showDialog(String message, String title, String key1, String key2){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(false)
                .setPositiveButton(key1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences preferences = getSharedPreferences("Quiz2",MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putString("username",name);
                        editor.putString("useremail",email);
                        editor.putString("userphone1",phn1);
                        editor.putString("userphone2",phn2);
                        editor.commit();

                        saveProfilePhoto();
                        Toast toast=Toast. makeText(getApplicationContext(),"Contact Info Saved Successfully",Toast. LENGTH_SHORT);
                        toast.show();
                        dialog.cancel();
                    }

                })
                .setNegativeButton(key2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    public void pressExit(View view) {
        finish();
    }

}