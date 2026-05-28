# Play Console — Wear OS 업로드 (K2GolfWear)

## 오류 원인 (가장 흔함)

> **「이 APK 또는 번들에는 Wear OS 시스템 기능 android.hardware.type.watch이(가) 필요합니다」**

이 메시지는 **Phones(휴대폰) 릴리스 트랙**에 Wear 전용 AAB를 올릴 때 자주 나옵니다.  
번들 자체는 Wear용이 맞고, **업로드 위치(폼 팩터)** 가 잘못된 경우가 많습니다.

### 해결

1. Play Console → **테스트 및 출시** → 내부 테스트(또는 원하는 트랙)
2. **새 버전 만들기** 클릭 **전에** 상단 드롭다운에서  
   **Phones** ❌ → **Wear OS** ✅ 로 변경
3. `app-release.aab` 업로드
4. App Bundle Explorer에서 **지원 폼 팩터: Wear OS** 인지 확인

---

## 업로드 파일

| 항목 | 값 |
|------|-----|
| AAB 경로 | `wear\build\outputs\bundle\release\app-release.aab` |
| 패키지 | `com.nahohoon.golfgps` |
| versionCode | `4` (이번 빌드) |
| 서명 | `release-keystore.jks` / alias `k2-golf` |

---

## Manifest 검증 (로컬)

빌드 후 다음 파일에서 `android:required="true"` 확인:

```
wear\build\intermediates\merged_manifest\release\processReleaseMainManifest\AndroidManifest.xml
```

또는 Android Studio: **Build → Analyze APK / App Bundle** → `uses-feature` → `android.hardware.type.watch`

---

## 빌드 명령

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
cd C:\Users\Gram\Downloads\golf-main
.\gradlew.bat :wear:clean :wear:bundleRelease
```

로그에 `RELEASE signing (keystore.properties)` 가 보여야 Play 업로드용 서명입니다.
