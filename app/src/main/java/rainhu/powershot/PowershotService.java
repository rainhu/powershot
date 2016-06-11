package rainhu.powershot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by hu on 16-5-24.
 */
public class PowershotService extends Service{
    private SensorManager mSensorManager;
    private Vibrator mVibrator;
    private WindowManager mWindowManager;
    private BallView mBallView;

    private final SensorEventListener mShakeListener = new SensorEventListener() {
        private static final float SENSITIVITY = 16;
        private static final int BUFFER = 5;
        private float[] gravity = new float[3];
        private float average = 0;
        private int fill = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.8F;
            for (int i=0;i < 3;i++){
                gravity[i] = alpha * gravity[i] + (1- alpha) * event.values[i];
            }
            float x = event.values[0] - gravity[0];
            float y = event.values[1] - gravity[1];
            float z = event.values[2] - gravity[2];

            if (fill <= BUFFER){
                average += Math.abs(x) + Math.abs(y) + Math.abs(z);
                fill++;
            }else{
                CLog.i("average:"+average);
                CLog.i("average / BUFFER:"+(average / BUFFER));
                if (average / BUFFER >= SENSITIVITY) {
                    handleShakeAction();
                }
                average  = 0;
                fill = 0;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //通过摇一摇触发powershot
    private void handleShakeAction() {
        Toast.makeText(getApplicationContext(),"shake success",Toast.LENGTH_SHORT).show();

        //震动
        long [] pattern = {100,400};
        mVibrator.vibrate(pattern, -1);

       // shotScreen();

    }


    @Override
    public void onCreate() {
        CLog.i("service oncreate");
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.width = 150;
        mLayoutParams.height = 150;
        mLayoutParams.x = 50;
        mLayoutParams.y = 50;

        //mLayoutParams.alpha = 0;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        mBallView = new BallView(getApplicationContext());
        mBallView.setParams(mLayoutParams);

        mWindowManager.addView(mBallView, mLayoutParams );
        //mBallView.setTipText("开始截屏");
        //mBallView.setStartBtnText("点击开始");


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSensorManager.registerListener(mShakeListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),50*1000);
        return super.onStartCommand(intent,flags,startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mShakeListener);
    }
}
