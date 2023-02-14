// main index.js

import { NativeModules } from 'react-native';

const { SunmiPrinter } = NativeModules;

export default {
  myfunc1: () => 1,
  myfunc2: (param) => param
};
