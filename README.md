<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="App Logo" width="120" height="120">
  <h1>morphlect</h1>
  <p><strong>Native Android image processing app using on-device inference</strong></p>

  <!-- Badges -->
  <a href="https://github.com/silviu28/morphlect-android/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/silviu28/morphlect-android/android.yml?style=for-the-badge" alt="Build Status">
  </a>
  <a href="https://github.com/silviu28/morphlect-android/issues">
    <img src="https://img.shields.io/github/issues/silviu28/morphlect-android?style=for-the-badge&color=orange" alt="Issues">
  </a>
  <a href="https://github.com/yourusername/your-repo/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/silviu28/morphlect-android?style=for-the-badge" alt="License">
  </a>
  <br>
  <a href="https://play.google.com/store/apps/details?id=com.yourpackage">
    <img src="https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white" alt="Google Play">
  </a>
</div>

---

**Morphlect** (stylized as **morphlect**) is a photo editing app. It uses high-performance image processing and machine-learning to both offer advanced tools for personal tweaks while also using algorithms to automatically enhance images and giving you different perspectives. The app focuses on augmenting the user's creativity rather than replace them in the creative process.

### Key Features

- **Easy and quick filter adjustment** - Use familiar controls to manually adjust images however you like
- **Image quality assessment and enhancement** - Let the app find what's best for your image
- **Creative tooling** - Switch up the way you're looking at image processing by either describing the final result or adding another reference image
- **Extensible** - Download and manage MXT (Morphlect eXTension) extensions that further add to your creative process
- **Advanced tools** - Use professional tools (like undo stack and layers) from your device with reduced friction
- **Offline support** - Everything works perfectly fine even without a persistent Internet connection. Only required for online features, like extension download
- **Camera mode** - Use artificial-intelligence tooling before and after taking your picture

---

### Technologies

> The app is written in Kotlin, using Jetpack Compose for state management and user interface.
> 
> For hardware-accelerated image processing it uses the <a href="https://mvnrepository.com/artifact/org.opencv/opencv">OpenCV Android port</a>.
>
> AI inference is done through TFLite/LiteRT model bundling and processing.
>
> Camera, preferences storage and other native functionalities are implemented through first-party Android packages.

Check out the <a href="https://github.com/silviu28/morphlect-server">server template</a> repository for more information on the extension server backend.

---

## Screenshots

<div align="center">
  <img src="screenshots/Screenshot_20260417_180521-portrait.png" alt="Studio Filtering" width="250">
  <img src="screenshots/Screenshot_20260417_180706-portrait.png" alt="Image Quality Assessment" width="250">
  <img src="screenshots/Screenshot_20260417_181150-portrait.png" alt="Tags to Filter Transformation" width="250">
  <img src="screenshots/Screenshot_20260417_181335-portrait.png" alt="Expanded Layers View" width="250">
  <img src="screenshots/Screenshot_20260417_185301-portrait.png" alt="MXT Search" width="250">
</div>

---
## Development and Build

### Prerequisites

- [JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) (17 or higher)
- [Git](https://git-scm.com/)
- An IDE (Android Studio / IntelliJ) or text editor (VS Code, Sublime Text)
- A mobile device or emulator running Android 10 or higher

### Installation

1. **Clone the repository**

```
git clone https://github.com/silviu28/morphlect-android.git
cd morphlect-android
```

2a. **Create a fast build (no test run)**

```
./gradlew build -x test
```

2b. **Create a build (runs unit tests)**

```
./gradlew build
```

3. **Run local tests**

```
./gradlew test
```
These run on the host, thus not requiring a connection to mobile device/emulator.

4. **Run instrumented tests**

```
./gradlew connectedAndroidTest
```
These only run on the device. Beware if you have an existing build as the test execution might remove the app.

5. **Install on device**

```
./gradlew :app:installDebug
```

## Contributing

While the project isn't actively maintained, contributions are welcome. Do not push directly on the `master` branch and create a PR. Even if the workflow fails your contribution may still pass after approval. Purely AI-generated contributions are prohibited.

## Additional Credits
- Android Semantic Search repository for helpful transformer loading and wiring logic https://github.com/hissain/AndroidSemanticSearch
- CameraX OpenCV repository for helpful CameraX and OpenCV linking logic https://github.com/mcanyucel/camerax-opencv
  
<sub>silviu28 | 2026</sub>
