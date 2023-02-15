// SunmiPrinterModule.java

package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.reactlibrary.PrintUtil.*;
public class SunmiPrinterModule extends ReactContextBaseJavaModule {
    public static String PRINTER_DEBUG = "PRINTER_DEBUG";
    private final ReactApplicationContext reactContext;

    public SunmiPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SunmiPrinter";
    }
    @ReactMethod
    public void connectPrinterService(){
        PrinterSdk.getInstance().connectPrinterService(this, new OnPrinterStatusChangeListener() {
            public void onStatusChanged(int status, long timestamp) {
              if (status == PrinterStatus.START_PRINTER) {
                Log.e(PRINTER_DEBUG, "==Start== timestamp" + timestamp);
              }
            }
          });
    }
    
  
    @ReactMethod
    public void flush() {
      PrinterSdk.getInstance().flush();
    }
  
    @ReactMethod
    public void printPDF(String pdf) {
      byte[] bytes = Base64.decode(pdf, Base64.DEFAULT);
      PrinterSdk.getInstance().pdf(bytes);
  
    }
  
    @ReactMethod
    public void printText(String text) {
  
      try {
        byte[] bytes = Base64.decode(text, Base64.DEFAULT);
        // PrinterSdk.getInstance().initPrinter();
        PrinterSdk.getInstance().pdf(bytes);
        Log.e(PRINTER_DEBUG, " print text success");
      } catch (Exception e) {
        Log.e(PRINTER_DEBUG, " print text fail");
        // TODO: handle exception
      }
    }
  
    @ReactMethod
    public void getPrinterStatus(Promise promise) {
      try {
        WritableMap map = Arguments.createMap();
        int a = PrinterSdk.getInstance().getPrinterStatus();
        // ./ Log.e(PRINTER_DEBUG, PrinterSdk.getInstance().getPrinterStatus());
        // Log.e(PRINTER_DEBUG, "Printer Status : " + a);
        promise.resolve(a);
      } catch (Exception e) {
        Log.e(PRINTER_DEBUG, "Get Printer Status fail ");
        promise.reject("Get Printer Status fail", e);
      }
    }
  
    @ReactMethod
    public void printTicketPDF(String base64, ReadableArray items, String code, Promise promise) {
      try {
        byte[] backgroundPdf = Base64.decode(base64, Base64.DEFAULT);
        printTicketLayout(backgroundPdf, items, code);
        promise.resolve(true);
      } catch (Exception e) {
        Log.e("printTicketPDF", e.toString());
        promise.resolve(false);
      }
    }
  
    private void printTicketLayout(byte[] background, ReadableArray items, String code) {
      try {
        ReactApplicationContext context = getReactApplicationContext();
        PdfRenderer renderer = getPdf(background);
        if (renderer != null && renderer.getPageCount() > 0) {
          PdfRenderer.Page page = renderer.openPage(0);
          int width = 560;
          float scale = (float) width / page.getWidth();
          float height = (scale * page.getHeight());
          Bitmap bitmap = Bitmap.createBitmap(width, (int) height, Bitmap.Config.ARGB_8888);
          Canvas canvas = new Canvas(bitmap);
          bitmap.eraseColor(Color.WHITE);
          page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
          page.close();
          renderer.close();
          Utils uti = new Utils();
          for (int i = 0; i < items.size(); i++) {
            ReadableMap item = items.getMap(i);
            String type = "other";
            if (item.hasKey("type"))
              type = item.getString("type");
            if (type.equals("textarea")) {
              drawText(type, canvas, scale, height, item);
            } else if (type.equals("barcodearea")) {
              drawQR(type, canvas, scale, height, item);
            }
          }
          Ticket ticket = new Ticket(uti.floydSteinbergDithering(bitmap), code);
          PrintManager.getInstance(context).addTicket(ticket);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  
    private Typeface getTypeface(ReadableMap item) {
      Context context = getReactApplicationContext();
      boolean bold = false;
      if (item.hasKey("bold")) {
        bold = item.getBoolean("bold");
      }
      boolean italic = false;
      if (item.hasKey("italic")) {
        italic = item.getBoolean("italic");
      }
      if (bold && italic)
        return Typeface.create(ResourcesCompat.getFont(context, R.font.opensans_bolditalic), Typeface.BOLD_ITALIC);
      if (bold)
        return Typeface.create(ResourcesCompat.getFont(context, R.font.opensans_semibold), Typeface.BOLD);
      if (italic)
        return Typeface.create(ResourcesCompat.getFont(context, R.font.opensans_italic), Typeface.ITALIC);
      return Typeface.create(ResourcesCompat.getFont(context, R.font.opensans_regular), Typeface.NORMAL);
    }
  
    private float stringToFloat(String a, float defaultNum) {
      try {
        return Float.parseFloat(a);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return defaultNum;
    }
  
    private float millimeterToPoint(float a) {
      return a * 2.834645f;
    }
  
    private PaintLayout getAlignAndXY(ReadableMap item, String type, float scale, float heightPage, Paint paint) {
      float w = 10f;
      if (item.hasKey("width")) {
        String width = item.getString("width");
        w = millimeterToPoint(stringToFloat(width, w)) * scale;
      } else if (item.hasKey("size")) {
        String width = item.getString("size");
        w = millimeterToPoint(stringToFloat(width, w)) * scale;
      }
      float h = w;
      if (type.equals("textarea")) {
        Rect bounds = new Rect();
        paint.getTextBounds("test", 0, 4, bounds);
        h = bounds.height();
      }
      float x = 0;
      if (item.hasKey("left")) {
        String left = item.getString("left");
        x = millimeterToPoint(stringToFloat(left, x)) * scale;
      }
      float y = 0;
      if (item.hasKey("bottom")) {
        String bottom = item.getString("bottom");
        y = heightPage - millimeterToPoint(stringToFloat(bottom, y)) * scale;
      }
  
      if (item.hasKey("downward")) {
        boolean downward = item.getBoolean("downward");
        if (downward)
          y += h;
        else
          y -= h / 2;
      }
      Paint.Align align = Paint.Align.LEFT;
      if (item.hasKey("align")) {
        String str = item.getString("align");
        if (str.equals("center")) {
          align = Paint.Align.CENTER;
          x += w / 2;
        } else if (str.equals("right")) {
          align = Paint.Align.RIGHT;
          x += w;
        }
      }
      return new PaintLayout(x, y, w, h, align);
    }
  
    private ParcelFileDescriptor fileDescriptor(byte[] buffer) {
      try {
        File file = File.createTempFile("print", "pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(buffer);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
      } catch (Throwable t) {
        t.printStackTrace();
      }
      return null;
    }
  
    private byte[] getBlankPdf() {
      try {
        InputStream is = getReactApplicationContext().getAssets().open("blank.pdf");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  
        int nRead;
        byte[] data = new byte[16384];
  
        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
  
        return buffer.toByteArray();
      } catch (Throwable e) {
        e.printStackTrace();
      }
      return null;
    }
  
    private PdfRenderer getPdf(byte[] background) {
      try {
        if (background == null)
          background = getBlankPdf();
        ParcelFileDescriptor file = fileDescriptor(background);
        return new PdfRenderer(file);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
  
    private void drawQR(String type, Canvas canvas, Float scale, Float heightPage, ReadableMap item) {
      Paint paint = new Paint();
      String text = "";
      if (item.hasKey("text")) {
        text = item.getString("text");
      }
      PaintLayout pos = getAlignAndXY(item, type, scale, heightPage, paint);
      paint.setTextAlign(pos.align);
      try {
        Bitmap bitmap = encodeAsBitmap(text, (int) pos.w);
        canvas.drawBitmap(bitmap, pos.x, pos.y - pos.w, paint);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  
    private Bitmap encodeAsBitmap(String str, int width) throws WriterException {
      BitMatrix result;
      try {
        Map<EncodeHintType, Object> hints = new EnumMap(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 0);
        result = new MultiFormatWriter().encode(str,
            BarcodeFormat.QR_CODE, width, width, hints);
      } catch (IllegalArgumentException iae) {
        // Unsupported format
        return null;
      }
      int w = result.getWidth();
      int h = result.getHeight();
      int[] pixels = new int[w * h];
      for (int y = 0; y < h; y++) {
        int offset = y * w;
        for (int x = 0; x < w; x++) {
          pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
        }
      }
      Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
      return bitmap;
    }
  
    private void drawText(String type, Canvas canvas, Float scale, Float heightPage, ReadableMap item) {
      float fontSize = 10;
      TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
      if (item.hasKey("fontsize")) {
        String str = item.getString("fontsize");
        fontSize = stringToFloat(str, fontSize) * scale;
      }
      paint.setTextSize(fontSize);
      PaintLayout pos = getAlignAndXY(item, type, scale, heightPage, paint);
      paint.setTextAlign(pos.align);
      paint.setTypeface(getTypeface(item));
      paint.setColor(Color.BLACK);
      StaticLayout layout;
      String text = "";
      if (item.hasKey("text")) {
        text = item.getString("text");
      }
  
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder builder = StaticLayout.Builder.obtain(text, 0, text.length(), paint, (int) pos.w);
        builder.setAlignment(Layout.Alignment.ALIGN_NORMAL);
        layout = builder.build();
      } else {
        layout = new StaticLayout(text, paint, (int) pos.w, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
      }
  
      canvas.save();
      canvas.translate(pos.x, pos.y - pos.h);
      if (item.hasKey("rotation")) {
        float rotation = (float) item.getDouble("rotation");
        if (rotation > 0) {
          canvas.rotate(rotation, pos.x - pos.w / 2, pos.y - pos.h);
        }
      }
      layout.draw(canvas);
      canvas.restore();
    }
}
