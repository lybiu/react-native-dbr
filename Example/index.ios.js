/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Button
} from 'react-native';

import BarcodeReaderManager from 'react-native-dbr';

export default class Example extends Component {
  constructor(props) {
    super(props);
    this.state = {
      result: 'N/A'
    };
    this.onButtonPress = this.onButtonPress.bind(this);
  }

  onButtonPress() {
    BarcodeReaderManager.readBarcode('your license here').then((events) =>{
      this.setState({result: events});
      }).catch((err) => {
        console.log(err);
      });
  }

  render() {
    return (
      <View style={styles.container}>
        <Button title='Read Barcode' onPress={this.onButtonPress}/>
        <Text style={styles.display}>
          Barcode Result : {this.state.result}
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  display: {
    fontSize: 20,
    textAlign: 'center',
    color:'#FFD700',
    margin: 10
  },
});

AppRegistry.registerComponent('Example', () => Example);
