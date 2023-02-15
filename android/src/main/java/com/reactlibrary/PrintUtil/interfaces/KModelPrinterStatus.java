package com.reactlibrary.PrintUtil.interfaces;

public interface KModelPrinterStatus {
    int UNKNOWN = -99;
    int OFFLINE = -1;

    int NORMAL = 0;
    int COVER_OPEN = 1;
    int OUT_OF_PAPER = 2;
    int LESS_OF_PAPER = 3;
    int OVERHEAT = 4;

    int CONNECTED = 100;
    int START_PRINTER = 101;

}

