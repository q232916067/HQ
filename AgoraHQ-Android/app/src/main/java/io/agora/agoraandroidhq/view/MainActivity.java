package io.agora.agoraandroidhq.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import io.agora.agoraandroidhq.R;
import io.agora.agoraandroidhq.tools.GameControl;
import io.agora.agoraandroidhq.tools.SharedPreferenceHelper;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_view);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playGame.setClickable(true);
        sharedPreferenceHelper = getSharedPreference();
    }


    private Button playGame;
    private ImageButton questionImage;
    private EditText channelNameEditText;

    private EditText labelName;
    private ImageView labelImage;

    private SharedPreferenceHelper sharedPreferenceHelper;


    private void findView() {
        channelNameEditText = findViewById(R.id.channel_name_editText);
        playGame = findViewById(R.id.btnStartPlayGame);

        labelName = findViewById(R.id.user_label);
        labelImage = findViewById(R.id.user_image);

        setOnEditTextChangeListener(labelName);

        String userNameFromSharedPreference = getNameFromSharedPreference();

        labelName.setText(userNameFromSharedPreference);

        setUserImageFormSharedPreference(labelImage);


    }


    private String getNameFromSharedPreference() {
        GameControl.logD("getNameFromSharedPreference");

        String userName = SharedPreferenceHelper.newInstance(getApplicationContext()).getName();

        return userName;
    }

    private void setUserImageFormSharedPreference(ImageView imageView) {

        Drawable drawable = getDrawableFromSharedPreference();

        if (drawable == null) {
            return;
        } else {

            imageView.setImageDrawable(drawable);
        }

    }


    private Drawable getDrawableFromSharedPreference() {

        Drawable drawable = SharedPreferenceHelper.newInstance(getApplicationContext()).getDrawable();

        return drawable;
    }

    private void setOnEditTextChangeListener(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                String userName = editText.getText().toString();
                if (TextUtils.isEmpty(userName)) {

                    editText.setHint("Label");
                    editText.setHintTextColor(Color.GRAY);
                    GameControl.logD("text is null");

                } else {

                }
            }
        });
    }


    public void startClick(View view) {
        String ChannelName = channelNameEditText.getText().toString();
        String userName = labelName.getText().toString();

        if (TextUtils.isEmpty(ChannelName)) {
            Toast.makeText(MainActivity.this, R.string.text_channel_name_can_not_be_null, Toast.LENGTH_SHORT).show();

        } else {
            playGame.setClickable(false);
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("ChannelName", ChannelName);
            startActivity(intent);
            // logD("onConnectSuccess");

            sharedPreferenceHelper.saveName(userName);

            GameControl.currentUserHeadImage = labelImage.getDrawable();
            GameControl.currentUserName = labelName.getText().toString();

            // GameControl.logD("saveName = " + userName);
        }
    }


    public void questionImageClick(View views) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("Suggestion");

        View view = View.inflate(MainActivity.this, R.layout.dialog_view, null);
        // Toast.makeText(MainActivity.this,"hehe ",Toast.LENGTH_SHORT).show();

        final TextView tel = view.findViewById(R.id.question_tel_text);
        tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 200);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel.getText().toString()));
                    startActivity(intent);
                }
            }
        });

        builder.setView(view);
        builder.setCancelable(true);
        builder.create().show();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            Log.d("zhangpermission  ", grantResults[0] + "");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:400 632 6626"));
                startActivity(intent);
            }
        } else if (requestCode == 0) {

            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }else {
                Toast.makeText(MainActivity.this, R.string.text_do_not_have_permission,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    private SharedPreferenceHelper getSharedPreference() {

        return SharedPreferenceHelper.newInstance(getApplicationContext());

    }

    private static final int IMAGE = 1;


    public void userImage(View v) {
        //调用相册
       /* Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE);*/

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("zhang   ", "  permission != granted");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);

        } else {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            GameControl.logD("bitmapSIze  filePath  =  " + imagePath);
            if (imagePath != null) {


                //GameControl.logD("bitmapSIze =  " + bitmap.getByteCount());

                Bitmap bitmaps = resizeBitmap(imagePath, 200, 200);


                if (bitmaps != null) {
                    labelImage.setImageBitmap(bitmaps);
                    sharedPreferenceHelper.saveDrawable(bitmaps);
                }


                c.close();
            }
        }

    }


    private Bitmap showImage(String imaePath) {

        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        labelImage.setImageBitmap(bm);

        return bm;
    }


    private void showChooseDialog() {


    }


    private Bitmap resizeBitmap(String file, int width, int height) {

        GameControl.logD("filePath =  " + file);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //Bitmap bitmap = BitmapFactory.decodeFile(file);
        Bitmap bitmap = BitmapFactory.decodeFile(file);
        GameControl.logD("bitmapSIze  bitmap = " + bitmap);
        GameControl.logD("bitmapSIze =  before" + bitmap.getByteCount());

//        Bitmap bitmap = BitmapFactory.decodeFile(file);
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        // 缩放图片的尺寸
        float scaleWidth = (float) width / bitmapWidth;
        float scaleHeight = (float) height / bitmapHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 产生缩放后的Bitmap对象
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
        // save file

        GameControl.logD("bitmapSIze =  after" + resizeBitmap.getByteCount());
        return resizeBitmap;
    }

}