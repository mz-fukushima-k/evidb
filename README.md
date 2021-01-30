# EvidenceDB
## 概要

EvidenceDB は DBのテーブルやビューのダンプを採取しダンプ同士の差分をEXCELで出力するためのタスク群です。
また、DBのDDLから設定ファイルを作成するタスクも含まれております。

**差分出力の例**

![diff-sample](https://user-images.githubusercontent.com/77838284/106358617-b16a4f80-6350-11eb-9e5e-10f41acec872.png)

## インストール
ダウンロードしたフォルダにて以下のコマンドを実行します。

`gradlew clean install -x test `

## 設定ファイル
samples フォルダに設定ファイル（build.gradle）のサンプルが入っています。

build.gradle
```
apply plugin: 'java'

def root = "$rootDir"

wrapper {
    gradleVersion = '6.6.1'
}

configurations {
    evidbRuntime
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    evidbRuntime 'com.mamezou.evidb:evidb-sqlgen:1.0-SNAPSHOT'
    evidbRuntime 'com.mamezou.evidb:evidb-dump:1.0-SNAPSHOT'
    evidbRuntime 'com.mamezou.evidb:evidb-diff:1.0-SNAPSHOT'
    evidbRuntime 'org.postgresql:postgresql:42.0.0'
}

task sqlgen doLast {

    ant.taskdef( resource: 'sqlgentask.properties' , classpath: configurations.evidbRuntime.asPath )

    ant.sqlgen( url                     : "jdbc:postgresql://localhost:5432/dvdrental"
              , user                    : "postgres"
              , password                : "admin"
              , driver                  : "org.postgresql.Driver"
              , dialect                 : "postgres"
              , tableTypes              : "TABLE"
              , outputPath              : "$root/sqlgen.yml"
              , tableNamePattern        : ".*"
              , ignoredTableNamePattern : ""
              , templateDir             : "$root/template/"
    )

}

task dump doLast {

    def dir = new File("$root/dump")
    dir.mkdir()

    ant.taskdef( resource: 'dumptask.properties' , classpath: configurations.evidbRuntime.asPath )

    ant.dump( url        : "jdbc:postgresql://localhost:5432/dvdrental"
            , user       : "postgres"
            , password   : "admin"
            , driver     : "org.postgresql.Driver"
            , outputDir  : "$root/dump"
            , configFile : "$root/sqlgen.yml"
    )

}

task diff doLast {

    def dir = new File("$root/diff")
    dir.mkdir()

    ant.taskdef( resource: 'difftask.properties' , classpath: configurations.evidbRuntime.asPath )

    ant.diff( dumpDir   : "$root/dump"
            , reportDir : "$root/diff"
    )

}

```
## JDBCドライバーの設定
dependencies へ適宜仕様したいDBのJDBCドライバのリポジトリを記述してください。

設定ファイル(build.gradle)抜粋
```
：
dependencies {
    evidbRuntime 'com.mamezou.evidb:evidb-sqlgen:1.0-SNAPSHOT'
    evidbRuntime 'com.mamezou.evidb:evidb-dump:1.0-SNAPSHOT'
    evidbRuntime 'com.mamezou.evidb:evidb-diff:1.0-SNAPSHOT'
    evidbRuntime 'org.postgresql:postgresql:42.0.0'             <--- この行を適宜変更する。
}
：
```


## sqlgen タスク

## dump タスク

## diff タスク
