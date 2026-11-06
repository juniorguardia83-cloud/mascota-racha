package com.mascotaracha;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Mascota 3D con luz: perrito, gatito, conejito y pandita armados con elipsoides y conos. */
public class PetRenderer implements GLSurfaceView.Renderer {
  FloatBuffer sph, coneP, coneN;
  int program, aPos, aNrm, uMvp, uRot, uInv, uCol, uShine;
  volatile boolean touched = false;
  volatile String animal = "Perrito";
  volatile int outfit = -1;
  float t, jump, by, eb = 1f;
  final float[] proj = new float[16], view = new float[16], vp = new float[16], base = new float[16], gRot = new float[16];
  final float[] local = new float[16], rot = new float[16], tmp = new float[16], model = new float[16], mvp = new float[16], rotW = new float[16];

  public PetRenderer() {
    int lat = 18, lon = 24;
    float[] a = new float[lat * 2 * (lon + 1) * 3];
    int k = 0;
    for (int i = 0; i < lat; i++) for (int j = 0; j <= lon; j++) for (int row = 0; row < 2; row++) {
      double th = Math.PI * (i + row) / lat, ph = 2 * Math.PI * j / lon;
      a[k++] = (float) (Math.sin(th) * Math.cos(ph));
      a[k++] = (float) Math.cos(th);
      a[k++] = (float) (Math.sin(th) * Math.sin(ph));
    }
    sph = fb(a);
    int n = 24;
    float[] p = new float[n * 9], nm = new float[n * 9];
    k = 0;
    for (int i = 0; i < n; i++) {
      double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n, am = (a0 + a1) / 2;
      float[] vs = {0, 1, 0, (float) Math.cos(a0), -1, (float) Math.sin(a0), (float) Math.cos(a1), -1, (float) Math.sin(a1)};
      float[] ns = {(float) Math.cos(am), .5f, (float) Math.sin(am), (float) Math.cos(a0), .5f, (float) Math.sin(a0), (float) Math.cos(a1), .5f, (float) Math.sin(a1)};
      for (int j = 0; j < 9; j++) { p[k] = vs[j]; nm[k] = ns[j]; k++; }
    }
    coneP = fb(p);
    coneN = fb(nm);
  }

  static FloatBuffer fb(float[] a) {
    FloatBuffer f = ByteBuffer.allocateDirect(a.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    f.put(a).position(0);
    return f;
  }

  public void onSurfaceCreated(GL10 g, EGLConfig c) {
    GLES20.glClearColor(.91f, .86f, 1f, 1f);
    String vs = "attribute vec3 a; attribute vec3 nr; uniform mat4 m; uniform mat4 r; uniform vec3 inv; varying vec3 n;"
      + "void main(){ gl_Position=m*vec4(a,1.0); n=(r*vec4(nr*inv,0.0)).xyz; }";
    String fs = "precision mediump float; uniform vec4 c; uniform float shine; varying vec3 n;"
      + "void main(){ vec3 N=normalize(n); vec3 L=normalize(vec3(-0.4,0.7,0.9)); float d=max(dot(N,L),0.0);"
      + "float rim=pow(1.0-max(N.z,0.0),3.0)*0.12; vec3 H=normalize(L+vec3(0.0,0.0,1.0));"
      + "float s=pow(max(dot(N,H),0.0),28.0)*shine; gl_FragColor=vec4(c.rgb*(0.52+0.62*d)+rim+s,1.0); }";
    int v = shader(GLES20.GL_VERTEX_SHADER, vs), f = shader(GLES20.GL_FRAGMENT_SHADER, fs);
    program = GLES20.glCreateProgram();
    GLES20.glAttachShader(program, v);
    GLES20.glAttachShader(program, f);
    GLES20.glLinkProgram(program);
    aPos = GLES20.glGetAttribLocation(program, "a");
    aNrm = GLES20.glGetAttribLocation(program, "nr");
    uMvp = GLES20.glGetUniformLocation(program, "m");
    uRot = GLES20.glGetUniformLocation(program, "r");
    uInv = GLES20.glGetUniformLocation(program, "inv");
    uCol = GLES20.glGetUniformLocation(program, "c");
    uShine = GLES20.glGetUniformLocation(program, "shine");
  }

  int shader(int type, String s) {
    int x = GLES20.glCreateShader(type);
    GLES20.glShaderSource(x, s);
    GLES20.glCompileShader(x);
    return x;
  }

  public void onSurfaceChanged(GL10 g, int w, int h) {
    GLES20.glViewport(0, 0, w, h);
    float asp = (float) w / h;
    Matrix.frustumM(proj, 0, -asp, asp, -1, 1, 2, 20);
    Matrix.setLookAtM(view, 0, 0, 0, 7, 0, 0, 0, 0, 1, 0);
    Matrix.multiplyMM(vp, 0, proj, 0, view, 0);
  }

  public void onDrawFrame(GL10 g) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    t = (System.currentTimeMillis() % 1000000) / 1000f;
    if (touched) { jump = 1f; touched = false; }
    if (jump > 0) jump = Math.max(0f, jump - .035f);
    float sway = (float) Math.sin(t * .8) * 20f;
    by = (float) Math.sin(t * 2.5) * .04f + (jump > 0 ? (float) Math.sin((1 - jump) * Math.PI) * .55f : 0f);
    eb = (t % 3.4f) < .14f ? .12f : 1f;
    Matrix.setIdentityM(gRot, 0);
    Matrix.rotateM(gRot, 0, sway, 0, 1, 0);
    GLES20.glUseProgram(program);
    setBase(by);
    build();
  }

  void setBase(float yoff) {
    float s = animal.equals("Conejito") ? 1.12f : 1.3f;
    Matrix.setIdentityM(base, 0);
    Matrix.translateM(base, 0, 0, yoff - .15f, 0);
    Matrix.multiplyMM(tmp, 0, base, 0, gRot, 0);
    System.arraycopy(tmp, 0, base, 0, 16);
    Matrix.scaleM(base, 0, s, s, s);
  }

  void part(boolean cone, float x, float y, float z, float sx, float sy, float sz, float rx, float rz, float r, float g, float b, float sh) {
    Matrix.setIdentityM(rot, 0);
    Matrix.rotateM(rot, 0, rz, 0, 0, 1);
    Matrix.rotateM(rot, 0, rx, 1, 0, 0);
    Matrix.setIdentityM(local, 0);
    Matrix.translateM(local, 0, x, y, z);
    Matrix.multiplyMM(tmp, 0, local, 0, rot, 0);
    Matrix.multiplyMM(model, 0, base, 0, tmp, 0);
    Matrix.scaleM(model, 0, sx, sy, sz);
    Matrix.multiplyMM(mvp, 0, vp, 0, model, 0);
    Matrix.multiplyMM(rotW, 0, gRot, 0, rot, 0);
    GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
    GLES20.glUniformMatrix4fv(uRot, 1, false, rotW, 0);
    GLES20.glUniform3f(uInv, 1f / sx, 1f / sy, 1f / sz);
    GLES20.glUniform4f(uCol, r, g, b, 1f);
    GLES20.glUniform1f(uShine, sh);
    GLES20.glEnableVertexAttribArray(aPos);
    GLES20.glEnableVertexAttribArray(aNrm);
    if (cone) {
      coneP.position(0); coneN.position(0);
      GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, coneP);
      GLES20.glVertexAttribPointer(aNrm, 3, GLES20.GL_FLOAT, false, 0, coneN);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 72);
    } else {
      sph.position(0);
      GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, sph);
      GLES20.glVertexAttribPointer(aNrm, 3, GLES20.GL_FLOAT, false, 0, sph);
      for (int strip = 0; strip < 18; strip++) GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, strip * 50, 50);
    }
    GLES20.glDisableVertexAttribArray(aPos);
    GLES20.glDisableVertexAttribArray(aNrm);
  }

  void E(float x, float y, float z, float sx, float sy, float sz, float[] c) { part(false, x, y, z, sx, sy, sz, 0, 0, c[0], c[1], c[2], .2f); }
  void S(float x, float y, float z, float sx, float sy, float sz, float[] c) { part(false, x, y, z, sx, sy, sz, 0, 0, c[0], c[1], c[2], 1f); }
  void R(float x, float y, float z, float sx, float sy, float sz, float rx, float rz, float[] c) { part(false, x, y, z, sx, sy, sz, rx, rz, c[0], c[1], c[2], .2f); }
  void Cn(float x, float y, float z, float sx, float sy, float sz, float rz, float[] c) { part(true, x, y, z, sx, sy, sz, 0, rz, c[0], c[1], c[2], .2f); }

  void eye(float x, float y, float z, float s, float[] ir, float[] pu, boolean slit) {
    float[] wh = new float[]{1f, 1f, 1f};
    E(x, y, z, .19f * s, .23f * s * eb, .13f * s, ir);
    float pw = slit ? .05f : .11f;
    S(x * 1.03f, y - .02f * s, z + .06f * s, pw * s, .15f * s * eb, .08f * s, pu);
    S(x * .9f, y + .09f * s * eb, z + .12f * s, .045f * s, .045f * s * eb, .03f * s, wh);
  }

  void build() {
    boolean dog = animal.equals("Perrito"), cat = animal.equals("Gatito"), bun = animal.equals("Conejito"), pan = animal.equals("Pandita");
    float[] c = dog ? new float[]{.80f, .52f, .28f} : cat ? new float[]{.96f, .63f, .28f} : bun ? new float[]{.97f, .94f, .96f} : new float[]{.97f, .97f, .97f};
    float[] lt = dog ? new float[]{.98f, .89f, .74f} : cat ? new float[]{1f, .91f, .78f} : bun ? new float[]{1f, .82f, .88f} : new float[]{.88f, .88f, .9f};
    float[] dk = dog ? new float[]{.47f, .27f, .14f} : cat ? new float[]{.65f, .34f, .13f} : bun ? new float[]{.85f, .8f, .86f} : new float[]{.1f, .1f, .12f};
    float[] pk = new float[]{1f, .5f, .62f};
    float[] bl = new float[]{.08f, .07f, .1f};
    float[] wh = new float[]{1f, 1f, 1f};
    float[] gr = new float[]{.4f, .78f, .35f};
    float br = 1f + .02f * (float) Math.sin(t * 3);
    float w = (float) Math.sin(t * 10);

    setBase(0f);
    E(0, -1.8f, 0, 1.35f, .08f, 1.05f, new float[]{.78f, .7f, .9f});
    setBase(by);

    if (dog) {
      E(w * .18f, -.95f, -.95f, .15f, .15f, .4f, c);
      E(w * .36f, -.7f, -1.25f, .17f, .17f, .2f, dk);
    } else if (cat) {
      for (int i = 0; i < 6; i++) E(.55f + .12f * i + (float) Math.sin(t * 3 + i * .7f) * .05f * i, -1.1f + .3f * i, -.7f, .15f, .15f, .15f, i == 5 ? dk : c);
    } else if (bun) {
      E(0, -1.0f, -.95f, .3f, .3f, .3f, c);
    } else {
      E(0, -1.0f, -.9f, .2f, .2f, .2f, c);
    }

    E(0, -.8f, -.05f, .9f, .85f * br, .78f, c);
    E(0, -.78f, .3f, .58f, .6f, .5f, lt);
    float[] lg = pan ? dk : c;
    float[] pw = pan ? dk : lt;
    E(-.72f, -1.3f, -.15f, .32f, .4f, .46f, lg);
    E(.72f, -1.3f, -.15f, .32f, .4f, .46f, lg);
    E(-.42f, -1.35f, .4f, .27f, .4f, .3f, lg);
    E(.42f, -1.35f, .4f, .27f, .4f, .3f, lg);
    E(-.42f, -1.62f, .52f, .3f, .17f, .36f, pw);
    E(.42f, -1.62f, .52f, .3f, .17f, .36f, pw);

    float hx = cat ? 1.05f : bun ? .95f : 1f;
    float hy = cat ? .85f : .9f;
    E(0, .6f, .05f, hx, hy, .92f, c);

    if (dog) {
      R(-.95f, .5f, 0, .26f, .6f, .34f, 0, -12f, dk);
      R(.95f, .5f, 0, .26f, .6f, .34f, 0, 12f, dk);
      E(0, .36f, .86f, .5f, .38f, .44f, lt);
      eye(-.38f, .78f, .8f, 1f, wh, bl, false);
      eye(.38f, .78f, .8f, 1f, wh, bl, false);
      S(0, .52f, 1.25f, .2f, .14f, .13f, bl);
      E(0, .33f, 1.27f, .022f, .1f, .03f, bl);
      E(0, .05f, 1.1f, .13f, .07f, .18f, pk);
      E(-.7f, .32f, .62f, .15f, .1f, .08f, pk);
      E(.7f, .32f, .62f, .15f, .1f, .08f, pk);
    } else if (cat) {
      Cn(-.62f, 1.38f, 0, .34f, .45f, .18f, 18f, c);
      Cn(.62f, 1.38f, 0, .34f, .45f, .18f, -18f, c);
      Cn(-.6f, 1.35f, .14f, .2f, .3f, .1f, 18f, pk);
      Cn(.6f, 1.35f, .14f, .2f, .3f, .1f, -18f, pk);
      eye(-.4f, .78f, .8f, 1f, gr, bl, true);
      eye(.4f, .78f, .8f, 1f, gr, bl, true);
      E(-.16f, .32f, .9f, .22f, .15f, .13f, lt);
      E(.16f, .32f, .9f, .22f, .15f, .13f, lt);
      S(0, .5f, 1.0f, .11f, .08f, .06f, pk);
      E(-.7f, .32f, .62f, .15f, .1f, .08f, pk);
      E(.7f, .32f, .62f, .15f, .1f, .08f, pk);
      R(0, 1.36f, .5f, .05f, .2f, .06f, -45f, 0, dk);
      R(-.2f, 1.32f, .45f, .05f, .17f, .06f, -45f, 15f, dk);
      R(.2f, 1.32f, .45f, .05f, .17f, .06f, -45f, -15f, dk);
      for (int s = -1; s <= 1; s += 2) for (int j = -1; j <= 1; j++) R(s * .82f, .36f + j * .09f, .85f, .42f, .012f, .012f, 0, s * j * 12f, wh);
    } else if (bun) {
      R(-.4f, 1.9f, -.05f, .2f, .8f, .13f, 0, 8f, c);
      R(.4f, 1.9f, -.05f, .2f, .8f, .13f, 0, -8f, c);
      R(-.4f, 1.88f, .04f, .11f, .62f, .08f, 0, 8f, pk);
      R(.4f, 1.88f, .04f, .11f, .62f, .08f, 0, -8f, pk);
      eye(-.38f, .78f, .8f, 1f, wh, bl, false);
      eye(.38f, .78f, .8f, 1f, wh, bl, false);
      E(-.14f, .33f, .9f, .2f, .15f, .12f, lt);
      E(.14f, .33f, .9f, .2f, .15f, .12f, lt);
      S(0, .5f, .97f, .1f, .07f, .06f, pk);
      E(0, .36f, .96f, .02f, .1f, .03f, bl);
      E(-.06f, .2f, .96f, .07f, .12f, .04f, new float[]{.95f, .95f, .95f});
      E(.06f, .2f, .96f, .07f, .12f, .04f, new float[]{.95f, .95f, .95f});
      for (int s = -1; s <= 1; s += 2) for (int j = -1; j <= 1; j++) R(s * .78f, .34f + j * .09f, .8f, .4f, .012f, .012f, 0, s * j * 12f, dk);
    } else {
      E(-.78f, 1.38f, -.05f, .3f, .3f, .18f, dk);
      E(.78f, 1.38f, -.05f, .3f, .3f, .18f, dk);
      R(-.4f, .76f, .8f, .27f, .34f, .12f, 0, -25f, dk);
      R(.4f, .76f, .8f, .27f, .34f, .12f, 0, 25f, dk);
      eye(-.4f, .76f, .88f, .6f, wh, bl, false);
      eye(.4f, .76f, .88f, .6f, wh, bl, false);
      E(0, .4f, .8f, .38f, .3f, .3f, lt);
      S(0, .5f, 1.07f, .14f, .09f, .07f, bl);
      E(0, .36f, 1.08f, .02f, .09f, .03f, bl);
      E(-.7f, .3f, .62f, .13f, .09f, .07f, pk);
      E(.7f, .3f, .62f, .13f, .09f, .07f, pk);
    }

    if (outfit == 0) {
      E(-.2f, 1.5f, .15f, .22f, .14f, .1f, new float[]{1f, .35f, .6f});
      E(.2f, 1.5f, .15f, .22f, .14f, .1f, new float[]{1f, .35f, .6f});
      E(0, 1.5f, .2f, .1f, .1f, .1f, new float[]{.9f, .2f, .45f});
    } else if (outfit == 1) {
      float[] gold = new float[]{1f, .8f, .2f};
      S(0, 1.46f, .1f, .52f, .12f, .52f, gold);
      for (int i = 0; i < 5; i++) {
        float a = (float) (i * 2 * Math.PI / 5);
        Cn((float) Math.cos(a) * .42f, 1.7f, .1f + (float) Math.sin(a) * .42f, .12f, .2f, .12f, 0, gold);
      }
    } else if (outfit == 2) {
      float[] lens = new float[]{.06f, .06f, .09f};
      S(-.4f, .78f, .99f, .27f, .21f, .06f, lens);
      S(.4f, .78f, .99f, .27f, .21f, .06f, lens);
      E(0, .82f, 1.0f, .12f, .04f, .04f, lens);
    } else if (outfit == 3) {
      E(0, -.28f, .05f, .8f, .17f, .7f, new float[]{.9f, .2f, .25f});
      E(.45f, -.65f, .75f, .16f, .38f, .08f, new float[]{.8f, .15f, .2f});
    }
  }

  public void pet() { touched = true; }
  public void setPet(String p) { animal = p; }
  public void setOutfit(int i) { outfit = i; }
}
