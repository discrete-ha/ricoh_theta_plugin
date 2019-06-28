package com.airflare.ricoh_theta_plugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.StaticLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Activity that displays the photo list
 */

public class LivePreviewActivity extends Activity {

	private String cameraIpAddress;
	private Spinner spinnerTimer;
	private Button btnShoot, btnOptionOff, btnOptionAuto, btnOptionNoise, btnOptionHDR;
	private MJpegView mMv;
	private SeekBar sbBrightness;
	private TextView textBrightness;

	private ShowLiveViewTask livePreviewTask = null;
	private MaterialDialog progressDialog, infoDialog;
	private ConnectivityManager.NetworkCallback networkCallback;
	private boolean isRequestingPermissions;
	private boolean locationPermissionDenied;
	private double evNumbers[] = new double[] {-2.0, -1.7, -1.3, -1.0, -0.7, -0.3, 0.0, 0.3, 0.7, 1.0, 1.3, 1.7, 2.0};
	public String stringStyle;
	private WifiManager wifiManager;
	/**
     * onCreate Method
     * @param savedInstanceState onCreate Status value
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_live_preview);

		cameraIpAddress = getResources().getString(R.string.theta_ip_address);

		stringStyle = getIntent().getStringExtra("style");

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		mMv = findViewById(R.id.live_view);
		textBrightness = findViewById(R.id.text_brightness);
		initButtons();
	}

	private void initButtons(){

		sbBrightness = findViewById(R.id.sb_brightness);
		textBrightness.setText( String.valueOf( evNumbers[sbBrightness.getProgress()] ) );
		sbBrightness.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {

					private int progress;

					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						this.progress = progress;
						textBrightness.setText( String.valueOf( evNumbers[progress] ) );
					}

					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							JSONObject options = new JSONObject();
							options.put("exposureCompensation", evNumbers[progress]);
							new SettingOptionTask(options).execute();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
		);


		btnShoot = findViewById(R.id.btn_shoot);
		btnShoot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnShoot.setEnabled(false);
				new ShootTask().execute();
			}
		});


		btnOptionOff = findViewById(R.id.btn_option_off);
		btnOptionOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					JSONObject options = new JSONObject();
					options.put("_filter", getResources().getString(R.string.options_off));
					new SettingOptionTask(options,  btnOptionOff).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		btnOptionHDR = findViewById(R.id.btn_option_hdr);
		btnOptionHDR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					JSONObject options = new JSONObject();
					options.put("_filter", getResources().getString(R.string.options_hdr));
					new SettingOptionTask(options,  btnOptionHDR).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		btnOptionAuto = findViewById(R.id.btn_option_auto);
		btnOptionAuto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					JSONObject options = new JSONObject();
					options.put("_filter", getResources().getString(R.string.options_dr_comp));
					new SettingOptionTask(options,  btnOptionAuto).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		btnOptionNoise = findViewById(R.id.btn_option_noise);
		btnOptionNoise.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					JSONObject options = new JSONObject();
					options.put("_filter", getResources().getString(R.string.options_noise_reduction));
					new SettingOptionTask(options,  btnOptionNoise).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});


		spinnerTimer = findViewById(R.id.spinner_timer);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.timer_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerTimer.setAdapter(adapter);



        spinnerTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject options = new JSONObject();
                    options.put("exposureDelay", position);
                    new SettingOptionTask(options,  btnOptionHDR).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
	}

	private void setButtonSelected(Button targetButton){
		btnOptionOff.setSelected(false);
		btnOptionHDR.setSelected(false);
		btnOptionAuto.setSelected(false);
		btnOptionNoise.setSelected(false);
		targetButton.setSelected(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMv.stopPlay();
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (!isRequestingPermissions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && networkCallback != null) {
			cm.unregisterNetworkCallback(networkCallback);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMv.play();
		if (checkConnectedToWifi() && !isRequestingPermissions && !locationPermissionDenied ) {
			forceConnectToWifi();
		}
		if (locationPermissionDenied) {
			locationPermissionDenied = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GLPhotoActivity.REQUEST_PREVIEW_PICTURE) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK, data);
				Log.d("ImageListActivity", "File uri: "+data.getStringExtra("fileUri"));
			}
			finish();
		}
	}

	@Override
	protected void onDestroy() {

		if (livePreviewTask != null) {
			livePreviewTask.cancel(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ConnectivityManager.setProcessDefaultNetwork(null);
		}

		super.onDestroy();
	}

	private void appendLogView(final String log) {
		Log.d("THETA", log);
	}

	public void didConnect() {
		if (livePreviewTask != null) {
			livePreviewTask.cancel(true);
		}
		livePreviewTask = new ShowLiveViewTask();
		livePreviewTask.execute();
	}

	public void connectDidFail() {
		showInfoDialog(R.string.dialog_could_not_connect);
	}

	private class GettingOptionTask extends AsyncTask<Void, Void, Void> {

		private GettingOptionTask() {
		}

		@Override
		protected Void doInBackground(Void... voids) {

			HttpConnector camera = new HttpConnector(cameraIpAddress);
			camera.getStorageInfo();
			return null;
		}

	}


	private class SettingOptionTask extends AsyncTask<Void, Void, Boolean> {

		private JSONObject options;
		private Button targetButton;

		private SettingOptionTask(JSONObject options, Button targetButton) {
			this.options = options;
			this.targetButton = targetButton;
		}

		private SettingOptionTask(JSONObject options) {
			this.options = options;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {

			HttpConnector camera = new HttpConnector(cameraIpAddress);
			camera.setImageCaptureMode(options);
			boolean result = camera.startApiLevel2Session();
			return result;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			if (targetButton != null){
				setButtonSelected(targetButton);
			}
			new GettingOptionTask().execute();
		}
	}

	private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			btnShoot.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {

			HttpConnector camera = new HttpConnector(cameraIpAddress);
			boolean result = camera.startApiLevel2Session();
			return result;
		}

		@Override
		protected void onPostExecute(Boolean didConnect) {
			if (didConnect.booleanValue()) {
				didConnect();
			} else {
				connectDidFail();
			}
		}
	}

	private class ShowLiveViewTask extends AsyncTask<Void, String, MJpegInputStream> {

		@Override
		protected MJpegInputStream doInBackground(Void... voids) {
			MJpegInputStream mjis = null;
			final int MAX_RETRY_COUNT = 20;
			for (int retryCount = 0; retryCount < MAX_RETRY_COUNT; retryCount++) {
				try {
					publishProgress("start Live view");
					HttpConnector camera = new HttpConnector(cameraIpAddress);
					InputStream is = camera.getLivePreview();
					mjis = new MJpegInputStream(is);
					retryCount = MAX_RETRY_COUNT;
				} catch (IOException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (JSONException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}

			return mjis;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			for (String log : values) {
				appendLogView(log);
			}
		}

		@Override
		protected void onPostExecute(MJpegInputStream mJpegInputStream) {
			if (mJpegInputStream != null) {
				mMv.setSource(mJpegInputStream);
				btnShoot.setEnabled(true);
				setButtonSelected(btnOptionHDR);
//				new GettingOptionTask().execute();
			} else {
				connectDidFail();
			}
			hideProgressDialog();
		}
	}

	private class ShootTask extends AsyncTask<Void, Void, HttpConnector.ShootResult> {

		@Override
		protected void onPreExecute() {
			appendLogView("takePicture");
			showProgressDialog(R.string.dialog_processing);
			mMv.stopPlay();
		}

		@Override
		protected HttpConnector.ShootResult doInBackground(Void... params) {
			CaptureListener postviewListener = new CaptureListener();
			HttpConnector camera = new HttpConnector(getResources().getString(R.string.theta_ip_address));
			HttpConnector.ShootResult result = camera.takePicture(postviewListener);

			return result;
		}

		@Override
		protected void onPostExecute(HttpConnector.ShootResult result) {
			if (result == HttpConnector.ShootResult.FAIL_CAMERA_DISCONNECTED) {
				appendLogView("takePicture:FAIL_CAMERA_DISCONNECTED");
			} else if (result == HttpConnector.ShootResult.FAIL_STORE_FULL) {
				appendLogView("takePicture:FAIL_STORE_FULL");
			} else if (result == HttpConnector.ShootResult.FAIL_DEVICE_BUSY) {
				appendLogView("takePicture:FAIL_DEVICE_BUSY");
			} else if (result == HttpConnector.ShootResult.SUCCESS) {
				appendLogView("takePicture:SUCCESS");
			}
		}

		private class CaptureListener implements HttpEventListener {
			private String latestCapturedFileId;
			private boolean ImageAdd = false;

			@Override
			public void onCheckStatus(boolean newStatus) {
				if(newStatus) {
					appendLogView("takePicture:FINISHED");
				} else {
					appendLogView("takePicture:IN PROGRESS");
				}
			}

			@Override
			public void onObjectChanged(String latestCapturedFileId) {
				this.ImageAdd = true;
				this.latestCapturedFileId = latestCapturedFileId;
				appendLogView("ImageAdd:FileId " + this.latestCapturedFileId);
			}

			@Override
			public void onCompleted() {
				appendLogView("CaptureComplete");
				if (ImageAdd) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							btnShoot.setEnabled(true);
							new GetThumbnailTask(latestCapturedFileId).execute();
							hideProgressDialog();
						}
					});
				}
			}

			@Override
			public void onError(String errorMessage) {
				appendLogView("CaptureError " + errorMessage);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						btnShoot.setEnabled(true);
						hideProgressDialog();
					}
				});
			}
		}

	}

	private class GetThumbnailTask extends AsyncTask<Void, String, byte[]> {

		private String fileId;

		public GetThumbnailTask(String fileId) {
			this.fileId = fileId;
		}

		@Override
		protected byte[] doInBackground(Void... params) {
			HttpConnector camera = new HttpConnector(getResources().getString(R.string.theta_ip_address));
			Bitmap thumbnail = camera.getThumb(fileId);
			if (thumbnail != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] thumbnailImage = baos.toByteArray();
				return thumbnailImage;
			} else {
				publishProgress("failed to get file data.");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			for (String log : values) {
				appendLogView(log);
			}
		}

		@Override
		protected void onPostExecute(byte[] thumbnailImage) {
			GLPhotoActivity.startActivityForResult(LivePreviewActivity.this, cameraIpAddress, fileId, thumbnailImage, true);
		}
	}

	/**
	 * Force this application to connect to Wi-Fi
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void forceConnectToWifi() {
		showProgressDialog(R.string.dialog_connecting_camera);
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if ((info != null) && info.isAvailable()) {
				NetworkRequest.Builder builder = new NetworkRequest.Builder();
				builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
				NetworkRequest requestedNetwork = builder.build();

				networkCallback = new ConnectivityManager.NetworkCallback() {
					@Override
					public void onAvailable(Network network) {
						super.onAvailable(network);
						ConnectivityManager.setProcessDefaultNetwork(network);
						appendLogView("connect to Wi-Fi AP");
						checkWiFiSSID();
					}

					@Override
					public void onLost(Network network) {
						super.onLost(network);
						hideProgressDialog();
						appendLogView("lost connection to Wi-Fi AP");
					}
				};
				cm.registerNetworkCallback(requestedNetwork, networkCallback);
			} else {
				showInfoDialog(R.string.dialog_no_network_info);
			}
		} else {
			showInfoDialog(R.string.dialog_low_version);
		}
	}

	private void checkWiFiSSID() {
		runOnUiThread(() -> {
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

				boolean isConnectedToWifi = checkConnectedToWifi();
				if (!isConnectedToWifi){
					showNotConnectedToThetaDialog();
					return;
				}

				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				String ssid = wifiInfo.getSSID();

				if (ssid.toLowerCase().contains("theta")) {
					connectToCamera();
				} else {
					showNotConnectedToThetaDialog();
				}
			} else {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.ACCESS_COARSE_LOCATION)) {
					showLocationPermissionNeededDialog();
				} else {
					isRequestingPermissions = true;
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
				}

			}
		});
	}

	private boolean checkConnectedToWifi(){
		wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		if (wifiManager == null || !wifiManager.isWifiEnabled()) {
			showNotConnectedToThetaDialog();
			return false;
		}else{
			return true;
		}
	}

	private void showProgressDialog(int stringId){

		hideInfoDialog();
		hideProgressDialog();

		if (!this.isDestroyed()) {
			progressDialog = new MaterialDialog.Builder(LivePreviewActivity.this)
					.title(stringId)
					.progress(true, 0)
					.cancelable(false)
					.canceledOnTouchOutside(false)
					.build();
			progressDialog.show();
		}
	}

	private void hideProgressDialog(){
		if (!this.isDestroyed() && progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	private void showNotConnectedToThetaDialog() {

		hideInfoDialog();
		hideProgressDialog();

		if (!this.isDestroyed()) {
			new MaterialDialog.Builder(this)
				.title(R.string.dialog_confirm)
				.content(R.string.dialog_no_theta_wifi)
				.positiveText(R.string.dialog_settings)
				.onPositive((dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
				.negativeText(R.string.dialog_negative_button)
				.canceledOnTouchOutside(false)
				.onNegative((dialog, which) -> finish())
				.show();
		}
	}

	private void showInfoDialog(int stringId) {

		hideInfoDialog();
		hideProgressDialog();

		if (!this.isDestroyed()) {
			infoDialog = new MaterialDialog.Builder(this)
					.title(R.string.dialog_confirm)
					.content(stringId)
					.negativeText(R.string.dialog_positive_button)
					.canceledOnTouchOutside(false)
					.onNegative((dialog, which) -> finish()).build();
			infoDialog.show();
		}
	}

	private void hideInfoDialog(){
		if (!this.isDestroyed() && infoDialog != null) {
			infoDialog.dismiss();
		}
	}

	private void connectToCamera() {
		new ConnectTask().execute();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		isRequestingPermissions = false;
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			locationPermissionDenied = false;
			checkWiFiSSID();
		} else {
			locationPermissionDenied = true;
			showLocationPermissionNeededDialog();
		}
	}

	private void showLocationPermissionNeededDialog() {
		hideInfoDialog();
		hideProgressDialog();

		new MaterialDialog.Builder(this)
				.title(R.string.dialog_confirm)
				.content(R.string.dialog_why_wifi)
				.positiveText(R.string.dialog_settings)
				.onPositive((dialog, which) -> {
					Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
					startActivity(intent);
				})
				.negativeText(R.string.dialog_negative_button)
				.show();
	}
}
