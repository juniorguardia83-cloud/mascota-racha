package com.mascotaracha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Mascota 2D dibujada con Canvas, sin modelos 3D. */
public class PetView extends View {
  private final Paint p=new Paint(3);
  private String animal="Perrito";
  private int outfit=-1;
  private int room=0;
  private long started=System.currentTimeMillis();
  public PetView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  public void setPet(String value){animal=value;invalidate();}
  public void setOutfit(int value){outfit=value;invalidate();}
  public void setRoom(int value){room=value;invalidate();}
  public void pet(){started=System.currentTimeMillis();invalidate();}
  private float dp(float v){return v*getResources().getDisplayMetrics().density;}
  private void circle(Canvas c,float x,float y,float r,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
  private void oval(Canvas c,float l,float t,float r,float b,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawOval(new RectF(l,t,r,b),p);}
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);float w=getWidth(),h=getHeight();
    int bg=room==1?0xFFFFE8F0:room==2?0xFFE8F2FF:room==3?0xFFE2FAF5:0xFFFFF0D8;canvas.drawColor(bg);
    // Decoración distinta y reconocible en cada habitación.
    if(room==0){circle(canvas,w*.18f,h*.20f,dp(13),0xFFFFB7CB);circle(canvas,w*.22f,h*.17f,dp(9),0xFFFFD36E);oval(canvas,w*.08f,h*.70f,w*.30f,h*.84f,0xFF9BD28B);oval(canvas,w*.70f,h*.68f,w*.92f,h*.84f,0xFF9BD28B);}
    else if(room==1){oval(canvas,w*.12f,h*.57f,w*.88f,h*.87f,0xFFFFC7D9);oval(canvas,w*.18f,h*.53f,w*.82f,h*.79f,0xFFFFF8FB);oval(canvas,w*.15f,h*.48f,w*.85f,h*.62f,0xFFB7D8F5);circle(canvas,w*.78f,h*.18f,dp(20),0xFFFFD36E);}
    else if(room==2){oval(canvas,w*.08f,h*.57f,w*.92f,h*.88f,0xFFB7C9E8);oval(canvas,w*.15f,h*.52f,w*.85f,h*.78f,0xFFE7DDF8);oval(canvas,w*.05f,h*.28f,w*.28f,h*.56f,0xFF8BCB9A);oval(canvas,w*.72f,h*.28f,w*.95f,h*.56f,0xFF8BCB9A);}
    else {oval(canvas,w*.08f,h*.60f,w*.92f,h*.88f,0xFFB9E9E0);oval(canvas,w*.12f,h*.56f,w*.88f,h*.79f,0xFFEFFFFB);circle(canvas,w*.78f,h*.23f,dp(25),0xFF9DD8F4);circle(canvas,w*.78f,h*.23f,dp(15),0xFFEFFFFB);}
    p.setColor(0x22FFFFFF);p.setStyle(Paint.Style.FILL);canvas.drawCircle(w*.18f,h*.18f,dp(42),p);canvas.drawCircle(w*.82f,h*.25f,dp(52),p);
    p.setColor(0x337A547F);canvas.drawRect(0,h*.82f,w,h,p);
    canvas.save();canvas.translate(w/2f,h*.54f);float s=Math.min(w/260f,h/250f);canvas.scale(s,s);
    long elapsed=System.currentTimeMillis()-started;
    float bounce=(float)Math.sin(elapsed/180.0)*4;
    float sway=(float)Math.sin(elapsed/420.0)*5;
    canvas.save();canvas.rotate(sway,w/2f,h*.55f);
    int fur=animal.equals("Pandita")?Color.rgb(45,42,48):animal.equals("Gatito")?Color.rgb(245,177,205):animal.equals("Conejito")?Color.rgb(238,224,207):Color.rgb(207,151,105);
    int dark=Color.rgb(65,48,52);
    // sombra, cuerpo y patas
    oval(canvas,-76,88,76,110,0x337D568B);
    oval(canvas,-65,5+bounce,65,100+bounce,fur);
    oval(canvas,-48,65+bounce,-17,104+bounce,fur);oval(canvas,17,65+bounce,48,104+bounce,fur);
    // orejas según mascota
    if(animal.equals("Conejito")){oval(canvas,-62,-128+bounce,-22,-25+bounce,fur);oval(canvas,22,-128+bounce,62,-25+bounce,fur);oval(canvas,-53,-115+bounce,-34,-39+bounce,0xFFFFB6C8);oval(canvas,34,-115+bounce,53,-39+bounce,0xFFFFB6C8);}
    else if(animal.equals("Gatito")){oval(canvas,-66,-112+bounce,-18,-42+bounce,fur);oval(canvas,18,-112+bounce,66,-42+bounce,fur);oval(canvas,-53,-94+bounce,-32,-55+bounce,0xFFFFB6C8);oval(canvas,32,-94+bounce,53,-55+bounce,0xFFFFB6C8);}
    else {circle(canvas,-43,-72+bounce,29,fur);circle(canvas,43,-72+bounce,29,fur);if(animal.equals("Pandita")){circle(canvas,-43,-72+bounce,14,Color.WHITE);circle(canvas,43,-72+bounce,14,Color.WHITE);}}
    // cabeza
    circle(canvas,0,-40+bounce,69,fur);
    if(animal.equals("Pandita")){oval(canvas,-55,-78+bounce,-10,-4+bounce,Color.WHITE);oval(canvas,10,-78+bounce,55,-4+bounce,Color.WHITE);}
    circle(canvas,-25,-48+bounce,6,dark);circle(canvas,25,-48+bounce,6,dark);
    oval(canvas,-10,-22+bounce,10,-8+bounce,Color.rgb(242,130,157));
    // mejillas
    circle(canvas,-43,-22+bounce,9,0x55FF7799);circle(canvas,43,-22+bounce,9,0x55FF7799);
    if(outfit==0){oval(canvas,-25,-115+bounce,25,-91+bounce,0xFFFF6FAE);circle(canvas,0,-104+bounce,9,0xFFFFD45C);}
    else if(outfit==1){p.setColor(0xFFFFD34D);p.setStyle(Paint.Style.FILL);PathHelper.star(canvas,p,0,-118+bounce,22);}
    else if(outfit==2){oval(canvas,-45,-57+bounce,-5,-37+bounce,Color.BLACK);oval(canvas,5,-57+bounce,45,-37+bounce,Color.BLACK);p.setColor(Color.BLACK);p.setStrokeWidth(5);canvas.drawLine(-8,-48+bounce,8,-48+bounce,p);}
    else if(outfit==3){oval(canvas,-37,-2+bounce,37,15+bounce,0xFF8BC8F0);}
    else if(outfit==4){circle(canvas,0,-110+bounce,18,0xFFFF8FB5);circle(canvas,-14,-110+bounce,13,0xFFFFC4D8);circle(canvas,14,-110+bounce,13,0xFFFFC4D8);}
    else if(outfit==5){circle(canvas,0,-115+bounce,17,0xFFFFD34D);circle(canvas,-22,-109+bounce,9,0xFFFFE58A);circle(canvas,22,-109+bounce,9,0xFFFFE58A);}
    else if(outfit==6){oval(canvas,-92,4+bounce,-55,60+bounce,0xFF9D8CF5);oval(canvas,55,4+bounce,92,60+bounce,0xFF9D8CF5);}
    else if(outfit==7){oval(canvas,-28,3+bounce,28,17+bounce,0xFF65D9D0);circle(canvas,0,10+bounce,7,0xFFFFE27A);}
    else if(outfit==8){oval(canvas,-62,-4+bounce,62,28+bounce,0xFFFFC84A);circle(canvas,-32,-2+bounce,9,Color.BLACK);circle(canvas,32,-2+bounce,9,Color.BLACK);}
    else if(outfit==9){oval(canvas,-42,-111+bounce,42,-91+bounce,0xFF61B7E8);oval(canvas,-48,-96+bounce,48,-88+bounce,0xFF3E8FC5);}
    else if(outfit==10){oval(canvas,-26,-113+bounce,0,-92+bounce,0xFFFF79A8);oval(canvas,0,-113+bounce,26,-92+bounce,0xFFFF79A8);circle(canvas,0,-103+bounce,8,0xFFFFD45C);}
    else if(outfit==11){oval(canvas,-72,5+bounce,72,48+bounce,0xFFB78CF4);oval(canvas,-55,20+bounce,55,57+bounce,0xFFFF9ACD);}
    canvas.restore();
    postInvalidateDelayed(90);
  }
  static class PathHelper {static void star(Canvas c,Paint p,float x,float y,float r){android.graphics.Path path=new android.graphics.Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r:r*.48f;float px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();c.drawPath(path,p);}}
}
