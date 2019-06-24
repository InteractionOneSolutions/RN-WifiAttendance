/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Component } from "react";
import {
  Platform,
  StyleSheet,
  Text,
  View,
  AppRegistry,
  DeviceEventEmitter,
  PermissionsAndroid
} from "react-native";
import { NativeModules } from "react-native";
import { Toast } from "native-base";

import Beacons from "react-native-beacons-manager";
import BackgroundTask from "react-native-background-task";

type Props = {};
export default class App extends Component<Props> {
  constructor(props) {
    super(props);
    this.state = {
      geodata: ""
    };
  }

  componentWillMount() {}

  async checkStatus() {
    const status = await BackgroundTask.statusAsync();
    console.log("Status>>", status.available);
  }

  async componentDidMount() {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: "Wifi networks",
          message: "We need your permission in order to find wifi networks"
        }
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log("Thank you for your permission! :)");
      } else {
        console.log(
          "You will not able to retrieve wifi available networks list"
        );
      }
    } catch (err) {
      console.warn(err);
    }

    // Beacons.detectIBeacons();
    // Beacons.setForegroundScanPeriod(1000);
    // Beacons.setBackgroundScanPeriod(1000);
    // Beacons.setBackgroundBetweenScanPeriod(1000);

    // try {
    //   await Beacons.startRangingBeaconsInRegion("REGION1");
    //   console.log(`Beacons ranging started succesfully!`);
    // } catch (err) {
    //   console.log(`Beacons ranging not started, error: ${error}`);
    // }

    // // Print a log of the detected iBeacons (1 per second)
    // DeviceEventEmitter.addListener("beaconsDidRange", data => {
    //   console.log("Found1 beacons!", JSON.stringify(data.beacons));
    // });

    // BackgroundTask.schedule({
    //   period: 10
    // });

    NativeModules.BackgroundScan.startService();

    DeviceEventEmitter.addListener("updateBeacons", geoData => {
      console.log(JSON.stringify(geoData));
      this.setState({
        geodata: geoData.beaconData.minorId + "," + geoData.beaconData.majorId
      });
    });

    // NativeModules.WifiScanner.startService();
    // DeviceEventEmitter.addListener("updateWifi", wifi => {
    //   //console.log(JSON.stringify(wifi));
    //   this.setState({
    //     geodata: wifi.wifiResponse
    //   });
    // });
  }
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <Text style={styles.instructions}>To get started, edit App.js</Text>
        <Text style={styles.instructions}>{this.state.geodata}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  }
});
