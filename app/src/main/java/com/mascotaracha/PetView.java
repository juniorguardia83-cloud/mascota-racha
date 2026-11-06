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
  private long started=System.currentTimeMillis();
  public PetView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  public void setPet(String value){animal=value;invalidate();}
  public void setOutfit(int value){outfit=value;invalidate();}
  public void pet(){started=System.currentTimeMillis();invalidate();}
  private void circle(Canvas c,float x,float y,float r,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,p);}
  private void oval(Canvas c,float l,float t,float r,float b,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawOval(new RectF(l,t,r,b),p);}
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);float w=getWidth(),h=getHeight();canvas.drawColor(Color.rgb(249,232,255));
    canvas.save();canvas.translate(w/2f,h*.54f);float s=Math.min(w/260f,h/250f);canvas.scale(s,s);
    float bounce=(float)Math.sin((System.currentTimeMillis()-started)/180.0)*3;
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
    canvas.restore();
    postInvalidateDelayed(90);
  }
  static class PathHelper {static void star(Canvas c,Paint p,float x,float y,float r){android.graphics.Path path=new android.graphics.Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?r:r*.48f;float px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}path.close();c.drawPath(path,p);}}
}
