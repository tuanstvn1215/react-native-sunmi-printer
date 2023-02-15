package com.reactlibrary.PrintUtil;

import android.graphics.Bitmap;

public class Ticket {
  private Bitmap bitmap;
  private String code;

  public Ticket(Bitmap bitmap, String code) {
    this.bitmap = bitmap;
    this.code = code;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public String getCode() {
    return code;
  }

}
