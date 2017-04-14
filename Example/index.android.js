/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, {Component} from 'react';
import {AppRegistry, StyleSheet, Text, View, Button} from 'react-native';

import BarcodeReaderManager from 'react-native-dbr';

export default class Example extends Component {
  constructor(props) {
    super(props);
    this.state = {
      result: 'N/A'
    };

    this.onButtonPress = this
      .onButtonPress
      .bind(this);
  }

  onButtonPress() {
    BarcodeReaderManager.readBarcode('', (msg) => {
      this.setState({result: msg});
    }, (err) => {
      console.log(err);
    });
  };

  render() {
    return (
      <View style={styles.container}>
        <Button title='Read Barcode' onPress={this.onButtonPress}/>
        <Text style={styles.display}>
          Barcode Result: {this.state.result}
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
    backgroundColor: '#F5FCFF'
  },
  display: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  }
});

AppRegistry.registerComponent('Example', () => Example);
