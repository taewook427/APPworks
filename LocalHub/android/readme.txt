# test766 : LocalHub java
app/manifests/AndroidManifest.xml
app/java/com.example.kutil6_lhub/MainActivity.java
app/java/com.example.kutil6_lhub/KUtil6LHubService.java
app/java/com.example.kutil6_lhub/Server.java
app/res/layout/activity_main.xml
app/res/xml/network_security_config.xml
file - project structure - dependency 에서 org.nanohttpd:nanohttpd:2.3.1 검색 후 추가
app 우클릭 - new - folder - asset folder 에 index.html 과 favicon.ico (icon.ico) ctrlCV로 추가
res/drawable/alerticon.png 추가 (포그라운드.png)
res - new - image asset - 포그라운드 백그라운드 이미지 (512x512 이상) 설정하여 이이콘 설정
프로젝트 폴더에 ./gradlew.bat assembleRelease 으로 apk 빌드 // 현재 원인불명으로 assembleDebug 로 디버그 모드 빌드만 설치가능

keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-release-key.jks LocalHub.apk my-key-alias
jarsigner -verify -verbose -certs LocalHub.apk
