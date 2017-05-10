package com.exclusivetrace.exclusivetrace.map;

import android.graphics.PointF;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zxy94400 on 2016/3/21.
 */
public class MaskMapRender implements CustomRenderer {

    private boolean isNeedCalPoint = true;
    private float[] translate_vector = new float[4];
    public static float SCALE = 0.005F;// 缩放暂时使用这个

    private LatLng center = new LatLng(39.90403, 116.407525);// 北京市经纬度

    private AMap aMap;

    public MaskMapRender(AMap aMap) {
        this.aMap = aMap;

        aMap.addMarker(new MarkerOptions().position(center));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));

        aMap.showMapText(true);
        aMap.showBuildings(true);

        //index
        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(indices.length * 2);
        byteBuffer2.order(ByteOrder.nativeOrder());
        mIndexBuffer = byteBuffer2.asShortBuffer();
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);

        //顶点坐标
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }


    private int width;
    private int height;

    public FloatBuffer mVertexBuffer;
    public ShortBuffer mIndexBuffer;

    float vertices[] = {
            -1, -1, 1.0f,
            1, -1, 1.0f,
            -1, 1, 1.0f,
            1, 1, 1.0f,
    };

    short indices[] = {
            0, 1, 3,
            0, 3, 2
    };

    @Override
    public void onDrawFrame(GL10 gl) {
        //1.直接绘制四边形，然后缩放到和整个地图一样大，仰角在45°是 天空会显示出来
        gl.glPushMatrix();

        //平移到地图指定位置
        gl.glTranslatef(translate_vector[0], translate_vector[1], translate_vector[2]);
        //缩放物体大小适应地图
        gl.glScalef(width / 2.0f, height / 2.0f, 0);

        gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
        //关闭纹理绘图---刚刚少了这个
        gl.glDisable(GL10.GL_TEXTURE_2D);
        //启用混合模式---刚刚少了这个
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glVertexPointer(3, gl.GL_FLOAT, 0, mVertexBuffer);

        gl.glColor4f(1, 0, 0, 0.2f);

        gl.glDrawElements(gl.GL_TRIANGLES, indices.length, gl.GL_UNSIGNED_SHORT, mIndexBuffer);

        gl.glDisableClientState(gl.GL_VERTEX_ARRAY);

        gl.glDisable(GL10.GL_BLEND);

        gl.glPopMatrix();
        gl.glFlush();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void OnMapReferencechanged() {
        calScaleAndTranslate();
    }

    private void calScaleAndTranslate() {
        // 坐标会变化，重新计算计算偏移
        PointF pointF = aMap.getProjection().toOpenGLLocation(center);

        translate_vector[0] = pointF.x;
        translate_vector[1] = pointF.y;
        translate_vector[2] = 0;

        //重新计算缩放比例
        LatLng latLng2 = new LatLng(center.latitude + 0.001, center.longitude + 0.001);
        PointF pointF2 = aMap.getProjection().toOpenGLLocation(latLng2);
        double _x = Math.abs((pointF.x - pointF2.x));
        double _y = Math.abs((pointF.y - pointF2.y));
        SCALE = (float) Math.sqrt((_x * _x + _y * _y));

        isNeedCalPoint = true;
    }
}
