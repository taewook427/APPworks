code:
app/
    manifests/
        AndroidManifest.xml
    java/
        MainActivity.java
        ListActivity.java
        EditActivity.java
        NoteIO.java
        ItemAdapter.java
    res/
        drawable/
            icon_account.xml
            icon_add.xml
            icon_delete.xml
            icon_save.xml
            rectangle.xml
        layout/
            activity_main.xml
            list_item.xml
            note_list.xml
            note_edit.xml
        values/
            colors.xml
            themes/
                themes.xml (day)
                themes.xml (night)

resource:
    drawable - new - vector asset - search, add
    res - new - image asset - add foreground, background image

build:
    build.gradle.kts - set version info
    ./gradlew.bat [assembleRelease|assembleDebug]
