plugins {
    kotlin("jvm") version "2.4.10" apply false
    kotlin("plugin.compose") version "2.4.10" apply false
    kotlin("plugin.spring") version "2.4.10" apply false
    kotlin("plugin.jpa") version "2.4.10" apply false
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.android.application") version "9.3.0" apply false
}

group = "com.colonydirect"
version = "0.1.0-phase10"
