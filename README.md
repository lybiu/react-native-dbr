# Barcode Detection Module for React Native
The module only works for **Android** now.

## How to Run the Example

```bash
cd Example
npm install
react-native run-android
```

### Screenshots
![Barcode Detection for React Native](http://www.codepool.biz/wp-content/uploads/2017/04/react-native-barcode-detection.jpg)
![Barcode Detection for React Native](http://www.codepool.biz/wp-content/uploads/2017/04/react-native-barcode-result.png)

## How to Use the Module
1. Create a new React Native project:

    ```bash
    react-native init NewProject
    ```
2. Add the local module to dependencies in **package.json**: 

    ```json
    "dependencies": {
		"react": "16.0.0-alpha.6",
		"react-native": "0.43.3",
		"react-native-dbr":"file:../"
	},
    ```
3. Link dependencies:

    ```bash
    react-native link
    ```
4. Use **flatDir** to define library path in **android/build.gradle**:

    ```
    flatDir {
        dirs "$rootDir/../node_modules/react-native-dbr/android/lib"
    }
    ```

4. Use the module in **index.android.js**:

    ```javascript
    import BarcodeReaderManager from 'react-native-dbr';

    BarcodeReaderManager.readBarcode('C6154D1B6B9BD0CBFB12D32099F20B35', (msg) => {
        this.setState({result: msg});
    }, 
    (err) => {
        console.log(err);
    });
    ```
    If you do not have a valid license, please contact <support@dynamsoft.com>. With invalid license, the SDK can work but will not return a full result.
    ![Invalid license](http://www.codepool.biz/wp-content/uploads/2017/04/react-native-barcode-license.png)

## Blog
[Android Barcode Detection Component for React Native](http://www.codepool.biz/android-barcode-detection-component-react-native.html)
