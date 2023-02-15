
package com.reactlibrary.PrintUtil.interfaces;


public interface PrinterQueueStatus {
  int NORMAL = 0;
  int OFFLINE = -1;
  int FULL_CACHE = -2;
  int SEND_DATA_FAIl = -3;
  int SEND_COMMAND_FAIL = -4;
}