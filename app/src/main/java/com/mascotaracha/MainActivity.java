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
  String[] clothes={"🎀 Lazo rosa","👑 Corona real","🕶️ Gafas cool","🧣 Bufanda suave","🌸 Flor","⭐ Diadema estrella","🦋 Alas mágicas","💎 Collar brillante","🐝 Disfraz abeja","🧢 Gorra urbana","💗 Moño corazón","🌈 Capa arcoíris"};
  int[] prices={20,30,40,50,35,45,70,60,55,40,65,80};
  int missionIndex; LinearLayout root,missionBox,shopBox,inventoryBox,roomBox; ScrollView pageScroll;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("petgame",0);state=load();missionIndex=Math.abs(day().hashCode())%missions.length;buildUI();syncCloud();}
  String day(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
  JSONObject load(){try{return new JSONObject(prefs.getString("state","{}"));}catch(Exception e){return new JSONObject();}}
  int num(String k){return state.optInt(k,0);} String pet(){return state.optString("pet","Perrito");}
  void put(String k,Object v){try{state.put(k,v);}catch(Exception e){}save();}
  void save(){prefs.edit().putString("state",state.toString()).apply();refreshWidget();syncCloud();}
  void refreshWidget(){Intent i=new Intent(this,PetWidgetProvider.class);i.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);int[] ids=android.appwidget.AppWidgetManager.getInstance(this).getAppWidgetIds(new android.content.ComponentName(this,PetWidgetProvider.class));i.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);sendBroadcast(i);}
  void buildUI(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,12,18,18);root.setBackgroundColor(Color.rgb(248,244,255));
    pageScroll=new ScrollView(this);pageScroll.addView(root);setContentView(pageScroll);
    LinearLayout quick=new LinearLayout(this);quick.setOrientation(LinearLayout.HORIZONTAL);quick.setBackgroundColor(Color.WHITE);
    String[] tabs={"🏡 Inicio","🐾 Mascotas","🛍️ Tienda","🎒 Mochila","🔥 Racha"};int[] targets={0,1,2,3,4};
    for(int k=0;k<tabs.length;k++){final int target=targets[k];Button nav=btn(tabs[k]);nav.setTextSize(10);quick.addView(nav,new LinearLayout.LayoutParams(0,dp(44),1));nav.setOnClickListener(v->{View dest=target==0?stats:target==1?petView:target==2?shopBox:target==3?inventoryBox:missionText;pageScroll.post(()->pageScroll.smoothScrollTo(0,dest.getTop()));});}root.addView(quick);
    TextView title=txt("🐾 MIMOPETS",25,true);root.addView(title);
    stats=txt("",17,true);root.addView(stats);
    TextView tip=txt("Tu compañerito necesita tu cariño cada día",14,false);root.addView(tip);
    petView=new PetView(this);
    LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-1,dp(285));gp.topMargin=dp(10);root.addView(petView,gp);
    petView.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){petView.pet();return true;}return true;});
    TextView touch=txt("👆 Toca a tu mascota para acariciarla",14,true);root.addView(touch);
    section("🏡 Habitaciones");roomBox=new LinearLayout(this);roomBox.setOrientation(LinearLayout.HORIZONTAL);root.addView(roomBox);
    String[] rooms={"🌷 Jardín","🛏️ Dormitorio","🛋️ Sala","🫧 Baño"};for(int r=0;r<rooms.length;r++){final int ix=r;Button rb=btn(rooms[r]);roomBox.addView(rb,new LinearLayout.LayoutParams(0,dp(46),1));rb.setOnClickListener(v->{put("room",ix);petView.setRoom(ix);toast("¡Entraste a "+rooms[ix]+"!");});}
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
    section("🔥 Tu racha diaria");TextView streak=txt("",17,true);root.addView(streak);
    android.widget.ProgressBar week=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);week.setMax(7);week.setProgress(num("weekProgress"));root.addView(week,new LinearLayout.LayoutParams(-1,dp(12)));
    root.addView(txt("L  M  X  J  V  S  D",13,true));
    section("🎯 Misiones de hoy");missionText=txt("",17,true);root.addView(missionText);
    missionBox=new LinearLayout(this);missionBox.setOrientation(1);root.addView(missionBox);
    Button complete=btn("¡Misión completada!  +10 🪙");root.addView(complete);complete.setOnClickListener(v->completeMission());
    section("🛍️ Tienda de ropa y accesorios");shopBox=new LinearLayout(this);shopBox.setOrientation(1);root.addView(shopBox);
    for(int i=0;i<clothes.length;i++){final int ix=i;Button b=btn(clothes[i]+"  ·  "+prices[i]+" 🪙");shopBox.addView(b);b.setOnClickListener(v->buy(ix));}
    section("🎒 Mi mochila · cosas compradas");inventoryBox=new LinearLayout(this);inventoryBox.setOrientation(1);root.addView(inventoryBox);
    section("🏡 Mi casita");TextView house=txt("Cuida, juega y vuelve cada día. Tu mascota te espera ❤️",15,false);root.addView(house);
    section("☁️ Guarda tu mascota en tu cuenta");
    emailInput=new EditText(this);emailInput.setSingleLine(true);emailInput.setHint("Tu correo electrónico");emailInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);root.addView(emailInput);
    passwordInput=new EditText(this);passwordInput.setSingleLine(true);passwordInput.setHint("Contraseña (mínimo 6 caracteres)");passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);root.addView(passwordInput);
    LinearLayout accountButtons=new LinearLayout(this);
    Button register=btn("Crear cuenta");accountButtons.addView(register,new LinearLayout.LayoutParams(0,dp(48),1));
    Button login=btn("Iniciar sesión");accountButtons.addView(login,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(accountButtons);
    register.setOnClickListener(v->account("signup"));login.setOnClickListener(v->account("login"));
    Button share=btn("📤 Compartir mi mascota");root.addView(share);share.setOnClickListener(v->sharePet());
    status=txt("Guardando tus avances…",12,false);root.addView(status);refresh();
  }
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(48,35,68));if(bold)t.setTypeface(null,1);t.setPadding(4,dp(7),4,dp(7));return t;}
  void section(String s){TextView t=txt(s,19,true);t.setPadding(4,dp(16),4,dp(5));root.addView(t);}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(139,92,246)));return b;}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  void refresh(){if(stats==null)return;stats.setText("🔥 "+num("streak")+" días     🪙 "+num("coins")+" monedas    ❤️ "+state.optInt("fun",75)+"%");
    missionText.setText("Hoy: "+missions[missionIndex]);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));
    inventoryBox.removeAllViews();JSONArray owned=state.optJSONArray("owned");boolean any=false;
    if(owned!=null)for(int x=0;x<owned.length();x++){int ix=owned.optInt(x,-1);if(ix>=0&&ix<clothes.length){any=true;Button wear=btn(clothes[ix]+(state.optInt("outfit",-1)==ix?"  ✓ Puesto":"  · Usar"));inventoryBox.addView(wear);wear.setOnClickListener(v->{put("outfit",ix);refresh();toast("¡Nuevo look!");});}}
    if(!any)inventoryBox.addView(txt("Todavía no tienes accesorios. ¡Visita la tienda!",13,false));
  }
  void sharePet(){String message="¡Mira a mi "+pet()+" en MimoPets! 🔥 Llevo "+num("streak")+" días de racha y tengo "+num("coins")+" monedas. 🐾";
    Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,message+" https://github.com/juniorguardia83-cloud/mascota-racha");startActivity(Intent.createChooser(send,"Compartir mi mascota"));}

  void care(String key){int value=Math.min(100,state.optInt(key,55)+25);put(key,value);petView.pet();String msg=key.equals("food")?"¡Qué rico! Tu mascota comió 🍖":key.equals("fun")?"¡Se divirtieron muchísimo! 🎾":"¡Quedó limpita y feliz! 🫧";toast(msg);refresh();}
  void completeMission(){String today=day();if(state.optString("done","").equals(today)){toast("¡Ya completaste la misión de hoy!");return;}String last=state.optString("last","");int streak=last.equals(yesterday())?num("streak")+1:1;put("streak",streak);put("weekProgress",Math.min(7,state.optInt("weekProgress",0)+1));put("coins",num("coins")+10);put("last",today);put("done",today);refresh();toast("¡Muy bien! +10 monedas y racha de "+streak+" días 🔥");}
  String yesterday(){Calendar c=Calendar.getInstance();c.add(Calendar.DATE,-1);return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());}
  void buy(int i){JSONArray owned=state.optJSONArray("owned");if(owned==null)owned=new JSONArray();for(int x=0;x<owned.length();x++)if(owned.optInt(x)==i){toast("¡Ya lo tienes! Está en tu mochila 🎒");return;}
    int cost=prices[i];if(num("coins")<cost){toast("Te faltan monedas. Completa misiones ❤️");return;}
    owned.put(i);try{state.put("owned",owned);state.put("coins",num("coins")-cost);state.put("outfit",i);}catch(Exception e){}save();refresh();toast("¡Comprado! Lo encuentras en tu mochila 🎒");}
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
