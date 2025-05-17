app/manifests/AndroidManifest.xml 수정
app/java/com.example.lhub/ 에 MainActivity.java 와 Server.java 위치
app/res/layout/ 생성 후 안에 activity_main.xml 위치
app/res/xml/ 안에 network_security_config.xml 위치
file - project structure 에서 org.nanohttpd:nanohttpd:2.3.1 검색 후 추가
app 우클릭 - new - folder - asset folder 에 index.html 추가
res - new - image asset - 포그라운드 백그라운드 이미지 (512x512 이상) 설정하여 이이콘 설정
프로젝트 폴더에 ./gradlew.bat assembleRelease 으로 apk 빌드 // 현재 원인불명으로 assembleDebug 로 디버그 모드 빌드만 설치가능

keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-release-key.jks LocalHub.apk my-key-alias
jarsigner -verify -verbose -certs LocalHub.apk
