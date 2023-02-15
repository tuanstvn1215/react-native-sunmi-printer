package com.reactlibrary.PrintUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.sunmi.trans.TransBean;
import com.reactlibrary.R;

import com.reactlibrary.PrintUtil.interfaces.*;
import com.sunmi.extprinterservice.ExtPrinterService;
import com.reactlibrary.PrintUtil.interfaces.RealPrinterStatus;

import java.util.ArrayList;
import java.util.Calendar;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

class SunmiPrinter {
    public static final int FULL_CUTTING = 0;
    public static final int HALF_CUTTING = 1;
    public static final int PAPER_FEED_CUTTING = 2;

    public static final int ALIGNMENT_LEFT = 0;
    public static final int ALIGNMENT_CENTER = 1;
    public static final int ALIGNMENT_RIGHT = 2;

    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";
    private static final String SUNMI_K1_SERVICE＿PACKAGE = "com.sunmi.extprinterservice";
    private static final String SUNMI_K1_SERVICE＿ACTION = "com.sunmi.extprinterservice.PrinterService";

    private static final String NORMAL_ACTION = "com.sunmi.extprinterservice.NORMAL_ACTION";
    private static final String OFLLINE_ACTION = "com.sunmi.extprinterservice.OFLLINE_ACTION";
    private static final String COVER_OPEN_ACTION = "com.sunmi.extprinterservice.COVER_OPEN_ACTION";
    private static final String OUT_OF_PAPER_ACTION = "com.sunmi.extprinterservice.OUT_OF_PAPER_ACTION";
    private static final String LESS_OF_PAPER_ACTION = "com.sunmi.extprinterservice.LESS_OF_PAPER_ACTION";
    private static final String HOT_ACTION = "com.sunmi.extprinterservice.HOT_ACTION";
    public static String PRINTER_DEBUG = "PRINTER_DEBUG";
    private Context mContext;

    private IWoyouService woyouService;
    private ExtPrinterService sunmiK1Service;

    private ArrayList<OnPrinterStatusChangeListener> mListeners = new ArrayList<>();

    private boolean isSunmi() {
        return Build.MANUFACTURER.toLowerCase().equals("sunmi");
    }

    private boolean isPAndV1sModel() {
        return Build.MODEL.toLowerCase().startsWith("p") || Build.MODEL.toLowerCase().startsWith("v1s");
    }

    public boolean isKModel() {
        return Build.MODEL.toLowerCase().startsWith("k");
    }

    public boolean isConnect() {
        Log.d(PRINTER_DEBUG, " isConnect " + (woyouService != null || sunmiK1Service != null));
        if ((woyouService != null || sunmiK1Service != null)) {

        }
        if ((sunmiK1Service != null))
            Log.d(PRINTER_DEBUG, " sunmiK1Service " + sunmiK1Service.toString());
        if ((woyouService != null))
            Log.d(PRINTER_DEBUG, " woyouService " + woyouService.toString());
        return (woyouService != null || sunmiK1Service != null);
    }

    public void disconnectPrinterService() {
        mListeners.clear();
        Intent intent = new Intent();
        if (isKModel()) {
            intent.setPackage(SUNMI_K1_SERVICE＿PACKAGE);
            intent.setAction(SUNMI_K1_SERVICE＿ACTION);
        } else {
            intent.setPackage(SERVICE＿PACKAGE);
            intent.setAction(SERVICE＿ACTION);
        }
        boolean result = mContext.stopService(intent);

        Log.d(PRINTER_DEBUG, " ====disconnectPrinterService " + result);
        woyouService = null;
        sunmiK1Service = null;
    }

    public boolean connectPrinterService(Context context) {
        mListeners.clear();
        return connectPrinterService(context, null);
    }

    public void addOnPrinterStatusChangeListener(OnPrinterStatusChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeOnPrinterStatusChangeListener(OnPrinterStatusChangeListener listener) {
        if (listener == null) {
            mListeners.clear();
        }
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public boolean connectPrinterService(Context context, OnPrinterStatusChangeListener listener) {
        this.mContext = context;
        mListeners.clear();
        mListeners.add(listener);

        if (!isSunmi()) {
            return false;
        }

        Intent intent = new Intent();
        if (isKModel()) {
            intent.setPackage(SUNMI_K1_SERVICE＿PACKAGE);
            intent.setAction(SUNMI_K1_SERVICE＿ACTION);
        } else {
            intent.setPackage(SERVICE＿PACKAGE);
            intent.setAction(SERVICE＿ACTION);
        }

        ComponentName componentName = context.startService(intent);
        boolean bind = context.bindService(intent, connService, Context.BIND_AUTO_CREATE);

        Log.d(PRINTER_DEBUG, "==========connectPrinterService componentName " + componentName);
        Log.d(PRINTER_DEBUG, " ==========connectPrinterService bind " + bind);
        // Toast.makeText(context, "connectPrinterService", Toast.LENGTH_LONG).show();
        return true;
    }

    private ServiceConnection connService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(PRINTER_DEBUG, " ====onServiceDisconnected====");
            woyouService = null;
            sunmiK1Service = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PRINTER_DEBUG, " ====onServiceConnected====");
            if (isKModel()) {
                sunmiK1Service = ExtPrinterService.Stub.asInterface(service);
            } else {
                woyouService = IWoyouService.Stub.asInterface(service);
            }

            Log.d(PRINTER_DEBUG, "====initPrinter end ====");
        }

        @Override
        public void onBindingDied(ComponentName name) {

            Log.d(PRINTER_DEBUG, "====onBindingDied====");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(PRINTER_DEBUG, "====onNullBinding====");
        }
    };

    public void changePrinterStatus(int status) {
        for (OnPrinterStatusChangeListener listener : mListeners) {
            long timestamp = Calendar.getInstance().getTimeInMillis();
            listener.onStatusChanged(status, timestamp);
        }
    }

    public int initPrinter() {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;

        }

        try {
            if (isKModel()) {
                Log.d(PRINTER_DEBUG, "initPrinter isK1Model ==================");

                return sunmiK1Service.printerInit();
            } else {
                Log.d(PRINTER_DEBUG, "initPrinter ==================");
                woyouService.printerInit(new ICallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) throws RemoteException {
                        Log.d(PRINTER_DEBUG, "onRunResult " + isSuccess);
                    }

                    @Override
                    public void onReturnString(String result) throws RemoteException {
                        Log.d(PRINTER_DEBUG, "onReturnString " + result);

                    }

                    @Override
                    public void onRaiseException(int code, String msg) throws RemoteException {
                        Log.d(PRINTER_DEBUG, "onRaiseException " + code + " | " + msg);

                    }

                    @Override
                    public void onPrintResult(int code, String msg) throws RemoteException {
                        Log.d(PRINTER_DEBUG, "onPrintResult " + code + " | " + msg);
                    }
                });
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;

    }

    /**
     * 0 Left aligned (default)、1 Centered、2 Right aligned
     *
     * @param alignment
     * @return
     */
    public int setAlignment(int alignment) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;

        }

        try {
            if (isKModel()) {
                return sunmiK1Service.setAlignMode(alignment);
            } else {
                woyouService.setAlignment(alignment, null);
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int flush() {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (isKModel()) {
                return sunmiK1Service.flush();
            } else {
                woyouService.clearBuffer();
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int getPrinterStatus() {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                Log.d(PRINTER_DEBUG, " getPrinterStatus: " + Integer.toString(sunmiK1Service.getPrinterStatus()));
                switch (sunmiK1Service.getPrinterStatus()) {
                    case KModelPrinterStatus.CONNECTED: {
                        return RealPrinterStatus.NORMAL;
                    }
                    case KModelPrinterStatus.LESS_OF_PAPER:
                        return RealPrinterStatus.LESS_OF_PAPER;
                    case KModelPrinterStatus.OUT_OF_PAPER:
                        return RealPrinterStatus.OUT_OF_PAPER;
                    case KModelPrinterStatus.COVER_OPEN:
                        return RealPrinterStatus.COVER_OPEN;
                    default:
                        return RealPrinterStatus.PRINTER_NOT_READY;
                }
            } else {
                switch (woyouService.updatePrinterState()) {
                    case PModelPrinterStatus.CONNECTED: {
                        return RealPrinterStatus.NORMAL;
                    }
                    case PModelPrinterStatus.LESS_OF_PAPER:
                        return RealPrinterStatus.LESS_OF_PAPER;
                    case PModelPrinterStatus.OUT_OF_PAPER:
                        return RealPrinterStatus.OUT_OF_PAPER;
                    case PModelPrinterStatus.COVER_OPEN:
                        return RealPrinterStatus.COVER_OPEN;
                    default:
                        return RealPrinterStatus.PRINTER_NOT_READY;
                }
            }
        } catch (RemoteException e) {
            System.out.print("getPrinterStatus: 4");
            e.printStackTrace();
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

    }

    public int lineWrap() {
        return lineWrap(3);
    }

    public int lineWrap(int n) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (isKModel()) {
                return sunmiK1Service.lineWrap(n);
            } else {
                woyouService.lineWrap(n, null);
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;

    }

    /**
     * Half cutting
     *
     * @return
     */
    public int cutPaper() {
        return cutPaper(HALF_CUTTING);
    }

    public int cutPaper(int type) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (isKModel()) {
                switch (type) {
                    case FULL_CUTTING:
                        return sunmiK1Service.cutPaper(0, 0);
                    case HALF_CUTTING:
                        return sunmiK1Service.cutPaper(1, 0);
                    case PAPER_FEED_CUTTING:
                        return sunmiK1Service.cutPaper(2, 1);
                }
            }
            return RealPrinterStatus.NORMAL;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int printText(String content) {

        Log.d(PRINTER_DEBUG, "printText " + content);

        if (!isConnect()) {
            // Toast.LENGTH_LONG).show();
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (isKModel()) {

                Log.d(PRINTER_DEBUG, "printText K1 start" + content);
                return sunmiK1Service.printText(content);
            } else {
                woyouService.printText(content, null);
                return RealPrinterStatus.NORMAL;
            }

        } catch (RemoteException e) {

            Log.d(PRINTER_DEBUG, e.getMessage());
            e.printStackTrace();
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        // return PrinterStatus.UNKNOWN;
    }

    public int printBitmap(Bitmap bitmap) {
        return printBitmap(bitmap, true, null);
    }
    public int printBitmap(Bitmap bitmap,ICallback.Stub callback) {
        return printBitmap(bitmap, true, callback);
    }
    public int printBitmap(Bitmap bitmap, boolean autoScale, ICallback.Stub callback) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (isKModel()) {
                if (autoScale) {
                    Log.d(PRINTER_DEBUG, "print bitmap success ");
                    return sunmiK1Service.printBitmap(BitmapUtil.scaleWidth(bitmap, 560), 0);

                } else {
                    Log.d(PRINTER_DEBUG, "print bitmap success ");
                    return sunmiK1Service.printBitmap(bitmap, 0);
                }
            } else {
                // print gray image with type = 2
                if (autoScale) {

                    Bitmap bm = BitmapUtil.scaleWidth(bitmap, 384);
                    // Log.e(">>>>>>>>>>>>>>>>>>>>>", "======== LENGTH SCALE ======== " +
                    // (bm.getHeight() * 25.4f / 205f));

                    woyouService.printBitmapCustom(BitmapUtil.scaleWidth(bitmap, 384), 2, callback);
                } else {
                    woyouService.printBitmapCustom(bitmap, 2, callback);
                }
                woyouService.commitPrinterBuffer();
                // Log.e(">>>>>>>>>>>>", "==== DONE ====");
                return RealPrinterStatus.NORMAL;

            }

        } catch (RemoteException e) {
            Log.d(PRINTER_DEBUG, "print bitmap fail ");
            // Log.e(">>>>>>>>>>>>", "ERRROR", e);
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int sendRawData(byte[] data) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                return sunmiK1Service.sendRawData(data);
            } else {
                woyouService.sendRAWData(data, null);
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int tab() {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                return sunmiK1Service.tab();
            } else {
                // TODO
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int setHorizontalTab(int[] k) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                return sunmiK1Service.setHorizontalTab(k);
            } else {
                // TODO
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int setFontZoom(int hor, int ver) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                return sunmiK1Service.setFontZoom(hor, ver);
            } else {
                // TODO
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    /**
     * modeSize (Modulesize): code block size, unit: dot, values 4 to 16
     * <p>
     * Errorlevel qr code error correction level (0-3):
     * 0 error correction level L (7%)
     * 1 error correction level M (15%)
     * 2 error correction level Q (25%)
     * 3 error correction level H (30%) => DEFAULT
     *
     * @param code
     * @param modeSize
     * @return
     */
    public int printQrCode(String code, int modeSize) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }
        try {
            if (isKModel()) {
                return sunmiK1Service.printQrCode(code, modeSize, 3);
            } else {
                woyouService.printQRCode(code, modeSize, 3, null);
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int commitPrint(TransBean[] transBeans) {
        if (!isConnect()) {
            return RealPrinterStatus.PRINTER_NOT_READY;
        }

        try {
            if (!isKModel()) {
                woyouService.commitPrint(transBeans, null);
                return RealPrinterStatus.NORMAL;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return RealPrinterStatus.PRINTER_NOT_READY;
    }

    public int getPrintedLength(ICallback.Stub callback) {
        if (!isConnect()) {
            return -1;
        }

        try {
            if (isKModel()) {
                // TODO
            } else {
                woyouService.getPrintedLength(callback);
                return 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int commitPrinterBuffer() {
        if (!isConnect()) {
            return -1;
        }

        try {
            if (isKModel()) {
                // TODO
            } else {
                woyouService.commitPrinterBuffer();
                return 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
