package com.kayasanli.blogapp_java;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kayasanli.blogapp_java.databinding.ActivityMain2Binding;
import com.kayasanli.blogapp_java.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.Permission;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;
    ActivityResultLauncher<Intent > activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        db = this.openOrCreateDatabase("Blogs",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.equals("new")){
            //new blog
            binding.topicText.setText("");
            binding.nameText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView2.setImageResource(R.drawable.select);
        }else{
            int blogId = intent.getIntExtra("blogId",0);
            binding.button.setVisibility(View.INVISIBLE);
            binding.topicText.setFocusableInTouchMode(false);
            binding.nameText.setFocusableInTouchMode(false);
            binding.yearText.setFocusableInTouchMode(false);
            binding.imageView2.setFocusableInTouchMode(false);


            try{

                Cursor cursor = db.rawQuery("SELECT * FROM blogs WHERE id = ?",new String[]{String.valueOf(blogId)});
                int blogNameIx = cursor.getColumnIndex("topicname");
                int bloggerNameIx = cursor.getColumnIndex("bloggername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.topicText.setText(cursor.getString(blogNameIx));
                    binding.nameText.setText(cursor.getString(bloggerNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView2.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void save(View view){
        String topicText = binding.topicText.getText().toString();
        String nameText = binding.nameText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImg = makeSmallerImage(selectedImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImg.compress(Bitmap.CompressFormat.PNG,50, outputStream);
        byte[] byteArray= outputStream.toByteArray();
        try {

            db.execSQL("CREATE TABLE IF NOT EXISTS blogs(id INTEGER PRIMARY KEY,topicname VARCHAR,bloggername VARCHAR,year VARCHAR,image BLOB)");
            String sqlString = "INSERT INTO blogs(topicname,bloggername,year,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement = db.compileStatement(sqlString);
            sqLiteStatement.bindString(1,topicText);
            sqLiteStatement.bindString(2,nameText);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(MainActivity2.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//ONCEDEN BİRİKMİŞ AKTİVİTELERİ KAPATIR
        startActivity(intent);

    }
    //İmage küçültme
    public Bitmap makeSmallerImage(Bitmap img,int maxSize){
        int width = img.getWidth();
        int height = img.getHeight();

        float bitmapRatio = (float)width/(float) height;
        if(bitmapRatio>1){
            width = maxSize;
            height = (int)(width/bitmapRatio);
        }else{
            height = maxSize;
            width = (int)(height*bitmapRatio);
        }

        return img.createScaledBitmap(img,width,height,true);
    }
    public void saveImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                //Request Permisson
            }

        }
        else{
            Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intenttoGallery);
            //gallery

        }

    }
    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult!=null){
                        Uri imagedata= intentFromResult.getData();
                        //binding.imageView2.setImageURI(imagedata);
                        try {
                            if(Build.VERSION.SDK_INT>=28) {//Eğer telefon sürümü 28 in üzerindeyse
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imagedata);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView2.setImageBitmap(selectedImage);
                            }
                            else{
                                selectedImage = MediaStore.Images.Media.getBitmap(MainActivity2.this.getContentResolver(),imagedata);
                                binding.imageView2.setImageBitmap(selectedImage);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intenttoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intenttoGallery);
                    //Permission granted
                }
                else{
                    //Permission denied
                    Toast.makeText(MainActivity2.this,"Permission needed!!!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}