# Card Vault - Card Management Application

*This README is also available in [Czech](README_CZ.md) and [German](README_DE.md)*

Card Vault is a mobile application for Android that allows you to conveniently manage all your cards in one place. You can store loyalty cards, discount cards, membership cards, payment cards, and other types of cards.

<!-- Application logo will be added later -->

## Features

- **Management of various card types** - loyalty, discount, membership, payment, and others
- **Card photos** - storing front and back sides of cards
- **Barcodes** - generating and scanning barcodes
- **Security** - encrypting photos with a password
- **Search** - quick search for cards by name
- **Multilingual support** - Czech, English, and German

## Screenshots

*Screenshots will be added later*

## Requirements

- Android 13.0 (API level 33) or newer
- Camera for capturing cards and barcodes

## Installation

### Downloading and Installing APK

1. Download the latest APK file from [releases](https://github.com/JiriPrik/CardVault/releases) or directly from the [releases directory](https://github.com/JiriPrik/CardVault/tree/main/releases)
2. Open the file manager on your device
3. Find the downloaded APK file and click on it
4. Allow installation from unknown sources if required
5. Follow the installation instructions

### Building from Source Code

1. Clone the repository:
   ```
   git clone https://github.com/JiriPrik/CardVault.git
   ```

2. Open the project in Android Studio

3. Build the project:
   ```
   ./gradlew assembleDebug
   ```
   or
   ```
   ./gradlew assembleRelease
   ```

4. Install the APK on your device:
   - Connect your device to your computer using a USB cable
   - Enable USB debugging on your device
   - Run:
     ```
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```
     or
     ```
     adb install app/build/outputs/apk/release/app-release.apk
     ```

## Usage

1. **First Launch**
   - When you first launch the application, you will set a password that you will use for login
   - This password is also used for encrypting card photos

2. **Adding a Card**
   - Click the + button in the bottom right corner of the main screen
   - Fill in the card information (name, type, number)
   - Take photos of the front and back sides of the card
   - Scan the barcode or enter barcode data manually
   - Click the Save button

3. **Viewing a Card**
   - Click on a card in the list to view its details
   - Click on a photo to view it in full screen
   - Click the Show Barcode button to display the barcode

4. **Editing a Card**
   - In the card details, click the Edit button
   - Make the desired changes
   - Click the Save button

5. **Deleting a Card**
   - In the card details, click the Delete button
   - Confirm the deletion of the card

6. **Searching for Cards**
   - Click the magnifying glass icon in the top bar of the application
   - Enter the card name
   - The application will display matching cards

7. **Changing the Language**
   - On the splash screen, click the CZ, EN, or DE button
   - The application will restart and display in the selected language

## Contributing

Contributions are welcome! If you want to contribute to the development of the application, follow these steps:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Commit your changes (`git commit -m 'Add some amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for more information.

## Contact

PiKi

Project link: [https://github.com/JiriPrik/karty](https://github.com/JiriPrik/karty)

## Acknowledgements

- Thanks to everyone who contributed to the development of this application
- Thanks for your support through Lightning Network payments to: jiprik@bitlifi.com
