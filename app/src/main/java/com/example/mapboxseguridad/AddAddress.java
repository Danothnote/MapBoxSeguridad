package com.example.mapboxseguridad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AddAddress extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    Button btnFindImage, btnSaveImage;
    EditText etName;
    SQLiteDatabase db;
    ImageView previewImage;

    final int REQUEST_CODE_GALLERY = 999;

    public static SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        init();

        sqLiteHelper = new  SQLiteHelper(this, "ImageDB.sqlite", null,1);
        sqLiteHelper.queryData("CREATE TABLE IF NOT EXISTS IMAGE(Id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, image BLOG)");

        btnFindImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        AddAddress.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sqLiteHelper.insertData(etName.getText().toString().trim(),
                            imageToViewByte(previewImage)
                    );
                    Toast.makeText(getApplicationContext(),R.string.saveSucefull, Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    previewImage.setImageResource(R.mipmap.ic_launcher);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),R.string.errorSave, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public static byte[] imageToViewByte(ImageView image){
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private void init(){
        btnFindImage = (Button) findViewById(R.id.btnFindImage);
        btnSaveImage = (Button) findViewById(R.id.btnSaveImage);
        etName = (EditText) findViewById(R.id.etName);
        previewImage = (ImageView) findViewById((R.id.iwPreviewImage));

    }

    private void openGalery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else
                Toast.makeText(getApplicationContext(), R.string.errorPermissions, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri pathImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(pathImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                previewImage.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
