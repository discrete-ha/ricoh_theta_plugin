package com.airflare.ricoh_theta_plugin;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;


/**
 * Activity that displays photo object as a sphere
 */
public class GLPhotoActivity extends Activity implements ConfigurationDialog.DialogBtnListener {

    private static final String CAMERA_IP_ADDRESS = "CAMERA_IP_ADDRESS";
    private static final String OBJECT_ID = "OBJECT_ID";
    private static final String THUMBNAIL = "THUMBNAIL";

    private GLPhotoView mGLPhotoView;

    private Photo mTexture = null;
    private LoadPhotoTask mLoadPhotoTask = null;

    private RotateInertia mRotateInertia = RotateInertia.INERTIA_50;

    public static final int REQUEST_PREVIEW_PICTURE = 100;
    public static final int REQUEST_NOT_REFRESH_LIST = 101;
    private String fileUri;
    private String fileId;
    private byte[] rawImageData;
    private View buttonPanel;

    /**
     * onCreate Method
     * @param savedInstanceState onCreate Status value
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glphoto);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Intent intent = getIntent();
        String cameraIpAddress = intent.getStringExtra(CAMERA_IP_ADDRESS);
        fileId = intent.getStringExtra(OBJECT_ID);
        byte[] byteThumbnail = intent.getByteArrayExtra(THUMBNAIL);

        ByteArrayInputStream inputStreamThumbnail = new ByteArrayInputStream(byteThumbnail);
        Drawable thumbnail = BitmapDrawable.createFromStream(inputStreamThumbnail, null);

        Photo _thumbnail = new Photo(((BitmapDrawable)thumbnail).getBitmap());

        mGLPhotoView = findViewById(R.id.photo_image);
        mGLPhotoView.setTexture(_thumbnail);
        mGLPhotoView.setmRotateInertia(mRotateInertia);

        buttonPanel = findViewById(R.id.button_pannel);
        buttonPanel.setVisibility(View.INVISIBLE);

        mLoadPhotoTask = new LoadPhotoTask(cameraIpAddress, fileId);
        mLoadPhotoTask.execute();
    }

    @Override
    protected void onDestroy() {
        if (mTexture != null) {
            mTexture.getPhoto().recycle();
        }
        if (mLoadPhotoTask != null) {
            mLoadPhotoTask.cancel(true);
        }
        super.onDestroy();
    }

//    /**
//     * onCreateOptionsMenu method
//     * @param menu Menu initialization object
//     * @return Menu display feasibility status value
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.configuration, menu);
//
//        return true;
//    }
//
//
//    /**
//     * onOptionsItemSelected Method
//     * @param item Process menu
//     * @return Menu process continuation feasibility value
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//        switch (item.getItemId()) {
//            case R.id.configuration:
//                FragmentManager mgr = getFragmentManager();
//                ConfigurationDialog.show(mgr, mRotateInertia);
//                break;
//            default:
//                break;
//        }
//        return true;
//    }


    /**
     * onResume Method
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLPhotoView.onResume();

        if (null != mTexture) {
            if (null != mGLPhotoView) {
                mGLPhotoView.setTexture(mTexture);
            }
        }
    }

    /**
     * onPause Method
     */
    @Override
    protected void onPause() {
        this.mGLPhotoView.onPause();
        super.onPause();
    }


    /**
     * onDialogCommitClick Method
     * @param inertia selected inertia
     */
    @Override
    public void onDialogCommitClick(RotateInertia inertia) {
        mRotateInertia = inertia;
        if (null != mGLPhotoView) {
            mGLPhotoView.setmRotateInertia(mRotateInertia);
        }
    }


    private class LoadPhotoTask extends AsyncTask<Void, Object, ImageData> {

        private LogView logViewer;
        private ProgressBar progressBar;
        private String cameraIpAddress;
        private long fileSize;
        private long receivedDataSize = 0;

        public LoadPhotoTask(String cameraIpAddress, String fileId) {
            this.logViewer = (LogView) findViewById(R.id.photo_info);
            this.progressBar = (ProgressBar) findViewById(R.id.loading_photo_progress_bar);
            this.cameraIpAddress = cameraIpAddress;
//            this.fileId = fileId;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ImageData doInBackground(Void... params) {
            try {
                publishProgress("start to download image " + fileId);
                HttpConnector camera = new HttpConnector(cameraIpAddress);
                ImageData resizedImageData = camera.getImage(fileId, new HttpDownloadListener() {
                    @Override
                    public void onTotalSize(long totalSize) {
                        fileSize = totalSize;
                    }

                    @Override
                    public void onDataReceived(int size) {
                        receivedDataSize += size;

                        if (fileSize != 0) {
                            int progressPercentage = (int) (receivedDataSize * 100 / fileSize);
                            publishProgress(progressPercentage);
                        }
                    }
                });
                publishProgress("finish to download");

                rawImageData = resizedImageData.getRawData();

                writeToFile(rawImageData, fileId);

                return resizedImageData;

            } catch (Throwable throwable) {
                String errorLog = Log.getStackTraceString(throwable);
                publishProgress(errorLog);
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            for (Object param : values) {
                if (param instanceof Integer) {
                    progressBar.setProgress((Integer) param);
                } else if (param instanceof String) {
                    logViewer.append((String) param);
                }
            }
        }

        @Override
        protected void onPostExecute(ImageData imageData) {
            if (imageData != null) {

                byte[] dataObject = imageData.getRawData();

                if (dataObject == null) {
                    logViewer.append("failed to download image");
                    return;
                }

                Bitmap __bitmap = BitmapFactory.decodeByteArray(dataObject, 0, dataObject.length);

                progressBar.setVisibility(View.GONE);

                Double yaw = imageData.getYaw();
                Double pitch = imageData.getPitch();
                Double roll = imageData.getRoll();
                logViewer.append("<Angle: yaw=" + yaw + ", pitch=" + pitch + ", roll=" + roll + ">");

                mTexture = new Photo(__bitmap, yaw, pitch, roll);
                if (null != mGLPhotoView) {
                    mGLPhotoView.setTexture(mTexture);
                }
            } else {
                logViewer.append("failed to download image");
            }
            buttonPanel.setVisibility(View.VISIBLE);
        }
    }

    private void writeToFile(byte[] rawData, String fileUrl) {

        if (isExternalStorageWritable()) {

            if (ContextCompat.checkSelfPermission(GLPhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                String[] fileComponents = fileUrl.split("/");
                int nItems = fileComponents.length;
                String fileName = fileComponents[nItems - 2] + "/" + fileComponents[nItems - 1];


                File photo = new File(Environment.getExternalStorageDirectory(), fileName);

                if (photo.exists()) {
                    photo.delete();
                }

                try {

                    photo.getParentFile().mkdirs();

                    FileOutputStream fos=new FileOutputStream(photo.getPath());

                    fos.write(rawData);
                    fos.close();
                    fileUri = photo.toURI().toString();
                }
                catch (java.io.IOException e) {
                    Log.e("PictureDemo", "Exception in photoCallback", e);
                }


            } else {
                // Request permission from the user
                ActivityCompat.requestPermissions(GLPhotoActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        } else {
            showNoFileAccessDialog();
        }


    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("PhotoActivity", "Directory not created");
        }
        return file;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void onClickOk(View button) {
        if (fileUri == null) {
            showNoFileAccessDialog();
        } else {
            setResult(RESULT_OK, new Intent().putExtra("fileUri", fileUri));
            finish();
        }
    }

    private void showNoFileAccessDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_confirm)
                .content(R.string.dialog_no_storage)
                .positiveText(R.string.dialog_positive_button)
                .show();
    }

    public void onClickCancel(View button) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            writeToFile(rawImageData, fileId);
        }
    }

    /**
     * Activity call method
     * 
     * @param activity Call source activity
     * @param cameraIpAddress IP address for camera device
     * @param fileId Photo object identifier
     * @param thumbnail Thumbnail
     * @param refreshAfterClose true is to refresh list after closing this activity, otherwise is not to refresh
     */
    public static void startActivityForResult(Activity activity, String cameraIpAddress, String fileId, byte[] thumbnail, boolean refreshAfterClose) {
        int requestCode;
        if (refreshAfterClose) {
            requestCode = REQUEST_PREVIEW_PICTURE;
        } else {
            requestCode = REQUEST_NOT_REFRESH_LIST;
        }

        Intent intent = new Intent(activity, GLPhotoActivity.class);
        intent.putExtra(CAMERA_IP_ADDRESS, cameraIpAddress);
        intent.putExtra(OBJECT_ID, fileId);
        intent.putExtra(THUMBNAIL, thumbnail);
        activity.startActivityForResult(intent, requestCode);
    }
}