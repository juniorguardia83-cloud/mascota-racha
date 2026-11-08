package com.mascotaracha;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Personajes originales 2D de MimoPets y escenarios ilustrados. */
public class PetView extends View {
  private final Paint p=new Paint(3);
  private String animal="Perrito";
  private int outfit=-1,room=0;
  private long started=System.currentTimeMillis(),reactionUntil=0,sitUntil=0;
  public PetView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
  public void setPet(String value){animal=value;invalidate();}
  public void setOutfit(int value){outfit=value;invalidate();}
  public void setRoom(int value){room=value;invalidate();}
  public void pet(){started=System.currentTimeMillis();reactionUntil=started+1500;invalidate();}
  public void sit(){sitUntil=System.currentTimeMillis()+2600;pet();}
  private void fill(Canvas c,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(1);}
  private void circle(Canvas c,float x,float y,float r,int color){fill(c,color);c.drawCircle(x,y,r,p);}
  private void oval(Canvas c,float l,float t,float r,float b,int color){fill(c,color);c.drawOval(new RectF(l,t,r,b),p);}
  private void round(Canvas c,float l,float t,float r,float b,float radius,int color){fill(c,color);c.drawRoundRect(new RectF(l,t,r,b),radius,radius,p);}
  private void line(Canvas c,float x1,float y1,float x2,float y2,int color,float width){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}
  private void text(Canvas c,String value,float x,float y,int size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-rounded",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);c.drawText(value,x,y,p);}
  private int fur(){return animal.equals("Pandita")?0xFF33323B:animal.equals("Gatito")?0xFFFFD4D9:animal.equals("Conejito")?0xFFF2E6D6:animal.equals("Zorrito")?0xFFFFA66D:animal.equals("Osito")?0xFFB98769:0xFFD99C70;}
  private void drawRoom(Canvas c,float w,float h){
    int bg=room==1?0xFFFFEAF1:room==2?0xFFEAF1FF:room==3?0xFFE1F8F2:room==4?0xFFFFEFD8:room==5?0xFFF8F8F2:0xFFFFF2D9;
    c.drawColor(bg);
    if(room==1){ // dormitorio
      round(c,w*.05f,h*.18f,w*.95f,h*.73f,dp(18),0xFFFFD5E2);round(c,w*.10f,h*.27f,w*.90f,h*.72f,dp(16),0xFFFFF8FC);
      round(c,w*.13f,h*.30f,w*.47f,h*.49f,dp(13),0xFFB9D7F5);round(c,w*.52f,h*.30f,w*.86f,h*.49f,dp(13),0xFFFFC1D4);
      round(c,w*.72f,h*.53f,w*.93f,h*.78f,dp(8),0xFFB98468);circle(c,w*.82f,h*.49f,dp(12),0xFFFFD36A);round(c,w*.79f,h*.38f,w*.85f,h*.50f,dp(4),0xFFFFE7A4);
      text(c,"Z z z",w*.76f,h*.24f,dp(17),0xFF9A8BC9);
    }else if(room==2){ // sala
      round(c,w*.04f,h*.49f,w*.42f,h*.78f,dp(20),0xFF86C9A0);round(c,w*.02f,h*.42f,w*.44f,h*.61f,dp(18),0xFF9DDBB4);
      round(c,w*.56f,h*.59f,w*.94f,h*.76f,dp(14),0xFFB98769);round(c,w*.60f,h*.55f,w*.90f,h*.66f,dp(10),0xFFD7A98B);
      round(c,w*.42f,h*.72f,w*.60f,h*.80f,dp(9),0xFFFFD27C);circle(c,w*.50f,h*.69f,dp(8),0xFFFFF0B5);
      round(c,w*.83f,h*.18f,w*.94f,h*.48f,dp(7),0xFFFFD36E);line(c,w*.885f,h*.48f,w*.885f,h*.60f,0xFF9A744F,dp(4));
      text(c,"♡",w*.20f,h*.37f,dp(20),0xFFFFFFFF);
    }else if(room==3){ // baño
      round(c,w*.07f,h*.13f,w*.36f,h*.63f,dp(17),0xFFB8E6F5);line(c,w*.09f,h*.22f,w*.34f,h*.22f,0xFFFFFFFF,dp(4));
      for(int j=0;j<4;j++){circle(c,w*(.14f+j*.055f),h*(.30f+(j%2)*.05f),dp(3),0xFFFFFFFF);}
      oval(c,w*.62f,h*.50f,w*.91f,h*.72f,0xFFFFFFFF);round(c,w*.64f,h*.37f,w*.88f,h*.58f,dp(12),0xFFFFFFFF);oval(c,w*.68f,h*.43f,w*.84f,h*.52f,0xFFB7E9F4);
      round(c,w*.43f,h*.16f,w*.51f,h*.43f,dp(8),0xFF9DC6D5);line(c,w*.47f,h*.18f,w*.58f,h*.18f,0xFF82B6C8,dp(5));
      for(int j=0;j<5;j++)line(c,w*(.40f+j*.045f),h*.43f,w*(.37f+j*.055f),h*.55f,0xFF73CBEA,dp(2));
      text(c,"✦",w*.77f,h*.31f,dp(20),0xFF83CDE3);
    }else if(room==4){ // cocina
      round(c,w*.72f,h*.15f,w*.94f,h*.72f,dp(13),0xFFB9D7E8);round(c,w*.75f,h*.19f,w*.91f,h*.43f,dp(8),0xFFEAF8FF);round(c,w*.75f,h*.48f,w*.91f,h*.68f,dp(8),0xFFEAF8FF);
      circle(c,w*.88f,h*.45f,dp(3),0xFF6E9CB1);circle(c,w*.88f,h*.57f,dp(3),0xFF6E9CB1);
      round(c,w*.07f,h*.50f,w*.60f,h*.62f,dp(10),0xFFBD8A65);round(c,w*.10f,h*.44f,w*.57f,h*.53f,dp(7),0xFFE5B98D);
      for(int j=0;j<3;j++)circle(c,w*(.22f+j*.12f),h*.43f,dp(8),0xFF777777);
      round(c,w*.19f,h*.65f,w*.43f,h*.78f,dp(12),0xFFFFF1C9);oval(c,w*.22f,h*.63f,w*.40f,h*.70f,0xFFFFD36C);text(c,"🍎",w*.31f,h*.72f,dp(16),0xFFB96A39);
    }else if(room==5){ // escenario limpio para miniaturas
      round(c,w*.10f,h*.60f,w*.90f,h*.87f,dp(30),0xFFFFE8EF);
    }else{ // jardín
      circle(c,w*.18f,h*.22f,dp(18),0xFFFFD36E);round(c,w*.72f,h*.43f,w*.85f,h*.78f,dp(10),0xFF9A714E);circle(c,w*.78f,h*.35f,dp(30),0xFF8BCB8A);
      for(int j=0;j<5;j++){float x=w*(.12f+j*.18f);circle(c,x,h*.77f,dp(7),j%2==0?0xFFFF9FC4:0xFFFFD36E);circle(c,x,h*.77f,dp(2),0xFFFFF5C8);}
      oval(c,w*.05f,h*.80f,w*.95f,h*.91f,0xFF9BD28B);
    }
    // floor and soft highlights
    p.setColor(0x20FFFFFF);p.setStyle(Paint.Style.FILL);c.drawCircle(w*.12f,h*.14f,dp(30),p);c.drawCircle(w*.88f,h*.12f,dp(34),p);
  }
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);float w=getWidth(),h=getHeight();drawRoom(canvas,w,h);
    canvas.save();canvas.translate(w/2f,h*.58f);float s=Math.min(w/260f,h/270f);canvas.scale(s,s);
    long elapsed=System.currentTimeMillis()-started;boolean reacting=System.currentTimeMillis()<reactionUntil,sitting=System.currentTimeMillis()<sitUntil;
    float idle=(float)Math.sin(elapsed/520.0),bounce=idle*2+(reacting?(float)Math.abs(Math.sin(elapsed/75.0))*11:0);
    float sway=(float)Math.sin(elapsed/430.0)*3;int fur=fur(),dark=0xFF49353A;
    canvas.save();canvas.rotate(sway+(reacting?(float)Math.sin(elapsed/115.0)*8:0),0,0);
    // tail and ears
    canvas.save();canvas.rotate((float)Math.sin(elapsed/180.0)*19,53,42+bounce);oval(canvas,48,24+bounce,91,61+bounce,fur);circle(canvas,88,45+bounce,10,fur);canvas.restore();
    if(animal.equals("Conejito")){oval(canvas,-62,-137+bounce,-22,-22+bounce,fur);oval(canvas,22,-137+bounce,62,-22+bounce,fur);oval(canvas,-52,-121+bounce,-34,-38+bounce,0xFFFFB6C8);oval(canvas,34,-121+bounce,52,-38+bounce,0xFFFFB6C8);}
    else if(animal.equals("Gatito")){android.graphics.Path ears=new android.graphics.Path();ears.moveTo(-65,-42+bounce);ears.lineTo(-59,-119+bounce);ears.quadTo(-34,-105+bounce,-20,-57+bounce);ears.close();fill(canvas,fur);canvas.drawPath(ears,p);ears.reset();ears.moveTo(65,-42+bounce);ears.lineTo(59,-119+bounce);ears.quadTo(34,-105+bounce,20,-57+bounce);ears.close();canvas.drawPath(ears,p);oval(canvas,-53,-99+bounce,-35,-58+bounce,0xFFFFA8B8);oval(canvas,35,-99+bounce,53,-58+bounce,0xFFFFA8B8);}
    else{circle(canvas,-43,-76+bounce,29,fur);circle(canvas,43,-76+bounce,29,fur);if(animal.equals("Pandita")){circle(canvas,-43,-76+bounce,15,0xFF17171C);circle(canvas,43,-76+bounce,15,0xFF17171C);}}
    // arms, body, outfit and feet
    oval(canvas,-65,5+bounce,65,101+bounce,fur);
    canvas.save();canvas.rotate((float)Math.sin(elapsed/170.0)*17,-57,44+bounce);oval(canvas,-78,25+bounce,-50,70+bounce,fur);canvas.restore();
    canvas.save();canvas.rotate((float)Math.sin(elapsed/170.0+1)*17,57,44+bounce);oval(canvas,50,25+bounce,78,70+bounce,fur);canvas.restore();
    if(sitting){oval(canvas,-55,66+bounce,-4,91+bounce,fur);oval(canvas,4,66+bounce,55,91+bounce,fur);oval(canvas,-54,82+bounce,-5,105+bounce,0xFFFFDCC7);oval(canvas,5,82+bounce,54,105+bounce,0xFFFFDCC7);}
    else{oval(canvas,-47,66+bounce,-16,109+bounce,fur);oval(canvas,16,66+bounce,47,109+bounce,fur);oval(canvas,-52,94+bounce,-10,111+bounce,0xFFFFDCC7);oval(canvas,10,94+bounce,52,111+bounce,0xFFFFDCC7);}
    // cute clothing
    round(canvas,-55,15+bounce,55,78+bounce,22,outfit==8?0xFFFFC84A:outfit==11?0xFFB78CF4:outfit==3?0xFF8BC8F0:0xFFFFA8C7);
    oval(canvas,-19,14+bounce,19,38+bounce,0xFFFFF4F1);
    if(outfit==0||outfit==10){circle(canvas,-17,22+bounce,11,0xFFFF78AA);circle(canvas,17,22+bounce,11,0xFFFF78AA);circle(canvas,0,22+bounce,8,0xFFFFD45C);}
    // head and muzzle
    circle(canvas,0,-40+bounce,69,fur);
    if(animal.equals("Pandita")){oval(canvas,-56,-79+bounce,-9,-7+bounce,0xFF17171C);oval(canvas,9,-79+bounce,56,-7+bounce,0xFF17171C);oval(canvas,-48,-72+bounce,-15,-18+bounce,0xFFFFFFFF);oval(canvas,15,-72+bounce,48,-18+bounce,0xFFFFFFFF);}
    else if(animal.equals("Perrito")){oval(canvas,-65,-57+bounce,-43,-3+bounce,0xFFC4875F);oval(canvas,43,-57+bounce,65,-3+bounce,0xFFC4875F);}
    boolean blink=(elapsed%4300)<150;circle(canvas,-25,-48+bounce,blink?1.4f:7,dark);circle(canvas,25,-48+bounce,blink?1.4f:7,dark);
    circle(canvas,-43,-22+bounce,10,0x55FF7799);circle(canvas,43,-22+bounce,10,0x55FF7799);
    oval(canvas,-16,-22+bounce,16,2+bounce,0xFFFFE6D9);circle(canvas,0,-14+bounce,5,dark);
    p.setColor(dark);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setStrokeCap(Paint.Cap.ROUND);canvas.drawArc(new RectF(-10,-12+bounce,0,-2+bounce),0,90,false,p);canvas.drawArc(new RectF(0,-12+bounce,10,-2+bounce),90,90,false,p);p.setStyle(Paint.Style.FILL);
    // accessories
    if(outfit==1){circle(canvas,0,-112+bounce,20,0xFFFFD34D);Path star=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float r=i%2==0?20:9;float x=(float)Math.cos(a)*r,y=-112+bounce+(float)Math.sin(a)*r;if(i==0)star.moveTo(x,y);else star.lineTo(x,y);}star.close();fill(canvas,0xFFFFF2A5);canvas.drawPath(star,p);}
    else if(outfit==2){round(canvas,-49,-60+bounce,-5,-35+bounce,9,0xFF3C4657);round(canvas,5,-60+bounce,49,-35+bounce,9,0xFF3C4657);line(canvas,-7,-48+bounce,7,-48+bounce,dark,4);}
    else if(outfit==4){circle(canvas,0,-111+bounce,18,0xFFFF8FB5);circle(canvas,-14,-111+bounce,12,0xFFFFC4D8);circle(canvas,14,-111+bounce,12,0xFFFFC4D8);}
    else if(outfit==6){oval(canvas,-98,4+bounce,-55,62+bounce,0xFF9D8CF5);oval(canvas,55,4+bounce,98,62+bounce,0xFF9D8CF5);}
    else if(outfit==7){round(canvas,-30,5+bounce,30,18+bounce,6,0xFF65D9D0);circle(canvas,0,11+bounce,7,0xFFFFE27A);}
    else if(outfit==9){round(canvas,-45,-111+bounce,45,-88+bounce,9,0xFF61B7E8);oval(canvas,-51,-94+bounce,51,-87+bounce,0xFF3E8FC5);}
    canvas.restore();canvas.restore();postInvalidateDelayed(55);
  }
}
