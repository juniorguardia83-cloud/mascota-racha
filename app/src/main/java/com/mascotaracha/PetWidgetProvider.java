package com.mascotaracha;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/** Widget de mascota para la pantalla de inicio. */
public class PetWidgetProvider extends AppWidgetProvider {
  @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
    SharedPreferences prefs=context.getSharedPreferences("petgame",Context.MODE_PRIVATE);
    String name="Perrito";int coins=0,streak=0;org.json.JSONObject state;
    try{state=new org.json.JSONObject(prefs.getString("state","{}"));name=state.optString("pet","Perrito");coins=state.optInt("coins",0);streak=state.optInt("streak",0);}catch(Exception ignored){state=new org.json.JSONObject();}
    for(int id:ids){
      RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.pet_widget);
      views.setTextViewText(R.id.widget_pet,name);
      String face=name.equals("Gatito")?"🐱":name.equals("Conejito")?"🐰":name.equals("Pandita")?"🐼":"🐶";
      views.setTextViewText(R.id.widget_face,face);
      int energy=state.optInt("energy",75),fun=state.optInt("fun",75);
      String message=energy<30?"Está muy somnoliento… quiere ir a la cama. 🌙":fun<30?"Está aburrido… ¿jugamos un ratito? 🎾":"¡Hola! Está feliz de verte. 💗";
      views.setTextViewText(R.id.widget_message,message);
      views.setTextViewText(R.id.widget_stats,"🔥 "+streak+" días  ·  🪙 "+coins+" monedas");
      Intent launch=new Intent(context,MainActivity.class);
      PendingIntent pi=PendingIntent.getActivity(context,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
      views.setOnClickPendingIntent(R.id.widget_root,pi);
      manager.updateAppWidget(id,views);
    }
  }
}
