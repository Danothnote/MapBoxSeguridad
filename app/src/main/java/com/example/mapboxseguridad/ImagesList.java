package com.example.mapboxseguridad;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class ImagesList extends AppCompatActivity {

    GridView gridView, gridviewImgUpdate;
    ArrayList<Image> list;
    ImageListAdapter adapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.images_list);

        gridView = (GridView) findViewById(R.id.gridViewImages);
        list = new ArrayList<>();
        adapter = new ImageListAdapter(this, R.layout.item_image, list);
        gridView.setAdapter(adapter);

        Cursor cursor = AddAddress.sqLiteHelper.getData("SELECT * FROM IMAGE");
        list.clear();

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            byte[] image = cursor.getBlob(2);
            list.add(new Image(id, name, image));

        }
        adapter.notifyDataSetChanged();

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                CharSequence[] items = {"Actualizar", "Eliminar"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(ImagesList.this);

                dialog.setTitle("Seleccionar acción");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int item) {
                        if (item == 0){
                            Cursor consultSQL = AddAddress.sqLiteHelper.getData("SELECT * FROM IMAGE");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            String nameUpdateImage = null;
                            byte[] image = null;
                            while (consultSQL.moveToNext()){
                                arrID.add(consultSQL.getInt(0));
                                nameUpdateImage = consultSQL.getString(1);
                                image = consultSQL.getBlob(2);
                            }

                            Toast.makeText(getApplicationContext(),"Actualizar", Toast.LENGTH_SHORT).show();
                            showDialogUpdate(ImagesList.this, arrID.get(position), nameUpdateImage, image );


                        }
                        else{
                            Cursor consultSQL = AddAddress.sqLiteHelper.getData("SELECT id FROM IMAGE");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            while (consultSQL.moveToNext()){
                                arrID.add(consultSQL.getInt(0));
                            }
                            Toast.makeText(getApplicationContext(),"Eliminar", Toast.LENGTH_SHORT).show();
                            showDialogDelete(arrID.get(position));
                        }

                    }

                });
                dialog.show();
                return true;
            }
        });
    }

    ImageView viewimages;
    EditText updateEditext;
    private void showDialogUpdate(Activity activity, final int position, String name, byte[] image){

        final Dialog dialog = new Dialog(activity);

        dialog.setContentView(R.layout.update_image_activity);
        dialog.setTitle(R.string.btnUpdate);

        viewimages = (ImageView) dialog.findViewById(R.id.imgUpdate);
        updateEditext = (EditText) dialog.findViewById(R.id.edtUpdateName);
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
        viewimages.setImageBitmap(bmp);
        updateEditext.setText(name);
        final EditText textName = (EditText) dialog.findViewById(R.id.edtUpdateName);
        Button btnUpdate = (Button) dialog.findViewById(R.id.btnUpdate);

        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        viewimages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        ImagesList.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        888
                );
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AddAddress.sqLiteHelper.updateData(
                            textName.getText().toString().trim(),
                            AddAddress.imageToViewByte(viewimages),
                            position
                    );
                    Toast.makeText(getApplicationContext(),R.string.successUpdate, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }catch (Exception error){
                    Toast.makeText(getApplicationContext(),R.string.errorSave, Toast.LENGTH_SHORT).show();
                }
                updateImageList();
            }
        });

    }

    private  void showDialogDelete(final int idImage){
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(ImagesList.this);
        dialogDelete.setTitle(R.string.titleWarning);
        dialogDelete.setMessage(R.string.questioDelete);

        dialogDelete.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    AddAddress.sqLiteHelper.deleteData(idImage);
                    Toast.makeText(getApplicationContext(),R.string.successDelete, Toast.LENGTH_SHORT).show();
                    updateImageList();
                }catch (Exception error){
                    Toast.makeText(getApplicationContext(),R.string.errorDelete, Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogDelete.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void updateImageList(){
        Cursor cursor = AddAddress.sqLiteHelper.getData("SELECT * FROM IMAGE");
        list.clear();

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            byte[] image = cursor.getBlob(2);
            list.add(new Image(id, name, image));

        }
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 888){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 888);
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
        if (requestCode == 888 && resultCode == RESULT_OK && data != null){
            Uri pathImage = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(pathImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                viewimages.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
