package com.reactlibrary.PrintUtil;


import com.facebook.react.bridge.ReactApplicationContext;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.content.Context;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.sunmi.peripheral.printer.InnerResultCallback;

import java.util.LinkedList;
import java.util.Queue;
import android.util.Log;
import com.reactlibrary.PrintUtil.interfaces.KModelPrinterStatus;
import woyou.aidlservice.jiuiv5.ICallback;

public class PrintManager {
    private static final PrintManager manager = new PrintManager();
    private static ReactApplicationContext context;

    public static PrintManager getInstance(ReactApplicationContext contextx) {
        context = contextx;
        return manager;
    }

    private Queue<Bitmap> queue = new LinkedList();
    private boolean end = true;
    private boolean next = false;

    public void addTicket(Ticket ticket) {
        Log.d("PrintTask", "[Print] [run] START");
        // next = false;
        int printerStatus = PrinterSdk.getInstance().getPrinterStatus();
        Log.d("PrintTask", "[Print] [run] printerStatus:" + Integer.toString(printerStatus));
        WritableMap map = Arguments.createMap();
        map.putInt("printerStatus", printerStatus);
        map.putString("ticketId", ticket.getCode());
        if (printerStatus == KModelPrinterStatus.NORMAL || printerStatus == KModelPrinterStatus.LESS_OF_PAPER) {
            Log.d("PrintTask", "[Print] [run] Start print ticket");
            int printerQueueStatus = PrinterSdk.getInstance().printBitmap(ticket.getBitmap());
            if (printerQueueStatus >= KModelPrinterStatus.NORMAL) {
                Log.d("PrintTask", "[Print] [run] Done print ticket");
                PrinterSdk.getInstance().lineWrap();
                PrinterSdk.getInstance().flush();
                PrinterSdk.getInstance().cutHalfPaper();
                Log.d("PrintTask", "[Print] [run] send event , print successful");
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("PrintDone", map);
            } else {
                Log.d("PrintTask", "[Print] [run] send event, print failed");

                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("PrintDone", map);
            }
        } else {
            Log.d("PrintTask", "[Print] [run] send event, printer not ready");
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("PrintDone", map);
        }

    }

    public void addBitmap(Bitmap bitmap) {
        queue.add(bitmap);
        if (end) {
            end = false;
            next = true;
            startPrint();
        }
    }

    ICallback.Stub innerResultCallback = new ICallback.Stub() {
        @Override
        public void onRunResult(boolean isSuccess) throws RemoteException {
            if (isSuccess) {
                queue.remove();
            }
            next = true;
        }

        @Override
        public void onReturnString(String result) throws RemoteException {

        }

        @Override
        public void onRaiseException(int code, String msg) throws RemoteException {

        }

        @Override
        public void onPrintResult(int code, String msg) throws RemoteException {

        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (!end) {
                while (next) {
                    Bitmap bitmap = queue.peek();
                    if (bitmap != null) {
                        next = false;
                        PrinterSdk.getInstance().printBitmap(bitmap, innerResultCallback);
                        PrinterSdk.getInstance().lineWrap();
                        PrinterSdk.getInstance().cutHalfPaper();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

        }
    };

    private void startPrint() {
        try {
            ThreadPoolManager.getInstance().executeTask(runnable);
        } catch (Exception e) {

        }

    }
}