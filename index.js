// main index.js

import { NativeModules } from 'react-native';

const { SunmiPrinter } = NativeModules;
SunmiPrinter.connectPrinterService()

export default SunmiPrinter;
