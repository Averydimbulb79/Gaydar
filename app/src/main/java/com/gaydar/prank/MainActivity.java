package com.gaydar.prank;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.*;

/** Offline camera toy: images never enter the random persona generator. */
@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private static final int CYAN=0xFF00E5EF, PINK=0xFFFF008C, BG=0xFF080D12, MUTED=0xFFA2B4BF;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final PersonaGenerator generator=new PersonaGenerator();
    private FrameLayout root;
    private TextureView preview;
    private ImageView photo;
    private Radar radar;
    private LinearLayout controls, panel;
    private ScrollView results;
    private TextView status, persona, tagline, energy, wildcard;
    private Camera camera;
    private int cameraId;
    private boolean consent, scanning, active;
    private Bitmap captured;
    private PersonaGenerator.Result result;
    private ValueAnimator scanner;
    private final Runnable reveal=() -> {
        if (!active || captured==null) return;
        scanning=false; if(scanner!=null) scanner.cancel(); radar.setVisibility(View.GONE); showResult();
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        buildUi();
        new AlertDialog.Builder(this).setTitle("Welcome to gaydar · 18+")
            .setMessage("A camera toy for consenting adults. Every persona, energy score and fantasy wildcard is randomly made up. Nothing is inferred from your photo.\n\nBy continuing, you confirm you and everyone pictured are 18 or older and agree to participate. Photos stay on this device. You choose whether to save or share.")
            .setPositiveButton("I'm 18+ · Let's play",(d,w)->{consent=true;requestCamera();})
            .setNegativeButton("Exit",(d,w)->finish()).setCancelable(false).show();
    }
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private TextView text(String s,float size,int color){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;
    }
    private GradientDrawable surface(int color,int border,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));
        if(border!=0)d.setStroke(dp(1),border);return d;
    }
    private TextView button(String s,boolean primary,Runnable action){
        TextView b=text(s,16,Color.WHITE);b.setGravity(Gravity.CENTER);b.setTypeface(null,Typeface.BOLD);
        b.setBackground(surface(primary?PINK:0xB0080D12,primary?0:0xFF52616B,28));
        b.setPadding(dp(14),dp(12),dp(14),dp(12));b.setMinHeight(dp(48));
        b.setOnClickListener(v->action.run());b.setContentDescription(s);b.setFocusable(true);return b;
    }
    private FrameLayout.LayoutParams anchor(int w,int h,int gravity,int margin){
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h,gravity);
        p.setMargins(dp(margin),dp(margin),dp(margin),dp(margin));return p;
    }
    private void buildUi(){
        root=new FrameLayout(this);root.setBackgroundColor(BG);setContentView(root);
        root.setOnApplyWindowInsetsListener((v,i)->{
            v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());
            return i.consumeSystemWindowInsets();
        });
        preview=new TextureView(this);preview.setSurfaceTextureListener(this);root.addView(preview,new FrameLayout.LayoutParams(-1,-1));
        photo=new ImageView(this);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);photo.setVisibility(View.GONE);
        root.addView(photo,new FrameLayout.LayoutParams(-1,-1));
        View shade=new View(this);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xCC080D12,0x00080D12,0xEE080D12}));
        root.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        radar=new Radar();root.addView(radar,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("◉  gaydar",30,Color.WHITE);brand.setTypeface(null,Typeface.BOLD);
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=text("18+",14,Color.WHITE);badge.setPadding(dp(12),dp(8),dp(12),dp(8));badge.setBackground(surface(0xAA080D12,PINK,20));header.addView(badge);
        root.addView(header,anchor(-1,-2,Gravity.TOP,24));
        status=text("PRANK MODE · RANDOM FICTION",11,CYAN);status.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams sp=anchor(-1,-2,Gravity.TOP,24);sp.topMargin=dp(86);root.addView(status,sp);
        controls=new LinearLayout(this);controls.setOrientation(1);controls.setGravity(Gravity.CENTER);
        TextView hint=text("Make up your alter ego",18,Color.WHITE);hint.setGravity(Gravity.CENTER);controls.addView(hint);
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);row.setPadding(0,dp(24),0,dp(22));
        row.addView(button("Photos",false,this::pickPhoto),new LinearLayout.LayoutParams(dp(88),dp(52)));
        TextView shutter=button("●",true,this::capture);shutter.setTextSize(30);shutter.setBackground(surface(PINK,Color.WHITE,48));
        shutter.setContentDescription("Take photo and generate random persona");
        LinearLayout.LayoutParams sh=new LinearLayout.LayoutParams(dp(84),dp(84));sh.setMargins(dp(14),0,dp(14),0);row.addView(shutter,sh);
        row.addView(button("Flip",false,this::flipCamera),new LinearLayout.LayoutParams(dp(88),dp(52)));controls.addView(row);
        TextView foot=text("Consenting adults only · Photos stay on device",11,MUTED);foot.setGravity(Gravity.CENTER);controls.addView(foot);
        root.addView(controls,anchor(-1,-2,Gravity.BOTTOM,18));
        results=new ScrollView(this);results.setVisibility(View.GONE);
        panel=new LinearLayout(this);panel.setOrientation(1);panel.setPadding(dp(22),dp(22),dp(22),dp(18));panel.setBackground(surface(0xF0080D12,0xFF46545E,30));
        TextView fiction=text("✦  RANDOM FICTION",12,CYAN);fiction.setGravity(Gravity.CENTER);panel.addView(fiction);
        persona=text("",32,Color.WHITE);persona.setGravity(Gravity.CENTER);persona.setAllCaps(true);persona.setPadding(0,dp(16),0,dp(8));panel.addView(persona);
        tagline=text("",13,MUTED);tagline.setGravity(Gravity.CENTER);panel.addView(tagline);
        LinearLayout stats=new LinearLayout(this);stats.setPadding(0,dp(22),0,dp(22));
        LinearLayout ec=new LinearLayout(this);ec.setOrientation(1);ec.addView(text("ϟ  BIG ENERGY",10,MUTED));energy=text("",26,CYAN);ec.addView(energy);stats.addView(ec,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout wc=new LinearLayout(this);wc.setOrientation(1);wc.addView(text("✦  FANTASY WILDCARD",10,MUTED));wildcard=text("",18,Color.WHITE);wc.addView(wildcard);stats.addView(wc,new LinearLayout.LayoutParams(0,-2,1));panel.addView(stats);
        addButton(button("⚄  Reroll",true,this::showResult));
        LinearLayout exports=new LinearLayout(this);LinearLayout.LayoutParams half=new LinearLayout.LayoutParams(0,-2,1);half.setMargins(0,0,dp(8),0);
        exports.addView(button("Save card",false,()->exportCard(false)),half);exports.addView(button("Share",false,()->exportCard(true)),new LinearLayout.LayoutParams(0,-2,1));panel.addView(exports);
        addButton(button("Retake",false,this::retake));
        TextView disclaimer=text("For laughs. Not an assessment.",11,MUTED);disclaimer.setGravity(Gravity.CENTER);panel.addView(disclaimer);
        results.addView(panel);root.addView(results,anchor(-1,dp(420),Gravity.BOTTOM,16));
    }
    private void addButton(View b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));panel.addView(b,p);}
    private void requestCamera(){
        if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)openCamera();
        else requestPermissions(new String[]{Manifest.permission.CAMERA},10);
    }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==10&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)openCamera();
        else status.setText("Camera unavailable · Tap Photos to import");
    }
    private void openCamera(){
        if(!active||!consent||captured!=null||!preview.isAvailable()||camera!=null||checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)return;
        try{
            camera=Camera.open(cameraId);Camera.CameraInfo info=new Camera.CameraInfo();Camera.getCameraInfo(cameraId,info);
            int rotation=info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT?(360-info.orientation)%360:info.orientation;
            camera.setDisplayOrientation(rotation);Camera.Parameters p=camera.getParameters();
            Camera.Size size=p.getSupportedPreviewSizes().get(0);
            for(Camera.Size candidate:p.getSupportedPreviewSizes())
                if(candidate.width<=1920&&Math.abs((float)candidate.width/candidate.height-16f/9f)<Math.abs((float)size.width/size.height-16f/9f))size=candidate;
            p.setPreviewSize(size.width,size.height);
            if(p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(p);camera.setPreviewTexture(preview.getSurfaceTexture());camera.startPreview();
            float w=preview.getWidth(),h=preview.getHeight(),pw=rotation%180==0?size.width:size.height,ph=rotation%180==0?size.height:size.width;
            if(w>0&&h>0){float scale=Math.max(w/pw,h/ph);Matrix m=new Matrix();m.setScale(pw*scale/w,ph*scale/h,w/2,h/2);preview.setTransform(m);}
            status.setText("PRANK MODE · RANDOM FICTION");
        }catch(Exception e){closeCamera();status.setText("Camera unavailable · Tap Photos to import");}
    }
    private void closeCamera(){if(camera!=null){try{camera.stopPreview();}catch(Exception ignored){}camera.release();camera=null;}}
    private void flipCamera(){
        if(scanning||captured!=null)return;
        if(Camera.getNumberOfCameras()<2){toast("One camera available");return;}
        closeCamera();cameraId=(cameraId+1)%Camera.getNumberOfCameras();openCamera();
    }
    private void capture(){
        if(!consent||scanning)return;if(camera==null){requestCamera();return;}
        Bitmap frame=preview.getBitmap();if(frame==null){toast("Camera is warming up. Try again.");return;}usePhoto(frame);
    }
    private void pickPhoto(){
        if(!consent||scanning)return;
        Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*");pick.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(pick,20);
    }
    @Override protected void onActivityResult(int request,int code,Intent data){
        super.onActivityResult(request,code,data);if(request!=20||code!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();io.execute(()->{
            try{
                Bitmap bitmap=ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(),uri),(decoder,info,src)->{
                    int longest=Math.max(info.getSize().getWidth(),info.getSize().getHeight());decoder.setTargetSampleSize(Math.max(1,(longest+1599)/1600));decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                });handler.post(()->{if(!isDestroyed())usePhoto(bitmap);});
            }catch(Exception e){handler.post(()->toast("Couldn't open this photo. Try another image."));}
        });
    }
    private void usePhoto(Bitmap bitmap){
        captured=bitmap;photo.setImageBitmap(bitmap);photo.setVisibility(View.VISIBLE);closeCamera();controls.setVisibility(View.GONE);scanning=true;
        status.setText("GENERATING YOUR FICTIONAL ALTER EGO…");radar.setVisibility(View.VISIBLE);
        scanner=ValueAnimator.ofFloat(0,360);scanner.setDuration(900);scanner.setRepeatCount(ValueAnimator.INFINITE);
        scanner.addUpdateListener(a->{radar.angle=(float)a.getAnimatedValue();radar.invalidate();});scanner.start();handler.postDelayed(reveal,1800);
    }
    private void showResult(){
        if(captured==null)return;result=generator.next();persona.setText(result.persona);tagline.setText(result.tagline);energy.setText(result.energy+" / 100");wildcard.setText(result.wildcard);
        status.setText("JUST FOR FUN · NO PHOTO ANALYSIS");results.setVisibility(View.VISIBLE);
    }
    private void retake(){
        handler.removeCallbacks(reveal);scanning=false;if(scanner!=null)scanner.cancel();results.setVisibility(View.GONE);
        photo.setImageDrawable(null);photo.setVisibility(View.GONE);captured=null;result=null;controls.setVisibility(View.VISIBLE);radar.setVisibility(View.VISIBLE);openCamera();
    }
    private void exportCard(boolean share){
        if(captured==null||result==null)return;Bitmap source=captured;PersonaGenerator.Result cardResult=result;
        io.execute(()->{
            Uri saved=null;
            try{
                Bitmap card=drawCard(source,cardResult);ContentValues values=new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,"gaydar-"+System.currentTimeMillis()+".jpg");values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Gaydar");values.put(MediaStore.Images.Media.IS_PENDING,1);
                saved=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);if(saved==null)throw new IllegalStateException();
                try(OutputStream stream=getContentResolver().openOutputStream(saved)){if(stream==null||!card.compress(Bitmap.CompressFormat.JPEG,92,stream))throw new IllegalStateException();}finally{card.recycle();}
                values.clear();values.put(MediaStore.Images.Media.IS_PENDING,0);getContentResolver().update(saved,values,null,null);Uri ready=saved;
                handler.post(()->{
                    if(isDestroyed())return;
                    if(share){Intent send=new Intent(Intent.ACTION_SEND).setType("image/jpeg");send.putExtra(Intent.EXTRA_STREAM,ready);send.setClipData(ClipData.newRawUri("Gaydar card",ready));send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(send,"Share fictional card"));}
                    else toast("Saved to Pictures/Gaydar");
                });
            }catch(Exception e){if(saved!=null)getContentResolver().delete(saved,null,null);handler.post(()->toast("Couldn't save this card. Check available storage."));}
        });
    }
    private Bitmap drawCard(Bitmap source,PersonaGenerator.Result r){
        Bitmap card=Bitmap.createBitmap(1080,1560,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(card);c.drawColor(BG);
        float scale=Math.max(1080f/source.getWidth(),1000f/source.getHeight()),w=source.getWidth()*scale,h=source.getHeight()*scale;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);c.save();c.clipRect(0,0,1080,1000);
        c.drawBitmap(source,null,new RectF((1080-w)/2,(1000-h)/2,(1080+w)/2,(1000+h)/2),p);c.restore();
        p.setShader(new LinearGradient(0,0,0,300,0xCC080D12,0x00080D12,Shader.TileMode.CLAMP));c.drawRect(0,0,1080,300,p);p.setShader(null);
        paintText(c,p,"gaydar",62,100,64,Color.WHITE);paintText(c,p,"18+  /  RANDOM FICTION",62,1060,26,CYAN);
        String title=r.persona.toUpperCase(Locale.ROOT);float titleSize=68;p.setTextSize(titleSize);while(p.measureText(title)>956)p.setTextSize(--titleSize);
        paintText(c,p,title,62,1160,titleSize,Color.WHITE);paintText(c,p,"BIG ENERGY",62,1240,24,MUTED);paintText(c,p,r.energy+" / 100",62,1300,44,CYAN);
        paintText(c,p,"FANTASY WILDCARD",540,1240,24,MUTED);paintText(c,p,r.wildcard,540,1300,32,Color.WHITE);
        paintText(c,p,"Randomly made up. No photo analysis.",62,1410,28,MUTED);paintText(c,p,"For laughs. Not an assessment.",62,1470,28,MUTED);return card;
    }
    private void paintText(Canvas c,Paint p,String s,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));c.drawText(s,x,y,p);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onResume(){super.onResume();active=true;openCamera();}
    @Override protected void onPause(){active=false;closeCamera();if(scanning){handler.removeCallbacks(reveal);if(scanner!=null)scanner.cancel();scanning=false;radar.setVisibility(View.GONE);showResult();}super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);io.shutdown();super.onDestroy();}
    @Override public void onBackPressed(){if(captured!=null)retake();else super.onBackPressed();}
    @Override public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h){openCamera();}
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h){closeCamera();openCamera();}
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture t){closeCamera();return true;}
    @Override public void onSurfaceTextureUpdated(SurfaceTexture t){}
    private class Radar extends View {
        final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG);float angle;
        Radar(){super(MainActivity.this);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas c){
            float cx=getWidth()/2f,cy=getHeight()*.40f,r=getWidth()*.37f;
            pen.setStyle(Paint.Style.STROKE);pen.setStrokeWidth(dp(1));pen.setColor(0x5500E5EF);c.drawCircle(cx,cy,r,pen);c.drawCircle(cx,cy,r*.91f,pen);
            pen.setColor(CYAN);pen.setStrokeWidth(dp(2));float l=dp(24),right=getWidth()-dp(24),top=dp(142),bottom=getHeight()-dp(225),len=dp(22);
            if(bottom>top){c.drawLine(l,top,l+len,top,pen);c.drawLine(l,top,l,top+len,pen);c.drawLine(right-len,top,right,top,pen);c.drawLine(right,top,right,top+len,pen);c.drawLine(l,bottom,l+len,bottom,pen);c.drawLine(l,bottom-len,l,bottom,pen);c.drawLine(right-len,bottom,right,bottom,pen);c.drawLine(right,bottom-len,right,bottom,pen);}
            c.drawArc(cx-r,cy-r,cx+r,cy+r,angle-80,100,false,pen);
            if(scanning){float y=cy-r+r*2*angle/360;pen.setStrokeWidth(dp(1));c.drawLine(cx-r,y,cx+r,y,pen);}
        }
    }
}
