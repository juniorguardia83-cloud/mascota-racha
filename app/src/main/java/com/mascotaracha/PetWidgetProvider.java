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
    String pet="🐶 "+prefs.getString("pet","Perrito");
    int coins=0,streak=0;
    try{org.json.JSONObject s=new org.json.JSONObject(prefs.getString("state","{}"));coins=s.optInt("coins",0);streak=s.optInt("streak",0);}catch(Exception ignored){}
    for(int id:ids){
      RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.pet_widget);
      views.setTextViewText(R.id.widget_pet,pet);
      views.setTextViewText(R.id.widget_stats,"🔥 "+streak+" días  ·  🪙 "+coins);
      Intent launch=new Intent(context,MainActivity.class);
      PendingIntent pi=PendingIntent.getActivity(context,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
      views.setOnClickPendingIntent(R.id.widget_root,pi);
      manager.updateAppWidget(id,views);
    }
  }
}
