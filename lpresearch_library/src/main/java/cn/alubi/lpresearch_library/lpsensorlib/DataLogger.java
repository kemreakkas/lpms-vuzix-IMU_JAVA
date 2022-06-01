package cn.alubi.lpresearch_library.lpsensorlib;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataLogger {

    private static final String TAG = "DataLogger";
    File logFile;
    FileOutputStream logFileStream;
    OutputStreamWriter logFileWriter;
    boolean isLoggingStarted = false;

    String statusMesg = "Logging Stopped";
    String outputFilename ="";
    public long militemp;
    public float accelx,accely,accelz;
    public float magnex, magney, magnez,gyrosx,gyrosy,gyrosz,light,pressure;
    private SensorEvent sensorEvent;



    public DataLogger(){

    }
    boolean isDebugLoggingStarted = false;
    OutputStreamWriter debugLogFileWriter;
    int debugFrameNumber = 0;
    String debugOutputFilename;
    String debugStatusMesg;
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean startLogging() {
        boolean status = false;
        if (isExternalStorageWritable()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String currentDateandTime = sdf.format(new Date());
                File logFileDir = new File(Environment.getExternalStorageDirectory()+ "/LpLogger");
                logFileDir.mkdirs();

                logFile = new File(logFileDir, "DataLogaksam" + ".csv");
                logFile.createNewFile();
                Log.e(TAG, "Dosya oluşturuldu");

                logFileStream = new FileOutputStream(logFile);
                logFileWriter = new OutputStreamWriter(logFileStream);



                Log.e(TAG, "Dosya başlığı eklendi");

                logFileWriter.append("Time ,"+
                        "TimeStamp (s)," +
                        " AccX (g), AccY (g), AccZ (g), " +
                //        "GyroX (deg/s), GyroY (deg/s), GyroZ (deg/s), " +
                //        "MagX (uT), MagY (uT), MagZ (uT), " +

                        "\n");

                isLoggingStarted = true;
                outputFilename = logFile.getAbsolutePath();
                statusMesg = "Logging to " +  outputFilename;
                status = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                statusMesg = e.getMessage();
            }
        } else {
            statusMesg = "Couldn't write to external storage. Please detach device from PC";
        }
        return status;
    }

    public boolean stopLogging() {
        boolean status = false;
        if (!isLoggingStarted) {
            statusMesg = "Logging not started";
            return status;
        }
        try {Log.e(TAG, "Stop logging 1");
            isLoggingStarted = false;

            synchronized(logFileWriter) {
                logFileWriter.close();
            }

            logFileStream.close();

            statusMesg = "Logging Stopped";
            status = true;
        } catch (Exception e) {
            statusMesg = e.getMessage();
        }
        return status;
    }

    public boolean isLogging(){
        return isLoggingStarted;

    }

    public boolean logLpmsData(LpmsBData d) {
        //COMMAND MODE
        Log.e(TAG, "logLpmsData start 1");
        boolean status = false;
        if (isLoggingStarted) {
            try {
                synchronized(logFileWriter) {
                    Log.e(TAG, "logLpmsData start 11");
                    SensorOkuma(sensorEvent);
                    Log.e(TAG, "logLpmsData start 111");
                    logFileWriter.append(
                            String.valueOf( Calendar.getInstance().getTime().toString())+","+
                             d.timestamp + ", "
                   //         +d.frameNumber+","
                            + d.acc[0] + ", " + d.acc[1] + ", " + d.acc[2] + ", "
                   //         + d.gyr[0] + ", " + d.gyr[1] + ", " + d.gyr[2] + ", "
                   //         + d.mag[0] + ", " + d.mag[1] + ", " + d.mag[2] + ", "
                   //         + d.angVel[0] +", "+ d.angVel[1] + ", " +d.angVel[2] + ", "
                   //         + d.euler[0] +", "+ d.euler[1] +", "+ d.euler[2] +", "
                   //         + d.quat[0] + ", " + d.quat[1] + ", " + d.quat[2] + ", " + d.quat[3] + ", "
                   //         + d.linAcc[0] +", "+ d.linAcc[1] +", "+ d.linAcc[2] +", "
                   //         + d.pressure + ", "
                   //         + d.altitude + ", "
                   //         + d.temperature+","
                   //         + d.batteryLevel
                            + "\n");
                }
                status = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                statusMesg = e.getMessage();
            }
        }
        return status;
    }

    public void setStatusMesg(String s) {
        statusMesg = s;
    }

    public String getStatusMesg(){
        return statusMesg;
    }

    public String getOutputFilename(){
        return outputFilename;
    }

    /////////////////////////
    //Flash Debug Data Log
    /////////////////////////



    public boolean isDebugLogging(){
        return isDebugLoggingStarted;

    }

    public boolean startDebugLogging() {
        Log.e(TAG, "debuglogging start1");
        boolean status = false;
        if (isExternalStorageWritable() == true) {
            try {Log.e(TAG, "debuglogging start11");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());

                File logFileDir = new File(Environment.getExternalStorageDirectory()+ "/LpLogger");
                logFileDir.mkdirs();

             //   File DebuglogFile = new File(logFileDir, "FlashDataLog" + currentDateandTime + ".csv");
                File DebuglogFile = new File(logFileDir, "FlashDataLogaksam1"  + ".csv");
                DebuglogFile.createNewFile();

                debugLogFileWriter = new OutputStreamWriter(new FileOutputStream(DebuglogFile));
                logFileStream = new FileOutputStream(logFile);
                logFileWriter = new OutputStreamWriter(logFileStream);
                Log.e(TAG, "debuglogging start111 dosya başlık sonu");
                debugFrameNumber = 0;
                debugLogFileWriter.append(

                                "TimeStamp (s)," +

                                " AccX (g), AccY (g), AccZ (g), " +
                //                "GyroX (deg/s), GyroY (deg/s), GyroZ (deg/s), " +
                //                "MagX (uT), MagY (uT), MagZ (uT), " +
                //                "QuatW, QuatX, QuatY, QuatZ, " +
                //                "Pressure(kPa)," +
                //                "Temperature(degC)
                                        "\n");

                isDebugLoggingStarted = true;
                debugOutputFilename = DebuglogFile.getAbsolutePath();
                debugStatusMesg = "Logging to " +  debugOutputFilename;
                status = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                debugStatusMesg = e.getMessage();
            }
        } else {
            debugStatusMesg = "Couldn't write to external storage. Please detach device from PC";
        }
        return status;
    }

    public boolean stopDebugLogging() {
        boolean status = false;
        if (!isDebugLoggingStarted) {
            debugStatusMesg = "Logging not started";
            return status;
        }
        try {
            isDebugLoggingStarted = false;

            synchronized(debugLogFileWriter) {
                debugLogFileWriter.close();
            }

            debugStatusMesg = "Logging Stopped";
            status = true;
        } catch (Exception e) {
            debugStatusMesg = e.getMessage();
        }
        return status;
    }

    public boolean logDebugLpmsData(LpmsBData d) {
        Log.e(TAG, "debuglpms data start1");
        boolean status = false;
        if (isDebugLoggingStarted) {
            try {
                synchronized(debugLogFileWriter) {
                    Log.e(TAG, "debuglogging start11");
                    SensorOkuma(sensorEvent);
                      debugLogFileWriter.append(

                                    + d.timestamp + ", "

                                    + d.acc[0] + ", " + d.acc[1] + ", " + d.acc[2] + ", "+
                //                    + d.gyr[0] + ", " + d.gyr[1] + ", " + d.gyr[2] + ", "
                //                    + d.mag[0] + ", " + d.mag[1] + ", " + d.mag[2] + ", "
                //                    + d.quat[0] + ", " + d.quat[1] + ", " + d.quat[2] + ", " + d.quat[3] + ", "
                //                    + d.pressure + ", "
                //                    + d.temperature+
                                  "\n");


                }
                status = true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                statusMesg = e.getMessage();
            }
        }
        return status;
    }

    public void SensorOkuma(SensorEvent sensorEvent){

        switch (sensorEvent.sensor.getStringType()){
            case Sensor.STRING_TYPE_ACCELEROMETER:
                Log.e(TAG, " ACCELEROMETER init1");
                accelx=  sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    accely=  sensorEvent.values[1];
                    accelz=  sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_MAGNETIC_FIELD:
                magnex=  sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    magney=  sensorEvent.values[1];
                    magnez=  sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_GYROSCOPE:
                gyrosx=sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    gyrosy=sensorEvent.values[1];
                    gyrosz=sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_LIGHT:
                light=sensorEvent.values[0];
                break;
            case Sensor.STRING_TYPE_PRESSURE:
                pressure=sensorEvent.values[0];
                Log.d(TAG, Calendar.getInstance().getTime().toString());
                Log.d(TAG, String.valueOf((Calendar.getInstance().getTimeInMillis())));
                break;
        }

    }public void onSensorChanged(FileOutputStream logFileStream) {
        Log.e(TAG, " onSensorChanged start1");
        switch (sensorEvent.sensor.getStringType()){
            case Sensor.STRING_TYPE_ACCELEROMETER:
                Log.e(TAG, " ACCELEROMETER init1");
                accelx= (long) sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    accely= (long) sensorEvent.values[1];
                    accelz= (long) sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_MAGNETIC_FIELD:
                magnex= (long) sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    magney= (long) sensorEvent.values[1];
                    magnez= (long) sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_GYROSCOPE:
                gyrosx=sensorEvent.values[0];
                if (sensorEvent.values.length > 1)
                {
                    gyrosy=sensorEvent.values[1];
                    gyrosz=sensorEvent.values[2];
                }
                break;
            case Sensor.STRING_TYPE_LIGHT:
                light=sensorEvent.values[0];
                break;
            case Sensor.STRING_TYPE_PRESSURE:
                pressure=sensorEvent.values[0];
                Log.d(TAG, Calendar.getInstance().getTime().toString());
                Log.d(TAG, String.valueOf((Calendar.getInstance().getTimeInMillis())));
                break;
        }
    }
}
