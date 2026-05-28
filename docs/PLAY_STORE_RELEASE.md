# K2 Golf GPS — Google Play Console 등록 가이드

## 앱 식별 정보

| 항목 | 값 |
|------|-----|
| 앱 이름 (스토어 표시) | **K2 Golf GPS** |
| 패키지명 | `com.golfgps.watch` |
| versionCode | `11` |
| versionName | `1.0.0` |
| 유형 | Wear OS standalone (`com.google.android.wearable.standalone=true`) |
| minSdk | 30 (Wear OS 3+) |

---

## Release AAB 빌드 & 서명 검증

### 1) 업로드 키 생성 (최초 1회)

```powershell
cd C:\Users\Gram\Downloads\golf-main

$keytool = "$env:JAVA_HOME\bin\keytool.exe"
& $keytool -genkeypair -v `
  -keystore release-keystore.jks `
  -alias k2golfgps `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -storepass "YOUR_STORE_PASSWORD" `
  -keypass "YOUR_KEY_PASSWORD" `
  -dname "CN=K2 Golf GPS, OU=Mobile, O=GolfGPS, L=KR, ST=KR, C=KR"
```

### 2) keystore.properties 설정

```powershell
Copy-Item keystore.properties.example keystore.properties
# keystore.properties 에 실제 비밀번호·경로 입력
```

| 파일 | 상태 확인 |
|------|-----------|
| `keystore.properties` | 존재 시 **RELEASE** 서명 |
| 없음 | **DEBUG** fallback (경고 로그 출력) |

### 3) Release AAB 빌드

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
.\gradlew.bat :wear:bundleRelease
```

**빌드 로그 확인:**

- `K2 Golf GPS: RELEASE signing (keystore.properties)` → Play 업로드용 OK
- `WARNING: ... DEBUG signing fallback` → `keystore.properties` 설정 후 재빌드 필요

**산출물:** `wear\build\outputs\bundle\release\wear-release.aab`

---

## 스토어 등록 문구 (준비 완료)

| 파일 | 용도 |
|------|------|
| `docs/store-assets/play-description-short.txt` | 짧은 설명 (80자 이내) |
| `docs/store-assets/play-description-full.txt` | 전체 설명 (4000자 이하) |

---

## 개인정보처리방침

| 파일 | 배포 |
|------|------|
| `docs/privacy-policy.html` | GitHub Pages 등 정적 호스팅 후 URL 확보 |

Play Console **개인정보처리방침 URL**에 게시 URL 입력 (예: `https://<user>.github.io/.../privacy-policy.html`).

---

## Play Store 에셋

| 파일 | 상태 |
|------|------|
| `docs/store-assets/icon-512.png` | 준비됨 |
| `docs/store-assets/screenshots-guide.md` | 촬영 가이드 |
| `screenshot-01.png` 등 | **수동 촬영 필요** |

---

## 권한 설명 (Play Console / 개인정보처리방침)

| 권한 | 용도 |
|------|------|
| `ACCESS_FINE_LOCATION` | 그린까지 거리(m) 계산 |
| `ACCESS_COARSE_LOCATION` | 위치 서비스 호환 |

- 인터넷 권한 없음 · 서버 전송 없음 · 광고 SDK 없음 · 계정 수집 없음

---

## Play Console 제출 체크리스트

### 앱 콘텐츠

- [ ] **AAB** `wear-release.aab` 업로드 (release 서명 확인)
- [ ] 앱 이름 **K2 Golf GPS**
- [ ] 짧은/전체 설명 (`play-description-*.txt` 붙여넣기)
- [ ] 512×512 아이콘
- [ ] 워치 스크린샷 최소 2장
- [ ] **개인정보처리방침 URL** 등록
- [ ] **콘텐츠 등급** 설문 완료

### Data Safety (데이터 보안)

- [ ] **위치** 수집: 예 (앱 기능에 필요)
- [ ] 수집 목적: 앱 기능(거리 계산)
- [ ] 데이터 공유: 아니오 (제3자 전송 없음)
- [ ] 데이터 보관: 서버 저장 없음 (기기 내 처리)
- [ ] 암호화 전송: 해당 없음 (서버 통신 없음)

### Wear OS / 기기

- [ ] **Wear OS** 앱으로 등록 (standalone)
- [ ] 카테고리: 스포츠 또는 건강/운동
- [ ] 타겟: Watch

### 배포·국가·가격

- [ ] **국가/지역** 선택 (예: 대한민국 우선)
- [ ] **무료 앱** 설정 (현재 유료 기능 없음)
- [ ] **내부 테스트** 트랙 생성 → 테스터 추가 → 설치 검증
- [ ] 내부 테스트 통과 후 **프로덕션** 승격

### 내부 테스트 트랙 절차

1. Play Console → **테스트** → **내부 테스트** → 새 릴리스 만들기
2. `wear-release.aab` 업로드
3. 테스터 이메일 목록 추가 (최대 100명)
4. 공유 링크로 Galaxy Watch에 설치
5. 확인: GPS, 홀 변경, 스와이프, 새로고침, 햅틱
6. `adb shell dumpsys package com.golfgps.watch | findstr version` → `1.0.0`

### 향후 유료화 시 고려

- 인앱 결제·구독 도입 시 Play Console **수익 창출** 설정
- 유료 전환 시 개인정보처리방침·스토어 설명 업데이트
- 코스 데이터 유료 패키지 시 `assets/courses/` 확장 및 별도 이용약관 검토

---

## courses 확장 구조 (코드 미연결)

```
wear/src/main/assets/courses/
  k2_daegu.json   ← 샘플
  README.md
```

향후 골프장별 JSON 로드 시 `MainActivity` 연동 예정. **현재 릴리스는 기존 하드코딩 좌표 사용.**

---

## 버전 정책

- Play 업로드마다 **versionCode** 증가 필수
- **versionName** 사용자 표시용 (예: 1.0.0 → 1.0.1)
- 수정 위치: `wear/build.gradle.kts` → `defaultConfig`

---

## 빠른 참조 명령

```powershell
.\gradlew.bat :wear:bundleRelease
.\.android-sdk\platform-tools\adb.exe install -r wear\build\outputs\apk\debug\wear-debug.apk
```
