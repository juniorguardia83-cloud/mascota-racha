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
  SharedPreferences prefs; JSONObject state; TextView status,stats,missionText,walletText; PetView petView; EditText emailInput,passwordInput; CheckBox termsCheck; ProgressBar weekBar; ImageView profilePhoto;
  String[] missions={"🍎 Dale su comida favorita","💧 Llena su recipiente de agua","🎾 Juega con tu mascota","💗 Acaríciala con cariño","🧼 Limpia su casita","🎵 Hazla bailar un ratito"};
  String[] clothes={"🎀 Lazo rosa","👑 Corona real","🕶️ Gafas cool","🧣 Bufanda suave","🌸 Flor","⭐ Diadema estrella","🦋 Alas mágicas","💎 Collar brillante","🐝 Disfraz abeja","🧢 Gorra urbana","💗 Moño corazón","🌈 Capa arcoíris"};
  int[] prices={20,30,40,50,35,45,70,60,55,40,65,80};
  int missionIndex; LinearLayout root,missionBox,shopBox,inventoryBox,roomBox; ScrollView pageScroll;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("petgame",0);state=load();if(state.optString("pet","").length()>0&&!state.optBoolean("pet_selected",false)){try{state.put("pet_selected",true);state.put("egg_hatched",true);prefs.edit().putString("state",state.toString()).apply();}catch(Exception ignored){}}missionIndex=Math.abs(day().hashCode())%missions.length;splashPage();new Handler().postDelayed(()->{buildUI();if(prefs.getBoolean("signed_in",false))syncCloud();},1500);}
  String day(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
  JSONObject load(){try{JSONObject saved=new JSONObject(prefs.getString("state","{}"));if(saved.has("pet")&&!saved.optString("pet","").isEmpty()){saved.put("pet_selected",true);saved.put("egg_hatched",true);saved.remove("egg_started");saved.remove("egg_pet");}return saved;}catch(Exception e){return new JSONObject();}}
  int num(String k){return state.optInt(k,0);} String pet(){return state.optString("pet","Perrito");}
  void put(String k,Object v){try{state.put(k,v);}catch(Exception e){}save();}
  void save(){prefs.edit().putString("state",state.toString()).putLong("state_saved_at",System.currentTimeMillis()).apply();refreshWidget();if(prefs.getBoolean("signed_in",false))syncCloud();}
  void refreshWidget(){Intent i=new Intent(this,PetWidgetProvider.class);i.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);int[] ids=android.appwidget.AppWidgetManager.getInstance(this).getAppWidgetIds(new android.content.ComponentName(this,PetWidgetProvider.class));i.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);sendBroadcast(i);}
  void buildUI(){
    if(!prefs.getBoolean("signed_in",false)){loginPage();return;}
    LinearLayout shell=new LinearLayout(this);shell.setOrientation(1);shell.setBackgroundColor(0xFFF5FAF2);
    pageScroll=new ScrollView(this);pageScroll.setFillViewport(true);
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(12),dp(16),dp(24));root.setBackgroundColor(0xFFF5FAF2);
    pageScroll.addView(root);shell.addView(pageScroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(5),dp(5),dp(5),dp(5));nav.setBackground(round(0xFFFFFFFF,dp(22)));
    String[] labels={"⌂\nInicio","🐾\nMascota","🏡\nCasa","🛍️\nTienda","👤\nPerfil"};
    for(int i=0;i<labels.length;i++){final int tab=i;Button b=btn(labels[i]);b.setTextSize(10);nav.addView(b,new LinearLayout.LayoutParams(0,dp(58),1));b.setOnClickListener(v->showTab(tab));}
    shell.addView(nav,new LinearLayout.LayoutParams(-1,dp(70)));setContentView(shell);showTab(0);
  }
  void loginPage(){
    LinearLayout page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(22),dp(30),dp(22),dp(24));page.setGravity(Gravity.CENTER_HORIZONTAL);page.setBackgroundColor(0xFFF1F9EC);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(page);setContentView(scroll);
    TextView logo=txt("🐾",58,true);logo.setGravity(Gravity.CENTER);page.addView(logo);
    TextView brand=txt("MimoPets",34,true);brand.setTextColor(0xFF326B3D);brand.setGravity(Gravity.CENTER);page.addView(brand);
    TextView tagline=txt("Un pequeño hogar para compartir y cuidar 💚",15,false);tagline.setGravity(Gravity.CENTER);tagline.setTextColor(0xFF718B73);page.addView(tagline);
    LinearLayout panel=card(0xFFFFFFFF);panel.setOrientation(1);panel.setPadding(dp(18),dp(18),dp(18),dp(18));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(26);page.addView(panel,pp);
    panel.addView(txt("¡Bienvenido a tu mundo!",22,true));
    panel.addView(txt("Inicia sesión para guardar tus mascotas, recuerdos y progreso.",14,false));
    TextView divider=txt("Entra con tu correo electrónico",13,true);divider.setGravity(Gravity.CENTER);panel.addView(divider);
    emailInput=new EditText(this);emailInput.setSingleLine(true);emailInput.setHint("Correo electrónico");emailInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);panel.addView(emailInput);
    passwordInput=new EditText(this);passwordInput.setSingleLine(true);passwordInput.setHint("Contraseña");passwordInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);panel.addView(passwordInput);
    termsCheck=new CheckBox(this);termsCheck.setText("Acepto los términos y condiciones");termsCheck.setTextColor(0xFF527653);panel.addView(termsCheck);
    TextView terms=txt("Leer términos y política de privacidad",13,true);terms.setTextColor(0xFFB45D8B);panel.addView(terms);terms.setOnClickListener(v->showTerms());
    Button login=btn("Iniciar sesión con correo");panel.addView(login,new LinearLayout.LayoutParams(-1,dp(48)));login.setOnClickListener(v->{if(termsCheck.isChecked())account("login");else toast("Lee y acepta los términos para continuar.");});
    Button signup=btn("Crear cuenta nueva");panel.addView(signup,new LinearLayout.LayoutParams(-1,dp(48)));signup.setOnClickListener(v->{if(termsCheck.isChecked())account("signup");else toast("Acepta los términos para crear tu cuenta.");});
    TextView forgot=txt("¿Olvidaste tu contraseña? Recuperar acceso",13,true);forgot.setTextColor(0xFFB45D8B);panel.addView(forgot);forgot.setOnClickListener(v->recoverPassword());
    status=txt("Tu cuenta protege y sincroniza tu aventura.",12,false);status.setGravity(Gravity.CENTER);page.addView(status);
  }

  void splashPage(){
    LinearLayout page=new LinearLayout(this);page.setOrientation(1);page.setGravity(Gravity.CENTER);page.setPadding(dp(24),dp(24),dp(24),dp(24));
    page.setBackgroundColor(0xFFF4FFF4);
    TextView logo=txt("🐾",76,true);logo.setGravity(Gravity.CENTER);page.addView(logo);
    TextView name=txt("MimoPets",36,true);name.setTextColor(0xFF286B43);name.setGravity(Gravity.CENTER);page.addView(name);
    TextView tagline=txt("Un hogar lleno de ternura 💗",16,true);tagline.setTextColor(0xFF83A98A);tagline.setGravity(Gravity.CENTER);page.addView(tagline);
    TextView loading=txt("Preparando tu mundo…",13,false);loading.setGravity(Gravity.CENTER);page.addView(loading);
    ProgressBar bar=new ProgressBar(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(46),dp(46));p.topMargin=dp(16);page.addView(bar,p);setContentView(page);
  }
  void chooseStarterPage(){
    header("¡Tu aventura comienza aquí! 🥚");
    LinearLayout intro=card(0xFFFFE8F2);intro.setOrientation(1);intro.setPadding(dp(14),dp(12),dp(14),dp(12));root.addView(intro);
    intro.addView(txt("Elige qué mascota quieres recibir. Su huevo tardará 3 horas en abrirse y conocerás a tu nuevo compañero.",15,true));
    String[] names={"Perrito 🐶","Gatito 🐱","Conejito 🐰","Pandita 🐼"};String[] keys={"Perrito","Gatito","Conejito","Pandita"};
    for(int i=0;i<keys.length;i++){final String chosen=keys[i];Button b=btn(names[i]+"  ·  Elegir huevo");root.addView(b);b.setOnClickListener(v->{try{state.put("egg_pet",chosen);state.put("egg_started",System.currentTimeMillis());state.put("pet_selected",true);state.put("egg_hatched",false);state.put("coins",state.optInt("coins",0)+100);state.put("gems",state.optInt("gems",0));}catch(Exception ignored){}save();eggPage();});}
  }
  void adoptAnotherPet(){
    String[] names={"Perrito 🐶","Gatito 🐱","Conejito 🐰","Pandita 🐼"};String[] keys={"Perrito","Gatito","Conejito","Pandita"};
    if(state.optLong("egg_started",0)>0&&!state.optBoolean("egg_hatched",true)){toast("Ya tienes un huevo incubándose. ¡No pierdas su progreso! 🥚");showTab(1);return;}
    new AlertDialog.Builder(this).setTitle("Elige un nuevo huevo 🥚").setItems(names,(d,w)->{try{state.put("egg_pet",keys[w]);state.put("egg_started",System.currentTimeMillis());state.put("egg_hatched",false);}catch(Exception ignored){}save();showTab(1);}).setNegativeButton("Cancelar",null).show();
  }
  void eggPage(){
    root.removeAllViews();header("¡Tu huevo está incubándose! 🥚");
    LinearLayout card=card(0xFFFFEBC8);card.setOrientation(1);card.setGravity(Gravity.CENTER);card.setPadding(dp(18),dp(24),dp(18),dp(24));root.addView(card);
    TextView egg=txt("🥚",82,true);egg.setGravity(Gravity.CENTER);card.addView(egg);
    TextView petName=txt("Un huevo de "+state.optString("egg_pet","mascota"),20,true);petName.setGravity(Gravity.CENTER);card.addView(petName);
    TextView timer=txt("",24,true);timer.setGravity(Gravity.CENTER);card.addView(timer);
    TextView hint=txt("Cuida la espera: cuando termine, toca el huevo para conocer a tu mascota. 💗",14,false);hint.setGravity(Gravity.CENTER);card.addView(hint);
    Runnable tick=new Runnable(){public void run(){if(timer.getParent()==null)return;long left=10800000L-(System.currentTimeMillis()-state.optLong("egg_started",System.currentTimeMillis()));if(left<=0){timer.setText("¡Ya quiere nacer! 🎉");egg.setText("🐣");egg.setOnClickListener(v->hatchEgg());hint.setText("¡Toca el pollito para abrir el huevo!");}else{long sec=left/1000;timer.setText(String.format(Locale.getDefault(),"%02d:%02d:%02d",sec/3600,(sec%3600)/60,sec%60));egg.setText("🥚");new Handler().postDelayed(this,1000);}}};tick.run();
  }
  void hatchEgg(){String chosen=state.optString("egg_pet","Perrito");try{JSONArray pets=state.optJSONArray("pets");if(pets==null)pets=new JSONArray();boolean exists=false;for(int i=0;i<pets.length();i++)if(chosen.equals(pets.optString(i)))exists=true;if(!exists)pets.put(chosen);state.put("pets",pets);if(state.optString("pet","").isEmpty()){state.put("pet",chosen);state.put("food",85);state.put("fun",90);state.put("clean",100);state.put("energy",100);}state.put("pet_selected",true);state.put("egg_hatched",true);state.remove("egg_started");state.remove("egg_pet");}catch(Exception ignored){}save();toast("¡"+chosen+" ha nacido y se unió a tu familia! 💗");showTab(0);}

  void header(String subtitle){
    LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);
    TextView brand=txt("🐾 MimoPets",25,true);brand.setTextColor(0xFF47794B);h.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
    walletText=txt("🪙 "+num("coins")+"     💎 "+num("gems"),16,true);walletText.setTextColor(0xFF9A7541);walletText.setGravity(Gravity.RIGHT);h.addView(walletText);root.addView(h);
    TextView sub=txt(subtitle,13,false);sub.setTextColor(0xFF789078);root.addView(sub);
  }
  void showTab(int tab){
    if(tab==2){
      root.removeAllViews();status=null;LinearLayout loading=card(0xFFFFEBC2);loading.setOrientation(1);loading.setGravity(Gravity.CENTER);loading.setPadding(dp(20),dp(50),dp(20),dp(50));
      TextView house=txt("🏡",58,true);house.setGravity(Gravity.CENTER);loading.addView(house);
      TextView message=txt("Preparando tu casita…",19,true);message.setGravity(Gravity.CENTER);loading.addView(message);
      ProgressBar spinner=new ProgressBar(this);loading.addView(spinner,new LinearLayout.LayoutParams(dp(48),dp(48)));
      root.addView(loading,new LinearLayout.LayoutParams(-1,-1));pageScroll.postDelayed(()->showTabContent(2),700);return;
    }
    showTabContent(tab);
  }
  void showTabContent(int tab){
    root.removeAllViews();status=null;stats=null;missionText=null;walletText=null;petView=null;weekBar=null;inventoryBox=null;shopBox=null;roomBox=null;
    if(tab==0&&!state.optBoolean("pet_selected",false))chooseStarterPage();else if(tab==0)homePage();else if(tab==1)petsPage();else if(tab==2)housePage();else if(tab==3)shopPage();else profilePage();
    pageScroll.post(()->pageScroll.scrollTo(0,0));
  }
  void homePage(){
    header("Tu mundo para compartir 💚");
    LinearLayout welcome=card(0xFFE5F5DE);welcome.setOrientation(1);welcome.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(welcome);
    welcome.addView(txt("¡Hola, "+prefs.getString("profile_name","amig@")+"! 🐾",21,true));
    welcome.addView(txt("Cuida a tus mascotas y crea recuerdos cada día.",13,false));
    TextView moodLine=txt("Hoy te sientes: "+state.optString("mood","Feliz 😄")+"  ·  Toca para cambiar",13,true);moodLine.setTextColor(0xFFB45D8B);welcome.addView(moodLine);moodLine.setOnClickListener(v->moodDialog());
    LinearLayout row1=new LinearLayout(this);row1.setOrientation(0);row1.setGravity(Gravity.CENTER);root.addView(row1);
    row1.addView(dashCard("Mascotas","Cuida y juega con ellas","🐱",0xFFFFE5B2,1),new LinearLayout.LayoutParams(0,dp(158),1));
    row1.addView(dashCard("Rastreador","Misiones y actividades","🎯",0xFFFFDDE8,1),new LinearLayout.LayoutParams(0,dp(158),1));
    LinearLayout row2=new LinearLayout(this);row2.setOrientation(0);row2.setGravity(Gravity.CENTER);root.addView(row2);
    row2.addView(dashCard("Sueño","Mira su energía y descanso","🌙",0xFFDDF1FC,1),new LinearLayout.LayoutParams(0,dp(158),1));
    row2.addView(dashCard("Nuestros días","Celebra tu racha diaria","💗",0xFFFFE0D2,0),new LinearLayout.LayoutParams(0,dp(158),1));
    LinearLayout row3=new LinearLayout(this);row3.setOrientation(0);row3.setGravity(Gravity.CENTER);root.addView(row3);
    row3.addView(dashCard("Te extraño","Comparte un mensaje","💌",0xFFF9DDF2,4),new LinearLayout.LayoutParams(0,dp(158),1));
    row3.addView(dashCard("Mi estado","Tu perfil y recuerdos","🌿",0xFFDDE4FF,4),new LinearLayout.LayoutParams(0,dp(158),1));
    section("🐾 Tu compañero de hoy");
    LinearLayout petCard=card(0xFFF1F8E9);petCard.setOrientation(1);petCard.setPadding(dp(10),dp(6),dp(10),dp(8));root.addView(petCard);
    petCard.addView(txt("¡"+pet()+" te está esperando!",17,true));
    petView=new PetView(this);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));petCard.addView(petView,new LinearLayout.LayoutParams(-1,dp(190)));
    petView.setOnClickListener(v->{petView.pet();toast("¡"+pet()+" está feliz! 💗");});
    section("🔥 Tu racha");
    LinearLayout streak=card(0xFFFFF1D9);streak.setOrientation(1);streak.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(streak);
    streak.addView(txt("Llevas "+num("streak")+" días cuidando de tu mascota",15,true));
    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(7);bar.setProgress(num("weekProgress"));streak.addView(bar,new LinearLayout.LayoutParams(-1,dp(12)));
    Button mission=btn("🎁 Completar misión de hoy · +10 🪙");root.addView(mission);mission.setOnClickListener(v->completeMission());
  }
  LinearLayout dashCard(String title,String subtitle,String emoji,int color,int tab){
    LinearLayout c=card(color);c.setOrientation(1);c.setPadding(dp(12),dp(8),dp(8),dp(8));c.setGravity(Gravity.CENTER_VERTICAL);
    TextView tag=txt(emoji,31,true);c.addView(tag);
    c.addView(txt(title,18,true));TextView sub=txt(subtitle,12,false);sub.setTextColor(0xFF776B72);c.addView(sub);
    LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)c.getLayoutParams();c.setOnClickListener(v->{if(tab==4&&title.equals("Te extraño"))sharePet();else if(tab==5)gamesPage();else if(title.equals("Estado de ánimo"))moodDialog();else showTab(tab);});
    return c;
  }
  void petsPage(){
    header("Conoce y cuida a tus compañeros 🐾");
    section("Elige a tu mascota");
    String[] names={"🐶 Perrito","🐱 Gatito","🐰 Conejito","🐼 Pandita"};String[] keys={"Perrito","Gatito","Conejito","Pandita"};
    JSONArray pets=state.optJSONArray("pets");if(pets==null){pets=new JSONArray();pets.put(pet());try{state.put("pets",pets);}catch(Exception ignored){}save();}
    for(int i=0;i<names.length;i++){final String k=keys[i];boolean found=false;for(int j=0;j<pets.length();j++)if(k.equals(pets.optString(j)))found=true;final boolean owned=found;Button b=btn(names[i]+(pet().equals(k)?"  ✓":""));b.setEnabled(owned);root.addView(b);b.setOnClickListener(v->{if(owned){put("pet",k);showTab(1);}else toast("Esta mascota aún no está desbloqueada 🥚");});}
    section("💗 ¿Cómo te sientes hoy?");
    Button mood=btn("😊 Estado: "+state.optString("mood","Feliz")+"  ·  Cambiar");root.addView(mood);mood.setOnClickListener(v->moodDialog());
    TextView petCollectionNote=txt("Tu compañero actual seguirá contigo mientras incubas y adoptas otra mascota. 💗",13,false);root.addView(petCollectionNote);
    if(state.optLong("egg_started",0)>0&&!state.optBoolean("egg_hatched",true))incubatorCard();
    Button adopt=btn("🥚 Adoptar otra mascota · Huevo de 3 horas");root.addView(adopt);adopt.setOnClickListener(v->adoptAnotherPet());
    section("💗 Cuidados diarios");
    petView=new PetView(this);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));root.addView(petView,new LinearLayout.LayoutParams(-1,dp(230)));
    String[] labels={"🍎 Dar comida","🎾 Jugar","🛁 Bañar","🌙 Descansar"};String[] keys2={"food","fun","clean","energy"};
    for(int i=0;i<labels.length;i++){final String k=keys2[i];Button b=btn(labels[i]);root.addView(b);b.setOnClickListener(v->{care(k);refresh();});}
    section("Estado de tu mascota");
    String[] meters={"food","fun","clean","energy"};String[] meterLabels={"Hambre","Diversión","Higiene","Energía"};
    for(int i=0;i<meters.length;i++){TextView t=txt(meterLabels[i]+"  ·  "+state.optInt(meters[i],70)+"%",14,true);root.addView(t);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress(state.optInt(meters[i],70));root.addView(p,new LinearLayout.LayoutParams(-1,dp(10)));}
    section("🎮 Tiempo de jugar");Button games=btn("Jugar minijuegos y ganar monedas");root.addView(games);games.setOnClickListener(v->gamesPage());
    section("✨ Crecimiento");int xp=num("xp"),level=Math.max(1,num("level"));root.addView(txt("Nivel "+level+"  ·  "+xp+"/100 XP",16,true));
    ProgressBar xpbar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);xpbar.setMax(100);xpbar.setProgress(xp%100);root.addView(xpbar,new LinearLayout.LayoutParams(-1,dp(12)));
  }
  void housePage(){
    header("Nuestro hogar 🏡");
    LinearLayout banner=card(0xFFFFE8B7);banner.setOrientation(1);banner.setPadding(dp(12),dp(8),dp(12),dp(8));root.addView(banner);
    banner.addView(txt("🏠  La casita de "+pet(),19,true));banner.addView(txt("Elige dónde quieres pasar el rato",13,false));
    LinearLayout rooms=new LinearLayout(this);rooms.setGravity(Gravity.CENTER);rooms.setPadding(0,dp(8),0,dp(8));root.addView(rooms);
    String[] labels={"🌿 Salón","🍳 Cocina","🛁 Baño","🛏️ Dormitorio","🌷 Jardín"};int[] roomIds={2,4,3,1,0};
    for(int i=0;i<labels.length;i++){final int ix=roomIds[i];Button b=btn(labels[i]);b.setTextSize(10);rooms.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));b.setOnClickListener(v->{put("room",ix);showTab(2);});}
    section("✨ Tu habitación");
    LinearLayout scene=card(0xFFFFDFAE);scene.setOrientation(1);scene.setPadding(dp(5),dp(5),dp(5),dp(5));root.addView(scene);
    TextView roomName=txt(new String[]{"Jardín 🌷","Dormitorio 🛏️","Sala 🛋️","Baño 🫧","Cocina 🍳"}[Math.min(4,state.optInt("room",0))],17,true);roomName.setGravity(Gravity.CENTER);scene.addView(roomName);
    petView=new PetView(this);petView.setPet(pet());petView.setRoom(state.optInt("room",0));petView.setOutfit(state.optInt("outfit",-1));scene.addView(petView,new LinearLayout.LayoutParams(-1,dp(330)));
    section("Acciones de la casa");
    String[] actions={"🍎 Preparar comida","🧸 Jugar en el salón","🫧 Bañar a "+pet(),"🌙 Llevar a dormir"};
    String[] keys={"food","fun","clean","energy"};
    for(int i=0;i<actions.length;i++){final String k=keys[i];Button b=btn(actions[i]);root.addView(b);b.setOnClickListener(v->{care(k);});}
    Button wardrobe=btn("👗 Abrir armario y ropa");root.addView(wardrobe);wardrobe.setOnClickListener(v->showTab(3));
  }
  void shopPage(){
    header("La boutique de MimoPets ✨");
    LinearLayout hero=card(0xFFFFE5EC);hero.setOrientation(1);hero.setPadding(dp(14),dp(10),dp(14),dp(10));root.addView(hero);
    TextView heroTitle=txt("Un look nuevo para tu mejor amigo 💗",19,true);hero.addView(heroTitle);
    hero.addView(txt("Prueba accesorios 2D, compra con tus monedas y equípalos al instante.",13,false));
    LinearLayout balance=card(0xFFFFF2CE);balance.setPadding(dp(12),dp(5),dp(12),dp(5));balance.setGravity(Gravity.CENTER_VERTICAL);balance.addView(txt("🪙 "+num("coins")+" monedas     💎 "+num("gems")+" gemas",16,true));root.addView(balance);
    section("Explorar colección");
    LinearLayout filters=new LinearLayout(this);filters.setOrientation(0);filters.setGravity(Gravity.CENTER);root.addView(filters);
    String[] categories={"Todo","Sombreros","Accesorios","Ropa"};int[] ids={0,1,2,3};
    for(int i=0;i<categories.length;i++){final int category=ids[i];Button chip=btn(categories[i]);chip.setTextSize(11);filters.addView(chip,new LinearLayout.LayoutParams(0,dp(42),1));chip.setOnClickListener(v->renderShopItems(category));}
    shopBox=new LinearLayout(this);shopBox.setOrientation(1);root.addView(shopBox);renderShopItems(0);
    section("🎒 Mi armario");inventoryBox=new LinearLayout(this);inventoryBox.setOrientation(1);root.addView(inventoryBox);refresh();
  }
  void renderShopItems(int category){
    if(shopBox==null)return;shopBox.removeAllViews();ArrayList<Integer> ids=new ArrayList<>();
    for(int i=0;i<clothes.length;i++){boolean show=category==0||(category==1&&(i==0||i==1||i==2||i==5||i==9||i==10))||(category==2&&(i==4||i==6||i==7||i==8||i==11))||(category==3&&i==3);if(show)ids.add(i);}
    android.widget.GridLayout grid=new android.widget.GridLayout(this);grid.setColumnCount(3);grid.setUseDefaultMargins(false);shopBox.addView(grid);
    int width=getResources().getDisplayMetrics().widthPixels/3-dp(25);
    for(int n=0;n<ids.size();n++){final int ix=ids.get(n);LinearLayout item=card(0xFFFFF0E5);item.setOrientation(1);item.setGravity(Gravity.CENTER);item.setPadding(dp(3),dp(5),dp(3),dp(6));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(width,dp(205));lp.setMargins(dp(3),dp(5),dp(3),dp(5));grid.addView(item,lp);
      TextView name=txt(clothes[ix],11,true);name.setGravity(Gravity.CENTER);name.setMaxLines(2);name.setMinHeight(dp(35));item.addView(name,new LinearLayout.LayoutParams(-1,dp(38)));
      FrameLayout stage=new FrameLayout(this);stage.setBackground(round(0xFFFFE4D5,dp(14)));PetView preview=new PetView(this);preview.setPet(pet());preview.setOutfit(ix);preview.setRoom(1);stage.addView(preview,new FrameLayout.LayoutParams(-1,-1));item.addView(stage,new LinearLayout.LayoutParams(-1,dp(103)));
      TextView price=txt("🪙 "+prices[ix],13,true);price.setTextColor(0xFF9A6A2F);price.setGravity(Gravity.CENTER);item.addView(price,new LinearLayout.LayoutParams(-1,dp(25)));
      Button b=btn(isOwned(ix)?"✓ Guardado":"Comprar");b.setTextSize(10);item.addView(b,new LinearLayout.LayoutParams(-1,dp(34)));b.setOnClickListener(v->{if(isOwned(ix)){put("outfit",ix);refresh();toast("¡Look equipado! 💖");}else buy(ix);});
    }
  }
  boolean isOwned(int ix){JSONArray owned=state.optJSONArray("owned");if(owned!=null)for(int i=0;i<owned.length();i++)if(owned.optInt(i)==ix)return true;return false;}
  void profilePage(){
    header("Tu espacio seguro y tus recuerdos ☁️");
    LinearLayout account=card(0xFFFFFFFF);account.setOrientation(1);account.setPadding(dp(16),dp(12),dp(16),dp(12));root.addView(account);
    LinearLayout photoRow=new LinearLayout(this);photoRow.setGravity(Gravity.CENTER_VERTICAL);photoRow.setOrientation(0);
    profilePhoto=new ImageView(this);LinearLayout.LayoutParams photoParams=new LinearLayout.LayoutParams(dp(88),dp(88));photoParams.rightMargin=dp(14);profilePhoto.setBackground(round(0xFFE4F2DD,dp(44)));profilePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);photoRow.addView(profilePhoto,photoParams);
    String savedPhoto=prefs.getString("profile_photo",null);if(savedPhoto!=null){try{profilePhoto.setImageURI(Uri.parse(savedPhoto));}catch(Exception ignored){}}
    Button choosePhoto=btn("📷 Elegir foto");photoRow.addView(choosePhoto);choosePhoto.setOnClickListener(v->{Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);pick.addCategory(Intent.CATEGORY_OPENABLE);pick.setType("image/*");startActivityForResult(pick,410);});account.addView(photoRow);
    account.addView(txt("💗 Tu perfil",20,true));
    EditText displayName=new EditText(this);displayName.setSingleLine(true);displayName.setHint("Tu nombre");displayName.setText(prefs.getString("profile_name",""));account.addView(displayName);
    Button saveProfile=btn("Guardar perfil");account.addView(saveProfile);saveProfile.setOnClickListener(v->{prefs.edit().putString("profile_name",displayName.getText().toString().trim()).apply();toast("Perfil guardado 💚");});
    account.addView(txt("Cuenta: "+prefs.getString("user_email","Sesión iniciada"),12,false));

    TextView session=txt("✓ Sesión iniciada",17,true);session.setTextColor(0xFF3C8B55);account.addView(session);
    TextView sessionInfo=txt("Tu cuenta está conectada. Tus monedas, mascotas y progreso se guardan en este dispositivo y se sincronizan con Supabase.",13,false);account.addView(sessionInfo);
    Button signOut=btn("Cerrar sesión");account.addView(signOut,new LinearLayout.LayoutParams(-1,dp(48)));
    signOut.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("¿Cerrar sesión?").setMessage("Tu progreso guardado permanecerá en este teléfono.").setNegativeButton("Cancelar",null).setPositiveButton("Cerrar sesión",(d,w)->{prefs.edit().putBoolean("signed_in",false).remove("token").remove("uid").apply();buildUI();}).show());
    section("📊 Tu aventura");root.addView(txt("🔥 Racha: "+num("streak")+" días",16,true));root.addView(txt("⭐ Nivel: "+Math.max(1,num("level")),16,true));root.addView(txt("🪙 Monedas: "+num("coins"),16,true));
    Button share=btn("📤 Compartir mi mascota");root.addView(share);share.setOnClickListener(v->sharePet());
    status=txt("☁️ Tu aventura está vinculada a tu cuenta.",12,false);root.addView(status);
  }
  void showTerms(){
    String text="MIMOPETS · TÉRMINOS Y PRIVACIDAD\\n\\n1. Datos de cuenta: usamos tu correo y los datos de autenticación gestionados por Supabase para crear y proteger tu cuenta. El acceso se realiza mediante correo electrónico y contraseña gestionados por Supabase.\\n\\n2. Datos del juego: guardamos el nombre de tu mascota, monedas, racha, nivel, artículos, habitación y progreso para conservar tu partida y sincronizarla cuando inicies sesión.\\n\\n3. Uso: estos datos se utilizan para iniciar sesión, guardar/restaurar tu partida y mantener las funciones del juego. No vendemos tus datos ni los usamos para publicidad personalizada.\\n\\n4. Almacenamiento: los datos de cuenta y sincronización se procesan mediante Supabase. El dispositivo también puede conservar una copia local.\\n\\n5. Seguridad y control: no compartas tu contraseña. Puedes dejar de usar la app cuando quieras; para solicitar eliminación de cuenta/datos, contacta al responsable de la app.\\n\\n6. Menores: si eres menor, pide a tu madre, padre o tutor que revise estos términos.\\n\\nAl continuar declaras que has leído esta información.";
    new AlertDialog.Builder(this).setTitle("Términos y condiciones").setMessage(text).setPositiveButton("Entendido",null).show();
  }
  android.graphics.drawable.GradientDrawable round(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
  LinearLayout card(int color){LinearLayout x=new LinearLayout(this);x.setBackground(round(color,dp(20)));return x;}
  TextView txt(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(48,35,68));if(bold)t.setTypeface(null,1);t.setPadding(4,dp(7),4,dp(7));return t;}
  void section(String s){TextView t=txt(s,17,true);t.setTextColor(0xFF47794B);t.setPadding(4,dp(16),4,dp(6));root.addView(t);}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xFF47794B);b.setTextSize(13);b.setAllCaps(false);b.setPadding(dp(4),dp(2),dp(4),dp(2));b.setBackground(round(0xFFE5F2DE,dp(16)));b.setElevation(dp(2));b.setSoundEffectsEnabled(true);b.setOnTouchListener((view,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_DOWN){view.animate().cancel();view.animate().scaleX(.93f).scaleY(.93f).rotation(-2.5f).translationY(dp(2)).alpha(.88f).setDuration(95).start();}else if(event.getAction()==android.view.MotionEvent.ACTION_UP){view.animate().cancel();view.animate().scaleX(1.04f).scaleY(1.04f).rotation(1.5f).translationY(-dp(2)).alpha(1f).setDuration(115).withEndAction(()->view.animate().scaleX(1f).scaleY(1f).rotation(0).translationY(0).setDuration(145).start()).start();}else if(event.getAction()==android.view.MotionEvent.ACTION_CANCEL){view.animate().scaleX(1f).scaleY(1f).rotation(0).translationY(0).alpha(1f).setDuration(130).start();}return false;});return b;}
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
  void refresh(){
    if(walletText!=null)walletText.setText("🪙 "+num("coins")+"     💎 "+num("gems"));
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


  void moodDialog(){
    String[] moods={"Feliz 😄","Triste 🥺","Cansado 😴","Enamorado 💗","Emocionado 🤩","Tranquilo 😌","Con energía ⚡"};
    new AlertDialog.Builder(this).setTitle("¿Cómo te sientes hoy?").setItems(moods,(d,w)->{String value=moods[w];try{state.put("mood",value);}catch(Exception ignored){}save();toast("Estado actualizado: "+value);showTab(1);}).setNegativeButton("Cancelar",null).show();
  }
  void gamesPage(){
    root.removeAllViews();header("¡Vamos a jugar y ganar monedas! 🎮");
    LinearLayout intro=card(0xFFE1F4E9);intro.setOrientation(1);intro.setPadding(dp(14),dp(10),dp(14),dp(10));root.addView(intro);intro.addView(txt("Completa los retos para ganar monedas. ¡Tu mascota te anima!",14,true));
    gameCard("🍓 Memoria de frutas","Encuentra las dos parejas iguales.",0xFFFFE4EC,()->playMemory());
    gameCard("💧 Puzzle de agua","Llena el vaso tocando 8 veces.",0xFFDDF1FF,()->playWater());
    gameCard("🍊 Juicy Merge","Combina frutas hasta llegar al nivel 5.",0xFFE3F6D9,()->playMerge());
    Button back=btn("← Volver al inicio");root.addView(back);back.setOnClickListener(v->showTab(0));
  }
  void gameCard(String title,String sub,int color,Runnable action){LinearLayout c=card(color);c.setOrientation(1);c.setPadding(dp(14),dp(8),dp(14),dp(8));c.addView(txt(title,19,true));c.addView(txt(sub,13,false));Button play=btn("🎮 Jugar");c.addView(play);root.addView(c);play.setOnClickListener(v->action.run());}
  void gameReward(int coins){try{state.put("coins",num("coins")+coins);state.put("gamesPlayed",num("gamesPlayed")+1);}catch(Exception ignored){}save();toast("¡Ganaste +"+coins+" monedas! 🪙");gamesPage();}
  void playWater(){final int[] taps={0};LinearLayout box=new LinearLayout(this);box.setOrientation(1);TextView count=txt("💧 0 / 8",24,true);count.setGravity(Gravity.CENTER);box.addView(count);Button fill=btn("🥤 Llenar vaso");box.addView(fill);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Puzzle de agua").setMessage("Toca hasta llenar el vaso.").setView(box).setNegativeButton("Salir",null).create();fill.setOnClickListener(v->{taps[0]++;count.setText("💧 "+taps[0]+" / 8");if(taps[0]>=8){dialog.dismiss();gameReward(15);}});dialog.show();}
  void playMerge(){final int[] level={1};LinearLayout box=new LinearLayout(this);box.setOrientation(1);TextView fruit=txt("🍊 Nivel 1",26,true);fruit.setGravity(Gravity.CENTER);box.addView(fruit);Button merge=btn("✨ Combinar fruta");box.addView(merge);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Juicy Merge").setMessage("Combina frutas cinco veces para ganar.").setView(box).setNegativeButton("Salir",null).create();merge.setOnClickListener(v->{level[0]++;fruit.setText(new String[]{"🍊","🍎","🍐","🍇","🍉","🍍"}[Math.min(5,level[0])]+" Nivel "+level[0]);if(level[0]>=5){dialog.dismiss();gameReward(20);}});dialog.show();}
  void playMemory(){
    final String[] fruits={"🍓","🍋","🍓","🍋"};java.util.Collections.shuffle(java.util.Arrays.asList(fruits));final int[] first={-1},matches={0};final boolean[] locked={false};
    LinearLayout grid=new LinearLayout(this);grid.setOrientation(1);Button[] cells=new Button[4];
    for(int row=0;row<2;row++){LinearLayout line=new LinearLayout(this);line.setOrientation(0);grid.addView(line);for(int col=0;col<2;col++){int ix=row*2+col;cells[ix]=btn("❔");line.addView(cells[ix],new LinearLayout.LayoutParams(0,dp(76),1));}}
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Memoria de frutas").setMessage("Encuentra las dos parejas.").setView(grid).setNegativeButton("Salir",null).create();
    for(int i=0;i<4;i++){final int ix=i;cells[i].setOnClickListener(v->{if(locked[0]||cells[ix].getText().toString().equals(fruits[ix]))return;cells[ix].setText(fruits[ix]);if(first[0]<0){first[0]=ix;}else{int prev=first[0];if(fruits[prev].equals(fruits[ix])){matches[0]++;first[0]=-1;if(matches[0]>=2){dialog.dismiss();gameReward(25);}}else{locked[0]=true;new android.os.Handler().postDelayed(()->{cells[prev].setText("❔");cells[ix].setText("❔");first[0]=-1;locked[0]=false;},650);}}});}
    dialog.show();
  }
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
      if(mode.equals("signup")&&(access.length()==0||user==null)){
        runOnUiThread(()->{if(status!=null)status.setText("Te enviamos un código de 6 dígitos. Revisa tu correo 💌");showOtpDialog(email);});
        return;
      }
      if(access.length()==0||user==null)throw new Exception("No se recibió una sesión válida. Comprueba tus datos.");
      saveSignedInUser(access,user);
      runOnUiThread(()->{buildUI();showTab(0);if(status!=null)status.setText("Sesión iniciada. Cuenta vinculada.");toast("¡Bienvenido! ❤️");});
    }catch(Exception e){String msg=e.getMessage()==null?"Error de conexión":e.getMessage();runOnUiThread(()->{if(status!=null)status.setText("No se pudo completar: "+msg);});}}).start();
  }
  void incubatorCard(){
    LinearLayout eggCard=card(0xFFFFEBC8);eggCard.setOrientation(1);eggCard.setGravity(Gravity.CENTER);eggCard.setPadding(dp(12),dp(10),dp(12),dp(10));root.addView(eggCard);
    TextView egg=txt("🥚",54,true);egg.setGravity(Gravity.CENTER);eggCard.addView(egg);
    TextView title=txt("Huevo de "+state.optString("egg_pet","mascota"),17,true);title.setGravity(Gravity.CENTER);eggCard.addView(title);
    TextView timer=txt("",20,true);timer.setGravity(Gravity.CENTER);eggCard.addView(timer);
    TextView hint=txt("Tu mascota actual sigue contigo mientras este huevo incuba.",12,false);hint.setGravity(Gravity.CENTER);eggCard.addView(hint);
    Runnable tick=new Runnable(){public void run(){if(timer.getParent()==null)return;long left=10800000L-(System.currentTimeMillis()-state.optLong("egg_started",System.currentTimeMillis()));if(left<=0){timer.setText("¡Listo para nacer! 🎉");egg.setText("🐣");Button hatch=btn("Abrir huevo");eggCard.addView(hatch);hatch.setOnClickListener(v->hatchEgg());}else{long sec=left/1000;timer.setText(String.format(Locale.getDefault(),"%02d:%02d:%02d",sec/3600,(sec%3600)/60,sec%60));new Handler().postDelayed(this,1000);}}};tick.run();
  }
  void showOtpDialog(String email){
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(18),dp(8),dp(18),dp(4));
    TextView info=txt("Escribe el código de 6 dígitos que enviamos a:",14,false);box.addView(info);
    TextView address=txt(email,14,true);address.setTextColor(0xFF47794B);box.addView(address);
    EditText code=new EditText(this);code.setSingleLine(true);code.setHint("000000");code.setTextSize(24);code.setGravity(Gravity.CENTER);code.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);code.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});box.addView(code);
    TextView resend=txt("¿No llegó? Reenviar código",13,true);resend.setTextColor(0xFFB45D8B);box.addView(resend);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Confirma tu correo 💌").setView(box).setPositiveButton("Verificar",null).setNegativeButton("Después",null).create();
    resend.setOnClickListener(v->resendSignupCode(email));
    dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String otp=code.getText().toString().trim();if(!otp.matches("[0-9]{6}")){code.setError("Escribe los 6 números");return;}verifySignupCode(email,otp,dialog);}));
    dialog.show();
  }
  void resendSignupCode(String email){
    new Thread(()->{try{JSONObject body=new JSONObject();body.put("type","signup");body.put("email",email);
      HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/resend").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(20000);c.setReadTimeout(20000);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(body.toString().getBytes("UTF-8"));
      int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));runOnUiThread(()->toast("Te enviamos otro código 💌"));
    }catch(Exception e){runOnUiThread(()->toast("No se pudo reenviar: "+e.getMessage()));}}).start();
  }
  void verifySignupCode(String email,String otp,AlertDialog dialog){
    new Thread(()->{try{JSONObject body=new JSONObject();body.put("type","signup");body.put("email",email);body.put("token",otp);
      HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/verify").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(20000);c.setReadTimeout(20000);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(body.toString().getBytes("UTF-8"));
      int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));JSONObject result=new JSONObject(response);String access=result.optString("access_token","");JSONObject user=result.optJSONObject("user");if(access.isEmpty()||user==null)throw new Exception("El código no generó una sesión. Intenta iniciar sesión.");
      saveSignedInUser(access,user);runOnUiThread(()->{dialog.dismiss();buildUI();showTab(0);toast("¡Correo confirmado! Bienvenido a MimoPets 💚");});
    }catch(Exception e){runOnUiThread(()->toast("Código no válido o vencido: "+e.getMessage()));}}).start();
  }
  String authError(String response){try{JSONObject e=new JSONObject(response);return e.optString("msg",e.optString("message",e.optString("error_description",response)));}catch(Exception ex){return response;}}
  void saveSignedInUser(String access,JSONObject user)throws Exception{
    String uid=user.getString("id");prefs.edit().putString("token",access).putString("uid",uid).putString("user_email",user.optString("email","")).putBoolean("signed_in",true).apply();
    HttpURLConnection get=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?id=eq."+uid+"&select=data").openConnection();get.setRequestProperty("apikey",KEY);get.setRequestProperty("Authorization","Bearer "+access);get.setConnectTimeout(15000);get.setReadTimeout(15000);
    if(get.getResponseCode()==200){JSONArray rows=new JSONArray(readResponse(get));if(rows.length()>0){JSONObject cloud=rows.getJSONObject(0).optJSONObject("data");if(cloud!=null&&cloud.length()>0){JSONObject merged=new JSONObject(cloud.toString());java.util.Iterator<String> it=state.keys();while(it.hasNext()){String key=it.next();merged.put(key,state.get(key));}state=merged;}}}
    if(state.optString("pet","").length()>0){state.put("pet_selected",true);state.put("egg_hatched",true);state.remove("egg_started");state.remove("egg_pet");}
    prefs.edit().putString("state",state.toString()).putLong("state_saved_at",System.currentTimeMillis()).apply();refreshWidget();syncCloud();
  }
  void recoverPassword(){
    String email=emailInput.getText().toString().trim();if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){toast("Escribe primero tu correo electrónico.");return;}
    new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/recover?redirect_to=mimopets://auth-callback").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(new JSONObject().put("email",email).toString().getBytes("UTF-8"));int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));runOnUiThread(()->{if(status!=null)status.setText("Si el correo existe, recibirás un enlace para restablecer la contraseña.");toast("Revisa tu correo 💌");});}catch(Exception e){runOnUiThread(()->toast("No se pudo enviar el enlace: "+e.getMessage()));}}).start();
  }
  
  @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
    super.onActivityResult(requestCode,resultCode,data);
    if(requestCode==410&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
      Uri photo=data.getData();try{getContentResolver().takePersistableUriPermission(photo,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
      prefs.edit().putString("profile_photo",photo.toString()).apply();if(profilePhoto!=null)profilePhoto.setImageURI(photo);toast("¡Foto de perfil actualizada! 📸");
    }
  }
  @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handleAuthIntent(intent);}
  void handleAuthIntent(Intent intent){
    if(intent==null||intent.getData()==null)return;
    Uri data=intent.getData();String fragment=data.getFragment();String token=null;String type="";
    if(fragment!=null)for(String part:fragment.split("&")){String[] kv=part.split("=",2);if(kv.length==2){if(kv[0].equals("access_token"))token=Uri.decode(kv[1]);if(kv[0].equals("type"))type=Uri.decode(kv[1]);}}
    if(token==null)token=data.getQueryParameter("access_token");
    if(token==null){String error=data.getQueryParameter("error_description");if(error!=null)runOnUiThread(()->toast("No se pudo completar el acceso: "+error));return;}
    final String access=token;
    if("recovery".equals(type)){prefs.edit().putString("recovery_token",access).apply();runOnUiThread(()->newPasswordDialog(access));return;}
    new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/user").openConnection();c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+access);int code=c.getResponseCode();if(code<200||code>=300)throw new Exception(readResponse(c));JSONObject user=new JSONObject(readResponse(c));saveSignedInUser(access,user);runOnUiThread(()->{buildUI();showTab(0);if(status!=null)status.setText("Sesión iniciada con éxito.");toast("¡Hola! 💚");});}catch(Exception e){runOnUiThread(()->toast("No se pudo validar la cuenta: "+e.getMessage()));}}).start();
  }
  @Override public void onBackPressed(){if(!prefs.getBoolean("signed_in",false)){super.onBackPressed();return;}showTab(0);}
  void newPasswordDialog(String access){
    EditText input=new EditText(this);input.setHint("Nueva contraseña (mínimo 6 caracteres)");input.setInputType(129);
    new AlertDialog.Builder(this).setTitle("Crear nueva contraseña").setView(input).setPositiveButton("Guardar",(d,w)->{String pass=input.getText().toString();if(pass.length()<6){toast("Usa al menos 6 caracteres.");return;}new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(URL+"/auth/v1/user").openConnection();c.setRequestMethod("PUT");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+access);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(new JSONObject().put("password",pass).toString().getBytes("UTF-8"));int code=c.getResponseCode();String response=readResponse(c);if(code<200||code>=300)throw new Exception(authError(response));runOnUiThread(()->toast("Contraseña actualizada. Ya puedes iniciar sesión."));}catch(Exception e){runOnUiThread(()->toast("No se pudo cambiar: "+e.getMessage()));}}).start();}).setNegativeButton("Cancelar",null).show();
  }
  String readResponse(HttpURLConnection c)throws Exception{InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();if(in==null)return "";ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}
  void syncCloud(){if(!prefs.getBoolean("signed_in",false))return;new Thread(()->{try{
    String token=prefs.getString("token",null);if(token==null)throw new Exception("No hay sesión activa");
    String uid=prefs.getString("uid","");JSONObject row=new JSONObject();row.put("id",uid);row.put("data",state);row.put("updated_at",new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).format(new Date()));
    HttpURLConnection c=(HttpURLConnection)new URL(URL+"/rest/v1/pet_saves?on_conflict=id").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("apikey",KEY);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","resolution=merge-duplicates");c.getOutputStream().write(new JSONArray().put(row).toString().getBytes("UTF-8"));int code=c.getResponseCode();if(code<200||code>=300)throw new Exception("Supabase HTTP "+code);runOnUiThread(()->{if(status!=null)status.setText("☁️ Avances sincronizados con Supabase");});
  }catch(Exception e){runOnUiThread(()->{if(status!=null)status.setText("📱 Guardado en este teléfono · nube pendiente");});}}).start();}
  String read(HttpURLConnection c)throws Exception{InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while((n=in.read(buf))>0)b.write(buf,0,n);return b.toString("UTF-8");}

}
