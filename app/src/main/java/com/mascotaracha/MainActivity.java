package com.mascotaracha;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** Native Android pet-care game. The pet is rendered as real OpenGL ES 3D geometry. */
public class MainActivity extends Activity {
  static final String URL="https://prbxumnozkapgnbhxkby.supabase.co";
  static final String KEY="sb_publishable_7xZa_3v3AYsRiyGMi0Mp9A_xj39SXjS";
  SharedPreferences prefs; JSONObject state; TextView status,stats,missionText; PetView petView; EditText emailInput,passwordInput;
  String[] missions={"🍎 Dale su comida favorita","💧 Llena su recipiente de agua","🎾 Juega con tu mascota","💗 Acaríciala con cariño","🧼 Limpia su casita","🎵 Hazla bailar un ratito"};
  String[] clothes={"🎀 Lazo rosa","👑 Corona real","🕶️ Gafas cool","🧣 Bufanda suave"};
  int missionIndex; LinearLayout root,missionBox,shopBox;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("petgame",0);state=load();missionIndex=Math.abs(day().hashCode())%missions.length;buildUI();syncCloud();}
  String day(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
  JSONObject load(){try{return new JSONObject(prefs.getString("state","{}"));}catch(Exception e){return new JSONObject();}}
  int num(String k){return state.optInt(k,0);} String pet(){return state.optString("pet","Perrito");}
  void put(String k,Object v){try{state.put(k,v);}catch(Exception e){}save();}
  void save(){prefs.edit().putString("state",state.toString()).apply();refreshWidget();syncCloud();}
  void refreshWidget(){Intent i=new Intent(this,PetWidgetProvider.class);i.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);int[] ids=android.appwidget.AppWidgetManager.getInstance(this).getAppWidgetIds(new android.content.ComponentName(this,PetWidgetProvider.class));i.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);sendBroadcast(i);}
  void buildUI(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,12,18,18);root.setBackgroundColor(Color.rgb(248,244,255));
    ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);
    TextView title=txt("🐾 MIMOPETS",25,true);root.addView(title);
    stats=txt("",17,true);root.addView(stats);
    TextView tip=txt("Tu compañerito necesita tu cariño cada día",14,false);root.addView(tip);
    petView=new PetView(this);
    LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,dp(285));gp.topMargin=dp(10);root.addView(petView,gp);
    petView.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){petView.pet();return true;}return true;});
    TextView touch=txt("👆 Toca a tu mascota para acariciarla",14,true);root.addView(touch);
    section("🐾 Elige tu mascota");
    LinearLayout choices=new LinearLayout(this);
    for(String p:new String[]{"Perrito","Gatito","Conejito","Pandita"}){Button b=btn(p);choices.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{put("pet",p);petView.setPet(p);refresh();});}root.addView(choices);
    section("💖 Cuida a tu mascota");
    LinearLayout care=new LinearLayout(this);
    String[] careNames={"🍖 Dar comida","🎾 Jugar","🛁 Bañar"};
    String[] careKeys={"food","fun","clean"};
    for(int i=0;i<careNames.length;i++){final String key=careKeys[i];Button action=btn(careNames[i]);care.addView(action,new LinearLayout.LayoutParams(0,dp(50),1));action.setOnClickListener(v->care(key));}
    root.addView(care);
    TextView careStats=txt("🍗 Hambre   🎾 Diversión   🫧 Limpieza",13,false);root.addView(careStats);
    section("🔥 Tu racha diaria");TextView streak=txt("Completa misiones para mantener tu racha",15,true);root.addView(streak);
    section("🎯 Misiones de hoy");missionText=txt("",17,true);root.addView(missionText);
    missionBox=new LinearLayout(this);missionBox.setOrientation(1);root.addView(missionBox);
    Button complete=btn("¡Misión completada!  +10 🪙");root.addView(complete);complete.setOnClickListener(v->completeMission());
    section("🛍️ Tienda de ropa y accesorios");shopBox=new LinearLayout(this);shopBox.setOrientation(1);root.addView(shopBox);
    for(int i=0;i<clothes.length;i++){final int ix=i;Button b=btn(clothes[i]+"  ·  "+(20+i*10)+" monedas");shopBox.addView(b);b.setOnClickListener(v->buy(ix));}
    section("🏡 Mi casita");TextView house=txt("Cuida, juega y vuelve cada día. Tu mascota te espera ❤️",15,false);root.addView(house);
    section("☁️ Guarda tu mascota en tu cuenta");
    emailInput=new EditText(this);emailInput.setSingleLine(true);emailInput.setHint("Tu correo electrónico");emailInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);root.addView(emailInput);
    passwordInput=new EditText(this);passwordInput.setSingleLine(true);passwordInput.setHint("Contraseña (mínimo 6 caracteres)");passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);root.addView(passwordInput);
    LinearLayout accountButtons=new LinearLayout(this);
    Button register=btn("Crear cuenta");accountButtons.addView(register,new LinearLayout.LayoutParams(0,dp(48),1));
    Button login=btn("Iniciar sesión");accountButtons.addView(login,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(accountButtons);
    register.setOnClickListener(v->account("signup"));login.setOnClickListener(v->account("login"));
    status=txt("Guardando tus avances…",12,false);root.addView(status);refresh();
  }
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(48,35,68));if(bold)t.setTypeface(null,1);t.setPadding(4,dp(7),4,dp(7));return t;}
  void section(String s){TextView t=txt(s,19,true);t.setPadding(4,dp(16),4,dp(5));root.addView(t);}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(139,92,246)));return b;}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  void refresh(){if(stats==null)return;stats.setText("🔥 "+num("streak")+" días     🪙 "+num("coins")+" monedas    ❤️ "+state.optInt("fun",75)+"%");missionText.setText("Hoy: "+missions[missionIndex]);petView.setPet(pet());}
  void care(String key){int value=Math.min(100,state.optInt(key,55)+25);put(key,value);petView.pet();String msg=key.equals("food")?"¡Qué rico! Tu mascota comió 🍖":key.equals("fun")?"¡Se divirtieron muchísimo! 🎾":"¡Quedó limpita y feliz! 🫧";toast(msg);refresh();}
  void completeMission(){String today=day();if(state.optString("done","").equals(today)){toast("¡Ya completaste la misión de hoy!");return;}String last=state.optString("last","");int streak=last.equals(yesterday())?num("streak")+1:1;put("streak",streak);put("coins",num("coins")+10);put("last",today);put("done",today);refresh();toast("¡Muy bien! +10 monedas y racha de "+streak+" días 🔥");}
  String yesterday(){Calendar c=Calendar.getInstance();c.add(Calendar.DATE,-1);return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());}
  void buy(int i){int cost=20+i*10;if(num("coins")<cost){toast("Te faltan monedas. Completa misiones ❤️");return;}put("coins",num("coins")-cost);put("outfit",clothes[i]);petView.setOutfit(i);refresh();toast("¡Tu mascota estrenó "+clothes[i]+"!");}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  void account(String mode){
    String email=emailInput.getText().toString().trim(),password=passwordInput.getText().toString();
    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.length()<6){toast("Escribe un correo válido y una contraseña de 6 caracteres o más.");return;}
    if(status!=null)status.setText("Conectando tu cuenta…");
    new Thread(()->{try{
      JSONObject body=new JSONObject();body.put("email",email);body.put("password",password);
      String endpoint=mode.equals("signup")?"/auth/v1/signup":"/auth/v1/token?grant_type=password";
      HttpURLConnection c=(HttpURLConnection)new URL(URL+endpoint).openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(15000);c.setReadTimeout(15000);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(body.toString().getBytes("UTF-8"));
      int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception("No se pudo entrar: "+response);
      JSONObject result=new JSONObject(response);String access=result.optString("access_token","");JSONObject user=result.optJSONObject("user");
      if(access.length()==0||user==null){runOnUiThread(()->{if(status!=null)status.setText("Revisa tu correo para confirmar la cuenta y luego inicia sesión.");});return;}
      String uid=user.getString("id");prefs.edit().putString("token",access).putString("uid",uid).apply();
      HttpURLConnection get=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?id=eq."+uid+"&select=data").openConnection();get.setRequestProperty("apikey",KEY);get.setRequestProperty("Authorization","Bearer "+access);get.setConnectTimeout(15000);get.setReadTimeout(15000);
      if(get.getResponseCode()==200){JSONArray rows=new JSONArray(readResponse(get));if(rows.length()>0){JSONObject cloud=rows.getJSONObject(0).optJSONObject("data");if(cloud!=null){state=cloud;prefs.edit().putString("state",state.toString()).apply();}}}
      refreshWidget();syncCloud();runOnUiThread(()->{refresh();if(status!=null)status.setText("✅ Cuenta conectada. Tu mascota está guardada en la nube.");toast("¡Cuenta conectada! ❤️");});
    }catch(Exception e){String msg=e.getMessage()==null?"Error de conexión":e.getMessage();runOnUiThread(()->{if(status!=null)status.setText("No se pudo conectar: "+msg);});}}).start();
  }
  String readResponse(HttpURLConnection c)throws Exception{InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();if(in==null)return "";ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}
  void syncCloud(){new Thread(()->{try{
    String token=prefs.getString("token",null);if(token==null){JSONObject a=new JSONObject();a.put("data",new JSONObject());HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/signup").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(a.toString().getBytes("UTF-8"));JSONObject res=new JSONObject(read(c));token=res.optString("access_token",null);if(token==null)throw new Exception("Anonymous auth unavailable");prefs.edit().putString("token",token).putString("uid",res.getJSONObject("user").getString("id")).apply();}
    String uid=prefs.getString("uid","");JSONObject row=new JSONObject();row.put("id",uid);row.put("data",state);row.put("updated_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).format(new Date()));
    HttpURLConnection c=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?on_conflict=id").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","resolution=merge-duplicates");c.getOutputStream().write(new JSONArray().put(row).toString().getBytes("UTF-8"));int code=c.getResponseCode();if(code<200||code>=300)throw new Exception("Supabase HTTP "+code);runOnUiThread(()->{if(status!=null)status.setText("☁️ Avances sincronizados con Supabase");});
  }catch(Exception e){runOnUiThread(()->{if(status!=null)status.setText("📱 Guardado en este teléfono · nube pendiente");});}}).start();}
  String read(HttpURLConnection c)throws Exception{InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}

}
