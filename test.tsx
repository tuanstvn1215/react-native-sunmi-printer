import { NativeModules } from 'react-native';

const { SunmiPrinter } = NativeModules;
SunmiPrinter.connectPrinterService()

const SunmiPrinterModule = {
  ...SunmiPrinter,
    test:()=>{return "test"}
}
export default SunmiPrinterModule