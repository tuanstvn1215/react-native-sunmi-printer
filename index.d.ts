declare module SunmiPrinter {
  function printText(text:string):void
  function flush():void
  function printPDF(pdfBase64:string ):void
  function connectPrinterService():void
  function getPrinterStatus():int
  function printTicketPDF(pdfBase64:string,items:any[],code:string):void
}
export default SunmiPrinter