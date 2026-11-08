package com.mascotaracha;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** MimoPets: juego Android nativo con mascotas 2D animadas, habitaciones, tienda y progreso en la nube. */
public class MainActivity extends Activity {
  static final String URL="https://prbxumnozkapgnbhxkby.supabase.co";
  static final String KEY="sb_publishable_7xZa_3v3AYsRiyGMi0Mp9A_xj39SXjS";
  SharedPreferences prefs; JSONObject state; TextView status,stats,missionText; PetView petView; EditText emailInput,passwordInput; CheckBox termsCheck; ProgressBar weekBar; ImageView profilePhoto;
  String[] missions={"🍎 Dale su comida favorita","💧 Llena su recipiente de agua","🎾 Juega con tu mascota","💗 Acaríciala con cariño","🧼 Limpia su casita","🎵 Hazla bailar un ratito"};
  String[] clothes={"🎀 Lazo rosa","👑 Corona real","🕶️ Gafas cool","🧣 Bufanda suave","🌸 Flor","⭐ Diadema estrella","🦋 Alas mágicas","💎 Collar brillante","🐝 Disfraz abeja","🧢 Gorra urbana","💗 Moño corazón","🌈 Capa arcoíris"};
  int[] prices={20,30,40,50,35,45,70,60,55,40,65,80};
  int missionIndex; LinearLayout root,missionBox,shopBox,inventoryBox,roomBox; ScrollView pageScroll;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("petgame",0);state=load();missionIndex=Math.abs(day().hashCode())%missions.length;buildUI();handleAuthIntent(getIntent());if(prefs.getString("token",null)!=null)showTab(0);else showTab(5);}
  String day(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
  JSONObject load(){try{return new JSONObject(prefs.getString("state","{}"));}catch(Exception e){return new JSONObject();}}
  int num(String k){return state.optInt(k,0);} String pet(){return state.optString("pet","Perrito");}
  void put(String k,Object v){try{state.put(k,v);}catch(Exception e){}save();}
  void save(){prefs.edit().putString("state",state.toString()).apply();refreshWidget();syncCloud();}
  void refreshWidget(){Intent i=new Intent(this,PetWidgetProvider.class);i.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);int[] ids=android.appwidget.AppWidgetManager.getInstance(this).getAppWidgetIds(new android.content.ComponentName(this,PetWidgetProvider.class));i.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);sendBroadcast(i);}
  void buildUI(){
    LinearLayout shell=new LinearLayout(this);shell.setOrientation(1);shell.setBackgroundColor(0xFFF5FAF2);
    pageScroll=new ScrollView(this);pageScroll.setFillViewport(true);
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(12),dp(16),dp(24));root.setBackgroundColor(0xFFF5FAF2);
    pageScroll.addView(root);shell.addView(pageScroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(5),dp(5),dp(5),dp(5));nav.setBackground(round(0xFFFFFFFF,dp(22)));
    String[] labels={"⌂\\nInicio","🐾\\nMascota","🏡\\nCasa","🛍️\\nTienda","👤\\nPerfil"};
    for(int i=0;i<labels.length;i++){final int tab=i;Button b=btn(labels[i]);b.setTextSize(10);nav.addView(b,new LinearLayout.LayoutParams(0,dp(58),1));b.setOnClickListener(v->showTab(tab));}
    shell.addView(nav,new LinearLayout.LayoutParams(-1,dp(70)));setContentView(shell);showTab(0);
  }
  void header(String subtitle){
    LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
    TextView brand=txt("🐾 MimoPets",25,true);brand.setTextColor(0xFF47794B);h.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
    TextView coin=txt("🪙 "+num("coins"),15,true);coin.setTextColor(0xFF9A7541);h.addView(coin);root.addView(h);
    TextView sub=txt(subtitle,13,false);sub.setTextColor(0xFF789078);root.addView(sub);
  }
  void showTab(int tab){
    root.removeAllViews();status=null;stats=null;missionText=null;petView=null;weekBar=null;inventoryBox=null;shopBox=null;roomBox=null;
    if(tab==5)loginPage();else if(tab==0)homePage();else if(tab==1)petsPage();else if(tab==2)housePage();else if(tab==3)shopPage();else profilePage();
    pageScroll.post(()->pageScroll.scrollTo(0,0));
  }
  void homePage(){
    header("Tu pequeño mundo feliz 🌿");
    LinearLayout wallet=card(0xFFE4F2DD);wallet.setPadding(dp(12),dp(6),dp(12),dp(6));stats=txt("",15,true);stats.setTextColor(0xFF47794B);wallet.addView(stats);root.addView(wallet);
    LinearLayout hero=card(0xFFDDF0D5);hero.setOrientation(1);hero.setPadding(dp(14),dp(10),dp(14),dp(10));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(12);root.addView(hero,hp);
    hero.addView(txt("¡Tu mascota te extrañó! 💚",19,true));hero.addView(txt("Tócala para darle cariño y verla moverse",13,false));
    petView=new PetView(this);hero.addView(petView,new LinearLayout.LayoutParams(-1,dp(255)));
    petView.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){petView.pet();toast("¡"+pet()+" está feliz! 💗");return true;}return true;});
    section("🎯 Misión de hoy");LinearLayout mission=card(0xFFFFE8F0);mission.setOrientation(1);mission.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(mission);
    missionText=txt("",16,true);missionText.setTextColor(0xFF98536F);mission.addView(missionText);
    Button done=btn("Completar misión  ·  +10 🪙");mission.addView(done);done.setOnClickListener(v->completeMission());
    section("🔥 Tu racha");LinearLayout streak=card(0xFFFFF1D9);streak.setOrientation(1);streak.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(streak);
    streak.addView(txt("¡Cada día juntos desbloquea premios!",15,true));weekBar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);weekBar.setMax(7);streak.addView(weekBar,new LinearLayout.LayoutParams(-1,dp(12)));
    streak.addView(txt("Lun   Mar   Mié   Jue   Vie   Sáb   Dom",12,true));
    Button goPets=btn("🐾 Cuidar a mi mascota");root.addView(goPets);goPets.setOnClickListener(v->showTab(1));
    Button goHome=btn("🏡 Decorar mi casita");root.addView(goHome);goHome.setOnClickListener(v->showTab(2));
    refresh();
  }
  void petsPage(){
    header("Conoce y cuida a tus compañeros 🐾");
    section("Elige a tu mascota");
    String[] names={"🐶 Perrito","🐱 Gatito","🐰 Conejito","🐼 Pandita"};String[] keys={"Perrito","Gatito","Conejito","Pandita"};
    for(int i=0;i<names.length;i++){final String k=keys[i];Button b=btn(names[i]+(pet().equals(k)?"  ✓":""));root.addView(b);b.setOnClickListener(v->{put("pet",k);showTab(1);});}
    section("💗 Cuidados diarios");
    petView=new PetView(this);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));root.addView(petView,new LinearLayout.LayoutParams(-1,dp(230)));
    String[] labels={"🍎 Dar comida","🎾 Jugar","🛁 Bañar","🌙 Descansar"};String[] keys2={"food","fun","clean","energy"};
    for(int i=0;i<labels.length;i++){final String k=keys2[i];Button b=btn(labels[i]);root.addView(b);b.setOnClickListener(v->{care(k);refresh();});}
    section("Estado de tu mascota");
    String[] meters={"food","fun","clean","energy"};String[] meterLabels={"Hambre","Diversión","Higiene","Energía"};
    for(int i=0;i<meters.length;i++){TextView t=txt(meterLabels[i]+"  ·  "+state.optInt(meters[i],70)+"%",14,true);root.addView(t);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress(state.optInt(meters[i],70));root.addView(p,new LinearLayout.LayoutParams(-1,dp(10)));}
    section("✨ Crecimiento");int xp=num("xp"),level=Math.max(1,num("level"));root.addView(txt("Nivel "+level+"  ·  "+xp+"/100 XP",16,true));
    ProgressBar xpbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);xpbar.setMax(100);xpbar.setProgress(xp%100);root.addView(xpbar,new LinearLayout.LayoutParams(-1,dp(12)));
  }
  void housePage(){
    header("Un hogar bonito para tu mascota 🏡");
    section("Elige una habitación");
    String[] rooms={"🛋️ Salón","🍳 Cocina","🫧 Baño","🛏️ Dormitorio","🌷 Jardín"};
    for(int i=0;i<rooms.length;i++){final int ix=i;Button b=btn(rooms[i]+(state.optInt("room",0)==i?"  ✓":""));root.addView(b);b.setOnClickListener(v->{put("room",ix);showTab(2);});}
    section("Así se ve tu habitación");
    petView=new PetView(this);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));root.addView(petView,new LinearLayout.LayoutParams(-1,dp(300)));
    root.addView(txt("Tu casa cambia de ambiente según la habitación. ¡Entra, cuida y decora!",13,false));
    section("🏠 Otras zonas de tu casa");String[] roomsButtons={"🛋️ Salón","🍳 Cocina","🫧 Baño","🛏️ Dormitorio","🌷 Jardín"};for(int i=0;i<roomsButtons.length;i++){final int ix=i;Button zone=btn(roomsButtons[i]);root.addView(zone);zone.setOnClickListener(v->{put("room",ix);showTab(2);});}\n    section("🎀 Personaliza a tu compañero");Button wardrobe=btn("Abrir mi armario");root.addView(wardrobe);wardrobe.setOnClickListener(v->showTab(3));
  }
  void shopPage(){
    header("Compra cositas con tus monedas 🛍️");
    LinearLayout wallet=card(0xFFFFF0D5);wallet.setPadding(dp(12),dp(6),dp(12),dp(6));wallet.addView(txt("Tu saldo: "+num("coins")+" 🪙",17,true));root.addView(wallet);
    section("✨ Accesorios para tu mascota");
    for(int i=0;i<clothes.length;i++){final int ix=i;Button b=btn(clothes[i]+"   ·   "+prices[i]+" 🪙");root.addView(b);b.setOnClickListener(v->buy(ix));}
    section("🎒 Mi armario");inventoryBox=new LinearLayout(this);inventoryBox.setOrientation(1);root.addView(inventoryBox);refresh();
  }

  void loginPage(){
    root.setGravity(Gravity.CENTER_HORIZONTAL);
    TextView logo=txt("🐾",54,true);logo.setGravity(Gravity.CENTER);root.addView(logo,new LinearLayout.LayoutParams(-1,dp(76)));
    TextView brand=txt("MimoPets",30,true);brand.setTextColor(0xFF397A48);brand.setGravity(Gravity.CENTER);root.addView(brand);
    TextView intro=txt("Un pequeño mundo para compartir con tus mascotas 💚",15,false);intro.setGravity(Gravity.CENTER);root.addView(intro);
    LinearLayout panel=card(0xFFFFFFFF);panel.setOrientation(1);panel.setPadding(dp(16),dp(16),dp(16),dp(16));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(20);root.addView(panel,pp);
    panel.addView(txt("¡Bienvenido a casa!",21,true));
    panel.addView(txt("Inicia sesión para guardar tu mascota y tu progreso.",13,false));
    emailInput=new EditText(this);emailInput.setSingleLine(true);emailInput.setHint("Correo electrónico");emailInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);panel.addView(emailInput);
    passwordInput=new EditText(this);passwordInput.setSingleLine(true);passwordInput.setHint("Contraseña");passwordInput.setInputType(129);panel.addView(passwordInput);
    termsCheck=new CheckBox(this);termsCheck.setText("Acepto los términos y la política de privacidad");termsCheck.setTextColor(0xFF527653);panel.addView(termsCheck);
    TextView terms=txt("Leer términos y condiciones",13,true);terms.setTextColor(0xFFB45D8B);panel.addView(terms);terms.setOnClickListener(v->showTerms());
    Button google=btn("🌈  Continuar con Google");panel.addView(google,new LinearLayout.LayoutParams(-1,dp(50)));
    Button login=btn("Iniciar sesión");panel.addView(login,new LinearLayout.LayoutParams(-1,dp(48)));
    Button signup=btn("Crear cuenta con correo");panel.addView(signup,new LinearLayout.LayoutParams(-1,dp(48)));
    TextView forgot=txt("¿Olvidaste tu contraseña? Recuperar acceso",13,true);forgot.setTextColor(0xFFB45D8B);panel.addView(forgot);
    google.setOnClickListener(v->{if(!termsCheck.isChecked()){toast("Acepta los términos para continuar.");return;}googleLogin();});
    login.setOnClickListener(v->{if(!termsCheck.isChecked()){toast("Acepta los términos para continuar.");return;}account("login");});
    signup.setOnClickListener(v->{if(!termsCheck.isChecked()){toast("Acepta los términos para continuar.");return;}account("signup");});
    forgot.setOnClickListener(v->recoverPassword());
    status=txt("Tu cuenta protege tu partida y permite recuperarla en otros dispositivos.",12,false);root.addView(status);
  }
  void chooseProfilePhoto(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,712);}
  @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==712&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){Uri u=data.getData();try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}prefs.edit().putString("profile_photo",u.toString()).apply();if(profilePhoto!=null)profilePhoto.setImageURI(u);}}
\n  void profilePage(){
    header("Tu espacio seguro y tus recuerdos ☁️");
    LinearLayout account=card(0xFFFFFFFF);account.setOrientation(1);account.setPadding(dp(16),dp(12),dp(16),dp(12));root.addView(account);
    account.addView(txt("💗 ¡Qué bueno verte!",20,true));
    account.addView(txt("Entra para guardar tu mascota y continuar tu aventura en otros dispositivos.",13,false));
    emailInput=new EditText(this);emailInput.setSingleLine(true);emailInput.setHint("Correo electrónico");emailInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);account.addView(emailInput);
    passwordInput=new EditText(this);passwordInput.setSingleLine(true);passwordInput.setHint("Contraseña (mínimo 6 caracteres)");passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);account.addView(passwordInput);
    termsCheck=new CheckBox(this);termsCheck.setText("He leído y acepto los términos y la política de privacidad");termsCheck.setTextColor(0xFF527653);account.addView(termsCheck);
    TextView termsLink=txt("Leer términos y condiciones",13,true);termsLink.setTextColor(0xFFB45D8B);account.addView(termsLink);termsLink.setOnClickListener(v->showTerms());
    Button signup=btn("Crear mi cuenta");account.addView(signup,new LinearLayout.LayoutParams(-1,dp(48)));
    Button login=btn("Iniciar sesión");account.addView(login,new LinearLayout.LayoutParams(-1,dp(48)));
    Button google=btn("🌈 Vincular con Google");account.addView(google,new LinearLayout.LayoutParams(-1,dp(48)));
    TextView forgot=txt("¿Olvidaste tu contraseña? Recuperar acceso",13,true);forgot.setTextColor(0xFFB45D8B);account.addView(forgot);
    signup.setOnClickListener(v->{if(termsCheck.isChecked())account("signup");else toast("Acepta los términos para crear tu cuenta.");});
    login.setOnClickListener(v->account("login"));google.setOnClickListener(v->googleLogin());forgot.setOnClickListener(v->recoverPassword());
    section("📊 Tu aventura");root.addView(txt("🔥 Racha: "+num("streak")+" días",16,true));root.addView(txt("⭐ Nivel: "+Math.max(1,num("level")),16,true));root.addView(txt("🪙 Monedas: "+num("coins"),16,true));
    Button share=btn("📤 Compartir mi mascota");root.addView(share);share.setOnClickListener(v->sharePet());
    status=txt("Tus avances se guardan en este teléfono. Al iniciar sesión puedes vincularlos a tu cuenta.",12,false);root.addView(status);
  }
  void showTerms(){
    String text="MIMOPETS · TÉRMINOS Y PRIVACIDAD\\n\\n1. Datos de cuenta: usamos tu correo y los datos de autenticación gestionados por Supabase para crear y proteger tu cuenta. Si eliges Google, Google procesa el acceso según sus propias condiciones.\\n\\n2. Datos del juego: guardamos el nombre de tu mascota, monedas, racha, nivel, artículos, habitación y progreso para conservar tu partida y sincronizarla cuando inicies sesión.\\n\\n3. Uso: estos datos se utilizan para iniciar sesión, guardar/restaurar tu partida y mantener las funciones del juego. No vendemos tus datos ni los usamos para publicidad personalizada.\\n\\n4. Almacenamiento: los datos de cuenta y sincronización se procesan mediante Supabase. El dispositivo también puede conservar una copia local.\\n\\n5. Seguridad y control: no compartas tu contraseña. Puedes dejar de usar la app cuando quieras; para solicitar eliminación de cuenta/datos, contacta al responsable de la app.\\n\\n6. Menores: si eres menor, pide a tu madre, padre o tutor que revise estos términos.\\n\\nAl continuar declaras que has leído esta información.";
    new AlertDialog.Builder(this).setTitle("Términos y condiciones").setMessage(text).setPositiveButton("Entendido",null).show();
  }
  android.graphics.drawable.GradientDrawable round(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
  LinearLayout card(int color){LinearLayout x=new LinearLayout(this);x.setBackground(round(color,dp(20)));return x;}
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(48,35,68));if(bold)t.setTypeface(null,1);t.setPadding(4,dp(7),4,dp(7));return t;}
  void section(String s){TextView t=txt(s,17,true);t.setTextColor(0xFF47794B);t.setPadding(4,dp(16),4,dp(6));root.addView(t);}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xFF47794B);b.setTextSize(13);b.setAllCaps(false);b.setPadding(dp(4),dp(2),dp(4),dp(2));b.setBackground(round(0xFFE5F2DE,dp(16)));return b;}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  void refresh(){
    if(stats!=null)stats.setText("🔥 "+num("streak")+" días     🪙 "+num("coins")+" monedas    ❤️ "+state.optInt("fun",75)+"%");
    if(missionText!=null)missionText.setText("Hoy: "+missions[missionIndex]);
    if(weekBar!=null)weekBar.setProgress(num("weekProgress"));
    if(petView!=null){petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));}
    if(inventoryBox!=null){inventoryBox.removeAllViews();JSONArray owned=state.optJSONArray("owned");boolean any=false;
      if(owned!=null)for(int x=0;x<owned.length();x++){int ix=owned.optInt(x,-1);if(ix>=0&&ix<clothes.length){any=true;Button wear=btn(clothes[ix]+(state.optInt("outfit",-1)==ix?"  ✓ Puesto":"  · Usar"));inventoryBox.addView(wear);wear.setOnClickListener(v->{put("outfit",ix);refresh();toast("¡Nuevo look!");});}}
      if(!any)inventoryBox.addView(txt("Todavía no tienes accesorios. ¡Visita la tienda!",13,false));
    }
  }
  void sharePet(){String message="¡Mira a mi "+pet()+" en MimoPets! 🔥 Llevo "+num("streak")+" días de racha y tengo "+num("coins")+" monedas. 🐾";
    Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,message+" https://github.com/juniorguardia83-cloud/mascota-racha");startActivity(Intent.createChooser(send,"Compartir mi mascota"));}

  void care(String key){
    int value=Math.min(100,state.optInt(key,55)+25);
    try{state.put(key,value);int xp=state.optInt("xp",0)+8;int level=Math.max(1,state.optInt("level",1));if(xp>=100){xp-=100;level++;state.put("coins",num("coins")+25);toast("¡Subiste al nivel "+level+"! +25 monedas ⭐");}state.put("xp",xp);state.put("level",level);}catch(Exception e){}
    save();if(petView!=null)petView.pet();
    String msg=key.equals("food")?"¡Qué rico! Tu mascota comió 🍖":key.equals("fun")?"¡Se divirtieron muchísimo! 🎾":key.equals("clean")?"¡Quedó limpita y feliz! 🫧":"¡Descansó y recuperó energía! 🌙";
    toast(msg+"  +8 XP");refresh();
  }
  void completeMission(){
    String today=day();if(state.optString("done","").equals(today)){toast("¡Ya completaste la misión de hoy!");return;}
    String last=state.optString("last","");int streak=last.equals(yesterday())?num("streak")+1:1;
    int progress=state.optInt("weekProgress",0)+1;int coins=num("coins")+10;String reward="";
    if(progress>=7){coins+=100;progress=0;reward=" ¡Premio semanal: +100 monedas! 🎁";}
    int xp=state.optInt("xp",0)+20;int level=Math.max(1,state.optInt("level",1));if(xp>=100){xp-=100;level++;coins+=25;reward+=" ¡Subiste al nivel "+level+"! ⭐";}
    try{state.put("streak",streak);state.put("weekProgress",progress);state.put("coins",coins);state.put("last",today);state.put("done",today);state.put("xp",xp);state.put("level",level);}catch(Exception e){}
    save();refresh();toast("¡Misión completada! +10 monedas · +20 XP."+reward);
  }
  String yesterday(){Calendar c=Calendar.getInstance();c.add(Calendar.DATE,-1);return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.getTime());}
  void buy(int i){JSONArray owned=state.optJSONArray("owned");if(owned==null)owned=new JSONArray();for(int x=0;x<owned.length();x++)if(owned.optInt(x)==i){toast("¡Ya lo tienes! Está en tu mochila 🎒");return;}
    int cost=prices[i];if(num("coins")<cost){toast("Te faltan monedas. Completa misiones ❤️");return;}
    owned.put(i);try{state.put("owned",owned);state.put("coins",num("coins")-cost);state.put("outfit",i);}catch(Exception e){}save();refresh();toast("¡Comprado! Lo encuentras en tu mochila 🎒");}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  void account(String mode){
    String email=emailInput.getText().toString().trim(),password=passwordInput.getText().toString();
    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.length()<6){toast("Escribe un correo válido y una contraseña de 6 caracteres o más.");return;}
    if(status!=null)status.setText(mode.equals("signup")?"Creando tu cuenta…":"Iniciando sesión…");
    new Thread(()->{try{
      JSONObject body=new JSONObject();body.put("email",email);body.put("password",password);
      String endpoint=mode.equals("signup")?"/auth/v1/signup?redirect_to=mimopets://auth-callback":"/auth/v1/token?grant_type=password";
      HttpURLConnection c=(HttpURLConnection)new URL(URL+endpoint).openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(20000);c.setReadTimeout(20000);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(body.toString().getBytes("UTF-8"));
      int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));
      JSONObject result=new JSONObject(response);String access=result.optString("access_token","");JSONObject user=result.optJSONObject("user");
      if(access.length()==0||user==null){runOnUiThread(()->{if(status!=null)status.setText("Cuenta creada. Revisa tu correo, confirma la dirección y después inicia sesión.");toast("Te enviamos la confirmación por correo 💌");});return;}
      saveSignedInUser(access,user);
      runOnUiThread(()->{refresh();if(status!=null)status.setText("Sesión iniciada. Cuenta vinculada.");toast("¡Bienvenido! ❤️");});
    }catch(Exception e){String msg=e.getMessage()==null?"Error de conexión":e.getMessage();runOnUiThread(()->{if(status!=null)status.setText("No se pudo completar: "+msg);});}}).start();
  }
  String authError(String response){try{JSONObject e=new JSONObject(response);return e.optString("msg",e.optString("message",e.optString("error_description",response)));}catch(Exception ex){return response;}}
  void saveSignedInUser(String access,JSONObject user)throws Exception{
    String uid=user.getString("id");prefs.edit().putString("token",access).putString("uid",uid).apply();
    HttpURLConnection get=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?id=eq."+uid+"&select=data").openConnection();get.setRequestProperty("apikey",KEY);get.setRequestProperty("Authorization","Bearer "+access);get.setConnectTimeout(15000);get.setReadTimeout(15000);
    if(get.getResponseCode()==200){JSONArray rows=new JSONArray(readResponse(get));if(rows.length()>0){JSONObject cloud=rows.getJSONObject(0).optJSONObject("data");if(cloud!=null){state=cloud;prefs.edit().putString("state",state.toString()).apply();}}}
    refreshWidget();syncCloud();
  }
  void recoverPassword(){
    String email=emailInput.getText().toString().trim();if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){toast("Escribe primero tu correo electrónico.");return;}
    new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/recover?redirect_to=mimopets://auth-callback").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(new JSONObject().put("email",email).toString().getBytes("UTF-8"));int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));runOnUiThread(()->{if(status!=null)status.setText("Si el correo existe, recibirás un enlace para restablecer la contraseña.");toast("Revisa tu correo 💌");});}catch(Exception e){runOnUiThread(()->toast("No se pudo enviar el enlace: "+e.getMessage()));}}).start();
  }
  void googleLogin(){
    try{String redirect=URLEncoder.encode("mimopets://auth-callback","UTF-8");String url=URL+"/auth/v1/authorize?provider=google&redirect_to="+redirect;startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));if(status!=null)status.setText("Abriendo Google…");}
    catch(Exception e){toast("No se pudo abrir el acceso con Google.");}
  }
  @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handleAuthIntent(intent);}
  void handleAuthIntent(Intent intent){
    if(intent==null||intent.getData()==null)return;String fragment=intent.getData().getFragment();if(fragment==null)return;
    String token=null;String type="";for(String part:fragment.split("&")){String[] kv=part.split("=",2);if(kv.length==2){if(kv[0].equals("access_token"))token=Uri.decode(kv[1]);if(kv[0].equals("type"))type=Uri.decode(kv[1]);}}
    if(token==null){String code=intent.getData().getQueryParameter("code");if(code!=null){toast("Google devolvió un código de acceso. Configura el flujo PKCE en Supabase para completar la sesión.");}return;}final String access=token;
    if("recovery".equals(type)){prefs.edit().putString("recovery_token",access).apply();runOnUiThread(()->newPasswordDialog(access));return;}
    new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/user").openConnection();c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+access);JSONObject user=new JSONObject(readResponse(c));saveSignedInUser(access,user);runOnUiThread(()->{showTab(4);if(status!=null)status.setText("Sesión iniciada con éxito.");toast("¡Hola! 💚");});}catch(Exception e){runOnUiThread(()->toast("No se pudo validar la cuenta de Google."));}}).start();
  }
  void newPasswordDialog(String access){
    EditText input=new EditText(this);input.setHint("Nueva contraseña (mínimo 6 caracteres)");input.setInputType(129);
    new AlertDialog.Builder(this).setTitle("Crear nueva contraseña").setView(input).setPositiveButton("Guardar",(d,w)->{String pass=input.getText().toString();if(pass.length()<6){toast("Usa al menos 6 caracteres.");return;}new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/user").openConnection();c.setRequestMethod("PUT");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+access);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(new JSONObject().put("password",pass).toString().getBytes("UTF-8"));int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));runOnUiThread(()->toast("Contraseña actualizada. Ya puedes iniciar sesión."));}catch(Exception e){runOnUiThread(()->toast("No se pudo cambiar: "+e.getMessage()));}}).start();}).setNegativeButton("Cancelar",null).show();
  }
  String readResponse(HttpURLConnection c)throws Exception{InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();if(in==null)return "";ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}
  void syncCloud(){new Thread(()->{try{
    String token=prefs.getString("token",null);if(token==null){JSONObject a=new JSONObject();a.put("data",new JSONObject());HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/signup").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(a.toString().getBytes("UTF-8"));JSONObject res=new JSONObject(read(c));token=res.optString("access_token",null);if(token==null)throw new Exception("Anonymous auth unavailable");prefs.edit().putString("token",token).putString("uid",res.getJSONObject("user").getString("id")).apply();}
    String uid=prefs.getString("uid","");JSONObject row=new JSONObject();row.put("id",uid);row.put("data",state);row.put("updated_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).format(new Date()));
    HttpURLConnection c=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?on_conflict=id").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","resolution=merge-duplicates");c.getOutputStream().write(new JSONArray().put(row).toString().getBytes("UTF-8"));int code=c.getResponseCode();if(code<200||code>=300)throw new Exception("Supabase HTTP "+code);runOnUiThread(()->{if(status!=null)status.setText("☁️ Avances sincronizados con Supabase");});
  }catch(Exception e){runOnUiThread(()->{if(status!=null)status.setText("📱 Guardado en este teléfono · nube pendiente");});}}).start();}
  String read(HttpURLConnection c)throws Exception{InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}

}
