# K2 Golf GPS — Play Store 스크린샷 가이드

## 최소 요구

| 항목 | 권장 |
|------|------|
| 장수 | **최소 2장** (권장 3~4장) |
| 형식 | PNG 또는 JPEG |
| 해상도 | **384×384 ~ 450×450** (워치 화면 비율, 정사각 권장) |
| 내용 | 실제 Wear OS 워치에서 촬영한 앱 화면 |

---

## 권장 캡처 화면

1. **거리 표시 화면** (필수)  
   - HOLE 번호 + 큰 거리 숫자(예: 4706m) + GPS 상태  
   - GPS 수신 후 촬영

2. **홀 변경 화면** (권장)  
   - HOLE 3 등 다른 홀 번호 + 변경된 거리

3. **GPS 연결/갱신 화면** (권장)  
   - `GPS 수신됨 | ±12m`, `갱신 HH:mm:ss` 표시

4. **버튼·UI 전체** (선택)  
   - 이전 / 새로고침 / 다음 버튼이 보이는 하단

---

## 워치에서 스크린샷 찍는 방법

### 방법 A — 워치 기본 기능

1. Galaxy Watch: 전원 키 + 홈(또는 기기 설정의 스크린샷)  
2. 갤러리/휴대폰 연동으로 이미지 전송

### 방법 B — ADB (PC 연결)

```powershell
cd C:\Users\Gram\Downloads\golf-main
$adb = ".\.android-sdk\platform-tools\adb.exe"

# 연결된 기기 확인
& $adb devices

# 워치 화면 캡처 (시리얼은 환경에 맞게 변경)
& $adb -s <WATCH_SERIAL> shell screencap -p /sdcard/k2golf_screen.png
& $adb -s <WATCH_SERIAL> pull /sdcard/k2golf_screen.png docs\store-assets\screenshot-01.png
```

### 방법 C — Android Studio

Device Manager → Wear 기기 → **Take Screenshot**

---

## 파일 저장 위치

```
docs/store-assets/
  screenshot-01.png   ← 거리 표시
  screenshot-02.png   ← 홀 변경 또는 GPS
  screenshot-03.png   ← (선택)
```

Play Console 업로드 시 위 파일을 **Phone/Tablet가 아닌 Wear OS / Watch** 섹션에 올립니다.

---

## Play 업로드 주의사항

- 다른 앱·타사 UI·허위 거리 수치 사용 금지
- 개발자 디버그 화면·에러 화면 캡처 금지
- 원형 워치에서 UI가 잘리지 않은 상태로 촬영
- 스크린샷에 개인 연락처·실제 GPS 좌표 등 민감 정보 노출 최소화
- 업로드 전 512×512 아이콘(`icon-512.png`)과 설명 문구(`play-description-*.txt`) 함께 준비

---

## 체크리스트

- [ ] 최소 2장 촬영 완료
- [ ] 거리 숫자·홀 번호가 선명하게 보임
- [ ] GPS 상태 줄이 읽히는지 확인
- [ ] `docs/store-assets/`에 파일 저장
- [ ] Play Console Wear 스크린샷 슬롯에 업로드
