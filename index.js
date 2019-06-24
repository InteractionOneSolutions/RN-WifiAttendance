/**
 * @format
 */

import { AppRegistry, DeviceEventEmitter } from "react-native";
import App from "./App";
import { name as appName } from "./app.json";
import Beacons from "react-native-beacons-manager";
import { Toast } from "native-base";

const BeaconScanTask = async data => {
  // navigator.geolocation.getCurrentPosition(position => {
  //   console.log(position.coords);
  // });
  console.log("DATA>>>", data);
  Beacons.detectIBeacons();
  DeviceEventEmitter.addListener("beaconsDidRange", data => {
    console.log("Found beacons!2222", data.beacons);
    Toast.show({
      text: "Beacons Found " + data.beacons.length,
      duration: 3000
    });
  });
};

AppRegistry.registerHeadlessTask("BeaconScanTask", () => BeaconScanTask);

AppRegistry.registerComponent(appName, () => App);
