# Purify
Purify is a Proof-of-Concept Android application that can remove advertisements (ads) from any apk (i.e. Android app). It doesn't need root privileges.
### Disclaimer
This application is just a proof of concept, I wrote it in few hours. The code isn't so tidy and the application can fail. If the application fails, the new purified apk (i.e. without ads) will crash.
### How it works
Purify takes in input an apk and changes the content of `classes.dex`. In details, the application looks for well known ads domains (e.g. example.ads.site.com) and replaces the domains with invalid domains. Thus, for example, an application instead to do an http request to `example.ads.site.com/banner.php?size=100` will request an invalid url as `aaaaaaaaaaaaaaa/banner.php?size=100`. For the majority of the apps this should mean that the app will not display any ads. However, some apps can crash, so it doesn't work always.
### Requirements
Purify can edit the `classes.dex` file inside an apk, it will produce a new *purified* apk containing the same (old) signature. You can try to install it, however probably you will get an error during the installation asserting that the package is corrupted. A valid apk **must be signed**, otherwise you can't install it, so you need to use also an apk signer to correctly sign the application. I recommend the open source [ZipSigner](https://play.google.com/store/apps/details?id=kellinwood.zipsigner2). Purify requires Android 4.2 or higher.

#### License
GPLv3
