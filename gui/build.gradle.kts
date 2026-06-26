import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.11.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

group = "ankol.mod.merger"
version = "1.7.1"

dependencies {
    // 依赖 CLI 编译后的 fat-jar
    implementation(files("../target/SuperModMerger-1.7.1-SNAPSHOT-all.jar"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // FileKit — 跨平台文件/目录选择器（使用平台原生 API）
    implementation("io.github.vinceglb:filekit-dialogs-compose:0.14.2")
}

compose.desktop {
    application {
        mainClass = "ankol.mod.merger.gui.AppKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "SuperModMerger"
            packageVersion = "1.7.1"
            vendor = "Ankol"
            description = "Dying Light Mod Merger Tool"

            windows {
                menuGroup = "SuperModMerger"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("../builds/icon.ico"))
            }
        }
    }
}
