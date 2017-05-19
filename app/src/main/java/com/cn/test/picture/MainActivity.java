package com.cn.test.picture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_1, btn_2;
    private ImageView img_1;
    private static final int FLAG_CHOOSE_PHONE = 1;// 截取
    private static final int FLAG_CROP_PHONE = 2;//截取返回
    private static final int FLAG_SELECT_PHOE = 3;//相册
    public static final String KEY_PHOTO_PATH = "photo_path";
    private static int PHOTOSIZE = 350;
    private SharedPreferences sharedPreferences = null;
    private Bitmap bmp = null;// 修改头像map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("temp",
                Activity.MODE_PRIVATE);
        img_1 = (ImageView) findViewById(R.id.img_1);
        btn_1 = (Button) findViewById(R.id.btn_1);
        btn_2 = (Button) findViewById(R.id.btn_2);
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_1:
                getCamera();
                break;
            case R.id.btn_2:
                getAlbum();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FLAG_CHOOSE_PHONE:
                    Uri uri = null;
                    if (data != null) {
                        uri = data.getData();
                        System.out.println("Data");
                    } else {
                        System.out.println("File");
                        String fileName = getSharedPreferences("temp",
                                Context.MODE_PRIVATE).getString("tempName",
                                "");
                        uri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), fileName));
                    }
                    try {
                        Bitmap bitmaps = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(), uri);
                        img_1.setImageBitmap(bitmaps);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cropImage(uri, PHOTOSIZE, PHOTOSIZE, FLAG_CROP_PHONE);
                    break;
                case FLAG_CROP_PHONE:
                    bmp = null;
                    Uri photoUri = data.getData();
                    if (photoUri != null) {
                        bmp = BitmapFactory.decodeFile(photoUri.getPath());
                    } else {
                        String fileName = getSharedPreferences("temp",
                                Context.MODE_PRIVATE).getString("tempName",
                                "");
                        photoUri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), fileName));

                    }
                    if (bmp == null) {
                        Bundle extra = data.getExtras();
                        if (extra != null) {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                                try {
                                    bmp = MediaStore.Images.Media.getBitmap(
                                            this.getContentResolver(), photoUri);
                                    img_1.setImageBitmap(bmp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                bmp = (Bitmap) extra.get("data");
                            }
                        } else {
                            try {
                                bmp = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(), photoUri);
                                img_1.setImageBitmap(bmp);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (bmp != null) {
                        //do something
                    } else {
                        Toast.makeText(MainActivity.this, "获取图片失败，请重试！",
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                case FLAG_SELECT_PHOE:
                    bmp = null;
                    String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("tempName", fileName);
                    editor.commit();
                    Uri imageUri = Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory(), fileName));
                    //此处启动裁剪程序
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    //此处注释掉的部分是针对android 4.4路径修改的一个测试
                    intent.setDataAndType(data.getData(), "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, FLAG_CROP_PHONE);
                default:
                    break;
            }
        }
    }

    /**
     * 相机
     */
    private void getCamera() {
        Uri imageUri = null;
        String fileName = null;
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 删除上一次截图的临时文件
        String cdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
        // 保存本次截图临时文件名字
        fileName = String.valueOf(System.currentTimeMillis()) + ".png";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("tempName", fileName);
        editor.commit();
        imageUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), fileName));
        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        PackageManager packageManager = getPackageManager();
        int permission = packageManager.checkPermission("android.permission.CAMERA", this.getPackageName());
        if (PackageManager.PERMISSION_GRANTED == permission) {
            //有这个权限
            startActivityForResult(openCameraIntent, FLAG_CHOOSE_PHONE);
        } else {
            //没有这个权限
//            Util.Perssion(UserInfoActivity.this, 3);
            getAppDetailSettingIntent(this);
        }
    }

    /**
     * 相册
     */
    private void getAlbum() {
        String intentactiong = "";
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            intentactiong = Intent.ACTION_PICK;
        } else {
            intentactiong = Intent.ACTION_GET_CONTENT;
        }
        Intent openAlbumIntent = new Intent(intentactiong);
        openAlbumIntent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(openAlbumIntent, FLAG_SELECT_PHOE);
    }

    // 截取图片
    public void cropImage(Uri uri, int outputX, int outputY, int requestCode) {
        if (uri != null) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("tempName", fileName);
                editor.commit();
                Uri imageUri = Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), fileName));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
            intent.putExtra("outputFormat", "PNG");
            intent.putExtra("noFaceDetection", true);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 获取权限跳转到应用设置权限界面
     *
     * @param context
     */
    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

}
