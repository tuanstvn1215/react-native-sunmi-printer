package com.reactlibrary.PrintUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.graphics.Color;
import com.reactlibrary.PrintUtil.callback.*;
import com.reactlibrary.PrintUtil.interfaces.RealPrinterStatus;

import woyou.aidlservice.jiuiv5.ICallback;

public class PrinterSdk {

    private static final String TAG = PrinterSdk.class.getSimpleName();
    private static PrinterSdk mPrinterSdk = new PrinterSdk();
    private Context mContext;
    private SunmiPrinter mSunmiPrinter;

    public PrinterSdk() {
        mSunmiPrinter = new SunmiPrinter();
    }

    public static PrinterSdk getInstance() {
        return mPrinterSdk;
    }
    public boolean connectPrinterService(Context context) {
        this.mContext = context.getApplicationContext();
        return mSunmiPrinter.connectPrinterService(context.getApplicationContext());
    }

    public boolean connectPrinterService(Context context, OnPrinterStatusChangeListener listener) {
        this.mContext = context.getApplicationContext();

        // this.mSunmiPrinterV2.initSunmiPrinterService(context);
        return mSunmiPrinter.connectPrinterService(context.getApplicationContext(), listener);
    }

    public void addOnPrinterStatusChangeListener(OnPrinterStatusChangeListener listener) {
        mSunmiPrinter.addOnPrinterStatusChangeListener(listener);
    }

    public void removeOnPrinterStatusChangeListener(OnPrinterStatusChangeListener listener) {
        mSunmiPrinter.removeOnPrinterStatusChangeListener(listener);
    }

    public void disconnect() {
        mSunmiPrinter.disconnectPrinterService();
    }

    public boolean isConnect() {
        return mSunmiPrinter.isConnect();
    }

    public int initPrinter() {
        return mSunmiPrinter.initPrinter();
    }

    private boolean canNext(int status) {
        return status == RealPrinterStatus.NORMAL || status == RealPrinterStatus.LESS_OF_PAPER;
    }

    public int getPrinterStatus() {
        Log.e("PrinterSdk", "getPrinterStatus");
        return mSunmiPrinter.getPrinterStatus();
    }

    public int printBitmap(Bitmap bitmap) {
        return mSunmiPrinter.printBitmap(bitmap);
    }
    public int printBitmap(Bitmap bitmap, ICallback.Stub callback) {
        return mSunmiPrinter.printBitmap(bitmap,callback);
    }
    public void lineWrap() {
        mSunmiPrinter.lineWrap();
    }

    public void lineWrap(int n) {
        mSunmiPrinter.lineWrap(n);
    }

    public void printText(String data) {
        mSunmiPrinter.printText(data);

    }
    public boolean isKModel() {
        return mSunmiPrinter.isKModel();

    }

    public void cutFullPaper() {
        mSunmiPrinter.cutPaper(SunmiPrinter.FULL_CUTTING);
    }

    public void cutHalfPaper() {
        mSunmiPrinter.cutPaper(SunmiPrinter.HALF_CUTTING);
    }

    public void getPrintedLength(OnResultCallback callback) {
        mSunmiPrinter.getPrintedLength(callback);
    }

    public void commitPrinterBuffer() {
        mSunmiPrinter.commitPrinterBuffer();
    }

    public void flush() {
        mSunmiPrinter.flush();
    }

    private ParcelFileDescriptor fileDescriptor(byte[] buffer) throws IOException {
        File file = File.createTempFile("print", "pdf");
        FileOutputStream outFile = new FileOutputStream(file);
        outFile.write(buffer);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    public void pdf(byte[] buffer) {
        try {
            int width = 600;
            ParcelFileDescriptor fd = fileDescriptor(buffer);
            PdfRenderer renderer = new PdfRenderer(fd);
            int count = renderer.getPageCount();
            int idx = 0;
            while (idx < count) {
                PdfRenderer.Page page = renderer.openPage(idx);
                double rate = (double) width / (double) page.getWidth();
                int height = (int) (rate * page.getHeight());
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                page.close();
                printBitmap(bitmap);
                lineWrap();
                lineWrap();
                cutHalfPaper();
                idx++;
            }
            renderer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}