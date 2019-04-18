package com.dynamsoft.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.BarcodeReaderException;
import com.dynamsoft.barcode.EnumImagePixelFormat;
import com.dynamsoft.barcode.TextResult;
import com.dynamsoft.barcodescanner.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBR extends Activity implements Camera.PreviewCallback {
    public static String TAG = "DBRDemo";
    public static String ACTION_BARCODE = "com.dynamsoft.dbr";
    private Camera.Size mPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //apply for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=  PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            }
        }

        mPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mFlashImageView = (ImageView)findViewById(R.id.ivFlash);
        mFlashTextView = (TextView)findViewById(R.id.tvFlash);
        mRectLayer = (RectLayer)findViewById(R.id.rectLayer);

        mSurfaceHolder = new CameraPreview(DBR.this);
        mPreview.addView(mSurfaceHolder);

        String licenseKey = "";

        Intent intent = getIntent();
        if (intent.getAction().equals(ACTION_BARCODE)) {
            mIsIntent = true;
            licenseKey = intent.getStringExtra("licenseKey");
        }

        try {
            mBarcodeReader = new BarcodeReader("t0068MgAAAFym/xLiM4ibsKEAFOu11gQUPPG1zDEDejtLlVwLNXRiM6Hoh4ec/HuyZUlvn6srXdgOQvDFv1QXiwzIRS4pHIs=");//this is a trail license
            //
            mBarcodeReader.initLicenseFromServer("https://www.dynamsoft.com/api/DbrLicense/Authorize",licenseKey, new DBRServerLicenseVerificationListener() {
                @Override
                public void licenseVerificationCallback(boolean isSuccess, Exception error) {
                    if (!isSuccess) {
						Log.i(TAG, "DBR license verify failed due to " + error.getMessage())；
					}
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final int PERMISSIONS_REQUEST_CAMERA = 473;
    private FrameLayout mPreview = null;
    private CameraPreview mSurfaceHolder = null;
    private Camera mCamera = null;
    private BarcodeReader mBarcodeReader;
    private ImageView mFlashImageView;
    private TextView mFlashTextView;
    private RectLayer mRectLayer;
    private boolean mIsDialogShowing = false;
    private boolean mIsReleasing = false;
    final ReentrantReadWriteLock mRWLock = new ReentrantReadWriteLock();

    @Override protected void onResume() {
        super.onResume();
        waitForRelease();
        if (mCamera == null)
            openCamera();
        else
            mCamera.startPreview();
    }

    @Override protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        if (mCamera != null) {
            mSurfaceHolder.stopPreview();
            mCamera.setPreviewCallback(null);
            mIsReleasing = true;
            releaseCamera();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        waitForRelease();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void showAbout(View v) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("About");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mIsDialogShowing = false;
            }
        });
        Spanned spanned = Html.fromHtml("<font color='#FF8F0D'><a href=\"http://www.dynamsoft.com\">Download Free Trial here</a></font><br/>");
        builder.setMessage(spanned);
        builder.create(R.layout.about, R.style.AboutDialog).show();
        mIsDialogShowing = true;
    }

    public void setFlash(View v) {
        if (mCamera != null) {
            Camera.Parameters p = mCamera.getParameters();

            String flashMode = p.getFlashMode();
             if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                 p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                 mFlashImageView.setImageResource(R.mipmap.flash_on);
                 mFlashTextView.setText("Flash on");
             }
            else {
                 p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                 mFlashImageView.setImageResource(R.mipmap.flash_off);
                 mFlashTextView.setText("Flash off");
             }
            mCamera.setParameters(p);
            mCamera.startPreview();
        }
    }

    private static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.i(TAG, "Camera is not available (in use or does not exist)");
        }
        return c; // returns null if camera is unavailable
    }

    private void openCamera()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCamera = getCameraInstance();
                if (mCamera != null) {
                    mCamera.setDisplayOrientation(90);
                    Camera.Parameters cameraParameters = mCamera.getParameters();
                    cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    mCamera.setParameters(cameraParameters);
                }

                Message message = handler.obtainMessage(OPEN_CAMERA, 1);
                message.sendToTarget();
            }
        }).start();
    }

    private void releaseCamera()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCamera.release();
                mCamera = null;
                mRWLock.writeLock().lock();
                mIsReleasing = false;
                mRWLock.writeLock().unlock();
            }
        }).start();
    }

    private void waitForRelease() {
        while (true) {
            mRWLock.readLock().lock();
            if (mIsReleasing) {
                mRWLock.readLock().unlock();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mRWLock.readLock().unlock();
                break;
            }
        }
    }

    private boolean mFinished = true;
    private final static int READ_RESULT = 1;
    private final static int OPEN_CAMERA = 2;
    private final static int RELEASE_CAMERA = 3;
    private boolean mIsIntent = false;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_RESULT:
                    TextResult[] result = (TextResult[])msg.obj;
                    TextResult barcode = (result != null && result.length > 0) ?  result[0]:null ;
                    if (barcode != null) {
                        if (mIsIntent) {
                            Intent data = new Intent();
                            data.putExtra("SCAN_RESULT", barcode.barcodeText);
                            data.putExtra("SCAN_RESULT_FORMAT", barcode.barcodeFormatString);
                            DBR.this.setResult(DBR.RESULT_OK, data);
                            DBR.this.finish();
                            mFinished = true;
                            return;
                        }

                        CustomDialog.Builder builder = new CustomDialog.Builder(DBR.this);
                        builder.setTitle("Result");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mIsDialogShowing = false;
                            }
                        });
                        int y = Integer.MIN_VALUE;
                        //rotate cornerPoints by 90, NewLeft = H - Ymax, NewTop = Left, NewWidth = Height, NewHeight = Width

                        com.dynamsoft.barcode.Point[] points = barcode.localizationResult.resultPoints;
                        int leftX,leftY,rightX,rightY;
                        rightX=leftX = points[0].x;
                        rightY=leftY = points[0].y;
                        for (com.dynamsoft.barcode.Point pt:points ) {
                            if(pt.x<leftX) leftX=pt.x;
                            if(pt.y<leftY) leftY=pt.y;
                            if(pt.x>rightX) rightX = pt.x;
                            if(pt.y>rightY) rightY = pt.y;
                        }
                        Rect frameRegion = new Rect(leftX,leftY,rightX,rightY);

                        Rect frameSize = new Rect(0,0,mPreviewSize.width,mPreviewSize.height);
                        Rect viewRegion = ConvertFrameRegionToViewRegion(frameRegion, frameSize,getOrientationDisplayOffset(getBaseContext(),90),mSurfaceHolder.getWidth(),mSurfaceHolder.getHeight());

                        builder.setMessage(barcode,viewRegion);
                        CustomDialog dialog = builder.create(R.layout.result, R.style.ResultDialog);
                        dialog.getWindow().setLayout(mRectLayer.getWidth() * 10 / 12, (mRectLayer.getHeight() >> 1) + 16);
                        dialog.show();
                        mIsDialogShowing = true;
                    }
                    mFinished = true;
                    break;
                case OPEN_CAMERA:
                    if (mCamera != null) {
                        mCamera.setPreviewCallback(DBR.this);
                        mSurfaceHolder.setCamera(mCamera);
                        Camera.Parameters p = mCamera.getParameters();
                        if (mFlashTextView.getText().equals("Flash on"))
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(p);
                        mSurfaceHolder.startPreview();
                    }
                    break;
                case RELEASE_CAMERA:
                    break;
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mFinished && !mIsDialogShowing) {
            mFinished = false;
            mPreviewSize = camera.getParameters().getPreviewSize();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                    mPreviewSize.width, mPreviewSize.height, null);

            int width = yuvImage.getWidth();
            int height = yuvImage.getHeight();
            int[] strides = yuvImage.getStrides();

            try {
                mBarcodeReader.decodeBuffer(yuvImage.getYuvData(), width, height, strides[0], EnumImagePixelFormat.IPF_NV21, "");
                TextResult[] readResult = mBarcodeReader.getAllTextResults();
                Message message = handler.obtainMessage(READ_RESULT, readResult);
                message.sendToTarget();

            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }
        }
    }

    public static Rect boundaryRotate(Point orgPt, Rect rect , boolean bLeft ){
        float orgx = orgPt.x;
        float orgy = orgPt.y;

        float rotatex =orgy;
        float rotatey = orgx;
        float[]currentBoundary = new float [8];
        currentBoundary[0] = rect.left;
        currentBoundary[1] = rect.top;

        currentBoundary[2] = rect.right;
        currentBoundary[3] = rect.top;

        currentBoundary[4] = rect.right;
        currentBoundary[5] = rect.bottom;

        currentBoundary[6] = rect.left;
        currentBoundary[7] = rect.bottom;

        int[] rotateBoundary = new int [8];
        if(bLeft){
            rotateBoundary[6] = (int)((currentBoundary[0]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[1] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[7] = (int)(-(currentBoundary[0]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[1] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

            rotateBoundary[0] = (int)((currentBoundary[2]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[1] = (int)(-(currentBoundary[2]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

            rotateBoundary[2] = (int)((currentBoundary[4]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[3] = (int)(-(currentBoundary[4]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

            rotateBoundary[4] = (int)((currentBoundary[6]-orgx)*Math.cos(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[5] = (int)(-(currentBoundary[6]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);
        }else
        {
            rotateBoundary[2] = (int)((currentBoundary[0]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[1] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[3] = (int)(((currentBoundary[0]-orgx)*Math.sin(Math.PI*0.5f))+((currentBoundary[1] - orgy)*Math.cos(Math.PI*0.5f))+rotatey);

            rotateBoundary[4] = (int)((currentBoundary[2]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[3] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[5] = (int)((currentBoundary[2]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[3] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

            rotateBoundary[6] = (int)((currentBoundary[4]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[5] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[7] = (int)((currentBoundary[4]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[5] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);

            rotateBoundary[0] = (int)((currentBoundary[6]-orgx)*Math.cos(Math.PI*0.5f)-(currentBoundary[7] - orgy)*Math.sin(Math.PI*0.5f)+rotatex);
            rotateBoundary[1] = (int)((currentBoundary[6]-orgx)*Math.sin(Math.PI*0.5f)+(currentBoundary[7] - orgy)*Math.cos(Math.PI*0.5f)+rotatey);
        }

        Rect rotateRect  = new Rect(rotateBoundary[0],rotateBoundary[1],rotateBoundary[2],rotateBoundary[5]);

        return rotateRect;
    }

    public static Rect boundaryRotate180(Point orgPt,Rect rect ){
        float orgx = orgPt.x;
        float orgy = orgPt.y;

        float rotatex =orgy;
        float rotatey = orgx;
        float[]currentBoundary = new float [8];
        currentBoundary[0] = rect.left;
        currentBoundary[1] = rect.top;

        currentBoundary[2] = rect.right;
        currentBoundary[3] = rect.top;

        currentBoundary[4] = rect.right;
        currentBoundary[5] = rect.bottom;

        currentBoundary[6] = rect.left;
        currentBoundary[7] = rect.bottom;
        int[] rotateBoundary = new int [8];
        rotateBoundary[4] =(int)(orgx - (currentBoundary[0]-orgx));
        rotateBoundary[5] =(int)(orgy - (currentBoundary[1]-orgy));

        rotateBoundary[6] =(int)(orgx - (currentBoundary[2]-orgx));
        rotateBoundary[7] =(int)(orgy - (currentBoundary[3]-orgy));

        rotateBoundary[0] =(int)(orgx - (currentBoundary[4]-orgx));
        rotateBoundary[1] =(int)(orgy - (currentBoundary[5]-orgy));

        rotateBoundary[2] =(int)(orgx - (currentBoundary[6]-orgx));
        rotateBoundary[3] =(int)(orgy - (currentBoundary[7]-orgy));
        Rect rotateRect  = new Rect(rotateBoundary[0],rotateBoundary[1],rotateBoundary[2],rotateBoundary[5]);

        return rotateRect;
    }

    public static int getOrientationDisplayOffset(Context context, int nSensorOrientation){
        int mDisplayOffset=0;
        Display display = ((WindowManager)  context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0: mDisplayOffset = 0; break;
            case Surface.ROTATION_90: mDisplayOffset = 90; break;
            case Surface.ROTATION_180: mDisplayOffset = 180; break;
            case Surface.ROTATION_270: mDisplayOffset = 270; break;
            default: mDisplayOffset = 0; break;
        }
        //		if (mFacing == Facing.FRONT) {
//			// Here we had ((mSensorOffset - mDisplayOffset) + 360 + 180) % 360
//			// And it seemed to give the same results for various combinations, but not for all (e.g. 0 - 270).
//			return (360 - ((mSensorOffset + mDisplayOffset) % 360)) % 360;
//		} else
        {
            int nOrientationDisplayOffset =  (nSensorOrientation - mDisplayOffset + 360) % 360;
            return nOrientationDisplayOffset;
        }
    }

    public static Rect ConvertViewRegionToVideoFrameRegion(Rect viewRegion, Rect frameSize, int nOrientationDisplayOffset, Camera.Size szCameraView){
        Rect convertRegion ;
        final int rotateDegree = nOrientationDisplayOffset;
        if(rotateDegree == 90){
            convertRegion =  boundaryRotate(new Point(szCameraView.width/2,szCameraView.height/2),viewRegion,true);
        }else if(rotateDegree == 180){
            convertRegion = boundaryRotate180(new Point(szCameraView.width/2,szCameraView.height/2),viewRegion);
        }else if(nOrientationDisplayOffset == 270){
            convertRegion =  boundaryRotate(new Point(szCameraView.width/2,szCameraView.height/2),viewRegion,false);
        }else{
            convertRegion = viewRegion;
        }

        int nViewW = szCameraView.width;
        int nViewH = szCameraView.height;
        float fScaleH = (nOrientationDisplayOffset%180 ==0)? 1.0f*frameSize.height()/nViewH:1.0f*frameSize.height()/nViewW;
        float fScaleW = (nOrientationDisplayOffset%180 ==0)?1.0f *frameSize.width()/nViewW:1.0f*frameSize.width()/nViewH;
        float fScale   = (fScaleH>fScaleW)?fScaleW:fScaleH;
        int boxLeft  =(int)(convertRegion.left*fScale);
        int boxTop   = (int)(convertRegion.top*fScale);
        int boxWidth = (int)(convertRegion.width()*fScale);
        int boxHeight = (int)(convertRegion.height()*fScale);
        Rect frameRegion = new Rect(boxLeft,boxTop,boxWidth+boxLeft,boxTop+boxHeight);
        return frameRegion;
    }

    public static Rect ConvertFrameRegionToViewRegion(Rect frameRegion, Rect frameSize,int nOrientationDisplayOffset,int cameraViewWidth,int cameraViewHeight){

        Rect imageRect = frameSize;
        Rect roateRect =frameRegion;
        int rotateDegree = nOrientationDisplayOffset;

        if(rotateDegree == 90){
            roateRect =  boundaryRotate(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion,false);
        }else if(rotateDegree == 180){
            roateRect = boundaryRotate180(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion);
        }else if(nOrientationDisplayOffset == 270){
            roateRect =  boundaryRotate(new Point(imageRect.width()/2,imageRect.height()/2),frameRegion,true);
        }

        int nViewW = cameraViewWidth;
        int nViewH = cameraViewHeight;

        float fScaleH = (nOrientationDisplayOffset%180 ==0)? 1.0f*nViewH /imageRect.height():1.0f*nViewH /imageRect.width();
        float fScaleW = (nOrientationDisplayOffset%180 ==0)?1.0f *nViewW/imageRect.width():1.0f*nViewW /imageRect.height();
        float fScale   = (fScaleH>fScaleW)?fScaleW:fScaleH;

        int boxLeft  =(int)(roateRect.left*fScale);
        int boxTop   = (int)(roateRect.top*fScale);
        int boxWidth = (int)(roateRect.width()*fScale);
        int boxHeight = (int)(roateRect.height()*fScale);
        Rect viewRegion = new Rect(boxLeft,boxTop,boxWidth+boxLeft,boxTop+boxHeight);
        return viewRegion;

    }
}
