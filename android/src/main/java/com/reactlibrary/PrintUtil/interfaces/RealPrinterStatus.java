package com.reactlibrary.PrintUtil.interfaces;

public interface RealPrinterStatus {
    int COVER_OPEN = 1;
    int NORMAL = 0;
    int OUT_OF_PAPER = 2;
    int LESS_OF_PAPER = 3;
    int PRINTER_NOT_READY = -1;
}
