Tic-Tac-Toe for Android (Kotlin with Jetpack Compose)
================================

Play Tic-Tac-Toe on your Android smartphone! This app also demonstrates how to use various Android
programming techniques in Kotlin using
Google's [Jetpack Compose](https://developer.android.com/compose).
It's the main reference for the course:
[CSE 5236: Mobile Application Development](http://web.cse.ohio-state.edu/~champion/5236).
(**Note:** Course materials are available to students via [Carmen-Canvas](https://carmen.osu.edu).)

**Caveats:**

- You can play first in Tic-Tac-Toe (with the X symbol), but not second (with the O symbol).
- The maps part of the app won't work unless you get your own [Mapbox](https://www.mapbox.com)
  token. You'll need to set a key-value pair, `MAPBOX_ACCESS_TOKEN`, to your token in your own
  gradle.properties
  file. [Mapbox's instructions](https://docs.mapbox.com/help/troubleshooting/private-access-token-android-and-ios/#android)
- Similarly, the Flickr Photo Gallery won't work unless you obtain
  a [Flickr](https://www.flickr.com) API token. Like before, you'll need to set a key-value pair,
  `FLICKR_ACCESS_TOKEN`, to your token in this file.
- The media player uses [Jetpack Media3](https://developer.android.com/media/media3) with Android
  framework views, not Compose.

License
=======

[Apache 2.0](https://apache.org/licenses/LICENSE-2.0)

Acknowledgments
===============

- Original code by [Prof. Rajiv Ramnath](http://web.cse.ohio-state.edu/~ramnath)
- *Android Programming: The Big Nerd Ranch Guide* (5th edition) for Compose guidelines and
  the [Flickr](https://www.flickr.com) photo-fetching code
- Deniz Nezza's [image handling with Compose](https://blog.eclypse.io/take-photo-or-pick-pictures-with-jetpack-compose-step-by-step-guide-a53cdced7c69) —  [source code](https://github.com/eclypse-tms/SnapCompose)
- Google's *Mars Photos* Android codelab ([part 1](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet), [part 2](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository), [part 3](https://developer.android.com/codelabs/basic-android-kotlin-compose-load-images)) — [source code](https://github.com/google-developer-training/basic-android-kotlin-compose-training-mars-photos) 
- Mapbox's [example location app](https://github.com/mapbox/mapbox-maps-android/tree/v11.25.0/compose-app)