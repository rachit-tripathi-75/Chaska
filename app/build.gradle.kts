plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.deificdigital.chaska"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.deificdigital.chaska"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.chrisbanes.photoview:library:1.2.3")
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation("com.pkmmte.view:circularimageview:1.1")
    implementation("com.mikhaellopez:circularimageview:3.2.0")
    implementation("com.melnykov:floatingactionbutton:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.github.bumptech.glide:glide:4.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation("androidx.mediarouter:mediarouter:1.3.1")
    implementation("androidx.browser:browser:1.5.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference:1.1.0")
    implementation("com.balysv:material-ripple:1.0.2")
    implementation("com.squareup.picasso:picasso:2.5.2")
    implementation("com.google.firebase:firebase-ads:21.5.0")
    implementation("com.google.firebase:firebase-messaging:23.1.2")
    implementation("com.google.firebase:firebase-analytics:21.2.1")
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation("com.google.android.gms:play-services-gcm:17.0.0")
    implementation("com.google.android.gms:play-services-ads:21.5.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.android.billingclient:billing:5.1.0")
    implementation("com.google.android.exoplayer:exoplayer:2.18.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
//    implementation ("com.google.firebase:firebase-core")
    implementation("com.google.firebase:firebase-auth")

    // https://mvnrepository.com/artifact/com.yuyakaido.android/card-stack-view
    implementation("com.yuyakaido.android:card-stack-view:2.3.4")
    implementation("com.google.android.gms:play-services-auth:20.4.1")

    implementation("com.otaliastudios:cameraview:2.7.2")
    implementation("com.otaliastudios:transcoder:0.10.5")

    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("androidx.activity:activity:1.9.1")

    // circular image view
    implementation("com.mikhaellopez:circularimageview:4.3.1")

    // lottie
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.google.firebase:firebase-database:21.0.0")

    //firebase added


    //implementation 'com.github.AbedElazizShe:LightCompressor:1.2.3'

    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }
}