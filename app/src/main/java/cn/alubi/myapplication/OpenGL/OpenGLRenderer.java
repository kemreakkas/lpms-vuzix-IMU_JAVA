package cn.alubi.myapplication.OpenGL;

/**
 * Created by birdy on 10/25/2015.
 */
import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    private Context context;   // Application context needed to read image (NEW)
    private Cube mCube;
    private float mCubeRotation;
    private float angle;
    private float[] axis = {1.0f, 0.0f, 0.0f};

    // Constructor
    public OpenGLRenderer(Context context) {
        this.context = context;   // Get the application context (NEW)
        mCube = new Cube();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);

        // Setup Texture, each time the surface is created (NEW)
        mCube.loadTexture(gl, context);    // Load image into Texture (NEW)
        gl.glEnable(GL10.GL_TEXTURE_2D);  // Enable texture (NEW)
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -5.5f);
        gl.glRotatef(angle, axis[0], axis[1], axis[2]);
        mCube.draw(gl);
        gl.glLoadIdentity();
        mCubeRotation -= 0.15f;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void updateRotation(float[] quat)
    {
        double rAngle = Math.acos(quat[0]) * 2;
        angle = (float)( rAngle * 180 / Math.PI);
        axis[0] =(float)( -quat[1] / Math.sin(rAngle / 2));
        axis[1] =(float)( -quat[2] / Math.sin(rAngle / 2));
        axis[2] =(float)( -quat[3] / Math.sin(rAngle / 2));
    }
}