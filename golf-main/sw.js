// ──────────────────────────────────────────────
//  골프 GPS 캐디 - Service Worker
//  캐시 버전을 올릴 때는 아래 CACHE_NAME의 숫자만 변경하세요.
//  예: golf-gps-cache-v2 → golf-gps-cache-v3
// ──────────────────────────────────────────────
var CACHE_NAME = 'golf-gps-cache-v2';

var CACHE_FILES = [
  './index.html',
  './watch.html',
  './manifest.json',
  './icons/icon.svg'
];

// ── INSTALL: 핵심 파일 캐시 ──
self.addEventListener('install', function(e) {
  e.waitUntil(
    caches.open(CACHE_NAME).then(function(cache) {
      return cache.addAll(CACHE_FILES);
    }).then(function() {
      // 새 버전이 설치되면 즉시 활성화 (기존 SW 대기 없이)
      return self.skipWaiting();
    })
  );
});

// ── ACTIVATE: 이전 버전 캐시 삭제 ──
self.addEventListener('activate', function(e) {
  e.waitUntil(
    caches.keys().then(function(keys) {
      return Promise.all(
        keys
          .filter(function(key) {
            // 현재 버전이 아닌 이전 캐시만 삭제
            return key !== CACHE_NAME;
          })
          .map(function(key) {
            return caches.delete(key);
          })
      );
    }).then(function() {
      // 새 SW가 즉시 페이지 제어권 가져오기
      return self.clients.claim();
    })
  );
});

// ── FETCH: Stale-While-Revalidate 전략 ──
self.addEventListener('fetch', function(e) {
  var url = e.request.url;

  // 외부 도메인 요청(skywork 등)은 SW가 개입하지 않음
  if (!url.startsWith(self.location.origin)) {
    return;
  }

  // GET 요청만 캐시 처리
  if (e.request.method !== 'GET') {
    return;
  }

  e.respondWith(
    caches.match(e.request).then(function(cached) {
      // 캐시 히트: 즉시 반환 + 백그라운드에서 네트워크 갱신
      if (cached) {
        fetch(e.request).then(function(response) {
          if (response && response.status === 200) {
            caches.open(CACHE_NAME).then(function(cache) {
              cache.put(e.request, response);
            });
          }
        }).catch(function() {});
        return cached;
      }

      // 캐시 미스: 네트워크 요청 후 캐시 저장
      return fetch(e.request).then(function(response) {
        if (!response || response.status !== 200 || response.type === 'opaque') {
          return response;
        }
        var responseClone = response.clone();
        caches.open(CACHE_NAME).then(function(cache) {
          cache.put(e.request, responseClone);
        });
        return response;
      }).catch(function() {
        // 오프라인 폴백: index.html 반환
        return caches.match('./index.html');
      });
    })
  );
});
