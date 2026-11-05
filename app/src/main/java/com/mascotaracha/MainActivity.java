package com.mascotaracha;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.opengl.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Native Android pet-care game. The pet is rendered as real OpenGL ES 3D geometry. */
public class MainActivity extends Activity {
  static final String URL="https://prbxumnozkapgnbhxkby.supabase.co";
  static final String KEY="sb_publishable_7xZa_3v3AYsRiyGMi0Mp9A_xj39SXjS";
  SharedPreferences prefs; JSONObject state; TextView status,stats,missionText; PetRenderer renderer;
  String[] missions={"🍎 Dale su comida favorita","💧 Llena su recipiente de agua","🎾 Juega con tu mascota","💗 Acaríciala con cariño","🧼 Limpia su casita","🎵 Hazla bailar un ratito"};
  String[] clothes={"🎀 Lazo rosa","👑 Corona real","🕶️ Gafas cool","🧣 Bufanda suave"};
  int missionIndex; LinearLayout root,missionBox,shopBox;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("petgame",0);state=load();missionIndex=Math.abs(day().hashCode())%missions.length;buildUI();syncCloud();}
  String day(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
  JSONObject load(){try{return new JSONObject(prefs.getString("state","{}"));}catch(Exception e){return new JSONObject();}}
  int num(String k){return state.optInt(k,0);} String pet(){return state.optString("pet","Perrito");}
  void put(String k,Object v){try{state.put(k,v);}catch(Exception e){}save();}
  void save(){prefs.edit().putString("state",state.toString()).apply();syncCloud();}
  void buildUI(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,12,18,18);root.setBackgroundColor(Color.rgb(248,244,255));
    ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);
    TextView title=txt("🐾 MASCOTA RACHA",25,true);root.addView(title);
    stats=txt("",17,true);root.addView(stats);
    TextView tip=txt("Tu compañerito 3D necesita tu cariño cada día",14,false);root.addView(tip);
    GLSurfaceView gl=new GLSurfaceView(this);gl.setEGLContextClientVersion(2);renderer=new PetRenderer();gl.setRenderer(renderer);gl.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,dp(285));gp.topMargin=dp(10);root.addView(gl,gp);
    gl.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){renderer.pet();return true;}return true;});
    TextView touch=txt("👆 Toca a tu mascota para acariciarla",14,true);root.addView(touch);
    section("🐾 Elige tu mascota");
    LinearLayout choices=new LinearLayout(this);
    for(String p:new String[]{"Perrito","Gatito","Conejito","Pandita"}){Button b=btn(p);choices.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{put("pet",p);renderer.setPet(p);refresh();});}root.addView(choices);
    section("🔥 Tu racha diaria");TextView streak=txt("Completa misiones para mantener tu racha",15,true);root.addView(streak);
    section("🎯 Misiones de hoy");missionText=txt("",17,true);root.addView(missionText);
    missionBox=new LinearLayout(this);missionBox.setOrientation(1);root.addView(missionBox);
    Button complete=btn("¡Misión completada!  +10 🪙");root.addView(complete);complete.setOnClickListener(v->completeMission());
    section("🛍️ Tienda de ropa y accesorios");shopBox=new LinearLayout(this);shopBox.setOrientation(1);root.addView(shopBox);
    for(int i=0;i<clothes.length;i++){final int ix=i;Button b=btn(clothes[i]+"  ·  "+(20+i*10)+" monedas");shopBox.addView(b);b.setOnClickListener(v->buy(ix));}
    section("🏡 Mi casita");TextView house=txt("Cuida, juega y vuelve cada día. Tu mascota te espera ❤️",15,false);root.addView(house);
    status=txt("Guardando tus avances…",12,false);root.addView(status);refresh();
  }
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(48,35,68));if(bold)t.setTypeface(null,1);t.setPadding(4,dp(7),4,dp(7));return t;}
  void section(String s){TextView t=txt(s,19,true);t.setPadding(4,dp(16),4,dp(5));root.addView(t);}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(139,92,246)));return b;}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  void refresh(){if(stats==null)return;stats.setText("🔥 "+num("streak")+" días     🪙 "+num("coins")+" monedas");missionText.setText("Hoy: "+missions[missionIndex]);renderer.setPet(pet());}
  void completeMission(){String today=day();if(state.optString("done","").equals(today)){toast("¡Ya completaste la misión de hoy!");return;}String last=state.optString("last","");int streak=last.equals(yesterday())?num("streak")+1:1;put("streak",streak);put("coins",num("coins")+10);put("last",today);put("done",today);refresh();toast("¡Muy bien! +10 monedas y racha de "+streak+" días 🔥");}
  String yesterday(){Calendar c=Calendar.getInstance();c.add(Calendar.DATE,-1);return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());}
  void buy(int i){int cost=20+i*10;if(num("coins")<cost){toast("Te faltan monedas. Completa misiones ❤️");return;}put("coins",num("coins")-cost);put("outfit",clothes[i]);renderer.setOutfit(i);refresh();toast("¡Tu mascota estrenó "+clothes[i]+"!");}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  void syncCloud(){new Thread(()->{try{
    String token=prefs.getString("token",null);if(token==null){JSONObject a=new JSONObject();a.put("data",new JSONObject());HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/signup").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(a.toString().getBytes("UTF-8"));JSONObject res=new JSONObject(read(c));token=res.optString("access_token",null);if(token==null)throw new Exception("Anonymous auth unavailable");prefs.edit().putString("token",token).putString("uid",res.getJSONObject("user").getString("id")).apply();}
    String uid=prefs.getString("uid","");JSONObject row=new JSONObject();row.put("id",uid);row.put("data",state);row.put("updated_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).format(new Date()));
    HttpURLConnection c=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?on_conflict=id").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","resolution=merge-duplicates");c.getOutputStream().write(new JSONArray().put(row).toString().getBytes("UTF-8"));int code=c.getResponseCode();if(code<200||code>=300)throw new Exception("Supabase HTTP "+code);runOnUiThread(()->{if(status!=null)status.setText("☁️ Avances sincronizados con Supabase");});
  }catch(Exception e){runOnUiThread(()->{if(status!=null)status.setText("📱 Guardado en este teléfono · nube pendiente");});}}).start();}
  String read(HttpURLConnection c)throws Exception{InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}

  static class PetRenderer implements GLSurfaceView.Renderer{
    FloatBuffer vertices;int program,pos,color;float angle=0,bounce=0;long last;volatile boolean touched=false;volatile String animal="Perrito";volatile int outfit=-1;
    final float[] projection=new float[16],view=new float[16],model=new float[16],mvp=new float[16];
    PetRenderer(){float[] v=sphere();vertices=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();vertices.put(v).position(0);}
    float[] sphere(){int lat=18,lon=24;float[] a=new float[(lat+1)*(lon+1)*3];int k=0;for(int i=0;i<=lat;i++){double th=Math.PI*i/lat;for(int j=0;j<=lon;j++){double ph=2*Math.PI*j/lon;a[k++]=(float)(Math.sin(th)*Math.cos(ph));a[k++]=(float)Math.cos(th);a[k++]=(float)(Math.sin(th)*Math.sin(ph));}}return a;}
    public void onSurfaceCreated(GL10 g,EGLConfig c){GLES20.glClearColor(.91f,.86f,1f,1f);String vs="attribute vec4 a; uniform mat4 m; void main(){gl_Position=m*a;}";String fs="precision mediump float; uniform vec4 c; void main(){gl_FragColor=c;}";int v=shader(GLES20.GL_VERTEX_SHADER,vs),f=shader(GLES20.GL_FRAGMENT_SHADER,fs);program=GLES20.glCreateProgram();GLES20.glAttachShader(program,v);GLES20.glAttachShader(program,f);GLES20.glLinkProgram(program);pos=GLES20.glGetAttribLocation(program,"a");color=GLES20.glGetUniformLocation(program,"c");}
    int shader(int type,String s){int x=GLES20.glCreateShader(type);GLES20.glShaderSource(x,s);GLES20.glCompileShader(x);return x;}
    public void onSurfaceChanged(GL10 g,int w,int h){GLES20.glViewport(0,0,w,h);Matrix.frustumM(projection,0,-(float)w/h,(float)w/h,-1,1,2,20);Matrix.setLookAtM(view,0,0,0,7,0,0,0,0,1,0);}
    public void onDrawFrame(GL10 g){GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);GLES20.glEnable(GLES20.GL_DEPTH_TEST);angle+=.35f;bounce=(float)Math.sin(System.currentTimeMillis()/280.0)*.08f+(touched?.22f:0);touched=false;
      draw(0,-.55f,0,1.0f,.9f,.65f,.75f,.48f,.42f);draw(0,.48f,0,1.0f,.56f,.56f,.55f,.48f,.44f);
      float ear=animal.equals("Conejito")?.42f:.22f;draw(-.42f,1.0f,0,1,.22f,ear,.16f,.13f,.12f);draw(.42f,1.0f,0,1,.22f,ear,.16f,.13f,.12f);
      draw(-.2f,.55f,.4f,1,.09f,.12f,.05f,.03f,.04f);draw(.2f,.55f,.4f,1,.09f,.12f,.05f,.03f,.04f);
      draw(0,.28f,.43f,1,.12f,.07f,.05f,.06f,.03f);
      draw(-.52f,-.95f,0,1,.22f,.24f,.22f,.72f,.48f);draw(.52f,-.95f,0,1,.22f,.24f,.22f,.72f,.48f);
      if(outfit>=0)draw(0,-.45f,.5f,1,.52f,.28f,.1f,1,.72f,.2f);
    }
    void draw(float x,float y,float z,float sx,float sy,float sz,float r,float gr,float b){Matrix.setIdentityM(model,0);Matrix.translateM(model,0,x,y+bounce,z);Matrix.rotateM(model,0,angle*.08f,0,1,0);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(mvp,0,view,0,model,0);Matrix.multiplyMM(mvp,0,projection,0,mvp,0);GLES20.glUseProgram(program);GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program,"m"),1,false,mvp,0);GLES20.glUniform4f(color,r,gr,b,1);GLES20.glEnableVertexAttribArray(pos);vertices.position(0);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,0,vertices);GLES20.glDrawArrays(GLES20.GL_POINTS,0,0);GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,0);GLES20.glDisableVertexAttribArray(pos);}
    void pet(){touched=true;}void setPet(String p){animal=p;}void setOutfit(int i){outfit=i;}
  }
}
