package com.reactlibrary.PrintUtil.interfaces;

public interface PModelPrinterStatus {
    int NORMAL = 1;
    int PREPARING_PRINTER = 2;
    int COMMUNICATION_ERROR=3;
    int OUT_OF_PAPER = 4;
    int OVERHEAT = 5;
    int COVER_OPEN=6;
    int CUTTER_ERROR = 7;
    int CUTTER_RESTORED = 8;
    int NO_BLACK_MARK = 9;
    int NO_PRINTER = 505;
    int FAIL_TO_UPDATE= 507;
}
    /* status for P1 */
    // 1 printer is normal
    // 2 Preparing the printer
    // 3 Communication error.
    // 4 Out of paper.
    // 5 Overheated.
    // 6 The printer’s cover is open
    // 7 cutter abnormal
    // 8 cutter recovery
    // 9 No black mark detected


