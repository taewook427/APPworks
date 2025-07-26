code:
app/
    manifests/
        AndroidManifest.xml
    java/
        Server.java
        KU6svc_localhub.java
        ItemAdapter.java
        MainActivity.java
    assets/
        favicon.ico
        index.html
    res/
        drawable/
            icon_copy.xml
            icon_service.xml
            round_rectangle.xml
        layout/
            activity_main.xml
            list_item.xml
        values/themes/
            themes.xml (day)
            themes.xml (night)
        xml/
            network_security_config.xml

resource:
    app - new - folder - asset folder - add index.html, favicon.ico
    drawable - new - vector asset - search, add
    res - new - image asset - add foreground, background image

build:
    file - project structure - dependency - add org.nanohttpd:nanohttpd
    build.gradle.kts - set version info
    ./gradlew.bat [assembleRelease|assembleDebug]
